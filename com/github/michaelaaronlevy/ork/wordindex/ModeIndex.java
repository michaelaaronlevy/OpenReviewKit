package com.github.michaelaaronlevy.ork.wordindex;

import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.github.michaelaaronlevy.ork.ExtractionMode;
import com.github.michaelaaronlevy.ork.ModeSimple;
import com.github.michaelaaronlevy.ork.OpenReviewKit;
import com.github.michaelaaronlevy.ork.PageConsumer;
import com.github.michaelaaronlevy.ork.PageConsumerSerial;
import com.github.michaelaaronlevy.ork.ripping.CharTest;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.ripping.TransformerMustHaveAtLeastOne;
import com.github.michaelaaronlevy.ork.ripping.TransformerRemoveChars;
import com.github.michaelaaronlevy.ork.util.ScriptReader;
import com.github.michaelaaronlevy.ork.util.Status;
import com.github.michaelaaronlevy.ork.util.Status.ErrorStatus;

/**
 * this class is used with the Ork
 * {@link com.github.michaelaaronlevy.ork.ExtractionGUI ExtractionGUI} (that you
 * can access by running the
 * {@link com.github.michaelaaronlevy.ork.OpenReviewKit#main(String[])
 * OpenReviewKit's main method}) to create a searchable word index from PDFs. It
 * is one of the selectable "modes"
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ModeIndex implements ExtractionMode
{
   public ModeIndex(final boolean keepNumbers)
   {
      this.keepNumbers = keepNumbers;
   }
   
   public String buttonText()
   {
      return "word index";
   }
   
   public String getExtension()
   {
      return ".grid";
   }
   
   public TextPositionsParser getParser()
   {
      return ModeSimple.ReaderMode.WORDS_MINUS.getParser();
   }
   
   public synchronized Transformer getTransformer()
   {
      if(wordList == null)
      {
         makeWordList();
      }
      
      Transformer t = new Transformer(wordList, null);
      t = new Transformer(new TransformerRemoveChars(new CharTest.IsInList("‘'’")), t);
      final TransformerMustHaveAtLeastOne uos = new TransformerMustHaveAtLeastOne(
            keepNumbers ? CharTest.IsLetterOrDigit : CharTest.IsLetter);
      t = new Transformer(uos, t);
      return t;
   }
   
   public synchronized PageConsumer getConsumer(File targetOut)
   {
      if(wordList == null)
      {
         makeWordList();
      }
      
      String name = targetOut.getName();
      int index = name.lastIndexOf('.');
      if(index > 0)
      {
         name = name.substring(0, index);
      }
      
      try
      {
         final PageConsumerSerial pc = new PageConsumerSerial(new BufferedOutputStream(new FileOutputStream(targetOut)),
               true);
         indexer = new Indexer(targetOut.getParentFile(), name, wordList);
         indexFile = new File(targetOut.getParentFile(), name + ".");
         return pc;
      }
      catch(final IOException iox)
      {
         JOptionPane.showMessageDialog(null, "Unable to Write Results.", "Error", JOptionPane.ERROR_MESSAGE);
         return null;
      }
   }
   
   public void post(final Status.ErrorStatus status)
   {
      if(status == ErrorStatus.NO_ERROR)
      {
         indexer.run();
         if(indexer.getStatus().hasError())
         {
            JOptionPane.showMessageDialog(null, indexer.getStatus().getStatusMessage(),
                  "Error: Indexing Attempt Failed.", JOptionPane.ERROR_MESSAGE);
         }
         else
         {
            OpenReviewKit.openOrk().finder(indexFile);
         }
      }
      else
      {
         final OpenReviewKit ork = OpenReviewKit.getOrk();
         if(ork != null)
         {
            ork.continueOption("Error: Indexer Failed.",
                  indexer.getStatus().getStatusMessage() + "\nReturn to the text extraction window?",
                  JOptionPane.ERROR_MESSAGE);
         }
      }
   }
   
   private void makeWordList()
   {
      final ScriptReader listReader = new ScriptReader(
            new BufferedReader(new InputStreamReader(getClass().getResourceAsStream("/word_index/skiplist.txt"))),
            false);
      final ArrayList<String> skipWords = new ArrayList<String>();
      while(listReader.hasNext())
      {
         final String word = listReader.nextLine();
         if(word.length() >= _WORD_INDEX_MIN)
         {
            skipWords.add(word);
         }
      }
      final String[] skipListContents = skipWords.toArray(new String[skipWords.size()]);
      skipWords.clear();
      
      wordList = new WordList(skipListContents, _WORD_INDEX_MIN, _WORD_INDEX_MAX);
   }
   
   public final boolean keepNumbers;
   
   private Indexer indexer = null;
   private File indexFile = null;
   private WordList wordList = null;
   
   private static final int _WORD_INDEX_MIN = 3;
   private static final int _WORD_INDEX_MAX = 25;
}
