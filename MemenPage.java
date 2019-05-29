package com.github.michaelaaronlevy.ork;

import java.io.IOException;
import java.io.InputStream;
import java.util.Collection;
import java.util.function.Predicate;

import com.github.michaelaaronlevy.ork.util.Grid;

/**
 * 
 * Each MemenPage object stores the text content of a PDF, as ripped by
 * PdfToTextGrid, with information about the location on the page for that text.
 * The text and location is saved in (basically) immutable {@link MemenText
 * MemenText} objects.
 * 
 * <p>
 * Each MemenPage object is immutable. To get the text, use the getWords method.
 * If you call the getWords method that takes a predicate as a parameter, only
 * {@link MemenText MemenText} objects for which the predicate is "true" will
 * be returned. This allows you to, for example, request all of the
 * {@link MemenText MemenText} words on a page that fit into a certain bounding
 * box, and ignore the rest.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class MemenPage
{
   public MemenPage(final int pdfPage, final int fileNumber, final MemenText[] words)
   {
      this.pdfPage = pdfPage;
      this.fileNumber = fileNumber;
      this.words = words.clone();
   }
   
   public MemenPage(final int pdfPage, final int fileNumber, final Collection<MemenText> words)
   {
      this.pdfPage = pdfPage;
      this.fileNumber = fileNumber;
      this.words = words.toArray(new MemenText[words.size()]);
   }
   
   public MemenPage(final InputStream is) throws IOException
   {
      fileNumber = Grid.readInt(is);
      pdfPage = Grid.readInt(is);
      
      Grid.readInt(is); // we don't need the project page
      Grid.readInt(is); // we don't need the row ids
      words = new MemenText[Grid.readInt(is)];
      for(int i = 0; i < words.length; i++)
      {
         words[i] = new MemenText(is, pdfPage);
      }
   }
   
   public void getWords(final Collection<MemenText> c)
   {
      for(final MemenText w : words)
      {
         c.add(w);
      }
   }
   
   public void getWords(final Collection<MemenText> c, final Predicate<MemenText> t)
   {
      for(final MemenText w : words)
      {
         if(t.test(w))
         {
            c.add(w);
         }
      }
   }
   
   public MemenText[] getWordsArray()
   {
      return words.clone();
   }
   
   public int getNumberOfWords()
   {
      return words.length;
   }
   
   public final int pdfPage;
   public final int fileNumber;
   private final MemenText[] words;
}
