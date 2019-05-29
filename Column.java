package com.github.michaelaaronlevy.ork.sorcerer;

import java.util.Collection;

import com.github.michaelaaronlevy.ork.MemenText;

/**
 * this is a work in progress. It is meant to assist with recognizing MemenWords
 * that make up tables and ordering them into 2D arrays corresponding with the
 * tables.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class Column implements Comparable<Column>
{
   public Column(final MemenText text)
   {
      this.xStart = text.xStart;
      this.xEnd = text.xEnd;
   }
   
   public Column(final int xStart, final int xEnd)
   {
      if(xStart > xEnd)
      {
         throw new IllegalArgumentException();
      }
      this.xStart = xStart;
      this.xEnd = xEnd;
   }
   
   public int compareTo(final Column that)
   {
      if(this.xStart != that.xStart)
      {
         return this.xStart < that.xStart ? -1 : 1;
      }
      else if(this.xEnd != that.xEnd)
      {
         return this.xEnd < that.xEnd ? -1 : 1;
      }
      else
      {
         return 0;
      }
   }
   
   public boolean overlapsWith(final Column that)
   {
      return !(this.xEnd < that.xStart || this.xStart > that.xEnd);
   }
   
   public boolean overlapsWith(final MemenText that)
   {
      return !(this.xEnd < that.xStart || this.xStart > that.xEnd);
   }
   
   /**
    * 
    * @param that
    *           grow this and that as necessary to accommodate all of both
    */
   public void eat(final Column that)
   {
      this.xStart = Math.min(this.xStart, that.xStart);
      this.xEnd = Math.max(this.xEnd, that.xEnd);
      that.xStart = this.xStart;
      that.xEnd = this.xEnd;
   }
   
   /**
    * 
    * @param that
    *           grow this as necessary to accommodate all of that
    */
   public void eat(final MemenText that)
   {
      this.xStart = Math.min(this.xStart, that.xStart);
      this.xEnd = Math.max(this.xEnd, that.xEnd);
   }
   
   public String toString()
   {
      return "c[" + xStart + "," + xEnd + "]";
   }
   
   private int xStart;
   private int xEnd;
   
   public static void combineAndSort(final Collection<Column> columns)
   {
      // TODO
      // TODO need a method that sorts a collection PLUS all overlapping columns
      // are combined (which is not necessarily limited to ONLY the columns that
      // are adjacent in a sort)
      
   }
}
