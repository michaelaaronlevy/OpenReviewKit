package com.github.michaelaaronlevy.ork.ripping;

import java.util.ArrayList;

import org.apache.pdfbox.text.TextPosition;

/**
 * this performs part of the default parsing operation (for "phrases").
 * 
 * <p>additional parsers can be added to the tail end for further processing, such
 * as splitting tokens wherever there is whitespace.
 * 
 * <p>What this primarily does is: if there are too many blank spaces
 * (whitespace) between non-whitespace characters, or otherwise the distance
 * between characters is equivalent to that many whitespace characters, it
 * splits the textPositions into multiple parts. The exact amount of blank space
 * that it looks for is set with a parameter to the constructor.
 * 
 * <p>ParserSpacingBackward is useful for documents that have been OCRed as well as
 * native electronic rendered text documents because sometimes PDFTextStripper,
 * for whatever reason, joins together text on multiple lines, where the first
 * part of the text has a higher x value and lower y value than the second part
 * of the text.
 * 
 * <p>This class only looks backwards (meaning: it will not break up TextPositions
 * if a latter character has a higher x-value than the former character; for
 * that, use {@link ParserSpacingForward ParserSpacingForward}).
 * 
 * @see com.github.michaelaaronlevy.ork.ripping.ParserSpacingForward
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ParserSpacingBackward extends TextPositionsParser
{
   public ParserSpacingBackward(final float spacing, final TextPositionsParser next)
   {
      super(next);
      this.spacing = -spacing;
   }
   
   public ParserSpacingBackward(final TextPositionsParser next)
   {
      this(_DEFAULT_SPACING, next);
   }
   
   protected void parseLine(ArrayList<TextPosition> textPositions, ArrayList<ArrayList<TextPosition>> output)
   {
      final float spaceWidth = textPositions.get(0).getWidthOfSpace();
      for(int i = 1; i < textPositions.size(); i++)
      {
         final int start = i - 1;
         final TextPosition one = textPositions.get(start);
         while(Character.isWhitespace(textPositions.get(i).getUnicode().charAt(0)) && ++i < textPositions.size())
         {
            // do nothing here, just keep looping
         }
         final float diff = i == textPositions.size() ? -spaceWidth - 1
               : textPositions.get(i).getXDirAdj() - (one.getXDirAdj() + one.getWidthDirAdj());
         
         if(diff < (spacing * spaceWidth))
         {
            if(start == i - 1)
            {
               textPositions.add(i++, null);
            }
            else
            {
               for(int j = start + 1; j < i; j++)
               {
                  textPositions.set(j, null);
               }
            }
         }
      }
      
      ArrayList<TextPosition> current = new ArrayList<TextPosition>();
      output.add(current);
      current.add(textPositions.get(0));
      
      for(int i = 1; i < textPositions.size(); i++)
      {
         final TextPosition tp = textPositions.get(i);
         if(tp == null)
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
   }
   
   private final float spacing;
   
   public static float _DEFAULT_SPACING = 1.0f;
}
