package com.github.michaelaaronlevy.ork.ripping;

import java.util.function.UnaryOperator;

/**
 * this class removes all characters for which the {@link CharTest CharTest}
 * (the one passed to the constructor) returns "false," keeping only the
 * characters for which it returns "true"
 * 
 * <p>
 * This is used by the searchable word index to remove all internal apostrophes.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class TransformerRemoveChars implements UnaryOperator<String>
{
   public TransformerRemoveChars(final CharTest test)
   {
      this.tester = test;
   }
   
   public String apply(final String in)
   {
      final StringBuilder b = new StringBuilder(in.length());
      for(int i = 0; i < in.length(); i++)
      {
         final char c = in.charAt(i);
         if(!tester.test(c))
         {
            b.append(c);
         }
      }
      return b.length() == in.length() ? in : b.toString();
   }
   
   private final CharTest tester;
}
