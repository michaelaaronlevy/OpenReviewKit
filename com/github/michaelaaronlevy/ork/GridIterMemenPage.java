package com.github.michaelaaronlevy.ork;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import com.github.michaelaaronlevy.ork.util.Grid;

/**
 * this class allows the user to iterate through the content of a .grid file,
 * one {@link MemenPage MemenPage} at a time. A static method also allows the
 * contents of a .grid file to be sent, one page at a time, to a
 * {@link PageConsumer PageConsumer} instance, which is identical to the outputs
 * the {@link PageConsumer PageConsumer} object would receive if it was
 * connected to a {@link PdfToTextGrid PdfToTextGrid} that was extracting the
 * text from PDFs.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class GridIterMemenPage
{
   /**
    * this static method reads the contents of a .grid file and sends them to a
    * PageConsumer object. As far as the PageConsumer can tell, there is no
    * difference between input from this class, and input from a
    * {@link PdfToTextGrid PdfToTextGrid} that is actually parsing the PDFs as
    * it runs. So if you are testing a custom implementation of PageConsumer, it
    * should be faster to use this method on a pre-existing .grid file than to
    * re-rip PDFs every time.
    * 
    * @param in
    * @param out
    * @throws IOException
    */
   public static void gridToConsumer(final InputStream in, final PageConsumer out) throws IOException
   {
      int totalPages = 1;
      int id = 1;
      int fN = 0;
      final GridIterMemenPage iter = new GridIterMemenPage(in);
      final File[] files = new File[iter.fileNames.length];
      for(int i = 0; i < files.length; i++)
      {
         files[i] = new File(iter.fileNames[i]);
      }
      out.startProject(files, iter.pagesPerPdf.clone());
      
      while(iter.pagesLeft() > 0)
      {
         final MemenPage p = iter.next();
         if(p.fileNumber != fN)
         {
            if(fN != 0)
            {
               out.endOfFile();
            }
            fN = p.fileNumber;
            out.startFile(files[fN - 1], fN, iter.getFilePageCount());
         }
         out.takePage(id, totalPages++, p);
         id += p.getNumberOfWords();
      }
      out.endOfFile();
      out.endOfProject();
   }
   
   public GridIterMemenPage(final InputStream in) throws IOException
   {
      this.in = in;
      pagesPerPdf = Grid.readIntArray(in);
      fileNames = Grid.readStringArray(in);
      
      int m = 0;
      for(final int i : pagesPerPdf)
      {
         m += i;
      }
      max = m;
   }
   
   /**
    * 
    * @return get the next MemenPage in the .grid file.
    * @throws IOException
    */
   public MemenPage next() throws IOException
   {
      if(projectPageCounter == max)
      {
         return null;
      }
      projectPageCounter++;
      final MemenPage m = new MemenPage(in);
      fileNumber = m.fileNumber;
      return m;
   }
   
   public int pagesLeft()
   {
      return max - projectPageCounter;
   }
   
   /**
    * 
    * @return the file number of the most-recently returned MemenPage object.
    */
   public int getFileNumber()
   {
      return fileNumber;
   }
   
   /**
    * 
    * @return the total number of files in this project.
    */
   public int getNumberOfFiles()
   {
      return pagesPerPdf.length;
   }
   
   /**
    * 
    * @return the file name associated with the most-recently returned MemenPage
    *         object.
    */
   public String getFileName()
   {
      return fileNames[fileNumber];
   }
   
   /**
    * 
    * @return the number of pages for the PDF file associated with the
    *         most-recently returned MemenPage object.
    */
   public int getFilePageCount()
   {
      return pagesPerPdf[fileNumber];
   }
   
   /**
    * 
    * @return the project page number for the most recently-returned MemenPage.
    *         Or zero, if no MemenPage has been returned yet.
    */
   public int getProjectPage()
   {
      return projectPageCounter;
   }
   
   private final InputStream in;
   private final int max;
   
   private final int[] pagesPerPdf;
   private final String[] fileNames;
   
   private int fileNumber = 0;
   private int projectPageCounter = 0;
}
