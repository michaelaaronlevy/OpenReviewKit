package com.github.michaelaaronlevy.ork.util;

import java.io.IOException;
import java.util.Date;

/**
 * the CellWriter interface is for writers that are capable of writing one cell
 * at a time of a spreadsheet (in order, going down one row at a time, then
 * writing cells from left-to-right; this interface does not require the
 * implementing class to permit random access).
 * 
 * <p>At present, the spreadsheet types supported by implementations of this
 * interface are .csv and .ods.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the CellWriter interface) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 */
public interface CellWriter
{
   /**
    * the writer should write an empty cell (and then be ready to write the next
    * data)
    */
   public void writeBlank() throws IOException;
   
   /**
    * the writer should write the text (and then be ready to write the next
    * data)
    * 
    * @param s
    *           the text to write
    */
   public void writeText(final String s) throws IOException;
   
   /**
    * the writer should write the integer (and then be ready to write the next
    * data)
    * 
    * @param i
    *           the integer to write
    */
   public void writeInt(final int i) throws IOException;
   
   /**
    * write a floating point number
    * @param f the value to write
    * @throws IOException
    */
   public void writeFloat(final float f) throws IOException;
   
   /**
    * write a date
    * @param d the value to write
    * @throws IOException
    */
   public void writeDate(Date d) throws IOException;
   
   /**
    * this method is called whenever the writer should end the current row and
    * start a new one (starting back at "Column A").
    */
   public void newRow() throws IOException;
   
   // this method should be called only once. It needs to end the current row
   // and not start a new one. Then it should flush output and shut down the
   // writer.
   public void close() throws IOException;
}
