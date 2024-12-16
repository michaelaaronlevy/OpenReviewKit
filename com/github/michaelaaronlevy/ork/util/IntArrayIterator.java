package com.github.michaelaaronlevy.ork.util;

import java.util.NoSuchElementException;

/**
 * iterate over an int[] or a subset of an int[] (a defined contiguous range).
 * If this is created by an {@link AscendingStack AscendingStack} object, it is
 * not backed by that {@link AscendingStack AscendingStack}. This class does not
 * modify the underlying array. If this is created by a non-dirty
 * {@link AscendingStack AscendingStack} object, the underlying array won't
 * change in the relevant range, the values will be unique, and they will be
 * already sorted in an ascending order. But if this is created via different
 * means, the array could be changed after this iterator is constructed and the
 * values within the range may not be unique or sorted in ascending order.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the IntArrayIterator class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class IntArrayIterator
{
   /**
    * 
    * @param data
    *           the numbers to iterate over; can be null
    */
   public IntArrayIterator(final int[] data)
   {
      this(data, 0, data == null ? 0 : data.length);
   }
   
   /**
    * 
    * @param data
    *           the numbers to iterate over; can be null
    * @param start
    *           index of starting value to iterate from (must not be less than
    *           zero)
    * @param end
    *           index of ending value to iterate to (must not exceed the
    *           boundaries of the array, which for a null array is zero)
    */
   public IntArrayIterator(final int[] data, final int start, final int end)
   {
      this.data = data;
      this.size = end - start;
      this.start = start;
      index = -1;
      final int max = data == null ? 0 : data.length;
      if(start < 0 || end > max)
      {
         throw new ArrayIndexOutOfBoundsException();
      }
   }
   
   /**
    * 
    * @return index is -1 before this starts and index == size when it is
    *         finished.
    */
   public int getIndex()
   {
      return index;
   }
   
   /**
    * 
    * @return the total number of elements that can ultimately be returned by
    *         this iterator
    */
   public int getSize()
   {
      return size;
   }
   
   /**
    * 
    * @return the total number of remaining elements. Before it starts, this is
    *         equal to size. When it is on the last element, it equals zero.
    *         When it is finished, it equals -1.
    */
   public int getRemaining()
   {
      return size - index - 1;
   }
   
   /**
    * 
    * @return true if the iterator has an element loaded; it has started and it
    *         is not finished.
    */
   public boolean hasCurrent()
   {
      return index >= 0 && index < size;
   }
   
   /**
    * 
    * @return true if this is started (even if it is finished).
    */
   public boolean isStarted()
   {
      return index > -1;
   }
   
   /**
    * 
    * @return true if this has advanced to the end and is no longer has an
    *         element available.
    */
   public boolean isFinished()
   {
      return index >= size;
   }
   
   /**
    * 
    * @return the current value
    * @throws NoSuchElementException
    *            if it is not started or if it is finished.
    */
   public int getCurrentValue()
   {
      if(index == -1 || index >= size)
      {
         throw new NoSuchElementException();
      }
      return data[start + index];
   }
   
   /**
    * if it is not finished, advance to the next element. (If it is finished, do
    * nothing.)
    */
   public void advance()
   {
      if(!isFinished())
      {
         index++;
      }
   }
   
   /**
    * @param target
    *           if the current loaded value is not at least equal to the target,
    *           then advance until either the iterator is at the end or until
    *           the loaded value is equal to or greater than the target.
    */
   public void advanceTo(final int target)
   {
      if(!isStarted())
      {
         advance();
      }
      while(!isFinished())
      {
         if(getCurrentValue() >= target)
         {
            return;
         }
         advance();
      }
   }
   
   /**
    * 
    * @param value
    *           skip past everything less than or equal to this value. (This can
    *           result in reaching the end.)
    */
   public void advancePast(final int value)
   {
      if(value == Integer.MAX_VALUE)
      {
         index = size;
      }
      else
      {
         advanceTo(value + 1);
      }
   }
   
   public IntArrayIterator clone()
   {
      final IntArrayIterator r = new IntArrayIterator(data, start, start + size);
      r.index = this.index;
      return r;
   }
   
   private int index;
   
   private final int size;
   private final int start;
   private final int[] data;
}
