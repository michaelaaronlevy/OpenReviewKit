package com.github.michaelaaronlevy.ork.ripping;

/**
 * a simple functional interface for use with both {@link TextPositionsParser TextPositionsParser} and
 * {@link Transformer Transformer}.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface CharTest
{
   public boolean test(char c);
   
   /**
    * return Character.isWhitespace
    */
   public static final CharTest IsWhiteSpace = new CharTest()
   {
      public boolean test(final char c)
      {
         return Character.isWhitespace(c);
      }
   };
   
   /**
    * return Character.isLetter
    */
   public static final CharTest IsLetter = new CharTest()
   {
      public boolean test(final char c)
      {
         return Character.isLetter(c);
      }
   };
   
   /**
    * return Character.isDigit
    */
   public static final CharTest IsDigit = new CharTest()
   {
      public boolean test(final char c)
      {
         return Character.isDigit(c);
      }
   };
   
   /**
    * return Character.isLetterOrDigit
    */
   public static final CharTest IsLetterOrDigit = new CharTest()
   {
      public boolean test(final char c)
      {
         return Character.isLetterOrDigit(c);
      }
   };
   
   /**
    * true if it is in the list; false otherwise.
    */
   public static class IsInList implements CharTest
   {
      public IsInList(final String list)
      {
         this.list = list;
      }
      
      public boolean test(final char c)
      {
         return list.indexOf(c) != -1;
      }
      
      private final String list;
   }
   
   /**
    * all letters and digits -&gt; return false unless it is in the blacklist. All
    * symbols and whitespace -&gt; return true unless it is in the whitelist.
    */
   public static class NotLetterOrDigit implements CharTest
   {
      public NotLetterOrDigit(final String blacklist, final String whitelist)
      {
         this.blacklist = blacklist;
         this.whitelist = whitelist;
      }
      
      public boolean test(final char c)
      {
         if(Character.isLetterOrDigit(c))
         {
            return blacklist.indexOf(c) > -1;
         }
         else
         {
            return whitelist.indexOf(c) == -1;
         }
      }
      
      private final String blacklist;
      private final String whitelist;
   }
   
   /**
    * reverse the polarity of the CharTest passed to the constructor
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static class Negate implements CharTest
   {
      public Negate(final CharTest ct)
      {
         this.ct = ct;
      }
      
      public boolean test(final char c)
      {
         return !ct.test(c);
      }
      
      private final CharTest ct;
   }
}
