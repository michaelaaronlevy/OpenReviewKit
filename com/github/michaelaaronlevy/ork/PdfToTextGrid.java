package com.github.michaelaaronlevy.ork;

import java.io.BufferedWriter;
import java.io.File;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.function.UnaryOperator;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.text.PDFTextStripper;
import org.apache.pdfbox.text.TextPosition;

import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.util.RunnableWithStatus;
import com.github.michaelaaronlevy.ork.util.Status;
import com.github.michaelaaronlevy.ork.util.StatusReporter;

/**
 * PdfToTextGrid is an enhanced alternative to the excellent PDFTextStripper
 * class. It is built around the PDFTextStripper class that is included with
 * PDFBox, but it has several advantages:
 * 
 * <p>
 * 1 - output is in the form of a {@link MemenPage MemenPage} object containing
 * an entire page of data, rather than streaming one String at a time.
 * PdfToTextGrid sends the {@link MemenPage MemenPage} objects, one page at a
 * time, to the {@link PageConsumer PageConsumer} instance that was passed to
 * the PdfToTextGrid constructor.
 * 
 * <p>
 * 2 - the {@link MemenPage MemenPage} objects contain a list of
 * {@link MemenText MemenText} objects, which are easier to work with than the
 * Strings and TextPosition[] used by PDFTextStripper. {@link MemenText
 * MemenText} uses integers and one String, rather than a combination of various
 * data types (integers, floats, Matrix, etc.) that the TextPosition class uses.
 * ("Memen" as in "Memento," which is Latin for "Remember," because the text
 * remembers the location it came from.) {@link MemenText MemenText} objects are
 * basically immutable.
 * 
 * <p>
 * 3 - the PDFTextStripper ignores blank pages, but PdfToTextGrid will send a
 * {@link MemenPage MemenPage} object that has no {@link MemenText MemenText}
 * content to show that there is a blank page. The PageConsumer will receive
 * exactly as many {@link MemenPage MemenPage} objects as there are pages in the
 * PDF(s) on which text extraction is performed.
 * 
 * <p>
 * 4 - PdfToTextGrid is meant to be a final class. You can customize the
 * behavior of how it parses the text without extending it. Customization is
 * achieved by passing certain objects to the constructor:
 * 
 * <p>
 * 4a - {@link com.github.michaelaaronlevy.ork.ripping.TextPositionsParser
 * TextPositionsParser} objects can be used to modify the raw TextPosition[]
 * information streamed by the PDFTextStripper class. This allows you to, for
 * example, delete certain unwanted characters. Characters that are removed at
 * this stage do not affect the position information of the {@link MemenText
 * MemenText} objects that are ultimately created.
 * 
 * <p>
 * Also, the TextPosition[] (which is put into an ArrayList&lt;TextPosition&gt;)
 * can be broken up into multiple separate ArrayList&lt;TextPosition&gt; so that
 * the output ends up in separate {@link MemenText MemenText} objects. For
 * example, if you split up the ArrayList&lt;TextPosition&gt; at whitespace
 * characters, you are effectively forcing each separate word to be in its own
 * {@link MemenText MemenText} object. The searchable word index does this
 * because it needs every word to be a separate {@link MemenText MemenText}
 * object so it can calculate which words are on any given page.
 * 
 * <p>
 * Another example of a
 * {@link com.github.michaelaaronlevy.ork.ripping.TextPositionsParser
 * TextPositionsParser} that breaks up ArrayList&lt;TextPosition&gt; into
 * multiple ArrayList&lt;TextPosition&gt; is ParserSpacing, which is especially
 * useful for documents that have been OCRed.
 * 
 * <p>
 * 4b - {@link com.github.michaelaaronlevy.ork.ripping.Transformer Transformer}
 * objects are used to modify the text content of the {@link MemenText
 * MemenText} object after the positional information is already set in stone.
 * For example, you could use a
 * {@link com.github.michaelaaronlevy.ork.ripping.Transformer Transformer} to
 * delete certain unwanted words (e.g., the skip list for the word index), to
 * change words to lowercase, or to completely change the text associated with a
 * given entry, while maintaining the positional information.
 * 
 * <p>
 * By moving these operations into implementations of functional interfaces,
 * that are called sequentially (in whatever order you arrange them before
 * passing them to the constructor), it dramatically reduces the potential for
 * error and promotes reusability of the code. The point is that you should
 * never need to extend, or know anything about, the PDFTextStripper class, in
 * order to use PdfToTextGrid. If I made this correctly, you should not be
 * tempted to create your own subclass of PDFTextStripper.
 * 
 * @author michaelaaronlevy@gmail.com
 */

