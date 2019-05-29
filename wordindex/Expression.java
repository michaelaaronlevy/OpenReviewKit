package com.github.michaelaaronlevy.ork.wordindex;

import java.util.ArrayList;

import com.github.michaelaaronlevy.ork.util.AscendingStack;
import com.github.michaelaaronlevy.ork.util.TokenParser.ParseException;

/**
 * a type of {@link Command Command} that, when evaluated by the {@link Context Context}, returns a value in the
 * form of an {@link com.github.michaelaaronlevy.ork.util.AscendingStack AscendingStack} (which is intended to represent all of the pages
 * corresponding with a certain word or variable, or boolean operations like
 * "and" "or" etc. that are performed on other expressions)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public abstract class Expression extends Command
{
   public final AscendingStack getCalculation(final Context context)
   {
      if(eval == null)
      {
         eval = calculate(context);
         eval.makeDirty(); // dirty objects perform faster
      }
      return eval;
   }
   
   public void execute(final Context context)
   {
      final AscendingStack as = getCalculation(context);
      context.displayValues(as);
   }
   
   protected abstract AscendingStack calculate(final Context context);
   
   private AscendingStack eval = null;
   
   public static Expression parse(final Object[] tokens) throws ParseException
   {
      if(tokens == null || tokens.length == 0)
      {
         throw new ParseException("Expected to find an expression but it is empty.", tokens);
      }
      return parse(tokens, 0, tokens.length);
   }
   
   /**
    * 
    * @param tokens
    *           a description of the expression (from start...end within this
    *           array). This array is not modified by this method.
    * @param start
    * @param end
    * @return an expression matching the description, or null if it is empty
    * @throws ParseException
    *            if the description is invalid
    */
   public static Expression parse(final Object[] tokens, final int start, final int end) throws ParseException
   {
      if(start >= end)
      {
         throw new ParseException("Expected to find an expression but it is empty. " + start + "..." + end, tokens);
      }
      
      final ArrayList<Object> work = new ArrayList<Object>(tokens.length);
      for(int i = start; i < end; i++)
      {
         work.add(tokens[i]);
      }
      
      // replace parenthesis (Object[]) with Expressions or Functions.
      
      for(int i = 0; i < work.size(); i++)
      {
         final Object o = work.get(i);
         if(o instanceof Object[])
         {
            if(i == 0 || !(work.get(i - 1) instanceof String))
            {
               work.set(i, parse((Object[]) o));
            }
            else
            {
               final String name = (String) work.get(i - 1);
               final Expression[] contents = parseCommas((Object[]) o);
               final Expression e;
               if(ExpressionOperation.isOperation(name))
               {
                  e = ExpressionOperation.getExpression(name, contents);
               }
               else
               {
                  e = new ExpressionFunction(name, contents);
               }
               work.set(i - 1, e);
               work.remove(i--);
            }
         }
      }
      
      for(int i = 0; i < work.size(); i++)
      {
         final Object o = work.get(i);
         if(o instanceof String)
         {
            work.set(i, new ExpressionWord((String) o));
         }
      }
      
      for(int i = 0; i < work.size(); i++)
      {
         if(check(work, i, ExpressionOperation._AND_OPERATOR))
         {
            if(i == 0 || i == work.size() - 1)
            {
               throw new ParseException("And operator (\"" + ExpressionOperation._AND_OPERATOR
                     + "\") must not be the first or last token.", tokens);
            }
            if(!(work.get(i - 1) instanceof Expression))
            {
               throw new ParseException(
                     "And operator (\"" + ExpressionOperation._AND_OPERATOR + "\") must follow an expression.", tokens);
            }
            final ArrayList<Expression> components = new ArrayList<Expression>();
            components.add((Expression) work.get(i - 1));
            boolean operator = true;
            int j;
            for(j = i + 1; j < work.size(); j++)
            {
               if(operator)
               {
                  if(work.get(j) instanceof Expression)
                  {
                     components.add((Expression) work.get(j));
                  }
                  else
                  {
                     throw new ParseException(
                           "And operator (\"" + ExpressionOperation._AND_OPERATOR + "\") must precede an expression.",
                           tokens);
                  }
               }
               else
               {
                  if(!check(work, j, ExpressionOperation._AND_OPERATOR))
                  {
                     break;
                  }
               }
               operator = !(operator);
            }
            work.set(i - 1,
                  new ExpressionOperation(components.size(), components.toArray(new Expression[components.size()])));
            for(int k = i; k < j; k++)
            {
               work.remove(i);
            }
         }
      }
      
      for(int i = 0; i < work.size(); i++)
      {
         if(check(work, i, ExpressionOperation._ODD_OPERATOR))
         {
            if(i == 0 || i == work.size() - 1)
            {
               throw new ParseException("Xor operator (\"" + ExpressionOperation._ODD_OPERATOR
                     + "\") must not be the first or last token.", tokens);
            }
            if(!(work.get(i - 1) instanceof Expression))
            {
               throw new ParseException(
                     "Xor operator (\"" + ExpressionOperation._ODD_OPERATOR + "\") must follow an expression.", tokens);
            }
            final ArrayList<Expression> components = new ArrayList<Expression>();
            components.add((Expression) work.get(i - 1));
            boolean operator = true;
            int j;
            for(j = i + 1; j < work.size(); j++)
            {
               if(operator)
               {
                  if(work.get(j) instanceof Expression)
                  {
                     components.add((Expression) work.get(j));
                  }
                  else
                  {
                     throw new ParseException(
                           "Xor operator (\"" + ExpressionOperation._ODD_OPERATOR + "\") must precede an expression.",
                           tokens);
                  }
               }
               else
               {
                  if(!check(work, j, ExpressionOperation._ODD_OPERATOR))
                  {
                     break;
                  }
               }
               operator = !(operator);
            }
            work.set(i - 1, new ExpressionOperation(0, components.toArray(new Expression[components.size()])));
            for(int k = i; k < j; k++)
            {
               work.remove(i);
            }
         }
      }
      
      for(int i = 0; i < work.size(); i++)
      {
         if(check(work, i, ExpressionOperation._OR_OPERATOR))
         {
            if(i == 0 || i == work.size() - 1)
            {
               throw new ParseException(
                     "Or operator (\"" + ExpressionOperation._OR_OPERATOR + "\") must not be the first or last token.",
                     tokens);
            }
            if(!(work.get(i - 1) instanceof Expression))
            {
               throw new ParseException(
                     "Or operator (\"" + ExpressionOperation._OR_OPERATOR + "\") must follow an expression.", tokens);
            }
            final ArrayList<Expression> components = new ArrayList<Expression>();
            components.add((Expression) work.get(i - 1));
            boolean operator = true;
            int j;
            for(j = i + 1; j < work.size(); j++)
            {
               if(operator)
               {
                  if(work.get(j) instanceof Expression)
                  {
                     components.add((Expression) work.get(j));
                  }
                  else
                  {
                     throw new ParseException(
                           "Or operator (\"" + ExpressionOperation._OR_OPERATOR + "\") must precede an expression.",
                           tokens);
                  }
               }
               else
               {
                  if(!check(work, j, ExpressionOperation._OR_OPERATOR))
                  {
                     break;
                  }
               }
               operator = !(operator);
            }
            work.set(i - 1, new ExpressionOperation(1, components.toArray(new Expression[components.size()])));
            for(int k = i; k < j; k++)
            {
               work.remove(i);
            }
         }
      }
      
      for(int i = 0; i < work.size(); i++)
      {
         if(check(work, i, ExpressionMinus._OPERATOR))
         {
            if(i == 0 || i == work.size() - 1)
            {
               throw new ParseException(
                     "Minus operator (\"" + ExpressionMinus._OPERATOR + "\") must not be the first or last token.",
                     tokens);
            }
            if(!(work.get(i - 1) instanceof Expression))
            {
               throw new ParseException(
                     "Minus operator (\"" + ExpressionMinus._OPERATOR + "\") must follow an expression.", tokens);
            }
            if(!(work.get(i + 1) instanceof Expression))
            {
               throw new ParseException(
                     "Minus operator (\"" + ExpressionMinus._OPERATOR + "\") must precede an expression.", tokens);
            }
            work.set(i - 1, new ExpressionMinus((Expression) work.get(i - 1), (Expression) work.get(i + 1)));
            work.remove(i);
            work.remove(i);
            i--;
         }
      }
      
      if(work.size() == 1 && work.get(0) instanceof Expression)
      {
         return (Expression) work.get(0);
      }
      else
      {
         throw new ParseException("Invalid format. Could not reduce to an Expression.", tokens);
      }
   }
   
   public static Expression[] parseCommas(final Object[] tokens) throws ParseException
   {
      final ArrayList<Object> receptacle = new ArrayList<Object>();
      int start = 0;
      int index;
      for(index = 0; index < tokens.length; index++)
      {
         final Object o = tokens[index];
         if(o instanceof Character && ((Character) o).charValue() == ',')
         {
            receptacle.add(parse(tokens, start, index));
            start = index + 1;
         }
      }
      if(start != index)
      {
         receptacle.add(parse(tokens, start, index));
      }
      return receptacle.toArray(new Expression[receptacle.size()]);
   }
   
   private static boolean check(final ArrayList<Object> a, final int i, final char c)
   {
      final Object o = a.get(i);
      if(!(o instanceof Character))
      {
         return false;
      }
      return ((Character) o).charValue() == c;
   }
}
