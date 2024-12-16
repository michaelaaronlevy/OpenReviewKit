package com.github.michaelaaronlevy.ork.sorcerer;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;

import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.util.CellWriter;

/**
 * This is very much a work in progress. The idea is to create a programmatic
 * tool to convert a group of MemenText objects (representing the elements in a
 * two-dimensional table) into a a two-dimensional array.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class Table
{
   public Table(final String[][] data, int pdfPage)
   {
      this.data = new String[data.length][];
      for(int i = 0; i < data.length; i++)
      {
         this.data[i] = data[i].clone();
      }
      this.pdfPage = new int[data.length];
      Arrays.fill(this.pdfPage, pdfPage);
   }
   
   /**
    * 
    * @param text
    *           the titles and table contents
    * @param titleRows
    *           the number of rows containing the name of the title.
    * @param pageHeight
    *           in inches. 11 is a normal-size document.
    */
   public Table(final ArrayList<MemenText> text, final int titleRows, final int pageHeight)
   {
      if(text.isEmpty())
      {
         throw new IllegalArgumentException("There must be at least 1 text object or there would not be any titles");
      }
      
      text.sort(null);
      final int firstPage = text.get(0).pdfPage;
      long smooth = text.get(0).getYSmooth();
      final ArrayList<ArrayList<MemenText>> tableData = new ArrayList<ArrayList<MemenText>>();
      ArrayList<MemenText> current = new ArrayList<MemenText>();
      tableData.add(current);
      final long pageHeightK = pageHeight * _PAGE_HEIGHT;
      
      // each row becomes its own separate ArrayList within "tableData"
      for(final MemenText t : text)
      {
         final long next = t.getYSmooth() + ((t.pdfPage - firstPage) * (pageHeightK));
         if(next != smooth)
         {
            smooth = next;
            current = new ArrayList<MemenText>();
            tableData.add(current);
         }
         current.add(t);
      }
      // each row is now its own separate ArrayList within "tableData"
      
      data = new String[tableData.size() - titleRows + 1][];
      pdfPage = new int[data.length];
      
      // now, the first N entries in tableData correspond to the title; the rest
      // of it is the data.
      final ArrayList<ArrayList<MemenText>> titles = new ArrayList<ArrayList<MemenText>>(titleRows);
      for(int i = 0; i < titleRows; i++)
      {
         titles.add(tableData.remove(0));
      }
      // now, all of the title information is in "titles" and all of the table
      // data is in "tableData"
      
      // TODO test
      System.err.println("Titles:");
      for(final ArrayList<MemenText> row : titles)
      {
         for(final MemenText mt : row)
         {
            System.err.print(mt);
         }
         System.err.println();
      }
      System.err.println("End Test Titles.");
      
      final ArrayList<Column> columns = new ArrayList<Column>();
      
      if(titleRows > 0)
      {
         pdfPage[0] = titles.get(0).get(0).pdfPage;
         for(final ArrayList<MemenText> row : titles)
         {
            for(final MemenText t : row)
            {
               columns.add(new Column(t));
            }
         }
         columns.sort(null);
         
         for(int i = columns.size() - 1; i > 0; i--)
         {
            if(columns.get(i).overlapsWith(columns.get(i - 1)))
            {
               final Column remove = columns.remove(i);
               remove.eat(columns.get(i - 1));
            }
         }
      }
      
      // TODO test
      System.err.println("Columns based only on Titles");
      for(final Column c : columns)
      {
         System.err.print(c);
         System.err.print(' ');
      }
      System.err.println();
      System.err.println("End Test Columns based only on Titles");
      
      // expand every column that overlaps with data
      for(final ArrayList<MemenText> row : tableData)
      {
         for(final MemenText t : row)
         {
            for(final Column col : columns)
            {
               if(col.overlapsWith(t))
               {
                  col.eat(t);
               }
            }
         }
      }
      
      // TODO test
      System.err.println("Columns expanded 1");
      for(final Column c : columns)
      {
         System.err.print(c);
         System.err.print(' ');
      }
      System.err.println();
      System.err.println("End Test Columns expanded 1");
      
      // create new anonymous columns as needed and expand them
      for(final ArrayList<MemenText> row : tableData)
      {
         for(final MemenText t : row)
         {
            boolean flag = false;
            for(final Column col : columns)
            {
               if(col.overlapsWith(t))
               {
                  col.eat(t);
                  flag = true;
                  break;
               }
            }
            if(!flag)
            {
               columns.add(new Column(t));
               columns.sort(null);
            }
         }
      }
      
      // TODO test
      System.err.println("Columns expanded 2");
      for(final Column c : columns)
      {
         System.err.print(c);
         System.err.print(' ');
      }
      System.err.println();
      System.err.println("End Test Columns expanded 2");
      
      // so what this does NOT do well is handle situations where columns can
      // overlap with each other, including where they overlap with a small
      // amount of the title word.
      
      final ArrayList<StringBuilder> titleText = new ArrayList<StringBuilder>(columns.size());
      for(int i = 0; i < columns.size(); i++)
      {
         titleText.add(new StringBuilder());
      }
      for(final ArrayList<MemenText> row : titles)
      {
         for(final MemenText title : row)
         {
            int column = -1;
            for(int i = 0; i < columns.size(); i++)
            {
               if(columns.get(i).overlapsWith(title))
               {
                  column = i;
                  break;
               }
            }
            final StringBuilder b = titleText.get(column);
            if(b.length() != 0)
            {
               b.append(' ');
            }
            b.append(title.text);
         }
      }
      
      for(int i = 0; i < data.length; i++)
      {
         data[i] = new String[columns.size()];
      }
      final String[] titlesArray = data[0];
      for(int i = 0; i < columns.size(); i++)
      {
         final StringBuilder b = titleText.get(i);
         if(b.length() == 0)
         {
            titlesArray[i] = null;
         }
         else
         {
            titlesArray[i] = b.toString();
         }
      }
      
      // TODO test
      System.err.println("Title Names Test");
      for(final String s : data[0])
      {
         if(s == null)
         {
            System.err.print("-\t");
         }
         else
         {
            System.err.print(s);
            System.err.print("\t");
         }
      }
      System.err.println("End Title Names Test");
      
      for(int i = 0; i < tableData.size(); i++)
      {
         final String[] array = data[i + 1];
         final ArrayList<MemenText> row = tableData.get(i);
         
         final MemenText first = row.get(0);
         pdfPage[i + 1] = first.pdfPage;
         
         for(final MemenText t : row)
         {
            int column = -1;
            for(int j = 0; j < columns.size(); j++)
            {
               if(columns.get(j).overlapsWith(t))
               {
                  column = j;
                  break;
               }
            }
            if(array[column] != null)
            {
               throw new RuntimeException("ERROR: Overlap between " + t.text + " and " + array[column]);
            }
            array[column] = t.text;
         }
      }
   }
   
   /**
    * 
    * @return the total number of columns, including anonymous (nameless)
    *         columns.
    */
   public int getNumberOfColumns()
   {
      return data[0].length;
   }
   
   public String[] getColumnNames()
   {
      return data[0].clone();
   }
   
   /**
    * 
    * @return the row number of the last row, which is also equal to the total
    *         number of rows.
    */
   public int getNumberOfRows()
   {
      return data.length - 1;
   }
   
   /**
    * 
    * @param row
    *           the row number (starting at 1. 0 is the titles, which is
    *           allowed)
    * @param column
    *           the column number (starting at 1.)
    * @return the value associated with that particular row/column
    * @throws ArrayIndexOutOfBoundsException
    *            if invalid values are provided for row or column
    */
   public String getData(final int row, final int column)
   {
      return data[row][column - 1];
   }
   
   /**
    * 
    * write the contents of the Table to a CellWriter (optional: add the pdf
    * page number before each line)
    * 
    * @param cw
    *           direct output to this writer
    * @param writePage
    *           false - no preface; true - preface each line with the PDF page
    *           number
    * @throws IOException
    *            may be thrown by the CellWriter
    */
   public void write(final CellWriter cw, final boolean writePage) throws IOException
   {
      final String[] preface = new String[writePage ? 1 : 0];
      if(writePage)
      {
         preface[0] = _PDF_PAGE;
      }
      write(cw, preface);
   }
   
   /**
    * write the contents of the Table to a CellWriter, with the specified
    * preface before each line
    * 
    * @param cw
    *           direct output to this writer
    * @param preface
    *           the columns to write before writing the table data; if a column
    *           == _PDF_PAGE, then the pdf page number is written by the
    *           CellWriter
    * @throws IOException
    *            may be thrown by the CellWriter
    */
   public void write(final CellWriter cw, final String[] preface) throws IOException
   {
      for(int index = 0; index < data.length; index++)
      {
         final String[] line = data[index];
         final int page = pdfPage[index];
         for(final String pre : preface)
         {
            if(pre == _PDF_PAGE)
            {
               cw.writeInt(page);
            }
            else if(pre == null)
            {
               cw.writeBlank();
            }
            else
            {
               cw.writeText(pre);
            }
         }
         for(final String s : line)
         {
            if(s == null || s.length() == 0)
            {
               cw.writeBlank();
            }
            else
            {
               cw.writeText(s);
            }
         }
         cw.newRow();
      }
   }
   
   private final String[][] data;
   private final int[] pdfPage;
   
   public static String _PDF_PAGE = "pdfPage";
   
   /**
    * this corresponds to a page that is over 15 times the size of a normal 11".
    * It is a safe value to use as a parameter in the constructor, unless you
    * actually have a page larger than this for some reason.
    */
   private static final long _PAGE_HEIGHT = 131072L;
}
