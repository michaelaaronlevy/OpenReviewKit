package com.github.michaelaaronlevy.ork.ripping;

import java.util.function.UnaryOperator;

/**
 * used to delete all text that does not contain at least one character for
 * which the {@link CharTest CharTest} (the one passed to the constructor)
 * returns true.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class TransformerMustHaveAtLeastOne implements UnaryOperator<String>
{
   public TransformerMustHaveAtLeastOne(final CharTest test)
   {
      this.tester = test;
   }
   
   /**
    * @return the input, if at least one character matches the test; otherwise,
    *         it returns null.
    */
   public String apply(final String s)
   {
      final char[] cc = s.toCharArray();
      for(final char c : cc)
      {
         if(tester.test(c))
         {
            return s;
         }
      }
      return null;
   }
   
   private final CharTest tester;
}
