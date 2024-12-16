package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.TokenParser;

/**
 * Objects of this class have no behavior of their own. The {@link Context
 * Context} implementation determines the behavior to carry out.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class CommandFunction extends Command
{
   public CommandFunction(final String name, final Expression[] arguments)
   {
      this.name = name;
      this.arguments = arguments;
   }
   
   public void execute(Context context)
   {
      if(expr == null && context.functionReturnsValue(name))
      {
         expr = new ExpressionFunction(name, arguments);
      }
      
      if(expr == null)
      {
         context.runFunction(name, arguments);
      }
      else
      {
         expr.execute(context);
      }
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append(TokenParser.literalIfNeeded(name));
      a.append(" (");
      if(arguments.length != 0)
      {
         arguments[0].writeScript(a);
      }
      for(int i = 1; i < arguments.length; i++)
      {
         a.append(", ");
         arguments[i].writeScript(a);
      }
      a.append(") ");
   }
   
   public final String name;
   public final Expression[] arguments;
   
   private ExpressionFunction expr = null;
}
