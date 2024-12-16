package com.github.michaelaaronlevy.ork.util;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * write arbitrary data to a new .csv (Comma-Separated Values) file that can be
 * opened directly by Excel, another spreadsheet program, or a text editor.
 * 
 * <p>
 * this class can only write data sequentially, one row at a time, from left to
 * right. Random access is not supported.
 * 
 * <p>
 * writing .csv files like this is not particularly complicated. The main reason
 * this class exists is because it implements the same interface as
 * {@link CellWriterOds CellWriterOds}.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the CellWriterCsv class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 */
public class CellWriterCsv implements CellWriter
{
   public CellWriterCsv(final File target) throws IOException
   {
      out = new BufferedWriter(new FileWriter(target));
   }
   
   public void writeBlank() throws IOException
   {
      out.write(_SEPARATOR);
   }
   
   public void writeText(final String s) throws IOException
   {
      out.write("\"");
      for(int i = 0; i < s.length(); i++)
      {
         out.write(literalCharacter(s.charAt(i)));
      }
      out.write("\"");
      out.write(_SEPARATOR);
   }
   
   private static String literalCharacter(final char c)
   {
      if(c == '\"')
      {
         return "\"\"";
      }
      else
      {
         return "" + c;
      }
   }
   
   public void writeInt(final int i) throws IOException
   {
      out.write(Integer.toString(i));
      out.write(_SEPARATOR);
   }
   
   public void writeFloat(final float f) throws IOException
   {
      out.write(Float.toString(f));
      out.write(_SEPARATOR);
   }
   
   public void writeDate(final Date d) throws IOException
   {
      writeText(sdf.format(d));
   }
   
   public void newRow() throws IOException
   {
      out.newLine();
   }
   
   public void close() throws IOException
   {
      out.close();
   }
   
   private BufferedWriter out;
   
   public static final SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:SS");
   public static final String _SEPARATOR = ",";
}
