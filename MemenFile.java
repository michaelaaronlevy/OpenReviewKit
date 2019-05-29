package com.github.michaelaaronlevy.ork;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.function.Predicate;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.font.PDType1Font;

/**
 * the data format for storing a single PDF File's text information (the content
 * itself, plus the location on the page of each string)
 * 
 * <p> Objects of this class are immutable.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class MemenFile
{
   public MemenFile(final String name, final MemenPage[] pages)
   {
      this.name = name;
      this.pages = pages.clone();
   }
   
   public MemenFile(final InputStream is, final String name, final int pages) throws IOException
   {
      this.name = name;
      this.pages = new MemenPage[pages];
      for(int i = 0; i < pages; i++)
      {
         this.pages[i] = new MemenPage(is);
      }
   }
   
   /**
    * put references to all MemenText objects for this file (all of the pages)
    * into the collection.
    * 
    * @param c
    */
   public void getWords(final Collection<MemenText> c)
   {
      for(final MemenPage p : pages)
      {
         p.getWords(c);
      }
   }
   
   /**
    * for each MemenText object in this file, if Predicate t.test(MemenText)
    * returns true, add it to the collection c; otherwise, do not add it.
    * 
    * @param c
    * @param t
    */
   public void getWords(final Collection<MemenText> c, final Predicate<MemenText> t)
   {
      for(final MemenPage p : pages)
      {
         p.getWords(c, t);
      }
   }
   
   /**
    * method to obtain an array of references to the immutable MemenPage objects
    * associated with this MemenFile.
    * 
    * @return a clone of the array kept by this object. Changing the returned
    *         array will not alter this object.
    */
   public MemenPage[] getPages()
   {
      return pages.clone();
   }
   
   public MemenPage getPage(final int index)
   {
      if(index < 0 || index >= pages.length)
      {
         return null;
      }
      else
      {
         return pages[index];
      }
   }
   
   /**
    * method to create a PDF with only the text content from this MemenFile
    * object, with the same number of pages. This could be useful in terms of
    * testing if you want to see a visual representation of what {@link PdfToTextGrid PdfToTextGrid}
    * has parsed out of a particular PDF. It assumes a standard (American Letter
    * 8.5x11") page size.
    * 
    * <p>A PDDocument object can be viewed in the {@link com.github.michaelaaronlevy.ork.util.pdfbox.SinglePdfViewer SinglePdfViewer} class (or the
    * {@link com.github.michaelaaronlevy.ork.util.pdfbox.SinglePdfWindow SinglePdfWindow} class) without the need to write the PDF to file.
    * 
    * @param test
    *           optional -- can be null -- to filter out text content if
    *           predicate returns "false"
    * @return the PDDocument object which can be written to file.
    * @throws IOException
    */
   public PDDocument toPdf(final Predicate<MemenText> test) throws IOException
   {
      final PDDocument doc = new PDDocument();
      for(final MemenPage p : getPages())
      {
         final PDPage page = new PDPage();
         doc.addPage(page);
         final PDPageContentStream contentStream = new PDPageContentStream(doc, page);
         
         final ArrayList<MemenText> words = new ArrayList<MemenText>();
         if(test == null)
         {
            p.getWords(words);
         }
         else
         {
            p.getWords(words, test);
         }
         for(final MemenText t : words)
         {
            final int yStart = _pageY - t.yStart;
            final int xStart = t.xStart;
            int fontSize = t.height;
            if(fontSize < _minFontSize)
            {
               fontSize = _minFontSize;
            }
            final String text = t.text;
            
            // System.err.println("yS=" + yStart + ", xS=" + xStart + ", fS=" +
            // fontSize + ": " + text);
            
            contentStream.beginText();
            contentStream.setFont(font, fontSize / 1000.0f);
            contentStream.newLineAtOffset(xStart / 1000.0f, yStart / 1000.0f);
            contentStream.showText(text);
            contentStream.endText();
         }
         contentStream.close();
      }
      return doc;
   }
   
   public final String name;
   private final MemenPage[] pages;
   
   private static final PDType1Font font = PDType1Font.TIMES_ROMAN;
   private static final int _pageY = 800000;
   private static final int _minFontSize = 3000;
}
