package com.github.michaelaaronlevy.ork.ripping;

import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;

import org.apache.pdfbox.text.TextPosition;

/**
 * Classes extending TextPositionsParser are used to parse the output from the
 * PDFTextStripper that lives inside the
 * {@link com.github.michaelaaronlevy.ork.PdfToTextGrid PdfToTextGrid}.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public abstract class TextPositionsParser
{
   public TextPositionsParser(final TextPositionsParser next)
   {
      this.next = next;
   }
   
   /**
    * Process output from the PDFTextStripper. The first parameter "nextWord" is
    * the text to be processed (but it is not necessarily a single word, it can
    * be an entire sentence). The second parameter "output" is the results. For
    * example, if you wanted to delete this text, just return without adding
    * anything to output.
    * 
    * <p>
    * If you want to make no changes, just call output.add(nextWord) and then
    * return.
    * 
    * <p>
    * You can use a TextPositionsParser to do things like: delete certain
    * characters. Or break up some text into multiple pieces. E.g., split the
    * text wherever there is whitespace, so if your input is equivalent to "one
    * two three" you can put "one" "two" and "three" into the output. Then the
    * {@link com.github.michaelaaronlevy.ork.PdfToTextGrid PdfToTextGrid} class,
    * instead of making a single
    * {@link com.github.michaelaaronlevy.ork.MemenText MemenText} object with
    * the text "one two three", would make three
    * {@link com.github.michaelaaronlevy.ork.MemenText MemenText} objects.
    * 
    * @param nextWord
    *           the word to process
    * @param output
    *           enters the method invocation empty. Add each processed word (as
    *           an ArrayList&lt;TextPosition&gt; to this. Add nothing, or null,
    *           or an empty ArrayList
    */
   protected abstract void parseLine(final ArrayList<TextPosition> nextWord,
         final ArrayList<ArrayList<TextPosition>> output);
   
   public final ArrayList<ArrayList<TextPosition>> parse(final List<TextPosition> list)
   {
      final ArrayList<TextPosition> inner = new ArrayList<TextPosition>();
      final ArrayList<ArrayList<TextPosition>> outer = new ArrayList<ArrayList<TextPosition>>();
      for(final TextPosition tp : list)
      {
         inner.add(tp);
      }
      outer.add(inner);
      TextPositionsParser current = this;
      while(current != null)
      {
         final ArrayList<TextPosition>[] iter = outer.toArray((ArrayList<TextPosition>[]) new ArrayList[outer.size()]);
         outer.clear();
         for(final ArrayList<TextPosition> textPositions : iter)
         {
            if(textPositions != null && textPositions.size() > 0)
            {
               current.parseLine(textPositions, outer);
            }
         }
         
         current = current.next;
      }
      return outer;
   }
   
   public final TextPositionsParser next;
   
   public static void printList(final PrintStream out, final List<TextPosition> list)
   {
      if(list == null)
      {
         out.print("-null-");
      }
      else
      {
         out.print('{');
         for(int i = 0; i < list.size(); i++)
         {
            if(i != 0)
            {
               out.print(',');
            }
            out.print(list.get(i).getUnicode());
         }
         out.print('}');
      }
   }
   
   public static void printListList(final PrintStream out, final List<? extends List<TextPosition>> list)
   {
      if(list == null)
      {
         out.print("--null--");
      }
      else
      {
         out.print('{');
         for(int i = 0; i < list.size(); i++)
         {
            if(i != 0)
            {
               out.print(',');
            }
            printList(out, list.get(i));
         }
         out.print('}');
      }
   }
}
