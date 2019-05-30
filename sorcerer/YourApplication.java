package com.github.michaelaaronlevy.ork.sorcerer;

import java.io.File;
import java.io.IOException;

import com.github.michaelaaronlevy.ork.MemenPage;
import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.ModeSimple;
import com.github.michaelaaronlevy.ork.ModeSimple.ReaderMode;
import com.github.michaelaaronlevy.ork.PageConsumer;
import com.github.michaelaaronlevy.ork.PdfToTextGrid;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;

/**
 * If you want to make an application that does not use the ORK GUI, but uses
 * PdfToTextGrid to extract text from one or more PDFs, and processes the
 * extracted text on a page-by-page basis, you can use this class as a starting
 * point.
 * 
 * <p>
 * The most obvious places where you might want to add or change code, to get
 * your application working, are marked with a "TODO" along with an explanation
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the YourApplication class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class YourApplication implements PageConsumer
{
   public static void main(String[] args) throws IOException
   {
      // instantiate an instance of YourApplication
      final YourApplication app = new YourApplication();
      
      // the DEFAULT ReaderMode returns the same text that is extracted by
      // PDFTextStripper.
      // You may want to experiment with using a different ReaderMode from
      // "ModeSimple" or even making your own parser and transformer objects.
      final ReaderMode mode = ModeSimple.ReaderMode.DEFAULT;
      final TextPositionsParser parser = mode.getParser();
      final Transformer transformer = mode.getTransformer();
      
      // TODO: "pdfs" needs to contain references to every single file for which
      // your application is going to extract the text. Right now it is empty,
      // which means that your program is not going to be extracting text from
      // anything.
      final File[] pdfs = new File[] {};
      
      new Thread(new PdfToTextGrid(pdfs, parser, transformer, app, null, false)).start();
   }
   
   public YourApplication()
   {
      // TODO: implement a constructor
   }
   
   public void startProject(final File[] files, final int[] pageCounts) throws IOException
   {
      // PdfToTextGrid will call this before text extraction begins
      // you do not necessarily need to have behavior here, especially because
      // you also have a constructor method
   }
   
   public void startFile(final File file, final int fileNumber, final int pageCount) throws IOException
   {
      // PdfToTextGrid will call this before extraction begins on each PDF
      // you do not necessarily need to have behavior here
      
      // I am including a simple message to the console, which you can feel free
      // to delete
      System.err.println("Status update: starting on file #" + fileNumber + ", " + file.getName());
   }
   
   public void takePage(final int firstId, final int totalPage, final MemenPage page) throws IOException
   {
      // TODO: this is the most important method to fill in
      // every time PdfToTextGrid finishes extracting text from a page, it will
      // invoke this method
      
      // the MemenPage object contains all of the extracted text and the
      // contextual information, from that page, in the form of MemenText
      // objects.
      
      // here is placeholder code that will print the extracted text and
      // contextual information associated with that text to the console
      for(final MemenText textWithContext : page.getWordsArray())
      {
         System.err.println(textWithContext.toString());
      }
   }
   
   public void endOfFile() throws IOException
   {
      // PdfToTextGrid will call this method each time extraction is
      // completed on a PDF file
      // you do not necessarily need to have behavior here
      
      // I am including a simple message to the console, which you can feel free
      // to delete
      System.err.println("Status update: completed extracting text from a PDF.");
   }
   
   public void endOfProject() throws IOException
   {
      // PdfToTextGrid will call this when it is finished extracting text
      // from all PDFs.
      // you do not necessarily need to have behavior here
      
      // of course, this may be a good place to close any streams and otherwise
      // wind down your program.
   }
}