public class PdfToTextGrid implements RunnableWithStatus
{
   /**
    * the primary means of interfacing programmatically with this class is to
    * use this constructor and then call the .run() method.
    * 
    * @param files
    *           the PDF files for which the text will be extracted. (Extracting
    *           text does not alter the files.)
    * @param parser
    *           the logic behind separating output into phrases/words/characters
    *           and transforming it (for example, changing all letters to
    *           lowercase, or removing all numbers). If null, it makes no
    *           attempt to change the output returned by PDFTextStripper.
    * @param transformer
    *           the logic for deleting text or altering the text after the
    *           positional data is fixed. Alterations are based purely on the
    *           content of the string and will not affect positional
    *           information. If null, there will be no change.
    * @param consumer
    *           the recipient of the text extracted from the PDF(s).
    *           PageConsumer objects receive the information for one PDF page at
    *           a time.
    * @param doAfter
    *           PdfToTextGrid calls this method after it is finished running.
    * @param includeArray
    *           if true, each MemenText object will have a reference to the
    *           TextPosition[] that it was based on. This is for power users
    *           (and perhaps for debugging).
    */
   public PdfToTextGrid(final File[] files, final TextPositionsParser parser, final Transformer transformer,
         final PageConsumer consumer, final ExtractionMode doAfter, final boolean includeArray) throws IOException
   {
      stripper = new RipperStripper();
      
      status = new Status("PdfToTextGrid");
      
      this.files = files;
      this.currentFileNumber = -1;
      this.parser = parser == null ? new NullParser() : parser;
      this.transformer = transformer == null ? new Transformer(new NullTransformer(), null) : transformer;
      this.consumer = consumer;
      this.doAfter = doAfter;
      
      rows = new ArrayList<MemenText>(999);
      this.id = 0;
      this.pdfPage = -1;
      this.projectPage = -1;
      this.includeArray = includeArray;
   }
   
