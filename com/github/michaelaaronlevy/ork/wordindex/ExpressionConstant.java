package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;
import java.util.Arrays;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * 
 * Each object of this class represents an array of positive integers. The array
 * is not supposed to change after ExpressionConstant is instantiated.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionConstant extends Expression
{
   public ExpressionConstant(final AscendingStack as)
   {
      this.constant = as;
   }
   
   public ExpressionConstant(final Expression[] integerLiterals)
   {
      int[] array = getIntegerLiterals(integerLiterals);
      
      // eliminate values less than 1
      Arrays.sort(array);
      int start = 0;
      while(start < array.length && array[start] < 1)
      {
         start++;
      }
      
      int duplicateCount = 0;
      for(int i = start + 1; i < array.length; i++)
      {
         final int value1 = array[i];
         final int value0 = array[i - 1];
         if(value0 == value1)
         {
            duplicateCount++;
         }
      }
      if(duplicateCount > 0 || start > 0)
      {
         int counter = 0;
         final int[] r = new int[array.length - duplicateCount - start];
         int current = -1;
         for(final int i : array)
         {
            if(i > 0 && i != current)
            {
               current = i;
               r[counter++] = i;
            }
         }
         array = r;
         start = 0;
      }
      
      if(start == 0)
      {
         this.constant = new AscendingStack(array);
      }
      else
      {
         this.constant = new AscendingStack(array, start, array.length);
      }
   }
   
   protected AscendingStack calculate(Context context)
   {
      return constant;
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      if(constant.size() == 0)
      {
         a.append(_EMPTY_NAME);
         a.append("()");
      }
      else
      {
         a.append(_CONSTANTS_NAME);
         a.append('(');
         final int[] array = constant.integerArray();
         for(int i = 0; i < array.length; i++)
         {
            if(i != 0)
            {
               a.append(',');
            }
            a.append(Integer.toString(array[i]));
         }
         a.append(')');
      }
   }
   
   public static int[] getIntegerLiterals(final Expression[] components)
   {
      final int[] array = new int[components.length];
      for(int i = 0; i < array.length; i++)
      {
         if(!(components[i] instanceof ExpressionWord))
         {
            throw new RuntimeException("ERROR: Not a valid integer literal: " + components[i]);
         }
         final String next = ((ExpressionWord) components[i]).word;
         try
         {
            array[i] = Integer.parseInt(next);
         }
         catch(final NumberFormatException nfe)
         {
            throw new RuntimeException("ERROR: Not a valid integer literal: " + next);
         }
      }
      return array;
   }
   
   private final AscendingStack constant;
   
   public static final String _EMPTY_NAME = "empty";
   public static final String _CONSTANTS_NAME = "integer";
}
