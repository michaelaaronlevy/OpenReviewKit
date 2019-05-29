package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;
import com.github.michaelaaronlevy.ork.util.TokenParser;

/**
 * instructs the {@link Context Context} object to assign a certain value to a
 * certain variable name. The {@link Context Context} may or may not obey.
 * (E.g., the {@link Context Context} might not allow you to overwrite an
 * existing variable, or the {@link Context Context} might not allow you to have
 * a variable name that is identical to a word that is already in the word
 * index.)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class CommandAssign extends Command
{
   public CommandAssign(final String varName, final Expression value)
   {
      this.varName = varName;
      this.value = value;
   }
   
   public void execute(final Context context)
   {
      final AscendingStack as = value.calculate(context);
      context.setVariable(varName, as);
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append(TokenParser.literalIfNeeded(varName));
      a.append(" = ");
      value.writeScript(a);
   }
   
   public final String varName;
   private final Expression value;
   
   public static final char _OPERATOR = '=';
}
