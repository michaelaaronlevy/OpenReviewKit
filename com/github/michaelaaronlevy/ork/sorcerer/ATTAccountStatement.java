package com.github.michaelaaronlevy.ork.sorcerer;

import java.io.File;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;

import javax.swing.JOptionPane;

import com.github.michaelaaronlevy.ork.ExtractionMode;
import com.github.michaelaaronlevy.ork.MemenPage;
import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.MemenText.Height;
import com.github.michaelaaronlevy.ork.MemenText.IsEntirelyInBox;
import com.github.michaelaaronlevy.ork.MemenText.Operation;
import com.github.michaelaaronlevy.ork.MemenText.SortBaseline;
import com.github.michaelaaronlevy.ork.ModeSimple;
import com.github.michaelaaronlevy.ork.PageConsumer;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.util.CellWriter;
import com.github.michaelaaronlevy.ork.util.CellWriterCsv;
import com.github.michaelaaronlevy.ork.util.Status;

/**
 * use this to transform an AT&amp;T Account Statement PDF into a spreadsheet
 * where each row represents an entry in the account statement (a phone call,
 * text message, data transfer, or video/picture message).
 * 
 * <p>
 * This class is both a tool that I actually use in my law practice, as well as
 * a proof of concept for what
 * {@link com.github.michaelaaronlevy.ork.PdfToTextGrid PdfToTextGrid} is
 * capable of. Trying to build something like this with PDFTextStripper would be
 * far, far more difficult. You can see workarounds for areas where the
 * PDFTextStripper combines two things that should be in separate cells (which
 * is probably because AT&amp;T did it that way, not because PDFTextStripper is
 * somehow deficient).
 * 
 * <p>
 * You can see where I read the Account Statement's page number (to make sure it
 * starts with an "A" - page numbers that are not preceded by an "A" correspond
 * to pages without this data).
 * 
 * <p>
 * You can see where I use
 * {@link com.github.michaelaaronlevy.ork.MemenText.IsEntirelyInBox
 * IsEntirelyInBox} objects to get just a subset of the
 * {@link com.github.michaelaaronlevy.ork.MemenText MemenText} objects for the
 * {@link com.github.michaelaaronlevy.ork.MemenPage MemenPage}, because I want
 * to process half of the page at a time. By having just a single set of
 * coordinates/position info for an entire String (rather than an array or
 * collection of TextPosition objects, which each have their own information)
 * these operations are dramatically simplified.
 * 
 * <p>
 * I only have limited access to examples of AT&amp;T Account Statements so:
 * this implementation might not work if you have a "weird" statement, or if
 * AT&amp;T changes the format of its statements. No warranty, express or
 * implied. It is your responsibility to check all output for accuracy.
 * 
 * <p>
 * (I am not affiliated with AT&amp;T in any way and AT&amp;T in no way endorses
 * this software...)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ATTAccountStatement implements PageConsumer
{
   /**
    * Information for the {@link com.github.michaelaaronlevy.ork.ExtractionGUI
    * ExtractionGUI} so it can display a button to run this function.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static class Mode implements ExtractionMode
   {
      public String buttonText()
      {
         return "AT&T -> .csv";
      }
      
      public String getExtension()
      {
         return ".csv";
      }
      
      public TextPositionsParser getParser()
      {
         return null;
      }
      
      public Transformer getTransformer()
      {
         return null;
      }
      
      public PageConsumer getConsumer(File targetOut)
      {
         try
         {
            final CellWriter fw = new CellWriterCsv(targetOut);
            return new ATTAccountStatement(fw);
         }
         catch(final IOException iox)
         {
            JOptionPane.showMessageDialog(null, "Unable to Write Results to " + targetOut.getName(), "Error",
                  JOptionPane.ERROR_MESSAGE);
            iox.printStackTrace();
            return null;
         }
      }
      
      public PageConsumer getConsumer(final CellWriter fw)
      {
         return new ATTAccountStatement(fw);
      }
      
      public void post(final Status.ErrorStatus status)
      {
         new ModeSimple(null, null).post(status);
      }
   }
   
   public ATTAccountStatement(final CellWriter out)
   {
      fw = out;
   }
   
   public void startProject(final File[] files, final int[] pageCounts) throws IOException
   {
      for(int i = 0; i < files.length; i++)
      {
         fw.writeInt(i + 1);
         fw.writeText(files[i].getAbsolutePath());
         fw.newRow();
      }
      fw.newRow();
      for(final String title : _COLUMN_TITLES)
      {
         fw.writeText(title);
      }
      fw.newRow();
   }
   
   public void startFile(final File file, final int fileNumber, final int pageCount) throws IOException
   {
      // do nothing
   }
   
   public void takePage(final int firstId, final int totalPage, final MemenPage page) throws IOException
   {
      final IsEntirelyInBox pageNumberFinder = new IsEntirelyInBox(35000, 46000, 490000, 600000);
      final ArrayList<MemenText> pageNumber = new ArrayList<MemenText>();
      
      final IsEntirelyInBox rangeFinder = new IsEntirelyInBox(45000, 58000, 490000, 600000);
      final ArrayList<MemenText> dateRange = new ArrayList<MemenText>();
      
      // System.err.println("ATTAS::parseStatement iterating pages");
      
      page.getWords(pageNumber, pageNumberFinder);
      
      // for(final Word w : pageNumber) System.err.println(w.text);
      
      if(!pageNumber.isEmpty() && pageNumber.get(0).text.charAt(0) == 'A')
      {
         page.getWords(dateRange, rangeFinder);
         final int yearStart = 2000 + Integer.parseInt(dateRange.get(0).text.substring(6));
         final int yearEnd = 2000 + Integer.parseInt(dateRange.get(2).text.substring(6));
         if(yearStart == yearEnd)
         {
            year = yearStart;
         }
         else
         {
            year = -yearStart;
         }
         
         try
         {
            parseSide(page, 198000, 730000, 21600);
            parseSide(page, 146000, 730000, 321600);
         }
         catch(final ParseException pex)
         {
            throw new RuntimeException(pex);
         }
      }
      pageNumber.clear();
   }
   
   public void endOfFile() throws IOException
   {
      // do nothing
   }
   
   public void endOfProject() throws IOException
   {
      fw.close();
   }
   
   private final CellWriter fw;
   private int year;
   
   private void parseSide(final MemenPage page, final int yMin, final int yMax, final int xOffset)
         throws IOException, ParseException
   {
      // System.err.println("ATTAS::parseSide " + yMin + "," + yMax + "," +
      // xOffset);
      
      final boolean rightSide = xOffset > 300000;
      final ArrayList<MemenText> listTitles = new ArrayList<MemenText>(10);
      final IsEntirelyInBox fullSide = new IsEntirelyInBox(yMin, yMax, xOffset - 10000, xOffset + 278400);
      final Height ninePoint = new Height(9000, 9000);
      final Operation titles = new Operation(fullSide, 2, ninePoint);
      page.getWords(listTitles, titles);
      
      final ArrayList<MemenText> firstWords = new ArrayList<MemenText>(100);
      page.getWords(firstWords,
            new Operation(new IsEntirelyInBox(yMin, yMin + 50000, xOffset - 10000, xOffset + 278400), -1, ninePoint));
      while(firstWords.size() > 1)
      {
         firstWords.remove(firstWords.size() - 1);
      }
      if(firstWords.isEmpty())
      {
         return;
      }
      final String firstWord = firstWords.get(0).text;
      
      for(int i = 0; i < listTitles.size(); i++)
      {
         final MemenText w = listTitles.get(i);
         if(w.text.equalsIgnoreCase("- Continued"))
         {
            listTitles.remove(i);
            i--;
         }
      }
      
      if(rightSide)
      {
         if(listTitles.isEmpty())
         {
            if(firstWord.equals("Place"))
            {
               parsePhone(page, yMin + 20000, yMax, xOffset);
            }
            else
            {
               parseData(page, yMin + 10000, yMax, xOffset);
            }
         }
         else
         {
            final MemenText firstTitle = listTitles.get(0);
            parsePhone(page, yMin + 20000, firstTitle.yStart, xOffset);
            
            parseData(page, firstTitle.yStart + 20000, yMax, xOffset);
            // this was 10k, which worked for some files but not others. I
            // increased it to 20k and I stopped seeing problems.
            // if the program crashes on a page that has both phone and data
            // information in the same column, the most likely culprit is that
            // this number needs to be adjusted up or down from 20k.
         }
      }
      else if(listTitles.size() == 0) // left side
      {
         if(firstWord.equals("Place"))
         {
            parsePhone(page, yMin + 20000, yMax, xOffset);
         }
         else
         {
            parseData(page, yMin + 10000, yMax, xOffset);
         }
      }
      else if(listTitles.size() == 1)
      {
         final MemenText midTitle = listTitles.get(0);
         parsePhone(page, yMin + 20000, midTitle.yStart, xOffset);
         parseData(page, midTitle.yStart + 20000, yMax, xOffset);
      }
      else
      {
         throw new RuntimeException("Too many Nine-Point Font Things on Left Side: " + listTitles.toString());
      }
   }
   
   public void parsePhone(final MemenPage page, final int yMin, final int yMax, final int xOffset)
         throws IOException, ParseException
   {
      final ArrayList<MemenText> words = new ArrayList<MemenText>();
      page.getWords(words, new IsEntirelyInBox(yMin, yMax, xOffset - 10000, xOffset + 278400));
      words.sort(new SortBaseline()); // I don't need a special comparator for
                                      // this
      words.add(new MemenText(0, 0, 0, 0, 0, 8000, 0, "Subtotal", null));
      
      String date = null;
      
      for(int i = 0; i < words.size(); i++)
      {
         MemenText w = words.get(i);
         if(w.height < 7000)
         {
            // copyright, so do nothing
         }
         else if(isDay(w.text))
         {
            i++;
            date = words.get(i).text;
            if(year > 0)
            {
               date = date + "/" + year;
            }
            else
            {
               final int mo = Integer.parseInt(date.substring(0, 2));
               if(mo < 7)
               {
                  date = date + "/" + (1 - year);
               }
               else
               {
                  date = date + "/" + (0 - year);
               }
            }
         }
         else if(w.text.equals("Subtotal"))
         {
            return;
         }
         else
         {
            fw.writeInt(page.fileNumber);
            fw.writeInt(page.pdfPage);
            fw.writeText("Phone Call");
            
            String dateStr = date + " " + w.text;
            final StringBuffer b = new StringBuffer(dateStr.length() + 2);
            b.append(dateStr);
            final char c = b.charAt(b.length() - 1);
            b.delete(b.length() - 1, b.length());
            b.append(' ');
            if(c == 'p')
            {
               b.append("PM");
            }
            else
            {
               b.append("AM");
            }
            dateStr = b.toString();
            
            fw.writeDate(_sdf.parse(dateStr));
            w = words.get(++i);
            fw.writeText(w.text); // sent/received
            w = words.get(++i);
            fw.writeText(w.text); // number
            w = words.get(++i);
            final String rateCode = w.text;
            w = words.get(++i);
            final String featureCode;
            if(w.xEnd - xOffset < 190000)
            {
               featureCode = w.text;
               w = words.get(++i);
            }
            else
            {
               featureCode = null;
            }
            fw.writeText(w.text); // minutes
            fw.writeText(rateCode);
            if(featureCode == null)
            {
               fw.writeBlank();
            }
            else
            {
               fw.writeText(featureCode);
            }
            w = words.get(++i);
            fw.writeText(w.text);
            w = words.get(++i);
            fw.writeText(w.text);
            fw.newRow();
         }
      }
   }
   
   public void parseData(final MemenPage page, final int yMin, final int yMax, final int xOffset)
         throws IOException, ParseException
   {
      // printSet(page, yMin, yMax, xOffset);
      
      final ArrayList<MemenText> words = new ArrayList<MemenText>();
      page.getWords(words, new IsEntirelyInBox(yMin, yMax, xOffset - 10000, xOffset + 278400));
      if(words.isEmpty())
      {
         return;
      }
      words.sort(new SortBaseline());
      words.add(new MemenText(0, 0, 0, 0, 0, 8000, 0, "Subtotal", null));
      String date = null;
      String dataType = null;
      
      for(int i = 0; i < words.size(); i++)
      {
         MemenText w = words.get(i);
         if(w.height < 7000)
         {
            continue; // copyright
         }
         else if(isDataType(w.text))
         {
            dataType = w.text;
         }
         else if(isDay(w.text))
         {
            i++;
            date = words.get(i).text;
            if(year > 0)
            {
               date = date + "/" + year;
            }
            else
            {
               final int mo = Integer.parseInt(date.substring(0, 2));
               if(mo < 7)
               {
                  date = date + "/" + (1 - year);
               }
               else
               {
                  date = date + "/" + (0 - year);
               }
            }
         }
         else if(w.text.startsWith("Subtotal"))
         {
            for(; i < words.size(); i++)
            {
               w = words.get(i);
               if(isDataType(w.text))
               {
                  dataType = w.text;
                  break;
               }
            }
         }
         else
         {
            final MemenText firstWord = w;
            
            fw.writeInt(page.fileNumber);
            fw.writeInt(page.pdfPage);
            fw.writeText(dataType);
            
            String dateStr = date + " " + w.text;
            
            final StringBuffer b = new StringBuffer(dateStr.length() + 2);
            b.append(dateStr);
            final char c = b.charAt(b.length() - 1);
            b.delete(b.length() - 1, b.length());
            b.append(' ');
            if(c == 'p')
            {
               b.append("PM");
            }
            else
            {
               b.append("AM");
            }
            dateStr = b.toString();
            fw.writeDate(_sdf.parse(dateStr));
            
            final ArrayList<String> wordList = new ArrayList<String>(8);
            while(words.get(i + 1).getYSmooth() == firstWord.getYSmooth())
            {
               w = words.get(++i);
               final String text = w.text;
               if(text.length() > 14 && text.endsWith("Pict Video MSG"))
               {
                  wordList.add(text.substring(0, text.length() - 14).trim());
                  wordList.add("Pict Video MSG");
               }
               else if(dataType.equals("Text Messages") && wordList.size() == 1)
               {
                  wordList.add(text.substring(0, 13).trim());
                  wordList.add(text.substring(13).trim());
               }
               else
               {
                  wordList.add(text);
               }
            }
            
            // i am re-ordering the output so that the columns are more
            // consistent between different data types
            if(dataType.equals("Data Plans"))
            {
               fw.writeText(wordList.get(0));
               fw.writeText(wordList.get(0));
               fw.writeBlank();
               fw.writeText(wordList.get(2));
               fw.writeText(wordList.get(1));
               fw.writeText(wordList.get(3));
            }
            else // text and picture/video messages
            {
               fw.writeText(wordList.get(0));
               fw.writeText(wordList.get(1));
               fw.writeBlank();
               fw.writeText(wordList.get(3));
               fw.writeText(wordList.get(2));
               fw.writeText(wordList.get(4));
            }
            
            fw.newRow();
         }
      }
   }
   
   public static void printSet(final MemenPage page, final int yMin, final int yMax, final int xOffset)
   {
      final ArrayList<MemenText> words = new ArrayList<MemenText>();
      page.getWords(words, new IsEntirelyInBox(yMin, yMax, xOffset - 10000, xOffset + 278400));
      int ys = -10000;
      for(final MemenText w : words)
      {
         if(w.getYSmooth() != ys)
         {
            ys = w.getYSmooth();
            System.err.println();
         }
         System.err.print(w.text);
         System.err.print("\t|  ");
      }
      System.err.println("");
      System.err.println("");
      System.err.println("PAGE END");
      System.err.println("");
      System.err.println("");
   }
   
   private static boolean isDay(final String s)
   {
      for(final String day : _DAYS_OF_WEEK)
      {
         if(day.equalsIgnoreCase(s))
         {
            return true;
         }
      }
      return false;
   }
   
   private static boolean isDataType(final String s)
   {
      for(final String type : _DATA_TYPES)
      {
         if(type.equalsIgnoreCase(s))
         {
            return true;
         }
      }
      return false;
   }
   
   private static final String[] _DAYS_OF_WEEK = new String[] { "Monday,", "Tuesday,", "Wednesday,", "Thursday,",
         "Friday,", "Saturday,", "Sunday,", };
   private static final String[] _DATA_TYPES = new String[] { "Text Messages", "Data Plans", "Picture/Video Messages" };
   
   private static final SimpleDateFormat _sdf = new SimpleDateFormat("MM/dd/yyyy hh:mm a");
   
   private static final String[] _COLUMN_TITLES = new String[] { "file#", "pg#", "type", "date and time", "to/from",
         "sender/recipient", "minutes", "rate_code", "feature_type", "charges1", "charges2" };
}
