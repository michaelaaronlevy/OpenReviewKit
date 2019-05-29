package com.github.michaelaaronlevy.ork.ripping;

import java.util.function.UnaryOperator;

/**
 * replace all contiguous whitespace with a single space ' '. All whitespace
 * characters are replaced with ' ' and if there is more than one ' ' in a row,
 * they are replaced with just one.
 * 
 * @author michaelaaronlevy@gmail.com
 */
public class TransformerSingleSpace implements UnaryOperator<String>
{
   /**
    * replace all contiguous whitespace with a single space ' '
    */
   public String apply(final String line)
   {
      boolean flag = false;
      for(int i = 0; i < line.length(); i++)
      {
         final char c1 = i == 0 ? 'x' : line.charAt(i - 1);
         final char c2 = line.charAt(i);
         if(Character.isWhitespace(c2))
         {
            if(Character.isWhitespace(c1) || c2 != ' ')
            {
               flag = true;
               break;
            }
         }
      }
      if(!flag)
      {
         return line.trim();
      }
      
      final StringBuilder b = new StringBuilder(line.length());
      for(int i = 0; i < line.length(); i++)
      {
         final char c1 = i == 0 ? 'x' : line.charAt(i - 1);
         final char c2 = line.charAt(i);
         if(Character.isWhitespace(c2))
         {
            if(!Character.isWhitespace(c1))
            {
               // replace all contiguous whitespace with a single ' ' character
               b.append(' ');
            }
         }
         else
         {
            b.append(c2);
         }
      }
      return b.toString().trim();
   }
}