   public synchronized void run()
   {
      if(status.reporter.hasFatalError() || status.reporter.isRunning() || status.reporter.isFinished())
      {
         return;
      }
      status.declareStart();
      
      try
      {
         final String[] filesInfo = new String[files.length];
         for(int i = 0; i < files.length; i++)
         {
            filesInfo[i] = files[i].getCanonicalPath();
         }
         
         final int[] pagesPerPdf = new int[files.length];
         final PageCounter[] pcs = new PageCounter[8];
         for(int i = 0; i < pcs.length; i++)
         {
            pcs[i] = new PageCounter(i, pcs.length, files, pagesPerPdf);
            new Thread(pcs[i]).start();
         }
         
         boolean flag = true;
         while(flag)
         {
            try
            {
               Thread.sleep(_WAIT_TIME);
            }
            catch(final InterruptedException iex)
            {
               // do nothing
            }
            
            flag = false;
            for(final PageCounter pc : pcs)
            {
               flag |= !pc.isFinished;
            }
         }
         for(int i = 0; i < pcs.length; i++)
         {
            pcs[i] = null;
         }
         
         consumer.startProject(files, pagesPerPdf);
      }
      catch(final IOException iox)
      {
         status.declareFatalError(iox.getMessage());
         iox.printStackTrace();
      }
      
      final LoadAhead loader = new LoadAhead(16, files);
      new Thread(loader).start();
      
      for(int fileIndex = 0; fileIndex < files.length; fileIndex++)
      {
         noTextInFile = true;
         final PDDocument doc = loader.getNext();
         if(doc == null)
         {
            status.declareFatalError(loader.error);
            status.declareEnd();
            return;
         }
         currentFileNumber++;
         try
         {
            consumer.startFile(files[fileIndex], getCurrentFileCount(), getCurrentTotalPageCount());
            
            pdfPage = -1;
            processFile(doc);
            
            // if there are additional blank pages at the end of the file, we
            // need to pass empty MemenPage objects to the PageConsumer.
            // PDFTextStripper ignores blank pages, so we need to check for them
            // and to act accordingly.
            final int blanksAtEnd = stripper.getPageNo() - pdfPage - 1;
            for(int i = 0; i < blanksAtEnd; i++)
            {
               pdfPage++;
               projectPage++;
               
               consumer.takePage(id, getCurrentTotalPageCount(),
                     new MemenPage(stripper.getPageNo(), getCurrentFileCount(), _EMPTY));
            }
            
            consumer.endOfFile();
            if(noTextInFile)
            {
               System.err.println("WARNING: File had no recognized text content. Perhaps run OCR: "
                     + files[fileIndex].getAbsolutePath());
            }
         }
         catch(final IOException iox)
         {
            status.declareFatalError(iox.getMessage());
            iox.printStackTrace();
         }
      }
      
      status.updateStatus("Text Extraction complete - Post-Processing by PageConsumer.");
      
      try
      {
         consumer.endOfProject();
      }
      catch(final IOException iox)
      {
         status.declareError(iox.getMessage());
         iox.printStackTrace();
      }
      
      status.declareEnd();
      
      if(doAfter != null)
      {
         status.updateStatus("Text Extraction complete - Post-Processing by ExtractionMode.");
         doAfter.post(status.errorStatus);
      }
      
      status.updateStatus("Text Extraction complete - Post-Processing Complete.");
   }
   
   void processFile(final PDDocument doc) throws IOException
   {
      final BufferedWriter b = new BufferedWriter(new OutputStreamWriter(System.err));
      setPdfPages(doc.getNumberOfPages());
      stripper.writeText(doc, b);
      doc.close();
      b.flush();
   }
   
   /**
    * 
    * @return the number of PDF files that this controller has finished
    *         processing, + 1 if it is currently processing one.
    */
   public int getCurrentFileCount()
   {
      return currentFileNumber + 1;
   }
   
   /**
    * 
    * @return the number of PDF files that this controller will attempt to
    *         process
    */
   public int getTotalFileCount()
   {
      return files.length;
   }
   
   /**
    * 
    * @return a unique identifier, that increases incrementally, for each
    *         separate bit of content from the PDFs being written (each id
    *         represents another MemenText object, which is another row in a
    *         spreadsheet if you are outputting the content to a basic
    *         spreadsheet)
    */
   public int getCurrentRowCount()
   {
      return id;
   }
   
   /**
    * 
    * @return the page number that the reader is currently working on, of the
    *         PDF that the reader is currently working on. Page numbers start at
    *         "1."
    */
   public int getCurrentPdfPageCount()
   {
      return pdfPage + 1;
   }
   
   /**
    * 
    * @return the page number corresponding to the total number of pages
    *         processed by the PDF reader, for all PDFs being processed. Page
    *         numbers start at "1."
    */
   public int getCurrentTotalPageCount()
   {
      return projectPage + 1;
   }
   
   private void setPdfPages(final int p)
   {
      pdfPagesTotal = p;
   }
   
   /**
    * @return the completion percentage (based on the number of pages) for the
    *         PDF currently being ripped. (Does not take into account that there
    *         may be multiple PDFs queued or that some pages will take longer to
    *         read than others.) This method is primarily for use with a GUI.
    */
   private String getPdfPercent()
   {
      final int p = pdfPagesTotal <= 0 ? 0 : (((pdfPage < 0 ? 0 : pdfPage) * 1000) / pdfPagesTotal);
      String s = (p < 100 ? " " : "") + (p == 0 ? "0" : "") + p + "%.";
      s = s.substring(0, s.length() - 3) + "." + s.substring(s.length() - 3);
      if(s.charAt(0) == '.')
      {
         s = "0" + s;
      }
      return s;
   }
   
