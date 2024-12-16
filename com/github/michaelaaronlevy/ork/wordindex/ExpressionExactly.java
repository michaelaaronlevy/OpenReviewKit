package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * This class supports nine functions, the names of which are listed below.
 * "exactly1(a, b, c ... f, g)" is an expression that returns a list of pages
 * where every page appears exactly one time, no more no less, in the parameters
 * of the function. "exactly2(...)" returns a list of pages where every page
 * appears exactly 2 times, no more no less, in the parameters. Etc.
 * 
 * <p>
 * The functions are named: exactly1() exactly2() exactly3() exactly4()
 * exactly5() exactly6() exactly7() exactly8() and exactly9()
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionExactly extends Expression
{
   public ExpressionExactly(final int count, final Expression[] components)
   {
      this.count = count;
      this.components = components;
   }
   
   protected AscendingStack calculate(Context context)
   {
      return new ExpressionMinus(new ExpressionOperation(count, components),
            new ExpressionOperation(count + 1, components)).getCalculation(context);
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append(ExpressionExactly._EXACTLY_NAME);
      a.append(Integer.toString(count));
      a.append("( ");
      for(int i = 0; i < components.length; i++)
      {
         if(i != 0)
         {
            a.append(", ");
         }
         components[i].writeScript(a);
      }
      a.append(") ");
   }
   
   private final int count;
   private final Expression[] components;
   public static final String _EXACTLY_NAME = "exactly";
}
