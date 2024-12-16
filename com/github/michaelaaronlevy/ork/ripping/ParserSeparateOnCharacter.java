package com.github.michaelaaronlevy.ork.ripping;

import java.util.ArrayList;

import org.apache.pdfbox.text.TextPosition;

/**
 * break text into multiple parts, deleting out the separator characters.
 * 
 * <p>This is used by the searchable word index to split words up by whitespace, to
 * ensure that each word is processed separately.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ParserSeparateOnCharacter extends TextPositionsParser
{
   public ParserSeparateOnCharacter(final CharTest test, final TextPositionsParser next)
   {
      super(next);
      this.tester = test;
   }
   
   protected void parseLine(ArrayList<TextPosition> textPositions, ArrayList<ArrayList<TextPosition>> output)
   {
      ArrayList<TextPosition> current = new ArrayList<TextPosition>();
      output.add(current);
      for(int index = 0; index < textPositions.size(); index++)
      {
         final TextPosition tp = textPositions.get(index);
         if(tester.test(tp.getUnicode().charAt(0)))
         {
            if(!current.isEmpty())
            {
               current = new ArrayList<TextPosition>();
               output.add(current);
            }
         }
         else
         {
            current.add(tp);
         }
      }
      if(output.get(output.size() - 1).isEmpty())
      {
         output.remove(output.size() - 1);
      }
   }
   
   private final CharTest tester;
}