   public void updateStatus()
   {
      if(status.reporter.isRunning() && !status.reporter.hasError())
      {
         if(files.length == 1)
         {
            status.updateStatus(_PROCESSING_FILE + getPdfPercent());
         }
         else
         {
            status.updateStatus(
                  _PROCESSING_FILE + getCurrentFileCount() + " of " + files.length + " - " + getPdfPercent());
         }
      }
   }
   
   public StatusReporter getStatus()
   {
      return status.reporter;
   }
   
   private static ArrayList<TextWithPositions> parseWords(final ArrayList<ArrayList<TextPosition>> words,
         final Transformer transformer)
   {
      final ArrayList<TextWithPositions> r = new ArrayList<TextWithPositions>(words.size());
      for(final ArrayList<TextPosition> word : words)
      {
         final TextWithPositions w = parseWord(word, transformer);
         if(w != null)
         {
            r.add(w);
         }
      }
      return r;
   }
   
   private static TextWithPositions parseWord(final ArrayList<TextPosition> word, final Transformer transformer)
   {
      if(word.size() > 0)
      {
         while(!word.isEmpty() && Character.isWhitespace(word.get(word.size() - 1).getUnicode().charAt(0)))
         {
            word.remove(word.size() - 1);
         }
         if(word.size() > 0)
         {
            final String content = transformer.transform(getString(word));
            if(content == null)
            {
               return null;
            }
            return new TextWithPositions(word, 0, word.size(), content);
         }
      }
      return null;
   }
   
   public static String getString(final List<TextPosition> list)
   {
      return getString(list, 0, list.size());
   }
   
   public static String getString(final List<TextPosition> list, final int start, final int end)
   {
      final char[] c = new char[end - start];
      for(int i = start; i < end; i++)
      {
         final int index = i - start;
         c[index] = list.get(i).getUnicode().charAt(0);
      }
      return new String(c);
   }
   
   private final RipperStripper stripper;
   private final TextPositionsParser parser;
   private final Transformer transformer;
   private final PageConsumer consumer;
   private final ExtractionMode doAfter;
   private final boolean includeArray;
   
   private final File[] files;
   private final ArrayList<MemenText> rows;
   
   private int id;
   private int pdfPage;
   private int pdfPagesTotal = 0;
   private int projectPage;
   private int currentFileNumber;
   
   private boolean noTextInFile;
   
   private static final String _PROCESSING_FILE = "Processing File: ";
   
   private final Status status;
   
   private static final int _WAIT_TIME = 10;
   
   private static final MemenText[] _EMPTY = new MemenText[0];
   
