package com.github.michaelaaronlevy.ork;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;
import javax.swing.filechooser.FileNameExtensionFilter;

import com.github.michaelaaronlevy.ork.util.DragDropFilesList;
import com.github.michaelaaronlevy.ork.util.FontUtility;
import com.github.michaelaaronlevy.ork.util.RunnableWithStatus;
import com.github.michaelaaronlevy.ork.wordindex.ModeIndex;

/**
 * This class is the graphical user interface for selecting the files to process
 * (including with drag and drop, and being able to re-order the files in the
 * list), and to select the type of processing that will occur. It can also be
 * used to launch the searchable word index.
 * 
 * <p>
 * This relies on the {@link ExtractionMode ExtractionMode} interface.
 * 
 * <p>
 * (drag and drop support is thanks to
 * {@link com.github.michaelaaronlevy.ork.util.FileDrop FileDrop} by Robert
 * Harder)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExtractionGUI
{
   /**
    * the constructor is only called by Ork's constructor; so there can only
    * ever be one of these objects.
    * 
    * @param ork
    */
   ExtractionGUI(final OpenReviewKit ork)
   {
      final Font mainFont = FontUtility.getFont("mainFont");
      final Font listFont = FontUtility.getFont("listFont");
      final Font helpFont = FontUtility.getFont("helpFont");
      
      if(ork.dimensionMain.width / mainFont.getSize() < 36.0)
      {
         helpButton.setText(_HELP_MESSAGE_SHORT);
         runButton.setText(_RUN_MESSAGE_SHORT);
      }
      
      jfcAdd = new JFileChooser();
      jfcAdd.setPreferredSize(ork.dimensionJFC);
      jfcAdd.setDialogTitle(_FILE_CHOOSER_ADD_MESSAGE);
      jfcAdd.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfcAdd.addChoosableFileFilter(_PDF_ONLY);
      jfcAdd.setFileFilter(_PDF_ONLY);
      jfcAdd.setMultiSelectionEnabled(true);
      
      jfcOut = new JFileChooser();
      jfcOut.setPreferredSize(ork.dimensionJFC);
      jfcOut.setDialogTitle(_FILE_CHOOSER_OUT_MESSAGE);
      jfcOut.setFileSelectionMode(JFileChooser.FILES_ONLY);
      jfcOut.setMultiSelectionEnabled(false);
      
      ddfList = new DragDropFilesList(ork.frame, jfcAdd, null, mainFont, listFont);
      
      final Listener listener = new Listener();
      
      runButton.addActionListener(listener);
      helpButton.addActionListener(listener);
      returnButton.addActionListener(listener);
      
      modeButton.addMouseListener(listener);
      
      final JPanel runButtons = new JPanel();
      runButtons.add(helpButton);
      runButtons.add(modeButton);
      runButtons.add(runButton);
      
      fileView = ddfList.mainPanel;
      fileView.add(runButtons, BorderLayout.SOUTH);
      
      statusView.setLayout(new BoxLayout(statusView, BoxLayout.Y_AXIS));
      statusView.add(run1);
      statusView.add(run2);
      
      if(ork.dimensionMain.height < 600 || (ork.dimensionMain.height < 800 && ork.dimensionMain.width < 600))
      {
         help.setText(_HELP_SMALL);
      }
      helpView.setLayout(new BorderLayout());
      helpView.add(buffer1, BorderLayout.WEST);
      helpView.add(help, BorderLayout.CENTER);
      helpView.add(buffer2, BorderLayout.EAST);
      helpView.add(returnButton, BorderLayout.SOUTH);
      returnButton.addActionListener(listener);
      
      FontUtility.setFonts(runButtons.getComponents(), mainFont);
      FontUtility.setFonts(helpView, helpFont);
      FontUtility.setFonts(helpView.getComponents(), helpFont);
      FontUtility.setFonts(statusView.getComponents(), mainFont);
      jfcAdd.setFont(mainFont);
      jfcOut.setFont(mainFont);
      FontUtility.setFonts(jfcAdd.getComponents(), listFont);
      FontUtility.setFonts(jfcOut.getComponents(), listFont);
      
      ork.updateView(fileView, true);
      
      fileView.setMinimumSize(ork.dimensionMain);
      fileView.setPreferredSize(ork.dimensionMain);
      helpView.setMinimumSize(ork.dimensionMain);
      helpView.setPreferredSize(ork.dimensionMain);
      statusView.setMinimumSize(ork.dimensionMain);
      statusView.setPreferredSize(ork.dimensionMain);
      
      modeButton.setPreferredSize(modeButton.getSize());
      setMode(0);
   }
   
   /**
    * 
    * @param mode
    *           if this mode is not already in the list, it will be added to the
    *           list that the user can cycle through
    */
   public void addMode(final ExtractionMode mode)
   {
      if(mode != null && !modes.contains(mode))
      {
         modes.add(mode);
      }
      if(this.mode == -1)
      {
         setMode(0);
      }
   }
   
   /**
    * 
    * @return a list of the modes available for user selection
    */
   public ExtractionMode[] getModes()
   {
      final ExtractionMode[] r = new ExtractionMode[modes.size()];
      modes.toArray(r);
      return r;
   }
   
   public void removeMode(final ExtractionMode mode)
   {
      modes.remove(mode);
   }
   
   private void setMode(final int newMode)
   {
      if(modes.isEmpty() || newMode < 0)
      {
         mode = -1;
         modeButton.setText("");
      }
      else
      {
         mode = newMode;
         modeButton.setText(modes.get(mode).buttonText());
      }
   }
   
   public void showThis()
   {
      OpenReviewKit.openOrk().updateView(this.fileView, true);
   }
   
   private final JPanel fileView;
   private final JPanel statusView = new JPanel();
   private final JPanel helpView = new JPanel();
   
   private final DragDropFilesList ddfList;
   
   private final JButton modeButton = new JButton("XXXXXXXXXXXXXXXXXXXX");
   private final JButton runButton = new JButton(_RUN_MESSAGE);
   private final JButton helpButton = new JButton(_HELP_MESSAGE);
   private final JLabel buffer1 = new JLabel("\u2003\u2003\u2003");
   private final JLabel buffer2 = new JLabel("\u2003\u2003\u2003");
   private final JFileChooser jfcAdd;
   private final JFileChooser jfcOut;
   
   private final JLabel help = new JLabel(_HELP);
   private final JButton returnButton = new JButton(_RETURN);
   
   private final JLabel run1 = new JLabel("Waiting for User to Select Course of Action via the GUI.");
   private final JLabel run2 = new JLabel("Waiting for User Input.");
   
   private RunnableWithStatus ripper = null;
   
   private final FileNameExtensionFilter _PDF_ONLY = new FileNameExtensionFilter("PDF (Paper Description Format) Files",
         "pdf");
   
   private final ArrayList<ExtractionMode> modes = new ArrayList<ExtractionMode>();
   private int mode = 0;
   
   private static final String _FILE_CHOOSER_ADD_MESSAGE = "Select PDF(s) to Rip.";
   private static final String _FILE_CHOOSER_OUT_MESSAGE = "Select Output File.";
   private static final String _RUN_MESSAGE = "Execute";
   private static final String _RUN_MESSAGE_SHORT = "Run";
   private static final String _HELP_MESSAGE = "Help/About";
   private static final String _HELP_MESSAGE_SHORT = "Help";
  
   private static final String _HELP = "<html><b>Using Open Review Kit / PdfToTextGrid:</b><ul><li>Select the PDF(s) to extract.  (Extracting text does not change the PDFs.)  Drag & drop files into the list, or use the [ + ] button to add PDFs.</li><li>You can remove PDFs from the list with the [ - ] button, or re-order them with the [ \u2191 ] and [ \u2193 ] buttons.</li><li>Choose the output format (.ods and .csv are both spreadsheet formats that can be opened with Microsoft Excel, Apache OpenOffice, or LibreOffice).</li><li>Choose whether the content is arranged as phrases or broken up by word (“default” or “phrases” is recommended).</li><li>Press “Execute” to begin extracting. You will be asked to choose an output file. If this file exists, you will be asked whether to overwrite it.</ul><br><p><b>About Open Review Kit:</b><br>\u2003\u2003This tool was designed to address a very specific problem: during litigation, data is commonly provided in PDF format (or in paper, that you can scan into PDF format). The data in the PDFs may be critical to your case, but you can only “access” it by eyeballing it, or by copying it out in an awkward way, such as “selecting” everything on a page, copying it, and pasting it into a Word document. Where the PDF has columns of data, usually the Word document won’t have the data lined up in neat columns: the data will be a jumbled mess. Un-jumbling it can take hours. Typing the data into a spreadsheet by hand can take hours.<br>\u2003<p>\u2003\u2003Open Review Kit pulls the content of the PDFs out and saves contextual information (the location on the page of each phrase/word).  With this contextual information, you can easily, quickly, and systematically determine which row/column each datum belongs in – so you can quickly and reliably create a spreadsheet with the data organized the same way it was in the PDF (or, more to the point: the same way it was organized in the document that was used to generate the PDF).<br>\u2003<p>\u2003\u2003Open Review Kit can only “see” text that is recognized by Acrobat. If your document is a scan of a printed page, Acrobat will not see the words unless Optical Character Recognition (“OCR”) is performed. The OCR process is rarely perfect and often results in errors. But I have used Open Review Kit successfully when OCR is high quality.<br>\u2003<p><b>About the Author:</b><p>\u2003\u2003Mr. Levy is an employment lawyer in Big Bear.  He represents employees against abusive employers in claims for unpaid or underpaid wages, harassment, discrimination, wrongful termination, and other workplace grievances.  For questions or technical support, contact michael@levycivilrights.com.  Please include “Open Review Kit” in the subject line of your email.</html>";
   private static final String _HELP_SMALL = "<html><b>Using Open Review Kit / PdfToTextGrid:</b><p>\u2003Select the PDF(s) to extract. (Extracting text does not change the PDFs.) Drag & drop files or the [ + ] button to add PDFs. Choose the output format (.ods or .csv are both spreadsheet formats that can be opened with Microsoft Excel, Apache OpenOffice, or LibreOffice). Choose whether the content is arranged as phrases or words (“phrases” is recommended). Press “Execute” (“Run”) to begin extracting. You will be asked to choose an output file. If this file exists, you will be asked whether to overwrite it.<br>\u2003<p><b>About Open Review Kit:</b><br>\u2003\u2003This tool was designed to address a very specific problem: during litigation, data is commonly provided in PDF format (or in paper, that you can scan into PDF format). The data in the PDFs may be critical to your case, but you can only “access” it by eyeballing it, or by copying it out in an awkward way, such as “selecting” everything on a page, copying it, and pasting it into a Word document. Where the PDF has columns of data, usually the Word document won’t have the data lined up in neat columns: the data will be a jumbled mess. Un-jumbling it can take hours. Typing the data into a spreadsheet by hand can take hours.<br>\u2003<p>\u2003\u2003Open Review Kit pulls the content of the PDFs out and saves contextual information (the location on the page of each phrase/word) so you can organize the data.<br>\u2003<p><b>About the Author:</b><p>\u2003\u2003Mr. Levy is an employment lawyer in Big Bear. He represents employees against abusive employers. For questions or technical support, contact michael@levycivilrights.com with “Open Review Kit” in the subject line.</html>";
   
   private static final String _RETURN = "Return";
   
   /**
    * this internal class exists so the listener methods can be private
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   private class Listener implements ActionListener, MouseListener
   {
      public void actionPerformed(final ActionEvent aev)
      {
         final OpenReviewKit ork = OpenReviewKit.openOrk();
         
         final Object source = aev.getSource();
         if(source == returnButton)
         {
            ork.updateView(fileView, true);
         }
         else if(source == runButton)
         {
            if(mode == -1)
            {
               JOptionPane.showMessageDialog(ork.frame, "No Mode Selected.", "Error", JOptionPane.ERROR_MESSAGE);
               return;
            }
            
            if(ddfList.getNumberOfFiles() == 0)
            {
               if(modes.get(mode) instanceof ModeIndex)
               {
                  ork.finder(null);
               }
               else
               {
                  JOptionPane.showMessageDialog(ork.frame, "No PDF files selected to rip.", "Error",
                        JOptionPane.ERROR_MESSAGE);
               }
            }
            else
            {
               File targetOut = null;
               final String extension = modes.get(mode).getExtension();
               if(extension != null)
               {
                  if(jfcOut.showDialog(ork.frame, "Output File") != JFileChooser.APPROVE_OPTION)
                  {
                     return;
                  }
                  targetOut = jfcOut.getSelectedFile();
                  if(targetOut == null)
                  {
                     return;
                  }
                  final int dot = targetOut.getName().indexOf(".");
                  if(dot == -1)
                  {
                     targetOut = new File(targetOut.getParentFile(), targetOut.getName() + extension);
                  }
                  if(targetOut.exists())
                  {
                     if(!targetOut.canWrite())
                     {
                        JOptionPane.showMessageDialog(ork.frame, "Cannot write to: " + targetOut.getName(),
                              "File Access Error", JOptionPane.ERROR_MESSAGE);
                        return;
                     }
                     if(JOptionPane.showConfirmDialog(ork.frame, "Overwrite Existing File?", "Confirm Overwrite",
                           JOptionPane.YES_NO_OPTION, JOptionPane.QUESTION_MESSAGE) != JOptionPane.YES_OPTION)
                     {
                        return;
                     }
                  }
               }
               
               ork.updateView(statusView, true);
               final File[] files = ddfList.getFilesArray();
               
               final ExtractionMode m = modes.get(mode);
               
               try
               {
                  ripper = new PdfToTextGrid(files, m.getParser(), m.getTransformer(), m.getConsumer(targetOut), m,
                        false);
               }
               catch(final IOException iox)
               {
                  iox.printStackTrace();
               }
               if(ripper != null)
               {
                  new Thread(ripper).start();
                  final JPanel view = ripper.getStatus().getComponent();
                  FontUtility.setFonts(view, FontUtility.getFont("mainFont"));
                  ork.updateView(view, true);
               }
            }
         }
         else if(source == helpButton)
         {
            ork.updateView(helpView, false);
         }
      }
      
      public void mouseClicked(final MouseEvent mev)
      {
         if(mev.getSource() == modeButton)
         {
            final boolean left = SwingUtilities.isLeftMouseButton(mev);
            final boolean right = SwingUtilities.isRightMouseButton(mev);
            
            if(left)
            {
               mode++;
            }
            else if(right)
            {
               mode--;
            }
            
            if(mode == modes.size())
            {
               mode = 0;
            }
            else if(mode == -1)
            {
               mode = modes.size() - 1;
            }
            
            setMode(mode);
         }
      }
      
      public void mousePressed(final MouseEvent mev)
      {
         return;
      }
      
      public void mouseReleased(final MouseEvent mev)
      {
         return;
      }
      
      public void mouseEntered(final MouseEvent mev)
      {
         return;
      }
      
      public void mouseExited(final MouseEvent mev)
      {
         return;
      }
   }
}
