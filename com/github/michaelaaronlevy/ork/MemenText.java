package com.github.michaelaaronlevy.ork;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.function.Predicate;

import org.apache.pdfbox.text.TextPosition;

import com.github.michaelaaronlevy.ork.util.Grid;

/**
 * MemenText: Memen as in "Memento," Latin for "Remember." Because each instance
 * of this class remembers the location it came from.
 * 
 * <p>
 * Each MemenText object has some text (a String). The text can be a sentence, a
 * word, or a single character - it depends on the settings used by the
 * {@link PdfToTextGrid PdfToTextGrid} (and to some extent, it depends on the
 * PDF's contents). Each MemenText object also has location, size, and rotation
 * information for the text. This class is meant to be significantly easier to
 * work with than the TextPosition class used by PDFTextStripper, because there
 * is a single set of coordinates per String, instead of separate coordinates
 * for each character.
 * 
 * <p>
 * With one exception (intended only for use by power users) all fields are
 * final or private, to create a basically-immutable object. The mutable field
 * is {@link MemenText#textPositions textPositions}.
 * 
 * <p>
 * (The ySmooth value is set by the {@link PdfToTextGrid PdfToTextGrid} with the
 * "smooth()" method, so every object of this class is immutable after its
 * reference is passed by {@link PdfToTextGrid PdfToTextGrid} to any object of
 * another class).
 * 
 * <p>
 * Each MemenText object is comparable with other objects of this class, based
 * on the position information, and a number of static
 * Predicate&lt;MemenText&gt; classes are here for the sake of convenience.
 * 
 * <p>
 * (including arrays of TextPosition objects in each MemenText object is
 * optional and it makes the objects mutable. Also, those arrays are not saved
 * to file when MemenText objects are serialized to the simple data format.)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class MemenText implements Comparable<MemenText>
{
   /**
    * to create your own MemenText object, use this constructor and define each
    * of the values. The new object will have exactly those values and it will
    * be immutable.
    */
   public MemenText(final int pdf_page, final int y_start, final int y_smooth, final int x_start, final int x_end,
         final int height, final int rotation, final String text, final TextPosition[] textPositions)
   {
      this.pdfPage = pdf_page;
      this.yStart = y_start;
      this.ySmooth = y_smooth;
      this.xStart = x_start;
      this.xEnd = x_end;
      this.height = height;
      this.rotation = rotation;
      this.text = text;
      this.textPositions = textPositions;
   }
   
   MemenText(final int pdf_page, final int y_start, final int x_start, final int x_end, final int height,
         final int rotation, final String text, final TextPosition[] textPositions)
   {
      this.pdfPage = pdf_page;
      this.yStart = y_start;
      this.ySmooth = _BEFORE_SMOOTH;
      this.xStart = x_start;
      this.xEnd = x_end;
      this.height = height;
      this.rotation = rotation;
      this.text = text;
      this.textPositions = textPositions;
   }
   
   /**
    * to create a MemenText object from a serialized data file
    * 
    * @param is
    * @param pdf_page
    * @throws IOException
    */
   public MemenText(final InputStream is, final int pdf_page) throws IOException
   {
      this.pdfPage = pdf_page;
      yStart = Grid.readInt(is);
      ySmooth = Grid.readInt(is);
      xStart = Grid.readInt(is);
      xEnd = Grid.readInt(is);
      height = Grid.readInt(is);
      rotation = Grid.readInt(is);
      text = Grid.readString(is);
   }
   
   public boolean isEntirelyInBox(final int top, final int bottom, final int left, final int right)
   {
      return yStart >= top && (yStart + height) <= bottom && xStart >= left && xEnd <= right;
   }
   
   public boolean isEntirelyOutsideBox(final int top, final int bottom, final int left, final int right)
   {
      return (yStart + height) < top || yStart > bottom || xEnd < left || xStart > right;
   }
   
   void smooth(final MemenText above)
   {
      if(above == null || this.getYEnd() - above.ySmooth >= _SMOOTH_FACTOR)
      {
         this.ySmooth = this.getYEnd();
      }
      else
      {
         this.ySmooth = above.ySmooth;
      }
   }
   
   public int getYSmooth()
   {
      return ySmooth;
   }
   
   /**
    * 
    * @return the baseline of the text
    */
   public int getYEnd()
   {
      return yStart + height;
   }
   
   public int compareTo(final MemenText that)
   {
      if(that == null)
      {
         return -1;
      }
      else if(this == that)
      {
         return 0;
      }
      else if(this.pdfPage != that.pdfPage)
      {
         return Integer.compare(this.pdfPage, that.pdfPage);
      }
      else if(this.ySmooth != that.ySmooth)
      {
         return Integer.compare(this.ySmooth, that.ySmooth);
      }
      else if(this.getYEnd() != that.getYEnd())
      {
         return Integer.compare(this.getYEnd(), that.getYEnd());
      }
      else if(this.yStart != that.yStart)
      {
         return Integer.compare(this.yStart, that.yStart);
      }
      else if(this.xStart != that.xStart)
      {
         return Integer.compare(this.xStart, that.xStart);
      }
      else if(this.xEnd != that.xEnd)
      {
         return Integer.compare(this.xEnd, that.xEnd);
      }
      else
      {
         return this.text.compareTo(that.text);
      }
   }
   
   public String toString()
   {
      return new StringBuilder().append("text[").append("pg:").append(pdfPage).append(", yS=").append(yStart)
            .append(",ySm=").append(ySmooth).append(",xS=").append(xStart).append(",xE=").append(xEnd).append(",h=")
            .append(height).append(",ro=").append(rotation).append("::").append(text).append("]").toString();
   }
   
   /**
    * the page number for the PDF this came from
    */
   public final int pdfPage;
   
   /**
    * the y-coordinate for the start of the text
    */
   public final int yStart;
   
   /**
    * the x-coordinate for the left side of the text
    */
   public final int xStart;
   
   /**
    * the x-coordinate for the right side of the text
    */
   public final int xEnd;
   
   /**
    * the height of the text, estimated by PDFTextStripper if the text is OCRed
    */
   public final int height;
   
   /**
    * text orientation, in degrees. Zero is standard for American English (left
    * to right, across the page).
    */
   public final int rotation;
   
   /**
    * the text content
    */
   public final String text;
   
   /**
    * For power users. the default setting of {@link PdfToTextGrid
    * PdfToTextGrid} is to leave this "null" for all MemenText objects created
    * by {@link PdfToTextGrid PdfToTextGrid}. This is the only field that is not
    * truly immutable.
    * 
    * <p>
    * The idea is that some power users might want to have access to the
    * original array of TextPosition objects created by PDFTextStripper. Most
    * people should prefer to use only the streamlined information kept in the
    * instance variables of this class. The way I see it, the main benefits to
    * using PdfToTextGrid (instead of directly subclassing PDFTextStripper
    * yourself) are (1) that it is easier to implement; and (2) you get
    * MemenText objects as output so you never have to interact programmatically
    * with TextPosition objects. But there may be some power user who wants to
    * get the benefit of being able to use MemenText and TextPosition objects
    * simultaneously for the same text, so I have included the option to have
    * PdfToTextGrid provide references to the TextPosition objects in every
    * MemenText object that is created. That is why this class is not truly
    * immutable.
    */
   public TextPosition[] textPositions;
   
   /**
    * for convenience, either the same value as getYEnd() (the baseline of the
    * text), or a value less than the getYEnd() that corresponds to another
    * MemenText object on the same page with a relatively-similar baseline.
    * Y_smooth is meant to be equal for MemenText objects that are just slightly
    * off vertically (in terms of their baseline), but appear to a human reader
    * as being on the same row (whether or not the human can see that there is a
    * slight height difference).
    * 
    * <p>
    * Among other things, ySmooth is for sorting the MemenText objects in the
    * PdfToTextGrid to determine the order they are put into the MemenPage
    * object that is sent to the PageConsumer.
    * 
    * <p>
    * This field is basically immutable. All MemenText objects created by
    * {@link PdfToTextGrid PdfToTextGrid} will have this value set, such that it
    * cannot be changed later, before a reference to that MemenText object is
    * shared by {@link PdfToTextGrid PdfToTextGrid}. And all MemenText objects
    * created by the public constructor have this value set, such that it cannot
    * be changed later. So for all practical purposes, this field is immutable.
    */
   private int ySmooth;
   
   private static final float _SMOOTH_FACTOR = 2.0f;
   private static final int _BEFORE_SMOOTH = -999999000;
   
   public static final String[] _HEADERS = { "row_id", "file_no", "pdf_page", "total_page", "y_start", "y_smooth",
         "x_start", "x_end", "size", "rotation", "content" };
   
   /**
    * ignore ySmooth, yStart, and height when sorting, consider only the
    * baseline (getYEnd())
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class SortBaseline implements Comparator<MemenText>
   {
      public int compare(MemenText one, MemenText two)
      {
         if(one == two)
         {
            return 0;
         }
         else if(two == null)
         {
            return -1;
         }
         else if(one == null)
         {
            return 1;
         }
         else if(one.pdfPage != two.pdfPage)
         {
            return Integer.compare(one.pdfPage, two.pdfPage);
         }
         else if(one.getYEnd() != two.getYEnd())
         {
            return Integer.compare(one.getYEnd(), two.getYEnd());
         }
         else if(one.xStart != two.xStart)
         {
            return Integer.compare(one.xStart, two.xStart);
         }
         else if(one.xEnd != two.xEnd)
         {
            return Integer.compare(one.xEnd, two.xEnd);
         }
         else
         {
            return one.text.compareTo(two.text);
         }
      }
   }
   
   /**
    * ignore ySmooth, yStart, baseline, and height when sorting, consider only
    * the x-values (and the String content if x-values are identical)
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class SortHoriz implements Comparator<MemenText>
   {
      public int compare(MemenText one, MemenText two)
      {
         if(one == two)
         {
            return 0;
         }
         else if(two == null)
         {
            return -1;
         }
         else if(one == null)
         {
            return 1;
         }
         else if(one.xStart != two.xStart)
         {
            return Integer.compare(one.xStart, two.xStart);
         }
         else if(one.xEnd != two.xEnd)
         {
            return Integer.compare(one.xEnd, two.xEnd);
         }
         else
         {
            return one.text.compareTo(two.text);
         }
      }
   }
   
   /**
    * true if the boundaries of the MemenText are entirely within the boundaries
    * of this object. This may be the main type of Predicate used when creating
    * a program to transform a specific type of PDF into a specific useful
    * output.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class IsEntirelyInBox implements Predicate<MemenText>
   {
      public IsEntirelyInBox(final int top, final int bottom, final int left, final int right)
      {
         this.top = top;
         this.bottom = bottom;
         this.left = left;
         this.right = right;
      }
      
      public boolean test(final MemenText w)
      {
         return w.isEntirelyInBox(top, bottom, left, right);
      }
      
      public final int top;
      public final int bottom;
      public final int left;
      public final int right;
   }
   
   /**
    * returns true if the MemenText fits entirely inside the specified column
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class IsInColumn implements Predicate<MemenText>
   {
      public IsInColumn(final int left, final int right)
      {
         this.left = left;
         this.right = right;
      }
      
      public boolean test(final MemenText w)
      {
         return w.xStart >= left && w.xEnd <= right;
      }
      
      public final int left;
      public final int right;
   }
   
   /**
    * returns true if the baseline of the MemenText is above the value passed to
    * the constructor. (Above means "less than," because 0,0 is the upper-left
    * corner.)
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class IsAbove implements Predicate<MemenText>
   {
      public IsAbove(final int y)
      {
         this.y = y;
      }
      
      public boolean test(final MemenText w)
      {
         return (w.yStart + w.height) < y;
      }
      
      public final int y;
   }
   
   /**
    * returns true if the baseline of the MemenText object is below the
    * arbitrary value provided to the constructor.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class IsBelow implements Predicate<MemenText>
   {
      public IsBelow(final int y)
      {
         this.y = y;
      }
      
      public boolean test(final MemenText w)
      {
         return w.yStart + w.height > y;
      }
      
      public final int y;
   }
   
   /**
    * true iff the MemenText object has the same {@link MemenText#ySmooth
    * ySmooth} value as one of the MemenText objects passed to the constructor.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class SameYSmooth implements Predicate<MemenText>
   {
      public SameYSmooth(final MemenText w)
      {
         final ArrayList<MemenText> a = new ArrayList<MemenText>(1);
         a.add(w);
         this.c = a;
      }
      
      public SameYSmooth(final Collection<MemenText> c)
      {
         this.c = c;
      }
      
      public boolean test(final MemenText test)
      {
         for(final MemenText w : c)
         {
            if(w.ySmooth == test.ySmooth)
            {
               return true;
            }
         }
         return false;
      }
      
      public final Collection<MemenText> c;
   }
   
   /**
    * returns true only if NO part of the MemenText object's boundaries overlap
    * with the boundaries of this object.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class IsEntirelyOutsideBox implements Predicate<MemenText>
   {
      public IsEntirelyOutsideBox(final int top, final int bottom, final int left, final int right)
      {
         this.top = top;
         this.bottom = bottom;
         this.left = left;
         this.right = right;
      }
      
      public boolean test(final MemenText w)
      {
         return w.isEntirelyOutsideBox(top, bottom, left, right);
      }
      
      public final int top;
      public final int bottom;
      public final int left;
      public final int right;
   }
   
   /**
    * true if the MemenText's height is between minHeight and maxHeight
    * (inclusive)
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class Height implements Predicate<MemenText>
   {
      public Height(final int minHeight, final int maxHeight)
      {
         this.minHeight = minHeight;
         this.maxHeight = maxHeight;
      }
      
      public boolean test(final MemenText w)
      {
         return w.height >= minHeight && w.height <= maxHeight;
      }
      
      public final int minHeight;
      public final int maxHeight;
   }
   
   /**
    * apply an arbitrary test (the Predicate&lt;String&gt; passed to the
    * constructor) to just the String component of the MemenText object,
    * MemenText.text
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class TestText implements Predicate<MemenText>
   {
      public TestText(final Predicate<String> test)
      {
         this.test = test;
      }
      
      public boolean test(final MemenText w)
      {
         return test.test(w.text);
      }
      
      public final Predicate<String> test;
   }
   
   /**
    * returns true/false based on the output of two Predicate&lt;MemenText&gt;
    * objects. Choose from four boolean operations: and, or, xor, minus
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static final class Operation implements Predicate<MemenText>
   {
      /**
       * 
       * @param one
       * @param op
       *           -1 is minus, 0 is xor, 1 is or, 2 is and. Anything else will
       *           throw a RuntimeException when test() is called.
       * @param two
       */
      public Operation(Predicate<MemenText> one, int op, Predicate<MemenText> two)
      {
         this.one = one;
         this.op = op;
         this.two = two;
      }
      
      /**
       * @param w
       *           the MemenText object to be tested
       * @return true if boolean operation(test1, test2) is true; false
       *         otherwise. Uses short-circuiting to avoid calling the second
       *         test method if the return value can be determined without that.
       * 
       */
      public boolean test(final MemenText w)
      {
         final boolean a = one.test(w);
         if(op == -1)
         {
            return !(!a || two.test(w));
         }
         if(op == 0)
         {
            return a ^ two.test(w);
         }
         else if(op == 1)
         {
            return a || two.test(w);
         }
         else if(op == 2)
         {
            return a && two.test(w);
         }
         else
         {
            throw new RuntimeException(
                  "Illegal operation value: " + op + ". Legal values: -1 is minus, 0 is xor, 1 is or, 2 is and");
         }
      }
      
      public final Predicate<MemenText> one;
      public final int op;
      public final Predicate<MemenText> two;
   }
}
