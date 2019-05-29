package com.github.michaelaaronlevy.ork;

import java.io.File;
import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.CellWriter;

/**
 * this class takes the output from {@link PdfToTextGrid PdfToTextGrid} and
 * sends it to a {@link com.github.michaelaaronlevy.ork.util.CellWriter CellWriter}
 * so the data can be saved to a spreadsheet (the text as well as location
 * information for the text)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class PageConsumerCellWriter implements PageConsumer
{
   public PageConsumerCellWriter(final CellWriter fw)
   {
      this.fw = fw;
   }
   
   public void startProject(final File[] files, final int[] pageCounts) throws IOException
   {
      for(int i = 0; i < files.length; i++)
      {
         fw.writeText(files[i].getAbsolutePath());
         fw.newRow();
      }
      fw.newRow();
      for(final String title : MemenText._HEADERS)
      {
         fw.writeText(title);
      }
      fw.newRow();
   }
   
   public void startFile(final File file, final int fileNumber, final int pageCount) throws IOException
   {
      // do nothing
   }
   
   public void takePage(int id, final int totalPage, final MemenPage page) throws IOException
   {
      for(final MemenText text : page.getWordsArray())
      {
         fw.writeInt(id++);
         
         fw.writeInt(page.fileNumber);
         fw.writeInt(page.pdfPage);
         fw.writeInt(totalPage);
         
         fw.writeInt(text.yStart);
         fw.writeInt(text.getYSmooth());
         fw.writeInt(text.xStart);
         fw.writeInt(text.xEnd);
         fw.writeInt(text.height);
         fw.writeInt(text.rotation);
         
         fw.writeText(text.text);
         fw.newRow();
      }
   }
   
   public void endOfFile() throws IOException
   {
      // do nothing
   }
   
   public void endOfProject() throws IOException
   {
      fw.close();
   }
   
   private final CellWriter fw;
}
