package com.github.michaelaaronlevy.ork.util;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;

/***
 * each AscendingStack object holds a list of unique integers in ascending
 * order. They must only be added in ascending order. Integers can have any
 * value between Integer.MIN_VALUE and Integer.MAX_VALUE.
 * 
 * <p>
 * This class is designed for merging lists of unique integers that are sorted
 * in ascending order (merging them in terms of simple boolean operations like
 * AND, OR, XOR, or MINUS).
 * 
 * <p>
 * The class was created to assist with storing lists of page numbers where
 * certain words appear (a word index, also called a "concordance") but
 * presumably it has other uses.
 * 
 * <p>
 * Each instance holds up to a billion elements. Capacity grows based on
 * necessity.
 * 
 * <p>
 * Apart from the multiple available constructors, and the "trim" method, the
 * only way to change the contents is the "push" method. That means an
 * AscendingStack will never shrink. Once a number is added it can never come
 * out. That alone does not make it thread-safe.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the AcendingStack class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class AscendingStack implements Comparable<AscendingStack>
{
   /**
    * for efficiency purposes at the expense of reliability: use this
    * constructor to create an AscendingStack that is locked and that is backed
    * by another array. Obviously you should only use this constructor if you
    * will keep careful control of the AscendingStack objects and the underlying
    * integer arrays. This will allow fast boolean operations without wasting
    * memory by copying arrays unnecessarily.
    * 
    * @param array
    * @param counter
    */
   public AscendingStack(final int[] array, final int counter)
   {
      this.data = array;
      this.counter = counter;
      state = _STATE_DIRTY;
   }
   
   /**
    * create a new AscendingStack object, empty, with 200 initial capacity.
    */
   public AscendingStack()
   {
      this(200);
   }
   
   /**
    * @param initialCapacity
    *           the initial capacity of the AscendingStack. Of course the stack
    *           will grow as necessary as integers are pushed onto it (up to a
    *           billion unique ascending integers) AscendingStack begins empty.
    * @throws IndexOutOfBoundsException
    *            if the specified capacity is above the maximum capacity
    */
   public AscendingStack(int initialCapacity)
   {
      if(initialCapacity > _MAX_CAPACITY)
      {
         throw new IndexOutOfBoundsException();
      }
      data = new int[initialCapacity < 0 ? 0 : initialCapacity];
      counter = 0;
   }
   
   /**
    * @param array
    *           the initial contents of this AscendingStack. Must not have any
    *           duplicates. The contents do not need to be in ascending order.
    *           The array is not modified by this operation.
    */
   public AscendingStack(final int[] array)
   {
      this(array, 0, array == null ? 0 : array.length);
   }
   
   /**
    * 
    * @param array
    *           the source of initial contents of this AscendingStack. Must not
    *           have any duplicates within the specified range. The contents of
    *           the array do not need to be in ascending order. The array is not
    *           modified by this operation.
    * @param startIndex
    * @param endIndex
    * @throws IndexOutOfBoundsException
    * @throws RuntimeException
    *            if there are duplicate elements
    */
   public AscendingStack(final int[] array, final int startIndex, final int endIndex)
   {
      if(endIndex < startIndex || startIndex < 0 || endIndex > (array == null ? 0 : array.length)
            || endIndex - startIndex > _MAX_CAPACITY)
      {
         throw new IndexOutOfBoundsException();
      }
      else if(startIndex == endIndex)
      {
         data = new int[0];
         counter = 0;
      }
      else
      {
         data = new int[endIndex - startIndex];
         System.arraycopy(array, startIndex, data, 0, data.length);
         counter = data.length;
         Arrays.sort(data);
         int t = data[0];
         for(int i = 1; i < counter; i++)
         {
            if(data[i] <= t)
            {
               throwException(data[i - 1], data[i]);
            }
            t = data[i];
         }
      }
   }
   
   /**
    * clone method, but starts with the minimum needed capacity. Clones always
    * start unlocked and not dirty.
    * 
    * @param that
    */
   public AscendingStack(final AscendingStack that)
   {
      counter = that.counter;
      data = new int[counter];
      System.arraycopy(that.data, 0, data, 0, counter);
   }
   
   /**
    * 
    * @param value
    *           add this to the top of the stack. Expand the internal array for
    *           the stack if necessary.
    * @throws RuntimeException
    *            if "value" is lower than or equal to the top value on the stack
    *            (because that would indicate an error condition: pushing this
    *            value onto the stack would either cause the AscendingStack to
    *            contain a duplicate or to no longer be in ascending order)
    * @throws IndexOutOfBoundsException
    *            if the stack is already at maximum allowed capacity for this
    *            class
    */
   public void push(final int value)
   {
      if(state != _STATE_NORMAL)
      {
         throw new IllegalStateException("Illegal to alter a locked AscendingStack");
      }
      if(data.length == counter)
      {
         int newLength = data.length * 2 + 10;
         if(newLength > _MAX_CAPACITY)
         {
            newLength = _MAX_CAPACITY;
         }
         if(newLength == data.length)
         {
            throw new IndexOutOfBoundsException();
         }
         final int[] nextData = new int[newLength];
         System.arraycopy(data, 0, nextData, 0, counter);
         data = nextData;
      }
      if(counter > 0 && data[counter - 1] >= value)
      {
         throwException(data[counter - 1], value);
      }
      data[counter++] = value;
   }
   
   /**
    * slightly faster method, gains in speed by skipping data integrity checks.
    * This method is private and it is only called with respect to
    * AscendingStack objects created by methods of this class.
    * 
    * @param value
    */
   private void pushSecret(final int value)
   {
      if(data.length == counter)
      {
         int newLength = data.length * 2 + 400;
         if(newLength > _MAX_CAPACITY)
         {
            newLength = _MAX_CAPACITY;
         }
         if(newLength == data.length)
         {
            throw new IndexOutOfBoundsException();
         }
         final int[] nextData = new int[newLength];
         System.arraycopy(data, 0, nextData, 0, counter);
         data = nextData;
      }
      data[counter++] = value;
   }
   
   /**
    * @return a new int[] object that is independent of this object (modifying
    *         the int[] won't affect this object, and vice versa. However, if
    *         this object is dirty, it will return the int[] that is underlying
    *         this object so modifying that could cause problems.
    */
   public int[] integerArray()
   {
      if(state == _STATE_DIRTY && counter == data.length)
      {
         return data;
      }
      final int[] r = new int[counter];
      System.arraycopy(data, 0, r, 0, counter);
      return r;
   }
   
   /**
    * 
    * @param one
    * @param two
    * @return a new AscendingStack object that has all of the values present in
    *         either or both of the two arguments.
    */
   public static AscendingStack or(final AscendingStack one, final AscendingStack two)
   {
      if(one == null || one.isEmpty())
      {
         return two == null ? new AscendingStack(0) : two.clone();
      }
      else if(two == null || two.isEmpty())
      {
         return one.clone();
      }
      else if(one.last() > two.last())
      {
         return or(two, one);
      }
      else
      {
         final AscendingStack r = new AscendingStack();
         int indexOne = 0;
         int indexTwo = 0;
         while(indexOne < one.counter)
         {
            final int oneData = one.data[indexOne];
            final int twoData = two.data[indexTwo];
            if(oneData == twoData)
            {
               r.pushSecret(oneData);
               indexOne++;
               indexTwo++;
            }
            else if(oneData < twoData)
            {
               r.pushSecret(oneData);
               indexOne++;
            }
            else // oneData > twoData
            {
               r.pushSecret(twoData);
               indexTwo++;
            }
         }
         for(; indexTwo < two.counter; indexTwo++)
         {
            r.pushSecret(two.data[indexTwo]);
         }
         return r;
      }
   }
   
   /**
    * 
    * @return a new AscendingStack object that has all of the values present in
    *         all of the inputs.
    */
   public static AscendingStack or(final AscendingStack[] inputs)
   {
      final ArrayList<AscendingStack> list = new ArrayList<AscendingStack>(inputs.length);
      for(final AscendingStack as : inputs)
      {
         if(as != null && !as.isEmpty())
         {
            list.add(as);
         }
      }
      if(list.isEmpty())
      {
         return new AscendingStack(0);
      }
      else if(list.size() == 1)
      {
         return list.get(0).clone();
      }
      else
      {
         while(list.size() > 1)
         {
            int last = list.size() - 1;
            list.sort(_biggestFirst);
            list.set(last - 1, or(list.get(last - 1), list.remove(last)));
         }
         return list.get(0);
      }
   }
   
   /**
    * 
    * @param one
    * @param two
    * @return a new AscendingStack object that has all of the values present in
    *         both of the arguments.
    */
   public static AscendingStack and(final AscendingStack one, final AscendingStack two)
   {
      if(one == null || one.isEmpty() || two == null || two.isEmpty())
      {
         return new AscendingStack(0);
      }
      else if(one.last() > two.last())
      {
         return and(two, one);
      }
      else
      {
         final AscendingStack r = new AscendingStack();
         int indexOne = 0;
         int indexTwo = 0;
         while(indexOne < one.counter)
         {
            final int oneData = one.data[indexOne];
            final int twoData = two.data[indexTwo];
            if(oneData == twoData)
            {
               r.pushSecret(oneData);
               indexOne++;
               indexTwo++;
            }
            else if(oneData < twoData)
            {
               indexOne++;
            }
            else
            {
               indexTwo++;
            }
         }
         return r;
      }
   }
   
   /**
    * 
    * @return a new AscendingStack object that has all of the values present in
    *         ALL of the stacks
    */
   public static AscendingStack and(final AscendingStack[] inputs)
   {
      final ArrayList<AscendingStack> list = new ArrayList<AscendingStack>(inputs.length);
      for(final AscendingStack as : inputs)
      {
         if(as != null && !as.isEmpty())
         {
            list.add(as);
         }
         else
         {
            return new AscendingStack(0);
         }
      }
      if(list.isEmpty())
      {
         return new AscendingStack(0);
      }
      else if(list.size() == 1)
      {
         return list.get(0).clone();
      }
      else
      {
         list.sort(_biggestFirst);
         while(list.size() > 1)
         {
            int last = list.size() - 1;
            list.set(last - 1, and(list.get(last - 1), list.remove(last)));
         }
         return list.get(0);
      }
   }
   
   /**
    * 
    * @param one
    * @param two
    * @return an AscendingStack object that has all of the values that are in
    *         the first one but not the second one
    */
   public static AscendingStack minus(final AscendingStack one, final AscendingStack two)
   {
      if(one == null || one.isEmpty())
      {
         return new AscendingStack(0);
      }
      else if(two == null || two.isEmpty())
      {
         return one.clone();
      }
      else
      {
         final AscendingStack r = new AscendingStack();
         int indexOne = 0;
         int indexTwo = 0;
         while(indexOne < one.counter)
         {
            final int oneData = one.data[indexOne];
            final int twoData = two.data[indexTwo];
            if(oneData == twoData)
            {
               indexOne++;
               indexTwo++;
               if(indexTwo == two.counter)
               {
                  break;
               }
            }
            else if(oneData < twoData)
            {
               r.pushSecret(oneData);
               indexOne++;
            }
            else
            {
               indexTwo++;
               if(indexTwo == two.counter)
               {
                  break;
               }
            }
         }
         
         for(; indexOne < one.counter; indexOne++)
         {
            r.pushSecret(one.data[indexOne]);
         }
         return r;
      }
   }
   
   /**
    * 
    * @param one
    * @param two
    * @return an AscendingStack object that has all of the values that are in
    *         one or the other, but not both, of the arguments
    */
   public static AscendingStack xor(final AscendingStack one, final AscendingStack two)
   {
      if(one == null || one.isEmpty())
      {
         return two == null ? new AscendingStack() : two.clone();
      }
      else if(two == null || two.isEmpty())
      {
         return one.clone();
      }
      else if(one.last() > two.last())
      {
         return xor(two, one);
      }
      else
      {
         final AscendingStack r = new AscendingStack();
         int indexOne = 0;
         int indexTwo = 0;
         while(indexOne < one.counter)
         {
            final int oneData = one.data[indexOne];
            final int twoData = two.data[indexTwo];
            if(oneData == twoData)
            {
               indexOne++;
               indexTwo++;
            }
            else if(oneData < twoData)
            {
               r.pushSecret(oneData);
               indexOne++;
            }
            else // oneData > twoData
            {
               r.pushSecret(twoData);
               indexTwo++;
               if(indexTwo == two.counter)
               {
                  break;
               }
            }
         }
         for(; indexTwo < two.counter; indexTwo++)
         {
            r.pushSecret(two.data[indexTwo]);
         }
         return r;
      }
   }
   
   /**
    * 
    * @param inputs
    * @return a new AscendingStack containing all elements of the inputs that
    *         appeared an odd number of times in the inputs. (Returning only
    *         those with odd parity.)
    */
   public static AscendingStack xor(final AscendingStack[] inputs)
   {
      if(inputs == null || inputs.length == 0)
      {
         return new AscendingStack(0);
      }
      else if(inputs.length == 1)
      {
         return inputs[0] == null ? new AscendingStack(0) : inputs[0].clone();
      }
      else
      {
         return counting(-1, inputs);
      }
   }
   
   /**
    * 
    * @param minimum
    *           add only if at least this many of the inputs contain this
    *           number. (So anything less than "1" is equivalent to "1"); 1 is
    *           equivalent to "or", if minimum==inputs.length that is equivalent
    *           to "and", and if minimum&gt;inputs.length then the return must
    *           be empty.
    * @param inputs
    * @return
    */
   public static AscendingStack atLeast(final int minimum, final AscendingStack[] inputs)
   {
      if(inputs == null || inputs.length == 0 || minimum > inputs.length)
      {
         return new AscendingStack(0);
      }
      else if(minimum <= 1)
      {
         return or(inputs);
      }
      else if(minimum == inputs.length)
      {
         return and(inputs);
      }
      else
      {
         return counting(minimum, inputs);
      }
   }
   
   /*
    * for performing "oddParity" and "atLeast" calculations
    */
   private static AscendingStack counting(final int minimum, final AscendingStack[] inputs)
   {
      final ArrayList<AscendingStack> stacks = new ArrayList<AscendingStack>(inputs.length);
      for(int i = 0; i < inputs.length; i++)
      {
         if(inputs[i] != null && !inputs[i].isEmpty())
         {
            stacks.add(inputs[i]);
         }
      }
      if(minimum > inputs.length)
      {
         return new AscendingStack(0);
      }
      final int[] indices = new int[stacks.size()];
      final AscendingStack r = new AscendingStack();
      
      while(!stacks.isEmpty() && stacks.size() >= minimum)
      {
         int smallest = Integer.MAX_VALUE;
         int smallestCount = 0;
         for(int i = 0; i < stacks.size(); i++)
         {
            final AscendingStack as = stacks.get(i);
            final int v = as.data[indices[i]];
            if(v < smallest)
            {
               smallest = v;
               smallestCount = 1;
            }
            else if(v == smallest)
            {
               smallestCount++;
            }
         }
         
         for(int i = 0; i < stacks.size(); i++)
         {
            final AscendingStack as = stacks.get(i);
            if(as.data[indices[i]] == smallest)
            {
               if(++indices[i] == as.counter)
               {
                  stacks.remove(i);
                  for(int j = i; j < indices.length - 1; j++)
                  {
                     indices[j] = indices[j + 1];
                  }
                  i--;
               }
            }
         }
         if(minimum == -1 ? smallestCount % 2 == 1 : smallestCount >= minimum)
         {
            r.pushSecret(smallest);
         }
      }
      return r;
   }
   
   /**
    * 
    * @param opCode
    *           -1 minus, 0 xor, 1 or, operands.length and, anything else is "at
    *           least __"
    * @param operands
    *           list of stacks to be operated on, null is treated as empty. This
    *           array is not modified by this method.
    * @return the results of the operation
    */
   public static AscendingStack generic(final int opCode, final AscendingStack... operands)
   {
      if(opCode == -1)
      {
         final AscendingStack first = operands[0];
         final AscendingStack removeList;
         if(operands.length == 2)
         {
            removeList = operands[1];
            if(removeList == null)
            {
               return first.clone();
            }
         }
         else
         {
            final AscendingStack[] toRemove = new AscendingStack[operands.length - 1];
            System.arraycopy(operands, 1, toRemove, 0, toRemove.length);
            removeList = AscendingStack.or(toRemove);
         }
         return AscendingStack.minus(first, removeList);
      }
      else if(opCode == 0)
      {
         return AscendingStack.xor(operands);
      }
      else if(opCode == 1)
      {
         return AscendingStack.or(operands);
      }
      else if(opCode == operands.length)
      {
         return AscendingStack.and(operands);
      }
      else
      {
         return AscendingStack.atLeast(opCode, operands);
      }
   }
   
   /**
    * 
    * @return true if the stack is empty
    */
   public boolean isEmpty()
   {
      return counter == 0;
   }
   
   /**
    * 
    * @return the number of integers in the stack
    */
   public int size()
   {
      return counter;
   }
   
   /**
    * 
    * @return the top element on the stack
    * @throws ArrayIndexOutOfBoundsException
    *            if the stack is empty
    */
   public int last()
   {
      return data[counter - 1];
   }
   
   public AscendingStack clone()
   {
      return new AscendingStack(this);
   }
   
   /**
    * return a String object describing the contents of this AscendingStack.
    */
   public String toString()
   {
      final StringBuilder b = new StringBuilder(10000);
      try
      {
         sendDescription(b, _MAX_DISPLAY);
      }
      catch(final IOException iox)
      {
         // this will not happen because StringBuilder does not throw
         // IOException
      }
      return b.toString();
   }
   
   /**
    * send a text description of this object to the specified Appendable object.
    * 
    * @param a
    *           an "Appendable" object (a StringBuilder, or a PrintStream, for
    *           example)
    */
   public void sendDescription(final Appendable a, final int maxDisplay) throws IOException
   {
      a.append("AscendingStack: ").append(super.toString());
      if(counter == 0)
      {
         a.append(" is empty.");
      }
      else
      {
         a.append("{").append(Integer.toString(data[0]));
         if(counter > maxDisplay)
         {
            final int halfMax = maxDisplay / 2;
            for(int i = 1; i < halfMax; i++)
            {
               a.append(",").append(Integer.toString(data[i]));
            }
            a.append(" . . . ").append(Integer.toString(data[counter - halfMax]));
            for(int i = (counter - halfMax) + 1; i < counter; i++)
            {
               a.append(",").append(Integer.toString(data[i]));
            }
         }
         else
         {
            for(int i = 1; i < counter; i++)
            {
               a.append(",").append(Integer.toString(data[i]));
            }
         }
         a.append("}");
      }
   }
   
   public int compareTo(final AscendingStack that)
   {
      if(that == null)
      {
         return 1;
      }
      if(this.counter < that.counter)
      {
         return -that.compareTo(this);
      }
      if(this.data == that.data)
      {
         return 0; // shortcut if both are dirty
      }
      for(int i = 0; i < that.counter; i++)
      {
         int thisData = this.data[i];
         int thatData = that.data[i];
         if(thisData != thatData)
         {
            return thisData < thatData ? -1 : 1;
         }
      }
      return this.counter == that.counter ? 0 : 1;
   }
   
   /**
    * 
    * @param that
    * @return true if the contents of the array are the same (the capacity of
    *         the data arrays are not relevant)
    */
   public boolean equals(final AscendingStack that)
   {
      if(this == that)
      {
         return true;
      }
      else if(that == null || this.counter != that.counter)
      {
         return false;
      }
      else
      {
         if(this.data == that.data)
         {
            return true; // shortcut if both are dirty
         }
         for(int i = 0; i < that.counter; i++)
         {
            if(this.data[i] != that.data[i])
            {
               return false;
            }
         }
         return true;
      }
   }
   
   /**
    * reduce the size of the data array to the number of elements in order to
    * save memory. (But not less than "1" because capacity less than 1 is never
    * allowed.)
    */
   public void trim()
   {
      if(state == _STATE_DIRTY)
      {
         return;
      }
      else if(counter == 0)
      {
         data = new int[1];
      }
      else if(counter != data.length)
      {
         final int[] newData = new int[counter];
         System.arraycopy(data, 0, newData, 0, counter);
         data = newData;
      }
   }
   
   public void lock()
   {
      if(state == _STATE_NORMAL)
      {
         trim();
         state = _STATE_LOCKED;
      }
   }
   
   private void throwException(int already, int fail)
   {
      throw new RuntimeException("Numbers can only be added to an AscendingStack in ascending order - invalid to add "
            + fail + " after " + already);
   }
   
   /**
    * once this object is made dirty, it no longer guarantees to conform to the
    * behavior rules set by the class. If you use "dirty" stacks, you are taking
    * responsibility for not breaking the functionality (by altering the
    * underlying arrays in ways that cause problems). But operations may become
    * more efficient.
    */
   public void makeDirty()
   {
      state = _STATE_DIRTY;
   }
   
   public boolean isLocked()
   {
      return state != _STATE_NORMAL;
   }
   
   /**
    * if the AscendingStack is "dirty," a call to the integerArray() method will
    * return the actual array that is used by this object, rather than a clone.
    * It runs faster but if the array is modified, the correct behavior of this
    * AscendingStack object ceases to be guaranteed.
    * 
    * @return
    */
   public boolean isDirty()
   {
      return state == _STATE_DIRTY;
   }
   
   private int[] data;
   
   /**
    * points to the next open spot in the array (or it is equal to data.length
    * when the data array is full)
    */
   private int counter;
   
   /**
    * if locked, attempting to push a value will result in an error message.
    */
   private byte state = _STATE_NORMAL;
   
   /**
    * the maximum capacity allowed.
    */
   public static int _MAX_CAPACITY = 1000000000;
   
   /**
    * for the toString method, if there are more than twice as many numbers, it
    * puts ellipsis in the middle rather than trying to display everything.
    */
   private static int _MAX_DISPLAY = 20;
   
   public IntArrayIterator getIterator()
   {
      return new IntArrayIterator(this.data, 0, this.counter);
   }
   
   /**
    * this comparator moves the longest stacks to the front of the list so we
    * can target the longest (or shortest) lists first, to make boolean
    * operations more efficient.
    */
   private static final Comparator<AscendingStack> _biggestFirst = new SizeComparator();
   
   private static final class SizeComparator implements Comparator<AscendingStack>
   {
      public int compare(AscendingStack one, AscendingStack two)
      {
         return (one == null) ? (two == null ? 0 : 1) : (two == null ? -1 : two.counter - one.counter);
      }
   }
   
   private static final byte _STATE_NORMAL = 0;
   private static final byte _STATE_LOCKED = 1;
   private static final byte _STATE_DIRTY = 2;
}
