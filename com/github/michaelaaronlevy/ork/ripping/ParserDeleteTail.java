package com.github.michaelaaronlevy.ork.ripping;

import java.util.ArrayList;

import org.apache.pdfbox.text.TextPosition;

/**
 * delete certain characters at the end of the text, moving backwards. It keeps
 * deleting characters until the CharTest returns "false."
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ParserDeleteTail extends TextPositionsParser
{
   public ParserDeleteTail(final CharTest test, final TextPositionsParser next)
   {
      super(next);
      this.tester = test;
   }
   
   /**
    * delete every character "c" on the tail end of the word if
    * CharTest.test(c) returns true, until CharTest(c) returns false.
    */
   protected void parseLine(ArrayList<TextPosition> textPositions, ArrayList<ArrayList<TextPosition>> output)
   {
      while(!textPositions.isEmpty() && tester.test(textPositions.get(textPositions.size() - 1).getUnicode().charAt(0)))
      {
         textPositions.remove(textPositions.size() - 1);
      }
      if(!textPositions.isEmpty())
      {
         output.add(textPositions);
      }
   }
   
   private final CharTest tester;
}
