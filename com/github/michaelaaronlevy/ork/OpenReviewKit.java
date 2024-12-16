package com.github.michaelaaronlevy.ork;

import java.awt.Dimension;
import java.awt.Font;
import java.io.File;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JOptionPane;

import com.github.michaelaaronlevy.ork.ModeSimple.ReaderMode;
import com.github.michaelaaronlevy.ork.ModeSimple.WriterType;
import com.github.michaelaaronlevy.ork.sorcerer.ATTAccountStatement;
import com.github.michaelaaronlevy.ork.sorcerer.ShadowViewer;
import com.github.michaelaaronlevy.ork.util.ConsoleGUI;
import com.github.michaelaaronlevy.ork.util.FontUtility;
import com.github.michaelaaronlevy.ork.util.OutputStreamNoLogger;
import com.github.michaelaaronlevy.ork.wordindex.FinderGUI;
import com.github.michaelaaronlevy.ork.wordindex.ModeIndex;

/**
 * The main class for the Open Review Kit (ORK) Project GUI. There can be
 * only one instance of OpenReviewKit. This object has a JFrame that can show
 * the text extractor GUI (to select files to process, the processing mode, and
 * the output file) or it can show any arbitrary JComponent (for example, the
 * searchable word index, or the "RangeFinder" tool).
 * 
 * <p>
 * The engine at the heart of OpenReviewKit is {@link PdfToTextGrid
 * PdfToTextGrid} which is itself built on top of the excellent PDFTextStripper
 * class that is a part of the PDFBox library.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class OpenReviewKit
{
   /**
    * create the Text Extraction GUI and populate it with standard modes.
    * 
    * @param args
    *           these have no effect.
    */
   public static void main(String[] args)
   {
      // TODO Auto-generated method stub
      // silence the Logger used by PDFBox
      // by replacing System.err
      final OutputStreamNoLogger filter = new OutputStreamNoLogger(System.err, null);
      System.setErr(filter.in);
      
      openOrk();
      
      final ExtractionGUI exGui = _ORK.exGui;
      
      exGui.addMode(new ModeSimple(WriterType.ODS, ReaderMode.DEFAULT));
      exGui.addMode(new ModeSimple(WriterType.ODS, ReaderMode.PHRASES));
      exGui.addMode(new ModeSimple(WriterType.ODS, ReaderMode.WORDS_PLUS));
      exGui.addMode(new ModeSimple(WriterType.ODS, ReaderMode.WORDS_MINUS));
      exGui.addMode(new ModeSimple(WriterType.CSV, ReaderMode.DEFAULT));
      exGui.addMode(new ModeSimple(WriterType.CSV, ReaderMode.PHRASES));
      exGui.addMode(new ModeSimple(WriterType.CSV, ReaderMode.WORDS_PLUS));
      exGui.addMode(new ModeSimple(WriterType.CSV, ReaderMode.WORDS_MINUS));
      exGui.addMode(new ModeSimple(WriterType.GRID, ReaderMode.DEFAULT));
      exGui.addMode(new ModeSimple(WriterType.GRID, ReaderMode.PHRASES));
      exGui.addMode(new ModeSimple(WriterType.GRID, ReaderMode.WORDS_PLUS));
      exGui.addMode(new ModeSimple(WriterType.GRID, ReaderMode.WORDS_MINUS));
      
      exGui.addMode(new ATTAccountStatement.Mode());
      exGui.addMode(new ShadowViewer.Mode(null, null));
      exGui.addMode(new ModeIndex(true));
   }
   
   /**
    * Control method to ensure that only a single instance of Ork can exist.
    * Will create the Ork if it does not yet exist.
    * 
    * @return the sole instance of Ork. It is created if there is none.
    */
   public static OpenReviewKit openOrk()
   {
      if(!_setup)
      {
         _setup = true;
         _ORK = new OpenReviewKit();
      }
      return _ORK;
   }
   
   /**
    * 
    * @return get the sole instance of Ork, or null if there is none
    */
   public static OpenReviewKit getOrk()
   {
      return _ORK;
   }
   
   private OpenReviewKit()
   {
      // reduce PDFBox logging messages
      String[] loggers = { "org.apache.pdfbox", "org.apache.pdfbox.util.PDFStreamEngine",
            "org.apache.pdfbox.pdmodel.font.PDSimpleFont" };
      for(String logger : loggers)
      {
         Logger logpdfengine = Logger.getLogger(logger);
         logpdfengine.setLevel(Level.SEVERE);
      }
      
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setTitle(OpenReviewKit._ORK_TITLE);
      
      final int screenWidth = FontUtility.getScreenWidth();
      final int screenHeight = FontUtility.getScreenHeight();
      
      dimensionMain = new Dimension((screenWidth * 3) / 4, (screenHeight * 9) / 10);
      dimensionJFC = new Dimension((dimensionMain.width * 9) / 10, (dimensionMain.height * 9) / 10);
      if(dimensionJFC.width > dimensionJFC.height)
      {
         dimensionJFC.width = dimensionJFC.height;
      }
      
      final int v = dimensionMain.height > (dimensionMain.width * 5) / 2 ? (dimensionMain.width * 5) / 2
            : dimensionMain.height;
      float bigSize = ((v + 280) / 50);
      float smallSize = ((v + 800) / 120);
      float mainSize = (int) ((bigSize + smallSize) / 2);
      bigSize = (int) bigSize;
      smallSize = (int) smallSize;
      
      float helpSize = (float) Math.sqrt(dimensionMain.height * dimensionMain.width / 2400.0);
      
      Font listFont = null;
      Font mainFont = null;
      Font helpFont = null;
      
      try
      {
         Font lib = Font.createFont(Font.PLAIN, getClass().getResourceAsStream("/ttf/LibertinusSerif-Regular.ttf"));
         listFont = lib.deriveFont(Font.PLAIN, smallSize);
         mainFont = lib.deriveFont(Font.PLAIN, mainSize);
         if(helpSize > mainFont.getSize())
         {
            helpFont = mainFont;
         }
         else
         {
            helpFont = lib.deriveFont(Font.PLAIN, helpSize);
         }
      }
      catch(final Exception exc)
      {
         listFont = new Font(Font.SERIF, Font.PLAIN, 12);
         mainFont = new Font(Font.SANS_SERIF, Font.PLAIN, 14);
         helpFont = new Font(Font.SERIF, Font.PLAIN, 14);
      }
      FontUtility.putFont("listFont", listFont);
      FontUtility.putFont("mainFont", mainFont);
      FontUtility.putFont("helpFont", helpFont);
      
      frame.setSize(dimensionMain);
      frame.setMinimumSize(dimensionMain);
      frame.setPreferredSize(dimensionMain);
      frame.setFont(mainFont);
      
      exGui = new ExtractionGUI(this);
   }
   
   /**
    * display the GUI for the searchable word index
    * 
    * @param initial
    *           a searchable word index file, or null if the GUI should invite
    *           the user to drag and drop a word index file in.
    */
   public void finder(final File initial)
   {
      final ConsoleGUI finder = new ConsoleGUI(true, true, null, FontUtility.getFont("listFont"),
            FontUtility.getFont("mainFont"));
      if(initial != null)
      {
         final File directory = initial.getParentFile();
         String name = initial.getName();
         name = name.substring(0, name.lastIndexOf('.'));
         FinderGUI c = null;
         try
         {
            c = new FinderGUI(directory, name, finder.stream, finder.stream, finder);
         }
         catch(final Exception iox)
         {
            return;
         }
         finder.addFileDropListener(c);
         new Thread(c).start();
      }
      
      updateView(finder.panel, true);
      if(initial == null)
      {
         finder.addFileDropListener(new FinderGUI.Loader(finder));
         finder.stream.println("Drag & Drop a Word Index file into this window to load that index.");
      }
   }
   
   /**
    * call this method to replace the contents of the frame. This is how you
    * transition from the main file processing GUI to some other GUI for a
    * custom application intended to run after processing is complete.
    * 
    * @param newView
    *           the Swing component representing the new program
    * @param resizable
    *           if the user should be permitted to resize the JFrame
    */
   public void updateView(final JComponent newView, final boolean resizable)
   {
      frame.setVisible(true);
      frame.setSize(dimensionMain);
      frame.setMinimumSize(dimensionMain);
      frame.setPreferredSize(dimensionMain);
      frame.setResizable(resizable);
      if(currentView != null)
      {
         frame.remove(currentView);
      }
      currentView = newView;
      frame.add(currentView);
      frame.setSize(dimensionMain);
      frame.pack();
      currentView.repaint();
   }
   
   /**
    * display the text extraction GUI
    */
   public void showExtractionGUI()
   {
      exGui.showThis();
   }
   
   /**
    * display a yes/no dialog window with the specified text. If the user
    * selects "yes" it will display the text extraction gui.
    * 
    * @param title
    *           the title for the dialog - can be null
    * @param message
    *           the message to display in the body of the dialog
    * @param messageType
    *           whether it should show up as an informational message, error
    *           message, etc. - use constants from JOptionPane
    * @return true if the user chose to display the text extraction GUI; or
    *         false if not.
    */
   public boolean continueOption(String title, final String message, int messageType)
   {
      title = title == null ? _ORK_LONG_NAME : title;
      final int option = JOptionPane.showConfirmDialog(frame, message, title, JOptionPane.YES_NO_OPTION, messageType);
      if(option == JOptionPane.YES_OPTION)
      {
         showExtractionGUI();
         return true;
      }
      else
      {
         return false;
      }
   }
   
   public final JFrame frame = new JFrame();
   public final Dimension dimensionMain;
   public final Dimension dimensionJFC;
   public final ExtractionGUI exGui;
   
   private JComponent currentView = null;
   
   public static final String _ORK_VERSION = "1.1.a";
   public static final String _ORK_SHORT_NAME = "ORK";
   public static final String _ORK_MEDIUM_NAME = "Open Review Kit";
   public static final String _ORK_LONG_NAME = "Open Review Kit, v" + _ORK_VERSION;
   public static final String _ORK_TITLE = _ORK_LONG_NAME
         + " - Michael Levy, Employment Lawyer - michael@levycivilrights.com";
   
   private static boolean _setup = false;
   private static OpenReviewKit _ORK = null;
   
}
