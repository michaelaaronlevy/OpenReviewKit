package com.github.michaelaaronlevy.ork.util;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.nio.charset.Charset;

/**
 * a collection of static methods associated with writing to and reading from a
 * data file (a file that is written with these methods would be read back by
 * these methods). The data is written/read one byte at a time. This is a very
 * simple/crude proprietary data format to support sequentially writing integers
 * and Strings to a file and reading it back sequentially later.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the Grid class) AND I AM PLACING IT IN THE
 * PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class Grid
{
   public static void writeInt(final OutputStream stream, final int i) throws IOException
   {
      // System.err.println("Grid::writeInt " + i);
      
      stream.write(i >> 24);
      stream.write(i >> 16);
      stream.write(i >> 8);
      stream.write(i >> 0);
   }
   
   public static void writeIntArray(final OutputStream stream, final int[] ii) throws IOException
   {
      // System.err.println("Grid::writeIntArray " +
      // java.util.Arrays.toString(ii));
      
      writeInt(stream, ii.length);
      for(final int i : ii)
      {
         writeInt(stream, i);
      }
   }
   
   public static void writeShort(final OutputStream stream, final short s) throws IOException
   {
      // System.err.println("Grid::writeShort " + s);
      
      stream.write(s >> 8);
      stream.write(s >> 0);
   }
   
   public static void writeShort(final OutputStream stream, final int i) throws IOException
   {
      if(i < Short.MIN_VALUE || i > Short.MAX_VALUE)
      {
         throw new RuntimeException("ERROR: Attempt to write 'short' value that is out-of-bounds.");
      }
      
      stream.write(i >> 8);
      stream.write(i >> 0);
   }
   
   public static void writeShortArray(final OutputStream stream, final short[] ss) throws IOException
   {
      // System.err.println("Grid::writeShortArray " +
      // java.util.Arrays.toString(ss));
      
      writeInt(stream, ss.length);
      for(final short s : ss)
      {
         writeShort(stream, s);
      }
   }
   
   public static void writeShortArray(final OutputStream stream, final int[] ii) throws IOException
   {
      // System.err.println("Grid::writeShortArray " +
      // java.util.Arrays.toString(ii));
      
      writeInt(stream, ii.length);
      for(final int i : ii)
      {
         if(i < Short.MIN_VALUE || i > Short.MAX_VALUE)
         {
            throw new NumberFormatException("Cannot Write as Short: " + i);
         }
         writeShort(stream, (short) i);
      }
   }
   
   /**
    * the data format used is: a short (16 bits) stating the length of the byte
    * array for the string (which may not be equal to the length of the string
    * in characters, given that it uses UTF-8 encoding.)
    * 
    * @param stream
    * @param text
    * @throws IOException
    */
   public static void writeString(final OutputStream stream, final String text) throws IOException
   {
      // System.err.println("Grid::writeText " + text + "!");
      
      final byte[] b = text.getBytes(_UTF8);
      writeShort(stream, (short) b.length);
      stream.write(b);
   }
   
   public static void writeStringArray(final OutputStream stream, final String[] text) throws IOException
   {
      writeInt(stream, text.length);
      for(final String s : text)
      {
         writeString(stream, s);
      }
   }
   
   public static void writeBoolean(final OutputStream stream, final boolean bool) throws IOException
   {
      stream.write(bool ? ((byte) 116) : ((byte) 102));
   }
   
   public static int readInt(final InputStream stream) throws IOException
   {
      int i = 0;
      i += stream.read();
      i <<= 8;
      i += stream.read();
      i <<= 8;
      i += stream.read();
      i <<= 8;
      i += stream.read();
      
      // System.err.println("Grid::readInt " + i);
      
      return i;
   }
   
   public static int[] readIntArray(final InputStream stream) throws IOException
   {
      int size = readInt(stream);
      final int[] r = new int[size];
      for(int i = 0; i < size; i++)
      {
         r[i] = readInt(stream);
      }
      
      // System.err.println("Grid::readIntArray " +
      // java.util.Arrays.toString(r));
      
      return r;
   }
   
   public static short readShort(final InputStream stream) throws IOException
   {
      short s = 0;
      s += stream.read();
      s <<= 8;
      s += stream.read();
      
      // System.err.println("Grid::readShort " + s);
      
      return s;
   }
   
   public static short[] readShortArray(final InputStream stream) throws IOException
   {
      int size = readInt(stream);
      final short[] r = new short[size];
      for(int i = 0; i < size; i++)
      {
         r[i] = readShort(stream);
      }
      
      // System.err.println("Grid::readShortArray " +
      // java.util.Arrays.toString(r));
      
      return r;
   }
   
   public static int[] readShortArrayToIntArray(final InputStream stream) throws IOException
   {
      int size = readInt(stream);
      final int[] r = new int[size];
      for(int i = 0; i < size; i++)
      {
         r[i] = readShort(stream);
      }
      
      // System.err.println("Grid::readShortArrayToIntArray " +
      // java.util.Arrays.toString(r));
      
      return r;
   }
   
   public static String readString(final InputStream stream) throws IOException
   {
      final int length = readShort(stream);
      final byte[] b = new byte[length];
      stream.read(b);
      final String r = new String(b, _UTF8);
      
      // System.err.println("Grid::readText " + (r.length() > 100 ?
      // r.substring(0, 100) : r));
      
      return r;
   }
   
   public static String[] readStringArray(final InputStream stream) throws IOException
   {
      int size = readInt(stream);
      final String[] r = new String[size];
      for(int i = 0; i < size; i++)
      {
         r[i] = readString(stream);
      }
      
      // System.err.println("Grid::readStringArray " +
      // java.util.Arrays.toString(r));
      
      return r;
   }
   
   public static boolean readBoolean(final InputStream stream) throws IOException
   {
      int i = stream.read();
      if(i == 102)
      {
         return false;
      }
      else if(i == 116)
      {
         return true;
      }
      else
      {
         throw new RuntimeException(
               "ERROR: Attempted to read a boolean value.  Invalid value found.  Byte must equal 102 or 116.");
      }
   }
   
   private static final Charset _UTF8 = org.apache.pdfbox.util.Charsets.UTF_8;
}
