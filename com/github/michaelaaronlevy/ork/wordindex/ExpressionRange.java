package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * A range is a series of integers from start to end, with step. If you wanted
 * to identify a specific document or group of documents, you could specify a
 * range matching those page numbers.
 * 
 * <p>
 * E.g., range(1,5,1) returns [1,2,3,4,5]. range(2,8,2) returns [2,4,6,8].
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionRange extends Expression
{
   public ExpressionRange(final int start, final int end, final int step)
   {
      this.start = start;
      this.end = end;
      this.step = step;
      if(start > end)
      {
         throw new RuntimeException("ERROR: start of the range cannot be larger than the end of the range. range("
               + start + ", " + end + ", " + step + ")");
      }
      else if(step < 1)
      {
         throw new RuntimeException(
               "ERROR: range must increment by at least 1. range(" + start + ", " + end + ", " + step + ")");
      }
   }
   
   public ExpressionRange(final Expression[] components)
   {
      final int[] array = ExpressionConstant.getIntegerLiterals(components);
      if(array.length < 2 || array.length > 3)
      {
         throw new RuntimeException("ERROR: range() must have 2-3 arguments.");
      }
      String text = "range(" + array[0] + "," + array[1] + (array.length == 3 ? "," + array[2] + ")" : ")");
      if(array[1] < array[0])
      {
         throw new RuntimeException(
               "ERROR: the end of the range must be greater than or equal to the start of the range. " + text);
      }
      
      start = array[0];
      end = array[1];
      step = array.length == 3 ? array[2] : 1;
      if(step < 1)
      {
         throw new RuntimeException("ERROR: step cannot be less than 1: " + text);
      }
   }
   
   protected AscendingStack calculate(Context context)
   {
      int size = (end - start) / step + 1;
      final int[] array = new int[size];
      for(int i = 0; i < size; i++)
      {
         array[i] = start + step * i;
      }
      
      int start = 0; // this eliminates page numbers less than 1 from the range
      // there is no safeguard at this point to prevent page numbers that are
      // too high.
      // but as long as there is at least one legal page number in the range,
      // the viewer should not crash
      while(start < array.length && array[start] < 1)
      {
         start++;
      }
      if(start == 0)
      {
         return new AscendingStack(array);
      }
      else
      {
         return new AscendingStack(array, start, array.length);
      }
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append(_RANGE_NAME);
      a.append('(');
      a.append(Integer.toString(start));
      a.append(',');
      a.append(Integer.toString(end));
      if(step != 1)
      {
         a.append(',');
         a.append(Integer.toString(step));
      }
      a.append(')');
   }
   
   public static final String _RANGE_NAME = "range";
   
   private final int start;
   private final int end;
   private final int step;
}
