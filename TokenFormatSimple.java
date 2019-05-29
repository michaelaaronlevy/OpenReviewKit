package com.github.michaelaaronlevy.ork.util;

/**
 * An implementation of the {@link TokenFormat TokenFormat} interface, which
 * allows some degree of customization (through the constructor) in terms of the
 * behavior.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the TokenFormatSimple class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class TokenFormatSimple implements TokenFormat
{
   /**
    * 
    * @param notOperators
    *           all of the characters that are treated the same as
    *           letters/numbers. These characters must not be whitespace.
    * @param illegalCharacters
    *           these characters must not appear outside of string literals
    * @param illegalLiteralCharacters
    *           these characters must not appear inside string literals (but CAN
    *           appear outside of them, unless they are also part of the
    *           illegalCharacters string)
    * @param comment
    *           the combination of operators that makes up a comment.
    *           TokenFormatSimple does not permit comments to be marked by
    *           anything other than some combination of operators. You cannot
    *           use a word (like "REM") to denote the start of a comment.
    */
   public TokenFormatSimple(final String notOperators, final String illegalCharacters,
         final String illegalLiteralCharacters, final String comment)
   {
      this.notOperators = notOperators == null ? "" : notOperators;
      this.illegalCharacters = illegalCharacters == null ? "" : illegalCharacters;
      this.illegalLiteralCharacters = illegalLiteralCharacters == null ? "" : illegalLiteralCharacters;
      this.comment = (comment == null || comment.length() == 0) ? null : comment;
      
      for(int i = 0; i < this.notOperators.length(); i++)
      {
         final char c = this.notOperators.charAt(i);
         if(Character.isWhitespace(c))
         {
            throw new IllegalArgumentException();
         }
      }
      
      if(comment != null)
      {
         for(int i = 0; i < comment.length(); i++)
         {
            final char c = comment.charAt(i);
            if(isEmpty(c) || isWord(c))
            {
               throw new IllegalArgumentException();
            }
         }
      }
   }
   
   public boolean isEmpty(final char c)
   {
      return Character.isWhitespace(c);
   }
   
   public boolean isWord(final char c)
   {
      return Character.isLetterOrDigit(c) || notOperators.indexOf(c) != -1;
   }
   
   public boolean illegalCharacter(final char c)
   {
      return illegalCharacters.indexOf(c) != -1;
   }
   
   public boolean illegalLiteralCharacter(final char c)
   {
      return illegalLiteralCharacters.indexOf(c) != -1;
   }
   
   public boolean isCommentAt(String s, final int index)
   {
      return comment == null ? false : s.startsWith(comment, index);
   }
   
   public final String notOperators;
   public final String illegalCharacters;
   public final String illegalLiteralCharacters;
   public final String comment;
}
