package com.github.michaelaaronlevy.ork.util;

/**
 * instances of this interface define rules for the {@link TokenParser
 * TokenParser} class
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the TokenFormat interface) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface TokenFormat
{
   /**
    * 
    * @param c
    * @return true if this character is not part of any token (e.g., if
    *         whitespace between two words indicates they are separate tokens).
    */
   public boolean isEmpty(char c);
   
   /**
    * 
    * @param c
    * @return return true if this character can be part of a word. If a
    *         character is not empty AND it is not a word, it will become an
    *         "operator" token.
    */
   public boolean isWord(char c);
   
   /**
    * 
    * @param c
    * @return true if the parser should throw a ParseException upon seeing this
    *         character outside of a String literal.
    */
   public boolean illegalCharacter(char c);
   
   /**
    * 
    * @param c
    * @return true if the parser should throw a ParseException upon seeing this
    *         character inside of a String literal.
    */
   public boolean illegalLiteralCharacter(char c);
   
   /**
    * @param s
    * @param index
    * @return true if index is the first character of a comment marker in the
    *         line
    */
   public boolean isCommentAt(final String s, final int index);
}
