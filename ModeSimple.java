package com.github.michaelaaronlevy.ork;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;

import javax.swing.JOptionPane;

import com.github.michaelaaronlevy.ork.ripping.CharTest;
import com.github.michaelaaronlevy.ork.ripping.ParserDeleteHead;
import com.github.michaelaaronlevy.ork.ripping.ParserDeleteTail;
import com.github.michaelaaronlevy.ork.ripping.ParserSeparateOnCharacter;
import com.github.michaelaaronlevy.ork.ripping.ParserSpacingBackward;
import com.github.michaelaaronlevy.ork.ripping.ParserSpacingForward;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.ripping.TransformerRemoveChars;
import com.github.michaelaaronlevy.ork.ripping.TransformerSingleSpace;
import com.github.michaelaaronlevy.ork.util.CellWriterCsv;
import com.github.michaelaaronlevy.ork.util.CellWriterOds;
import com.github.michaelaaronlevy.ork.util.CellWriterPrint;
import com.github.michaelaaronlevy.ork.util.Status;
import com.github.michaelaaronlevy.ork.util.Status.ErrorStatus;

/**
 * a collection of basic combinations of readers/writers for the {@link PdfToTextGrid PdfToTextGrid}.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ModeSimple implements ExtractionMode
{
   public ModeSimple(final WriterType writerType, final ReaderMode readerMode)
   {
      this.writerType = writerType;
      this.readerMode = readerMode;
   }
   
   public String buttonText()
   {
      return writerType.button + " - " + readerMode.button;
   }
   
   public String getExtension()
   {
      return writerType.extension;
   }
   
   public TextPositionsParser getParser()
   {
      return readerMode.getParser();
   }
   
   public Transformer getTransformer()
   {
      return readerMode.getTransformer();
   }
   
   public PageConsumer getConsumer(final File targetOut)
   {
      try
      {
         return writerType.getConsumer(targetOut);
      }
      catch(final IOException iox)
      {
         JOptionPane.showMessageDialog(null, "Unable to Write Results to " + targetOut.getName(), "Error",
               JOptionPane.ERROR_MESSAGE);
         iox.printStackTrace();
      }
      return null;
   }
   
   public void post(final Status.ErrorStatus status)
   {
      final OpenReviewKit ork = OpenReviewKit.getOrk();
      if(ork == null)
      {
         return;
      }
      final int type = status == ErrorStatus.NO_ERROR ? JOptionPane.INFORMATION_MESSAGE : JOptionPane.ERROR_MESSAGE;
      final boolean c = ork.continueOption(null,
            "Text extraction occurred with " + status.message + ".\nReturn to the text extraction window?", type);
      if(!c && status == ErrorStatus.NO_ERROR)
      {
         System.exit(0);
      }
   }
   
   public final WriterType writerType;
   public final ReaderMode readerMode;
   
   /**
    * The "ReaderMode" determines how to parse the text (as String and
    * TextPosition[]) that is streamed by the PDFTextStripper class.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public enum ReaderMode
   {
      /**
       * Attempts to keep sentences together, but will break up text where there
       * is too much empty space/whitespace between TextPosition objects. This
       * may be the best choice when extracting text from an OCRed document.
       */
      PHRASES("Phrases"),
      
      /**
       * Break each separate word into a separate MemenText object along
       * whitespace.
       */
      WORDS_PLUS("Words +"),
      
      /**
       * Break each separate word into a separate MemenText object and remove
       * leading/trailing punctuation. This is used for the searchable word
       * index.
       */
      WORDS_MINUS("Words -"),
      
      /**
       * Strings are whatever PDFTextStripper returns by default. For rendered
       * text PDFs that are programmatically generated, this may be the best
       * choice.
       */
      DEFAULT("Default");
      
      ReaderMode(final String s)
      {
         button = s;
      }
      
      /**
       * 
       * @return the parser that is to be used by the {@link PdfToTextGrid PdfToTextGrid} object to
       *         carry out this {@link ModeSimple.ReaderMode ReaderMode}
       */
      public TextPositionsParser getParser()
      {
         if(this == DEFAULT)
         {
            return null;
         }
         else if(this == PHRASES)
         {
            return new ParserSpacingForward(new ParserSpacingBackward(
                  new ParserDeleteHead(CharTest.IsWhiteSpace, new ParserDeleteTail(CharTest.IsWhiteSpace, null))));
         }
         else if(this == WORDS_PLUS)
         {
            return new ParserSeparateOnCharacter(CharTest.IsWhiteSpace,
                  new ParserSpacingForward(new ParserSpacingBackward(null)));
         }
         else if(this == WORDS_MINUS)
         {
            final CharTest t1 = new CharTest.NotLetterOrDigit("", "&‘-_~'’@.");
            final CharTest t2 = new CharTest.NotLetterOrDigit("", "~");
            return new ParserSeparateOnCharacter(t1, new ParserSpacingForward(
                  new ParserSpacingBackward(new ParserDeleteHead(t2, new ParserDeleteTail(t2, null)))));
         }
         else
         {
            return null;
         }
      }
      
      /**
       * 
       * @return the transformer that is to be used by the {@link PdfToTextGrid PdfToTextGrid} object
       *         in order to carry out this {@link ModeSimple.ReaderMode ReaderMode}
       */
      public Transformer getTransformer()
      {
         if(this == PHRASES)
         {
            return new Transformer(new TransformerSingleSpace(), null);
         }
         else if(this == WORDS_MINUS)
         {
            return new Transformer(new TransformerRemoveChars(new CharTest.IsInList("‘'’")), null);
         }
         else
         {
            return null;
         }
      }
      
      /**
       * the text to display on the GUI button
       */
      public final String button;
   }
   
   /**
    * The WriterType specifies the format for writing the output.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public enum WriterType
   {
      /**
       * write to a .csv file
       */
      CSV("csv", ".csv"),
      
      /**
       * write to a .ods file (with just one worksheet)
       */
      ODS("ods", ".ods"),
      
      /**
       * print to System.err
       */
      CONSOLE("console", null),
      
      /**
       * write the data to a .grid file (serialized bytes in a proprietary data
       * format that is only used by PdfToTextGrid; see the GritIterMemenPage
       * class for how to retrieve information from the .grid file)
       */
      GRID("grid", ".grid");
      
      private WriterType(final String btn, final String ext)
      {
         button = btn;
         extension = ext;
      }
      
      /**
       * 
       * @param target
       * @return the appropriate PageConsumer object to carry out the
       *         functionality for this particular WriterType
       * @throws IOException
       */
      public PageConsumer getConsumer(final File target) throws IOException
      {
         PageConsumer r = null;
         switch (this)
         {
         case CSV:
            r = new PageConsumerCellWriter(new CellWriterCsv(target));
            break;
         case ODS:
            r = new PageConsumerCellWriter(new CellWriterOds(target, null));
            break;
         case CONSOLE:
            r = new PageConsumerCellWriter(new CellWriterPrint(System.err));
            break;
         case GRID:
            r = new PageConsumerSerial(new BufferedOutputStream(new FileOutputStream(target)), true);
            break;
         }
         return r;
      }
      
      public final String button;
      public final String extension;
   }
}
