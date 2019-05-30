package com.github.michaelaaronlevy.ork.sorcerer;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.michaelaaronlevy.ork.MemenPage;
import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.ModeSimple;
import com.github.michaelaaronlevy.ork.ModeSimple.ReaderMode;
import com.github.michaelaaronlevy.ork.PageConsumer;
import com.github.michaelaaronlevy.ork.PdfToTextGrid;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.util.DragDropFilesList;

/**
 * If you want to make an application that will use PdfToTextGrid to extract
 * text from one or more PDFs (that the user can select with a drag-and-drop GUI
 * interface) and processes the extracted text on a page-by-page basis, and
 * sends the output to a file (that the user can select using the GUI), you can
 * use this class as a starting point.
 * 
 * <p>
 * The most obvious places where you might want to add or change code, to get
 * your application working, are marked with a "TODO" along with an explanation
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the YourApplication class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class YourApplicationWithDragDrop implements PageConsumer, ActionListener
{
   public static void main(String[] args) throws IOException
   {
      final Dimension d = new Dimension(400, 500);
      final JFrame frame = new JFrame();
      final YourApplicationWithDragDrop app = new YourApplicationWithDragDrop(frame);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setTitle(_YOUR_APPLICATION_NAME);
      frame.add(app.window.mainPanel);
      frame.setSize(d);
      frame.setPreferredSize(d);
      frame.setMinimumSize(d);
      frame.pack();
      frame.setVisible(true);
   }
   
   public YourApplicationWithDragDrop(final Component context)
   {
      // the DEFAULT ReaderMode returns the same text that is extracted by
      // PDFTextStripper.
      // You may want to experiment with using a different ReaderMode from
      // "ModeSimple" or even making your own parser and transformer objects.
      final ReaderMode mode = ModeSimple.ReaderMode.DEFAULT;
      parser = mode.getParser();
      transformer = mode.getTransformer();
      
      inputFC = new JFileChooser();
      inputFC.setFileSelectionMode(JFileChooser.FILES_ONLY);
      inputFC.addChoosableFileFilter(_PDF_ONLY);
      inputFC.setFileFilter(_PDF_ONLY);
      inputFC.setMultiSelectionEnabled(true);
      
      outputFC = new JFileChooser();
      
      window = new DragDropFilesList(context, inputFC, null, null, null);
      button.addActionListener(this);
      window.mainPanel.add(button, BorderLayout.SOUTH);
   }
   
   public void startProject(final File[] files, final int[] pageCounts) throws IOException
   {
      // PdfToTextGrid will call this before text extraction begins
      // you do not necessarily need to have behavior here, especially because
      // you also have a constructor method
   }
   
   public void startFile(final File file, final int fileNumber, final int pageCount) throws IOException
   {
      // PdfToTextGrid will call this before extraction begins on each PDF
      // you do not necessarily need to have behavior here
      
      // I am including a simple message to the console, which you can feel free
      // to delete
      System.err.println("Status update: starting on file #" + fileNumber + ", " + file.getName());
   }
   
   public void takePage(final int firstId, final int totalPage, final MemenPage page) throws IOException
   {
      // TODO: this is the most important method to fill in
      // every time PdfToTextGrid finishes extracting text from a page, it will
      // invoke this method
      
      // the MemenPage object contains all of the extracted text and the
      // contextual information, from that page, in the form of MemenText
      // objects.
      
      // here is placeholder code that will print the extracted text/context to
      // the output PrintStream.
      for(final MemenText textWithContext : page.getWordsArray())
      {
         out.println(textWithContext.toString());
      }
   }
   
   public void endOfFile() throws IOException
   {
      // PdfToTextGrid will call this method each time extraction is
      // completed on a PDF file
      // you do not necessarily need to have behavior here
      
      // I am including a simple message to the console, which you can feel free
      // to delete
      System.err.println("Status update: completed extracting text from a PDF.");
   }
   
   public void endOfProject() throws IOException
   {
      // PdfToTextGrid will call this when it is finished extracting text
      // from all PDFs.
      // you do not necessarily need to have behavior here
      
      // of course, this may be a good place to close any streams and otherwise
      // wind down your program. Since the example implementation uses a
      // PrintStream object, we will close the stream here, notify the user that
      // the program completed without exception, and then call System.exit(0).
      out.close();
      JOptionPane.showMessageDialog(null,
            _YOUR_APPLICATION_NAME + " completed without an uncaught exception. Press OK to Exit.",
            _YOUR_APPLICATION_NAME + " Complete.", JOptionPane.INFORMATION_MESSAGE);
      System.exit(0);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      if(isInProgress)
      {
         return;
      }
      
      if(window.getNumberOfFiles() == 0)
      {
         JOptionPane.showMessageDialog(null, "No PDF files selected.", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else
      {
         final File[] files = window.getFilesArray();
         if(outputFC.showDialog(null, "Output File") != JFileChooser.APPROVE_OPTION)
         {
            return;
         }
         
         isInProgress = true;
         window.freeze();
         File output = outputFC.getSelectedFile();
         
         // if output has no extension, and there is not already a .txt file
         // with this name, add the .txt extension. This assumes that the user
         // would not intentionally have an output file with no extension, and
         // since PrintStream outputs text, it is only logical that the
         // extension ought to be .txt
         String name = output.getName();
         if(name.indexOf(".") == -1)
         {
            name = name + ".txt";
            final File parent = output.getParentFile();
            final File output2 = new File(parent, name);
            if(!output2.exists())
            {
               output = output2;
            }
         }
         
         // if the output file cannot be written to, or if the user does not
         // want to overwrite an existing file, go back and give the user the
         // chance to select a different output file.
         if(output.exists())
         {
            if(!output.canWrite())
            {
               JOptionPane.showMessageDialog(null, "Cannot write to: " + output.getName(), "File Access Error",
                     JOptionPane.ERROR_MESSAGE);
               isInProgress = false;
               window.unfreeze();
               return;
            }
            if(JOptionPane.showConfirmDialog(null, "Overwrite Existing File?", "Confirm Overwrite",
                  JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
            {
               isInProgress = false;
               window.unfreeze();
               return;
            }
         }
         
         try
         {
            out = new PrintStream(output);
            new Thread(new PdfToTextGrid(files, parser, transformer, this, null, false)).start();
         }
         catch(final IOException iox)
         {
            iox.printStackTrace();
            JOptionPane.showMessageDialog(null, _YOUR_APPLICATION_NAME + " experienced an error: " + iox.getMessage(),
                  "ERROR", JOptionPane.ERROR_MESSAGE);
         }
      }
   }
   
   private final TextPositionsParser parser;
   private final Transformer transformer;
   
   private final DragDropFilesList window;
   private final JButton button = new JButton("Execute");
   
   private final JFileChooser inputFC;
   private final JFileChooser outputFC;
   
   private final FileNameExtensionFilter _PDF_ONLY = new FileNameExtensionFilter("PDF (Paper Description Format) Files",
         "pdf");
   
   private boolean isInProgress = false;
   
   /*
    * The example implementation has the application's output going to a file
    * chosen by the user at runtime, through a PrintStream. Of course you can
    * feel free to change this. If you always want the output to go to the
    * console, or to a particular file, you can eliminate the JFileChooser
    * entirely. Or if you want the output to be binary data rather than text,
    * you could wrap a FileOutputStream in a BufferedOutputStream. If you want
    * to write output to a .ods file (a spreadsheet) you can use a CellWriterOds
    * in the ork.util package. Or perhaps you don't want your application
    * writing data to a file at all. Perhaps you want your application to invoke
    * the methods of some other object. This class is just meant as a starting
    * point so you can quickly get started on your own application.
    */
   private PrintStream out = null;
   
   private static final String _YOUR_APPLICATION_NAME = "Your Application"; // TODO
}
