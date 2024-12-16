package com.github.michaelaaronlevy.ork;

import java.io.IOException;
import java.io.InputStream;

import com.github.michaelaaronlevy.ork.util.Grid;

/**
 * Read a .grid file to get one {@link MemenFile MemenFile} object at a time.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class GridIterMemenFile
{
   public GridIterMemenFile(final InputStream is) throws IOException
   {
      this.is = is;
      pagesPerPdf = Grid.readIntArray(is);
      fileNames = Grid.readStringArray(is);
      counter = 0;
   }
   
   public synchronized MemenFile getNext() throws IOException
   {
      if(counter == fileNames.length)
      {
         is.close();
         return null;
      }
      final MemenFile p = new MemenFile(is, fileNames[counter], pagesPerPdf[counter]);
      counter++;
      return p;
   }
   
   public int getProjectPage()
   {
      int pp = 0;
      for(int i = 0; i < counter; i++)
      {
         pp += pagesPerPdf[i];
      }
      return pp;
   }
   
   private final int[] pagesPerPdf;
   private final String[] fileNames;
   
   private final InputStream is;
   private int counter;
}
