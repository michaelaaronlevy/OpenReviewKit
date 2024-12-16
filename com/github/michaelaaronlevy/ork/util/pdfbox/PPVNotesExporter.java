package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

import javax.swing.JOptionPane;
import javax.swing.JTextField;

import com.github.michaelaaronlevy.ork.OpenReviewKit;

/**
 * This class saves notes to the file. Whatever arbitrary notes the user types
 * are saved, along with information about the page that the user was looking at
 * when the note was typed. So if you see an interesting page, you can make a
 * note, and then later you can come back and find that page again. It is part
 * of the ProjectPdfViewer, hence the class name starts with "PPV"
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVNotesExporter class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class PPVNotesExporter implements ActionListener, PPVElement
{
   public PPVNotesExporter(final ProjectPDFs project)
   {
      this.project = project;
      this.field = new JTextField();
      field.addActionListener(this);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      writeNext();
   }
   
   private synchronized void writeNext()
   {
      final String note = field.getText();
      if(note == null)
      {
         field.setText("");
         return;
      }
      if(writer == null)
      {
         setWriter();
      }
      
      if(note.length() == 0 || pageBeingViewed == null || pageBeingViewed.projectPage == pageOfLastNote)
      {
         // do nothing here
         // (which is to say: print this line without making a new header)
      }
      else
      {
         pageOfLastNote = pageBeingViewed.projectPage;
         final String pdfName = pageBeingViewed.getPdfName();
         messages.add("");
         starMessage(
               "Project Page #" + pageBeingViewed.projectPage + ", Pdf #" + pageBeingViewed.pdfIndex + " (" + pdfName
                     + ")",
               pageBeingViewed.pdfPage + " of " + pageBeingViewed.getPdfPageCount() + " at "
                     + _SDF.format(new Date(System.currentTimeMillis())));
      }
      
      messages.add(note);
      field.setText("");
      
      tryWrite();
   }
   
   private void tryWrite()
   {
      int tryAgain = JOptionPane.NO_OPTION;
      try
      {
         while(!messages.isEmpty())
         {
            writer.write(messages.get(0));
            messages.remove(0);
            writer.newLine();
         }
         writer.flush();
         return;
      }
      catch(final IOException iox)
      {
         final String m = "Error: Attempt to write note failed.  Try again?  First please make sure the notes file is closed.";
         System.err.println(m);
         tryAgain = JOptionPane.showConfirmDialog(null, m, "placeholder title", JOptionPane.YES_NO_OPTION,
               JOptionPane.WARNING_MESSAGE);
         if(tryAgain == JOptionPane.NO_OPTION)
         {
            System.err.println(
                  "If you enter another note, Open Review Kit will attempt to save these notes to a new file.");
         }
      }
      if(tryAgain == JOptionPane.YES_OPTION)
      {
         tryWrite();
      }
   }
   
   private synchronized void setWriter()
   {
      if(writer != null)
      {
         return;
      }
      int exceptionsCount = 0;
      String pre = _NOTES + project.getProjectName();
      File f;
      for(int i = 1; i <= _MAX_FILES; i++)
      {
         f = new File(project.getProjectDirectory(), pre + i + _EXT);
         if(!f.exists())
         {
            try
            {
               writer = new BufferedWriter(new FileWriter(f));
               starMessage(OpenReviewKit._ORK_LONG_NAME + " Notes for Project: " + project.getProjectName() + " at "
                     + _SDF.format(new Date(System.currentTimeMillis())), null);
               return;
            }
            catch(final FileNotFoundException fnfx)
            {
               pre = _NOTES;
            }
            catch(final IOException iox)
            {
               if(exceptionsCount++ > _MAX_ERRORS)
               {
                  System.err.println("Error Trying to Create Notes File - Failed " + _MAX_ERRORS
                        + " times.  Notes are not being saved.");
                  return;
               }
            }
         }
      }
      System.err.println("Error: " + _MAX_FILES
            + " files already exist.  Cannot create new notes file.  Notes are not being saved.");
   }
   
   private void starMessage(final String one, final String two)
   {
      int len = one.length();
      if(two != null && two.length() > len)
      {
         len = two.length();
      }
      len += 5;
      if(len > _MAX_STARS)
      {
         len = _MAX_STARS;
      }
      if(_STARS == null || _STARS.length() < len)
      {
         final char[] c = new char[_MAX_STARS];
         Arrays.fill(c, _STAR);
         _STARS = String.valueOf(c);
      }
      messages.add(_STARS.substring(_STARS.length() - len));
      messages.add(_PREFIX + one);
      if(two != null)
      {
         messages.add(_PREFIX + two);
      }
   }
   
   public void update(final PageReference ref)
   {
      pageBeingViewed = ref;
      if(init)
      {
         init = false;
         setWriter();
      }
   }
   
   public JTextField getComponent()
   {
      return field;
   }
   
   public synchronized void close()
   {
      if(writer != null)
      {
         messages.add("");
         starMessage("Session ended at " + _SDF.format(new Date(System.currentTimeMillis())), null);
         tryWrite();
         try
         {
            writer.close();
         }
         catch(final IOException iox)
         {
            // do nothing
         }
         writer = null;
      }
   }
   
   public void updateView(PageReference newRef)
   {
      pageBeingViewed = newRef;
   }
   
   public void setResults(int[] results)
   {
      throw new RuntimeException("Unsupported Operation for this Class");
   }
   
   private final JTextField field;
   private final ProjectPDFs project;
   private final ArrayList<String> messages = new ArrayList<String>();
   
   private BufferedWriter writer = null;
   private int pageOfLastNote = -1;
   private PageReference pageBeingViewed = null;
   private boolean init = true;
   
   private static final SimpleDateFormat _SDF = new SimpleDateFormat("yyyy-MM-dd 'at' HH:mm:ss z");
   private static final String _NOTES = "notes_";
   private static final String _EXT = ".txt";
   private static final int _MAX_FILES = 9999;
   private static final int _MAX_ERRORS = 99;
   private static final int _MAX_STARS = 120;
   private static final char _STAR = '*';
   private static final String _PREFIX = "*  ";
   
   private static String _STARS = null;
}
