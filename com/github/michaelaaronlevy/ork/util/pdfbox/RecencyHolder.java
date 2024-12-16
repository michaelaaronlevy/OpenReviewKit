package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * maintains PDDocument objects along with information about how recently they
 * were used. This is done so that the oldest ones can be closed, in order to
 * free up memory.
 * 
 * I AM THE SOLE AUTHOR OF THIS WORK (the RecencyHolder class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * <p>
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class RecencyHolder
{
   /**
    * @param capacity
    *           the maximum number of PDDocument objects to have open. This
    *           class will automatically close the PDDocument objects when they
    *           cease to be among the N most recent.
    */
   public RecencyHolder(final int capacity)
   {
      array = new Recency[capacity];
      counter = 0;
   }
   
   public PDDocument get(final int fileNumber) throws IOException
   {
      for(int i = 0; i < counter; i++)
      {
         final Recency r = array[i];
         if(r.fileNumber == fileNumber)
         {
            // System.err.println("Found, moving to front: " + r);
            
            r.updateTime();
            final Recency swap = array[0];
            array[0] = r;
            array[i] = swap;
            return r.doc;
         }
      }
      if(counter == array.length)
      {
         Arrays.sort(array);
         final Recency toKill = array[--counter];
         toKill.doc.close();
         array[counter] = null;
         
         // System.err.println("Removing " + toKill);
      }
      return null;
   }
   
   public int getIndexForPdf(final PDDocument doc)
   {
      for(int i = 0; i < counter; i++)
      {
         if(array[i].doc == doc)
         {
            return array[i].fileNumber;
         }
      }
      return -1;
   }
   
   /**
    * never call "add" unless you first call "get" and you get back a null
    * value. Otherwise, it might cause an ArrayIndexOutOfBoundsException
    * 
    * @param fileNumber
    * @param doc
    */
   public void add(final int fileNumber, final PDDocument doc)
   {
      array[counter++] = new Recency(doc, fileNumber);
      
      // System.err.println("Adding " + array[counter - 1]);
   }
   
   // the array is only sorted when it is time to select one for deletion
   // which is only when it looks for one, can't find it, AND it's full
   private final Recency[] array;
   
   // the number of Recency objects that are held
   private int counter;
   
   private static final class Recency implements Comparable<Recency>
   {
      public Recency(final PDDocument doc, final int fileNumber)
      {
         this.doc = doc;
         this.fileNumber = fileNumber;
         updateTime();
      }
      
      public int compareTo(final Recency that)
      {
         if(that == null)
         {
            return 1;
         }
         else if(this == that)
         {
            return 0;
         }
         else if(this.time == that.time)
         {
            return Integer.compare(this.fileNumber, that.fileNumber);
         }
         else
         {
            return Long.compare(that.time, this.time);
         }
      }
      
      public void updateTime()
      {
         time = System.currentTimeMillis();
      }
      
      public String toString()
      {
         return "Recency[fN:" + fileNumber + " age (seconds):" + ((System.currentTimeMillis() - time) / 1000) + "; doc:"
               + doc;
      }
      
      public final PDDocument doc;
      public final int fileNumber;
      
      // this identifies how "fresh" the doc is, so that when too many are open,
      // the program can close the least "fresh" one
      public long time;
   }
}
