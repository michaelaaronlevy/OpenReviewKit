package com.github.michaelaaronlevy.ork.wordindex;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.util.Arrays;
import java.util.TreeSet;

import com.github.michaelaaronlevy.ork.GridIterMemenPage;
import com.github.michaelaaronlevy.ork.MemenPage;
import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.util.Grid;
import com.github.michaelaaronlevy.ork.util.RunnableWithStatus;
import com.github.michaelaaronlevy.ork.util.Status;
import com.github.michaelaaronlevy.ork.util.StatusReporter;
import com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPDFs;

/**
 * this class creates a searchable word index.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class Indexer implements RunnableWithStatus
{
   /**
    * 
    * @param directory
    *           the directory to save the index to
    * @param name
    *           the name of the index file
    * @param wordList
    *           a WordList object that already has a complete list of words that
    *           will appear.
    */
   public Indexer(final File directory, final String name, final WordList wordList)
   {
      outputDirectory = directory;
      this.name = name;
      this.wordList = wordList;
   }
   
   public void run()
   {
      status.declareStart();
      
      try
      {
         step1(); // make a list of all the words (in the wordList that was
                  // given to the constructor).
      }
      catch(final IOException iox)
      {
         status.declareFatalError(iox.getMessage());
         status.declareEnd();
         return;
      }
      
      if(status.reporter.hasError())
      {
         status.declareEnd();
         return;
      }
      
      try
      {
         step2(); // make a smaller "grid" file equivalent and delete the .grid
      }
      catch(final IOException iox)
      {
         status.declareFatalError(iox.getMessage());
         status.declareEnd();
         return;
      }
      
      if(status.reporter.hasError())
      {
         status.declareEnd();
         return;
      }
      
      try
      {
         step3(); // for each word (in ascending dictionary order), a list of
                  // the pages that word appears in (in ascending order)
                  // and delete the unnecessary file
      }
      catch(final IOException iox)
      {
         status.declareFatalError(iox.getMessage());
         status.declareEnd();
         return;
      }
      
      if(status.reporter.hasError())
      {
         status.declareEnd();
         return;
      }
      
      status.declareEnd();
   }
   
   private void step1() throws IOException
   {
      status.updateStatus("Step 1 - make .conw file - a list of files/words");
      BufferedInputStream stream = new BufferedInputStream(
            new FileInputStream(new File(outputDirectory, name + ".grid")));
      BufferedOutputStream outW = new BufferedOutputStream(
            new FileOutputStream(new File(outputDirectory, name + ".conw")));
      
      pagesPerPdf = Grid.readIntArray(stream);
      totalPages = 0;
      for(final int i : pagesPerPdf)
      {
         totalPages += i;
      }
      
      Grid.writeIntArray(outW, pagesPerPdf);
      Grid.writeStringArray(outW, Grid.readStringArray(stream));
      stream.close();
      
      allWords = wordList.getWordList();
      wordOccurrence = new int[allWords.length];
      Grid.writeStringArray(outW, allWords);
      outW.close();
      
      final BufferedWriter wordListOut = new BufferedWriter(new FileWriter(new File(outputDirectory, name + ".words")));
      for(String word : allWords)
      {
         wordListOut.write(word);
         wordListOut.newLine();
      }
      wordListOut.close();
      
      wordList.clear();
   }
   
   private void step2() throws IOException
   {
      status.updateStatus("Step 2 - make efficient .grid-like file that is used and deleted in step 3");
      final boolean isShort = allWords.length < Short.MAX_VALUE;
      GridIterMemenPage iter = new GridIterMemenPage(
            new BufferedInputStream(new FileInputStream(new File(outputDirectory, name + ".grid"))));
      BufferedOutputStream outG = new BufferedOutputStream(
            new FileOutputStream(new File(outputDirectory, name + ".cong")));
      
      Grid.writeBoolean(outG, isShort);
      
      int projectPage = 0;
      final TreeSet<String> pageWords = new TreeSet<String>();
      while(iter.pagesLeft() > 0)
      {
         final MemenPage data = iter.next();
         for(final MemenText mt : data.getWordsArray())
         {
            pageWords.add(mt.text);
         }
         
         Grid.writeInt(outG, ++projectPage);
         Grid.writeInt(outG, pageWords.size());
         if(isShort)
         {
            for(final String word : pageWords)
            {
               final int index = Arrays.binarySearch(allWords, word);
               wordOccurrence[index]++;
               Grid.writeShort(outG, index);
            }
         }
         else
         {
            for(final String word : pageWords)
            {
               final int index = Arrays.binarySearch(allWords, word);
               if(index < 0)
               {
                  // this is not supposed to happen, it means it looked for
                  // a word that did not get put into the dictionary
                  System.err.println(
                        "Step 2 error - word is not in the array.  wordOccurrence.length = " + wordOccurrence.length);
               }
               
               wordOccurrence[index]++;
               Grid.writeInt(outG, index);
            }
         }
         pageWords.clear();
      }
      outG.close();
      
      new File(outputDirectory, name + ".grid").delete();
   }
   
   private void step3() throws IOException
   {
      status.updateStatus(
            "Step 3 - make the .coni file - which contains an index of all pages where each word appears");
      final int[][] wordIndex = new int[allWords.length][];
      for(int i = 0; i < wordIndex.length; i++)
      {
         wordIndex[i] = new int[wordOccurrence[i]];
      }
      Arrays.fill(wordOccurrence, 0); // wordOccurrence is now an array full of
                                      // counters
      
      BufferedInputStream stream = new BufferedInputStream(
            new FileInputStream(new File(outputDirectory, name + ".cong")));
      
      final boolean readShort = Grid.readBoolean(stream);
      while(stream.available() > 0)
      {
         final int projectPage = Grid.readInt(stream);
         final int pageWords = Grid.readInt(stream);
         if(readShort)
         {
            for(int w = 0; w < pageWords; w++)
            {
               int next = Grid.readShort(stream);
               wordIndex[next][wordOccurrence[next]++] = projectPage;
            }
         }
         else
         {
            for(int w = 0; w < pageWords; w++)
            {
               int next = Grid.readInt(stream);
               wordIndex[next][wordOccurrence[next]++] = projectPage;
            }
         }
      }
      stream.close();
      
      final boolean writeShort = totalPages < Short.MAX_VALUE;
      BufferedOutputStream outI = new BufferedOutputStream(
            new FileOutputStream(new File(outputDirectory, name + ".coni")));
      Grid.writeInt(outI, wordIndex.length);
      Grid.writeBoolean(outI, writeShort);
      if(writeShort)
      {
         for(final int[] ii : wordIndex)
         {
            Grid.writeShortArray(outI, ii);
         }
      }
      else
      {
         for(final int[] ii : wordIndex)
         {
            Grid.writeIntArray(outI, ii);
         }
      }
      outI.close();
      
      BufferedWriter outLegible = new BufferedWriter(new FileWriter(new File(outputDirectory, name + ".index")));
      for(int i = 0; i < allWords.length; i++)
      {
         final String word = allWords[i];
         final int[] pages = wordIndex[i];
         outLegible.write(word);
         final String filler = ((i + 1) % 4 == 0 ? "__________________________ " : "                           ");
         outLegible.write(" ");
         outLegible.write(filler.substring(word.length() + 1));
         outLegible.write(Integer.toString(pages[0]));
         for(int j = 1; j < pages.length; j++)
         {
            outLegible.write(",");
            outLegible.write(Integer.toString(pages[j]));
         }
         outLegible.newLine();
      }
      outLegible.close();
      
      Arrays.fill(wordIndex, null);
      
      new File(outputDirectory, name + ".cong").delete();
   }
   
   public StatusReporter getStatus()
   {
      return status.reporter;
   }
   
   /**
    * Create a new {@link com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPDFs
    * ProjectPDFs} object based on a word index file.
    * 
    * @param directory
    *           the directory where the word index files are
    * @param name
    *           the name of the index files (without extension)
    */
   public static ProjectPDFs buildProjectFromIndexFile(final File directory, final String name) throws IOException
   {
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(new File(directory, name + ".conw")));
      
      final int[] pageCountPerFile = Grid.readIntArray(bis);
      final String[] filePaths = Grid.readStringArray(bis);
      
      final File[] files = new File[pageCountPerFile.length];
      for(int i = 0; i < pageCountPerFile.length; i++)
      {
         files[i] = new File(filePaths[i]).getCanonicalFile();
      }
      bis.close();
      
      return new ProjectPDFs(directory, name, files, pageCountPerFile);
   }
   
   private final File outputDirectory;
   private final String name;
   
   private int[] pagesPerPdf = null;
   private final WordList wordList;
   private String[] allWords = null;
   private int[] wordOccurrence = null;
   private int totalPages = -1;
   
   private final Status status = new Status("Indexer");
}
