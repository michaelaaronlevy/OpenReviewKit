package com.github.michaelaaronlevy.ork.util;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.Charset;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.UnaryOperator;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

/**
 * write data to a new .ods (Open Document Spreadsheet) document that can be
 * opened directly by Excel or by other programs including OpenOfficeOrg and
 * LibreOffice. The "newSheet(String)" method can be used to create a workbook
 * with multiple sheets.
 * 
 * <p>
 * Excel can open an .ods file that has far more rows than for a .csv file. So
 * if you are making a really big spreadsheet, this may be preferable to .csv,
 * or if you want to separate the data into multiple worksheets.
 * 
 * <p>
 * this class can only write data sequentially, one row at a time, from left to
 * right. Random access is not supported.
 * 
 * @author michaelaaronlevy@gmail.com
 */
public class CellWriterOds implements CellWriter
{
   public CellWriterOds(final File target, final String initialSheet) throws IOException
   {
      ZipOutputStream zos = null;
      zos = new ZipOutputStream(new BufferedOutputStream(new FileOutputStream(target)));
      out = zos;
      
      zos.putNextEntry(new ZipEntry("Configurations2/accelerator/"));
      zos.putNextEntry(new ZipEntry("Configurations2/floater/"));
      zos.putNextEntry(new ZipEntry("Configurations2/images/"));
      zos.putNextEntry(new ZipEntry("Configurations2/menubar/"));
      zos.putNextEntry(new ZipEntry("Configurations2/popupmenu/"));
      zos.putNextEntry(new ZipEntry("Configurations2/progressbar/"));
      zos.putNextEntry(new ZipEntry("Configurations2/statusbar/"));
      zos.putNextEntry(new ZipEntry("Configurations2/toolbar/"));
      zos.putNextEntry(new ZipEntry("Configurations2/toolpanel/"));
      zos.putNextEntry(new ZipEntry("META-INF/manifest.xml"));
      resourceToZip("/ods/manifest.xml");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("manifest.rdf"));
      resourceToZip("/ods/manifest.rdf");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("meta.xml"));
      resourceToZip("/ods/meta.xml");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("mimetype"));
      resourceToZip("/ods/mimetype");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("settings.xml"));
      resourceToZip("/ods/settings.xml");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("styles.xml"));
      resourceToZip("/ods/styles.xml");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("Thumbnails/thumbnail.png"));
      resourceToZip("/ods/thumbnail.png");
      zos.closeEntry();
      zos.putNextEntry(new ZipEntry("content.xml"));
      resourceToZip("/ods/content-heading.txt");
      newSheet(initialSheet);
   }
   
   private void resourceToZip(final String resource) throws IOException
   {
      final InputStream is = getClass().getResourceAsStream(resource);
      while(is.available() > 0)
      {
         out.write(b, 0, is.read(b));
      }
      is.close();
   }
   
   /**
    * Create a new worksheet. future output will be to this worksheet, until
    * this method is called again. (This class also calls this method as needed,
    * if there are an extreme number of rows, in order to ensure that this
    * object does not exceed the maximum number of rows when creating an .ods
    * file.
    * 
    * @param sheetName
    *           the name for the new worksheet
    * @throws IOException
    */
   public void newSheet(String sheetName) throws IOException
   {
      sheetStart = row;
      sheetCount++;
      if(sheetName == null || sheetName.length() == 0)
      {
         sheetName = _SHEET_NAME + (sheetCount + 1);
         if(sheetCount == 9999)
         {
            namelen = 5;
         }
         sheetName = sheetName.substring(sheetName.length() - namelen);
      }
      if(sheetCount != 0)
      {
         out.write(_CONTENT_SHEET_END, 0, _CONTENT_SHEET_END.length);
      }
      out.write(_CONTENT_SHEET_START, 0, _CONTENT_SHEET_START.length);
      out.write(transformer.apply(sheetName).getBytes());
      resourceToZip("/ods/content-newsheet.txt");
      write(_CONTENT_ROW_START);
   }
   
   public void writeBlank() throws IOException
   {
      write(_CONTENT_BLANK);
   }
   
   public void writeText(final String s) throws IOException
   {
      try
      {
         // this will save some numbers in a way that the spreadsheet recognizes
         // them as numbers rather than text. We cannot do this for really big
         // numbers, however, because the spreadsheet will convert them to
         // scientific notation, which will change the underlying data (e.g.,
         // very long account numbers)
         if(s.length() < 14)
         {
            Double.parseDouble(s);
            writeTextAsNumber(s);
            return;
         }
      }
      catch(final NumberFormatException nfe)
      {
         // continue
      }
      
      write(_CONTENT_STRING_BEFORE);
      write(transformer.apply(s));
      write(_CONTENT_STRING_AFTER);
   }
   
   public void writeInt(int i) throws IOException
   {
      final byte[] b = Integer.toString(i).getBytes(_UTF8);
      write(_CONTENT_NUMBER_BEFORE);
      write(b);
      write(_CONTENT_NUMBER_MIDDLE);
      write(b);
      write(_CONTENT_NUMBER_AFTER);
   }
   
   private void writeTextAsNumber(final String s) throws IOException
   {
      write(_CONTENT_NUMBER_BEFORE);
      write(s);
      write(_CONTENT_NUMBER_MIDDLE);
      write(s);
      write(_CONTENT_NUMBER_AFTER);
   }
   
   public void writeFloat(float f) throws IOException
   {
      write(_CONTENT_NUMBER_BEFORE);
      write(Float.toString(f));
      write(_CONTENT_NUMBER_MIDDLE);
      write(String.format("%.2f", f));
      write(_CONTENT_NUMBER_AFTER);
   }
   
   public void writeDate(final Date d) throws IOException
   {
      final String date = _SDF_DATE.format(d);
      final String time = _SDF_TIME.format(d);
      
      write(_CONTENT_DATE_BEFORE);
      write(date + "T" + time);
      write(_CONTENT_DATE_MIDDLE);
      write(date + " " + time);
      write(_CONTENT_DATE_AFTER);
   }
   
   public void newRow() throws IOException
   {
      write(_CONTENT_ROW_END);
      write(_CONTENT_ROW_START);
      row++;
      if(row > sheetStart + _NEW_WORKSHEET)
      {
         newSheet(null);
      }
   }
   
   public void close() throws IOException
   {
      out.write(_CONTENT_ROW_END);
      out.write(_CONTENT_SHEET_END, 0, _CONTENT_SHEET_END.length);
      out.write(_CONTENT_END, 0, _CONTENT_END.length);
      out.closeEntry();
      out.close();
   }
   
   private void write(final byte[] toWrite) throws IOException
   {
      out.write(toWrite, 0, toWrite.length);
   }
   
   private void write(final String toWrite) throws IOException
   {
      write(toWrite.getBytes(_UTF8));
   }
   
   private ZipOutputStream out = null;
   private final byte[] b = new byte[_BYTE_ARRAY_LENGTH];
   private static final int _BYTE_ARRAY_LENGTH = 256;
   
   private int sheetCount = -1;
   private int sheetStart = 0;
   private int row = 0;
   private int namelen = 4;
   private int _NEW_WORKSHEET = 800000;
   
   private static final Charset _UTF8 = java.nio.charset.StandardCharsets.UTF_8;
   private static final String _SHEET_NAME = "0000";
   private static final byte[] _CONTENT_SHEET_START = "<table:table table:name=\"".getBytes(_UTF8);
   private static final byte[] _CONTENT_SHEET_END = "</table:table>".getBytes(_UTF8);
   private static final byte[] _CONTENT_END = "<table:named-expressions/></office:spreadsheet></office:body></office:document-content>"
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_ROW_START = "<table:table-row table:style-name=\"ro1\">".getBytes(_UTF8);
   private static final byte[] _CONTENT_ROW_END = "</table:table-row>".getBytes(_UTF8);
   
   private static final byte[] _CONTENT_NUMBER_BEFORE = "<table:table-cell calcext:value-type=\"float\" office:value-type=\"float\" office:value=\""
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_NUMBER_MIDDLE = "\"><text:p>".getBytes(_UTF8);
   private static final byte[] _CONTENT_NUMBER_AFTER = "</text:p></table:table-cell>".getBytes(_UTF8);
   
   private static final byte[] _CONTENT_DATE_BEFORE = "<table:table-cell table:style-name=\"ce3\" calcext:value-type=\"date\" office:value-type=\"date\" office:date-value=\""
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_DATE_MIDDLE = "\"><text:p>".getBytes(_UTF8);
   private static final byte[] _CONTENT_DATE_AFTER = _CONTENT_NUMBER_AFTER;
   
   private static final byte[] _CONTENT_STRING_BEFORE = "<table:table-cell calcext:value-type=\"string\" office:value-type=\"string\"><text:p>"
         .getBytes(_UTF8);
   private static final byte[] _CONTENT_STRING_AFTER = _CONTENT_NUMBER_AFTER;
   
   private static final byte[] _CONTENT_BLANK = "<table:table-cell/>".getBytes(_UTF8);
   
   private static final char[] _REPLACEES = { '\"', '\'', '&', '<', '>' };
   private static final String[] _REPLACERS = { "&quot;", "&apos;", "&amp;", "&lt;", "&gt;" };
   
   private static final SimpleDateFormat _SDF_DATE = new SimpleDateFormat("yyyy-MM-dd");
   private static final SimpleDateFormat _SDF_TIME = new SimpleDateFormat("HH:mm:ss");
   
   private final Transformer transformer = new Transformer();
   
   /**
    * transform a String into an xml-friendly String. E.g., replace "&amp;" with
    * "&amp;amp;"
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static class Transformer implements UnaryOperator<String>
   {
      public synchronized String apply(String s)
      {
         for(int i = 0; i < s.length(); i++)
         {
            boolean flag = true;
            final char c = s.charAt(i);
            
            for(int j = 0; j < _REPLACEES.length; j++)
            {
               if(c == _REPLACEES[j])
               {
                  b.append(_REPLACERS[j]);
                  flag = false;
                  break;
               }
            }
            if(flag)
            {
               b.append(c < 32 || Character.isWhitespace(c) ? ' ' : c);
            }
         }
         final String t = b.toString();
         b.delete(0, b.length());
         if(s.equals(t))
         {
            return s;
         }
         else
         {
            return t;
         }
      }
      
      private final StringBuilder b = new StringBuilder(999);
   }
}
