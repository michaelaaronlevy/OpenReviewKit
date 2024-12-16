package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * this class performs boolean operations (and, atLeast, or, xor, oddParity) on
 * multiple inputs.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionOperation extends Expression
{
   public ExpressionOperation(final int opCode, final Expression[] components)
   {
      this.opCode = opCode;
      this.components = components;
      if(opCode == 0)
      {
         separator = _ODD_OPERATOR;
      }
      else if(opCode == 1)
      {
         separator = _OR_OPERATOR;
      }
      else if(opCode == components.length)
      {
         separator = _AND_OPERATOR;
      }
      else if(opCode < 0 || opCode > components.length)
      {
         throw new IllegalArgumentException("Not a Valid Op Code: " + opCode);
      }
      else
      {
         separator = _COMMA;
      }
   }
   
   public static Expression getExpression(final String name, final Expression[] components)
   {
      if(name.equals(_AND_NAME))
      {
         if(components.length < 2)
         {
            throw new IllegalArgumentException("ERROR: " + _AND_NAME + "() must have at least two arguments.");
         }
         return new ExpressionOperation(components.length, components);
      }
      else if(name.equals(_OR_NAME))
      {
         if(components.length < 2)
         {
            throw new IllegalArgumentException("ERROR: " + _OR_NAME + "() must have at least two arguments.");
         }
         return new ExpressionOperation(1, components);
      }
      else if(name.equals(_ODD_NAME))
      {
         if(components.length < 2)
         {
            throw new IllegalArgumentException("ERROR: " + _ODD_NAME + "() must have at least two arguments.");
         }
         return new ExpressionOperation(0, components);
      }
      else if(name.equals(_XOR_NAME))
      {
         if(components.length != 2)
         {
            throw new IllegalArgumentException(
                  "ERROR: " + _XOR_NAME + "() must have exactly two arguments to avoid ambiguity.  Use " + _ODD_NAME
                        + "() or " + ExpressionExactly._EXACTLY_NAME + "1().");
         }
         return new ExpressionOperation(0, components);
      }
      else if(name.startsWith(_AT_LEAST_NAME) && name.length() == 8)
      {
         final char c = name.charAt(7);
         if(c < '1' && c > '9')
         {
            throw new IllegalArgumentException(
                  "ERROR: atLeast2-9 are valid function names but " + name + " is not valid.");
         }
         final int op = c - '0';
         if(op > components.length)
         {
            System.err.println(
                  "WARNING: " + name + " will always return empty() results because of the number of arguments.");
            return new ExpressionConstant(new AscendingStack(0));
         }
         else if(op == components.length)
         {
            System.err
                  .println("WARNING: " + name + " is the same as calling and() because of the number of arguments.");
         }
         else if(op == 1)
         {
            System.err.println("WARNING: " + name + " is the same as calling or().");
         }
         return new ExpressionOperation(op, components);
      }
      else if(name.startsWith(ExpressionExactly._EXACTLY_NAME) && name.length() == 8)
      {
         final char c = name.charAt(7);
         if(c < '1' && c > '9')
         {
            throw new IllegalArgumentException("ERROR: exactly1-9 are valid function names but " + name + " is not.");
         }
         // "exactlyN(all) = atLeastN(all) - atLeastN+1(all)"
         final int amount = c - '0';
         if(amount == components.length)
         {
            System.err
                  .println("WARNING: " + name + " is the same as calling and() because of the number of arguments.");
            return new ExpressionOperation(amount, components);
         }
         else if(amount > components.length)
         {
            System.err.println("Warning: exactly" + c + " will always return the empty set with just "
                  + components.length + " arguments.");
            return new ExpressionConstant(new AscendingStack(0));
         }
         else
         {
            return new ExpressionExactly(amount, components);
         }
      }
      else if(name.equals(ExpressionConstant._CONSTANTS_NAME))
      {
         return new ExpressionConstant(components);
      }
      else if(name.equals(ExpressionConstant._EMPTY_NAME))
      {
         if(components.length != 0)
         {
            throw new IllegalArgumentException("empty() function must take zero arguments.");
         }
         return new ExpressionConstant(components);
      }
      else if(name.equals(ExpressionRange._RANGE_NAME))
      {
         return new ExpressionRange(components);
      }
      else
      {
         throw new IllegalArgumentException();
      }
   }
   
   protected AscendingStack calculate(final Context context)
   {
      final AscendingStack[] stacks = new AscendingStack[components.length];
      for(int i = 0; i < stacks.length; i++)
      {
         stacks[i] = components[i].calculate(context);
      }
      return AscendingStack.generic(opCode, stacks);
   }
   
   public void writeScript(final Appendable a) throws IOException
   {
      if(separator == _ODD_OPERATOR && components.length > 2)
      {
         a.append(_ODD_NAME);
         a.append('(');
         for(int i = 0; i < components.length; i++)
         {
            if(i != 0)
            {
               a.append(' ');
               a.append(_COMMA);
               a.append(' ');
            }
            components[i].writeScript(a);
         }
         a.append(')');
      }
      else
      {
         if(separator == _COMMA)
         {
            a.append(_AT_LEAST_NAME);
            a.append((char) (opCode + '0'));
         }
         a.append("( ");
         components[0].writeScript(a);
         for(int i = 1; i < components.length; i++)
         {
            a.append(' ');
            a.append(separator);
            a.append(' ');
            components[i].writeScript(a);
         }
         a.append(" )");
         
      }
   }
   
   public static boolean isOperation(final String name)
   {
      boolean flag = false;
      if(name.equals(_AND_NAME) || name.equals(_OR_NAME) || name.equals(_XOR_NAME) || name.equals(_ODD_NAME)
            || name.equals(ExpressionConstant._CONSTANTS_NAME) || name.equals(ExpressionConstant._EMPTY_NAME)
            || name.equals(ExpressionRange._RANGE_NAME))
      {
         flag = true;
      }
      else if(name.startsWith(_AT_LEAST_NAME) && name.length() == 8)
      {
         final char c = name.charAt(7);
         flag = (c > '1' && c <= '9');
      }
      else if(name.startsWith(ExpressionExactly._EXACTLY_NAME) && name.length() == 8)
      {
         final char c = name.charAt(7);
         flag = (c >= '1' && c <= '9');
      }
      return flag;
   }
   
   public final int opCode;
   public final char separator;
   
   private final Expression[] components;
   
   public static final char _COMMA = ',';
   public static final char _AT_LEAST_OPERATOR = '>';
   public static final char _AND_OPERATOR = '&';
   public static final char _ODD_OPERATOR = '^';
   public static final char _OR_OPERATOR = '|';
   
   public static final String _OR_NAME = "or";
   public static final String _AND_NAME = "and";
   public static final String _ODD_NAME = "oddParity";
   public static final String _XOR_NAME = "xor";
   public static final String _AT_LEAST_NAME = "atLeast";
}
