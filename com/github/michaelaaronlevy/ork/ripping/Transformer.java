package com.github.michaelaaronlevy.ork.ripping;

import java.util.function.UnaryOperator;

/**
 * this class is used to create a chain of String operations that will be
 * applied to the text after its positional information is set in stone.
 * 
 * <p>
 * You could use an instance of this class to delete certain words that don't
 * need to sent to output, or to change a word into a slightly--or
 * completely--different word, e.g. to make it lowercase, delete certain
 * characters (without affecting positional information), change "1.1" into "one
 * point one", change "Pepsi" into "soda pop", etc.
 * 
 * <p>
 * Why use a Transformer instead of a {@link TextPositionsParser
 * TextPositionsParser}? If you attempt to make these sort of text-altering
 * changes with a {@link TextPositionsParser TextPositionsParser}, any
 * characters that are deleted will result in the location information for the
 * deleted characters not informing the location information that ultimately is
 * saved in the {@link com.github.michaelaaronlevy.ork.MemenText MemenText}
 * object (which, depending on the characters, may or may not matter, and may or
 * may not be desired). If you attempt to add characters using the
 * {@link TextPositionsParser TextPositionsParser}, you will need to provide
 * location information for each of the new characters, because that location
 * information will end up informing the location information saved in the
 * {@link com.github.michaelaaronlevy.ork.MemenText MemenText} object. By
 * performing these text-altering functions after the location information is
 * set in stone, you sidestep that problem and also miss the opportunity to
 * alter the location information.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class Transformer
{
   /**
    * 
    * @param operation
    *           change the String into something else (null to delete)
    * @param next
    *           null if this is the final operation to perform
    */
   public Transformer(final UnaryOperator<String> operation, final Transformer next)
   {
      this.operation = operation;
      this.next = next;
   }
   
   /**
    * 
    * @param in
    *           the String to transform.
    * @return the transformed String (which may be null, which indicates that
    *         this word is to be deleted)
    */
   public String transform(String in)
   {
      Transformer current = this;
      while(current != null)
      {
         if(in == null)
         {
            return null;
         }
         in = current.operation.apply(in);
         current = current.next;
      }
      return in;
   }
   
   private final UnaryOperator<String> operation;
   private final Transformer next;
}
