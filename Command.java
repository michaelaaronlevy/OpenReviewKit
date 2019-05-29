package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.TokenParser;
import com.github.michaelaaronlevy.ork.util.TokenParser.ParseException;

/**
 * Each command is an object representing a single instruction that can be
 * carried out by the relevant {@link Context Context} object. (A command that
 * returns a value, in the form of an
 * {@link com.github.michaelaaronlevy.ork.util.AscendingStack AscendingStack}
 * object, is referred to as an {@link Expression Expression}.)
 * 
 * <p>
 * Commands can have their functionality built in to the code of the Command
 * subclass, or it can be a {@link CommandFunction CommandFunction} object,
 * where the {@link Context Context} is responsible for recognizing the name of
 * the command and carrying out appropriate behavior. This means that different
 * implementations of the {@link Context Context} interface can support
 * different commands.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public abstract class Command
{
   public abstract void execute(final Context context);
   
   public abstract void writeScript(Appendable a) throws IOException;
   
   public String toString()
   {
      final StringBuilder b = new StringBuilder();
      try
      {
         writeScript(b);
      }
      catch(final IOException iox)
      {
         // this will not happen because StringBuilder does not throw
      }
      return b.toString();
   }
   
   /**
    * 
    * @param array
    *           an array of tokens (in the same format as
    *           {@link com.github.michaelaaronlevy.ork.util.TokenParser TokenParser}
    *           uses). This method will not alter the array.
    * @return a command object based on the provided description (or null if
    *         this is nothing)
    * @throws ParseException
    *            if the tokens cannot be properly interpreted
    */
   public static Command parse(final Object[] array) throws ParseException
   {
      if(array == null || array.length == 0)
      {
         return null;
      }
      if(array[0] instanceof String)
      {
         if(array.length == 2 && array[1] instanceof Object[])
         {
            final String name = (String) array[0];
            final Expression[] arguments = Expression.parseCommas((Object[]) array[1]);
            if(ExpressionOperation.isOperation(name))
            {
               return ExpressionOperation.getExpression(name, arguments);
            }
            else
            {
               return new CommandFunction(name, arguments);
            }
         }
         else if(array.length > 2 && array[1] instanceof Character
               && ((Character) array[1]).charValue() == CommandAssign._OPERATOR)
         {
            return new CommandAssign((String) array[0], Expression.parse(array, 2, array.length));
         }
      }
      return Expression.parse(array, 0, array.length);
   }
   
   public static Command parse(final String line) throws ParseException
   {
      return parse(TokenParser.parse(line));
   }
}
