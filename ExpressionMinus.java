package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * for A - B, return all of the pages that are in A only. Do not return any
 * pages that are in B only. Do not return any pages that are in both A &amp; B.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionMinus extends Expression
{
   public ExpressionMinus(final Expression left, final Expression right)
   {
      this.left = left;
      this.right = right;
   }
   
   protected AscendingStack calculate(final Context context)
   {
      final AscendingStack one = left.getCalculation(context);
      final AscendingStack two = right.getCalculation(context);
      return AscendingStack.minus(one, two);
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append("( ");
      left.writeScript(a);
      a.append(' ');
      a.append(_OPERATOR);
      a.append(' ');
      right.writeScript(a);
      a.append(" )");
   }
   
   public final Expression left;
   public final Expression right;
   
   public static final char _OPERATOR = '-';
}
