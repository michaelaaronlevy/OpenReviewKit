package com.github.michaelaaronlevy.ork;

import java.io.File;
import java.io.IOException;

/**
 * 
 * the PdfToTextGrid class outputs {@link MemenPage MemenPage} objects to an instance of
 * {@link PageConsumer PageConsumer}. The object that will immediately receive and process the text
 * content of the PDFs will be an instance of this class.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface PageConsumer
{
   public void startProject(final File[] files, final int[] pageCounts) throws IOException;
   
   public void startFile(final File file, final int fileNumber, final int pageCount) throws IOException;
   
   public void takePage(final int firstId, final int totalPage, final MemenPage page) throws IOException;
   
   public void endOfFile() throws IOException;
   
   public void endOfProject() throws IOException;
}
