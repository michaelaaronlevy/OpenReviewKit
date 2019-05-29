package com.github.michaelaaronlevy.ork.util;

import java.io.PrintStream;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * display arbitrary data in the console.
 * 
 * <p>
 * (this is meant primarily for debugging, if you ultimately plan to use a
 * different implementation of {@link CellWriter CellWriter}, but you want to
 * see in real time the output that your program is generating)
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the CellWriterPrint class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 */
public class CellWriterPrint implements CellWriter
{
   /**
    * 
    * @param out
    *           the stream to print to, or null for no output at all
    */
   public CellWriterPrint(final PrintStream out)
   {
      this.out = out;
   }
   
   public void writeBlank()
   {
      print("");
   }
   
   public void writeText(final String s)
   {
      print(s);
   }
   
   public void writeInt(final int i)
   {
      print(Integer.toString(i));
   }
   
   public void writeFloat(final float f)
   {
      print(Float.toString(f));
   }
   
   public void writeDate(final Date d)
   {
      print(df.format(d));
   }
   
   public void newRow()
   {
      if(out != null)
      {
         out.println();
         out.print(">>\t");
      }
   }
   
   public void close()
   {
      if(out != null)
      {
         out.flush();
      }
   }
   
   private void print(final String s)
   {
      if(out != null)
      {
         out.print(s);
         out.print(_SEPARATOR);
      }
   }
   
   private final PrintStream out;
   
   public static final String _SEPARATOR = ", ";
   public static final SimpleDateFormat df = new SimpleDateFormat("yyyy.MM.dd G 'at' HH:mm:ss z");
}
