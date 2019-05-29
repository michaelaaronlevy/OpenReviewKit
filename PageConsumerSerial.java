package com.github.michaelaaronlevy.ork;

import java.io.File;
import java.io.IOException;
import java.io.OutputStream;

import com.github.michaelaaronlevy.ork.util.Grid;

/**
 * Save the text extracted from the PDFs directly to a data file (in the crude,
 * proprietary serial format, which by convention has the file extension ".grid"
 * and is sometimes referred to in comments as a "grid file").
 * 
 * <p>The Searchable Word Index uses this so that the extracted text does not need
 * to stay in memory while the {@link PdfToTextGrid PdfToTextGrid} extracts text from potentially
 * thousands or more PDFs. Another reason to serialize the data is if you intend
 * to read it from the file more than once, which would save the need to
 * re-extract the text each time.
 * 
 * <p>The classes {@link GridIterMemenFile GridIterMemenFile} and {@link GridIterMemenPage GridIterMemenPage} can be used to easily
 * pull the data back out of a .grid file.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class PageConsumerSerial implements PageConsumer
{
   public PageConsumerSerial(final OutputStream out, final boolean closeOnClose)
   {
      this.out = out;
      this.closeOnClose = closeOnClose;
   }
   
   public void startProject(File[] files, int[] pageCounts) throws IOException
   {
      final String[] filesInfo = new String[files.length];
      for(int i = 0; i < files.length; i++)
      {
         filesInfo[i] = files[i].getAbsolutePath();
      }
      Grid.writeIntArray(out, pageCounts);
      Grid.writeStringArray(out, filesInfo);
   }
   
   public void startFile(File file, int fileNumber, int pageCount)
   {
      // do nothing
   }
   
   public void takePage(int firstId, int totalPage, MemenPage page) throws IOException
   {
      Grid.writeInt(out, page.fileNumber);
      Grid.writeInt(out, page.pdfPage);
      Grid.writeInt(out, totalPage);
      Grid.writeInt(out, firstId);
      Grid.writeInt(out, page.getNumberOfWords());
      
      for(final MemenText text : page.getWordsArray())
      {
         Grid.writeInt(out, text.yStart);
         Grid.writeInt(out, text.getYSmooth());
         Grid.writeInt(out, text.xStart);
         Grid.writeInt(out, text.xEnd);
         Grid.writeInt(out, text.height);
         Grid.writeInt(out, text.rotation);
         Grid.writeString(out, text.text);
      }
   }
   
   public void endOfFile()
   {
      // do nothing
   }
   
   public void endOfProject() throws IOException
   {
      if(closeOnClose)
      {
         out.close();
      }
   }
   
   private final OutputStream out;
   private final boolean closeOnClose;
}