   /**
    * this class was created to speed up the process of counting pages by
    * enabling multi-threading
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   private static final class PageCounter implements Runnable
   {
      private PageCounter(final int part, final int whole, final File[] files, final int[] target)
      {
         this.part = part;
         this.whole = whole;
         this.files = files;
         this.target = target;
      }
      
      public void run()
      {
         int index = part;
         for(; index < files.length; index += whole)
         {
            try
            {
               final PDDocument doc = Loader.loadPDF(files[index]);
               target[index] = doc.getNumberOfPages();
               doc.close();
            }
            catch(final IOException iox)
            {
               isFinished = true;
               System.err.println(iox.getMessage());
               return;
            }
         }
         isFinished = true;
      }
      
      private final File[] files;
      private final int[] target;
      private final int part;
      private final int whole;
      private boolean isFinished = false;
   }
   
   /**
    * this class was created to speed up text extraction by opening PDF files in
    * advance of when PdfToTextGrid attempts to read them.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   private static final class LoadAhead implements Runnable
   {
      private LoadAhead(final int numberToLoad, final File[] files)
      {
         this.files = files;
         rotating = new PDDocument[numberToLoad];
      }
      
      private PDDocument getNext()
      {
         // System.err.println("getNext() called." + toString());
         
         while(available() == 0)
         {
            try
            {
               Thread.sleep(_WAIT_TIME);
            }
            catch(final InterruptedException iex)
            {
               // System.err.println("InterruptedException in getNext");
               
               // do nothing
            }
            if(error != null)
            {
               return null;
            }
            
            // System.err.println("getNext() looping." + toString());
            
         }
         
         // System.err.println("getNext() done looping." + toString());
         
         final PDDocument r = rotating[nextAvailable];
         rotating[nextAvailable] = null;
         advanceLoaded();
         
         // System.err.println("getNext() returning." + toString());
         
         return r;
      }
      
      public void run()
      {
         // System.err.println("run() called");
         
         while(filesLoaded < files.length)
         {
            while(error == null && available() == rotating.length - 1)
            {
               try
               {
                  Thread.sleep(_WAIT_TIME);
               }
               catch(final InterruptedException iex)
               {
                  // System.err.println("InterruptedException in run");
               }
               
               // System.err.println("run() Looping" + toString());
            }
            if(error != null)
            {
               return;
            }
            try
            {
               // System.err.println("run() About to Load." + toString());
               
               final int target = nextEmpty;
               
               rotating[target] = Loader.loadPDF(files[filesLoaded++]);
               
               advanceFree();
               
               // System.err.println("run() Loading Complete." + toString());
               
            }
            catch(final IOException iox)
            {
               iox.printStackTrace();
               error = iox.getMessage();
               return;
            }
         }
      }
      
      private synchronized void advanceLoaded()
      {
         if(nextAvailable == rotating.length - 1)
         {
            nextAvailable = 0;
         }
         else
         {
            nextAvailable++;
         }
      }
      
      private synchronized void advanceFree()
      {
         if(nextEmpty == rotating.length - 1)
         {
            nextEmpty = 0;
         }
         else
         {
            nextEmpty++;
         }
      }
      
      private synchronized int available()
      {
         return (nextEmpty >= nextAvailable ? 0 : rotating.length) + nextEmpty - nextAvailable;
      }
      
      public String toString()
      {
         return " cL=" + nextAvailable + ", cF=" + nextEmpty + ", available=" + available();
      }
      
      private final File[] files;
      private final PDDocument[] rotating;
      private int nextAvailable = 0;
      private int nextEmpty = 0;
      private int filesLoaded = 0;
      private String error = null;
   }
   
   private static class TextWithPositions
   {
      private TextWithPositions(final List<TextPosition> tp)
      {
         this(tp, 0, tp.size());
      }
      
      private TextWithPositions(final List<TextPosition> tp, final int start, final int end)
      {
         positions = new TextPosition[end - start];
         final char[] c = new char[end - start];
         for(int i = start; i < end; i++)
         {
            final int index = i - start;
            positions[index] = tp.get(i);
            c[index] = positions[index].getUnicode().charAt(0);
         }
         content = new String(c);
      }
      
      private TextWithPositions(final List<TextPosition> tp, final int start, final int end, final String content)
      {
         positions = new TextPosition[end - start];
         for(int i = start; i < end; i++)
         {
            final int index = i - start;
            positions[index] = tp.get(i);
         }
         this.content = content;
      }
      
      private TextWithPositions(final TextPosition[] positions, final String content)
      {
         this.positions = positions;
         this.content = content;
      }
      
      private MemenText getWord(final int pageNumber, final boolean includeArray)
      {
         float ys = Float.MAX_VALUE;
         float xs = Float.MAX_VALUE;
         float xe = Float.MIN_VALUE;
         float fs = 0.0f;
         float fsip = 0.0f;
         float ysc = 0.0f;
         
         for(final TextPosition tp : positions)
         {
            if(ys > tp.getY())
            {
               ys = tp.getY();
            }
            if(xs > tp.getX())
            {
               xs = tp.getX();
            }
            if(xe < tp.getEndX())
            {
               xe = tp.getEndX();
            }
            if(fs < tp.getFontSize())
            {
               fs = tp.getFontSize();
            }
            if(fsip < tp.getFontSizeInPt())
            {
               fsip = tp.getFontSizeInPt();
            }
            if(ysc < tp.getYScale())
            {
               ysc = tp.getYScale();
            }
         }
         
         final int y_start = (int) (1000.0 * ys);
         final int x_start = (int) (1000.0 * xs);
         final int x_end = (int) (1000.0 * xe);
         final int height = (int) (1000.0 * (fs == 1.0f ? ysc : fsip));
         final int rotation = (int) (positions[0].getDir() + 0.1f);
         return new MemenText(pageNumber, y_start - height, x_start, x_end, height, rotation, content,
               includeArray ? positions : null);
      }
      
      public String toString()
      {
         return "twp[" + content + "]";
      }
      
      public final TextPosition[] positions;
      public final String content;
   }
   
   private static final class NullParser extends TextPositionsParser
   {
      public NullParser()
      {
         super(null);
      }
      
      protected void parseLine(ArrayList<TextPosition> nextWord, ArrayList<ArrayList<TextPosition>> output)
      {
         output.add(nextWord);
      }
   }
   
   private static final class NullTransformer implements UnaryOperator<String>
   {
      public String apply(final String s)
      {
         return s;
      }
   }
   
   private class RipperStripper extends PDFTextStripper
   {
      private RipperStripper() throws IOException
      {
         super();
         setSortByPosition(true);
         setArticleEnd("");
         setArticleStart("");
         setLineSeparator("");
         setPageEnd("");
         setPageStart("");
         setParagraphEnd("");
         setParagraphStart("");
         setWordSeparator("");
      }
      
      private int getPageNo()
      {
         return getCurrentPageNo();
      }
      
      protected void writeString(String text, List<TextPosition> textPositions) throws IOException
      {
         ArrayList<TextPosition> arrayList = null;
         
         if(textPositions instanceof ArrayList)
         {
            arrayList = (ArrayList<TextPosition>) textPositions;
         }
         else
         {
            arrayList = new ArrayList<TextPosition>(textPositions.size());
            for(final TextPosition tp : textPositions)
            {
               arrayList.add(tp);
            }
         }
         
         final ArrayList<TextWithPositions> words = parseWords(parser.parse(arrayList), transformer);
         
         for(final TextWithPositions word : words)
         {
            if(word != null && word.content != null & word.content.length() > 0)
            {
               rows.add(word.getWord(getCurrentPageNo(), includeArray));
            }
         }
      }
      
      protected void startPage(final PDPage page) throws IOException
      {
         final int p = getCurrentPdfPageCount();
         if(p == -1 || p % 30 == 0)
         {
            updateStatus();
         }
         
         pdfPage++;
         projectPage++;
         while(pdfPage + 1 < getCurrentPageNo())
         {
            // PDFTextStripper skips over blank pages. This creates a blank page
            // for each one skipped.
            consumer.takePage(id, getCurrentTotalPageCount(),
                  new MemenPage(stripper.getPageNo(), getCurrentFileCount(), _EMPTY));
            pdfPage++;
            projectPage++;
         }
      }
      
      protected void endPage(final PDPage page) throws IOException
      {
         try
         {
            if(!rows.isEmpty())
            {
               noTextInFile = false;
               rows.sort(null);
               rows.get(0).smooth(null);
               for(int i = 1; i < rows.size(); i++)
               {
                  rows.get(i).smooth(rows.get(i - 1));
               }
               rows.sort(null);
            }
            
            consumer.takePage(id, getCurrentTotalPageCount(),
                  new MemenPage(stripper.getPageNo(), getCurrentFileCount(), rows));
         }
         catch(final IOException iox)
         {
            status.declareFatalError(iox.getMessage());
         }
         rows.clear();
      }
   }
}
