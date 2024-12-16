package com.github.michaelaaronlevy.ork.ripping;

import java.util.ArrayList;

import org.apache.pdfbox.text.TextPosition;

/**
 * delete certain characters at the start of the text. It keeps deleting
 * characters until the CharTest returns "false."
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ParserDeleteHead extends TextPositionsParser
{
   public ParserDeleteHead(final CharTest test, final TextPositionsParser next)
   {
      super(next);
      this.tester = test;
   }
   
   /**
    * delete every character "c" on the front end of the word if
    * CharTest.test(c) returns true, until CharTest(c) returns false.
    */
   protected void parseLine(ArrayList<TextPosition> textPositions, ArrayList<ArrayList<TextPosition>> output)
   {
      while(!textPositions.isEmpty() && tester.test(textPositions.get(0).getUnicode().charAt(0)))
      {
         textPositions.remove(0);
      }
      if(!textPositions.isEmpty())
      {
         output.add(textPositions);
      }
   }
   
   private final CharTest tester;
}
