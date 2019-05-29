package com.github.michaelaaronlevy.ork.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;

/**
 * 
 * a simple class that allows files to be combined.
 * 
 * <p>
 * If "asText" is false, you can combine any files byte-for-byte. They will
 * simply be appended one after the other.
 * 
 * <p>
 * If "asText" is true, you can combine multiple text files (perhaps with
 * different byte encoding) end-after-end into a single text file (with just one
 * byte encoding).
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the FilesCombiner class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class FilesCombiner implements ActionListener
{
   public static void main(String[] args)
   {
      final Dimension d = new Dimension(400, 500);
      final JFrame frame = new JFrame();
      final FilesCombiner tfc = new FilesCombiner(frame, null, true);
      frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
      frame.setTitle("Text Files Combiner");
      frame.add(tfc.getComponent());
      frame.setSize(d);
      frame.setPreferredSize(d);
      frame.setMinimumSize(d);
      frame.pack();
      frame.setVisible(true);
   }
   
   public FilesCombiner(final Component context, final JFileChooser outputFileChooser, final boolean asText)
   {
      this.asText = asText;
      this.ofc = (outputFileChooser == null ? new JFileChooser() : outputFileChooser);
      window = new DragDropFilesList(context, null, null, null, null);
      button.addActionListener(this);
      window.mainPanel.add(button, BorderLayout.SOUTH);
   }
   
   public JPanel getComponent()
   {
      return window.mainPanel;
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      if(window.getNumberOfFiles() == 0)
      {
         JOptionPane.showMessageDialog(null, "No files selected to combine.", "Error", JOptionPane.ERROR_MESSAGE);
      }
      else if(window.getNumberOfFiles() == 1)
      {
         JOptionPane.showMessageDialog(null, "Cannot 'combine' a single file with itself.", "Error",
               JOptionPane.ERROR_MESSAGE);
      }
      else
      {
         final File[] files = window.getFilesArray();
         if(ofc.showDialog(null, "Output File") != JFileChooser.APPROVE_OPTION)
         {
            return;
         }
         final File output = ofc.getSelectedFile();
         try
         {
            if(asText)
            {
               writeCombinedText(files, output, true);
            }
            else
            {
               writeCombinedBytes(files, output, true);
            }
            JOptionPane.showMessageDialog(null, "FilesCombiner completed without error. Press OK to Exit.", "SUCCESS",
                  JOptionPane.INFORMATION_MESSAGE);
            isComplete = true;
            System.exit(0);
         }
         catch(final IOException iox)
         {
            iox.printStackTrace();
            JOptionPane.showMessageDialog(null, "FilesCombiner experienced an error: " + iox.getMessage(), "ERROR",
                  JOptionPane.ERROR_MESSAGE);
         }
      }
   }
   
   public static void writeCombinedText(final File[] files, final File output, final boolean verbose) throws IOException
   {
      if(verbose)
      {
         System.err.println("FilesCombiner::writeCombinedText method invoked for " + files.length + " files to: "
               + output.getAbsolutePath());
      }
      final BufferedWriter writer = new BufferedWriter(new FileWriter(output));
      for(final File f : files)
      {
         final BufferedReader reader = new BufferedReader(new FileReader(f));
         String line = reader.readLine();
         while(line != null)
         {
            writer.write(line);
            writer.newLine();
            line = reader.readLine();
         }
         reader.close();
         if(verbose)
         {
            System.err.println("FilesCombiner::writeCombinedText File complete " + f.getAbsolutePath());
         }
      }
      writer.close();
      if(verbose)
      {
         System.err.println("FilesCombiner::writeCombinedText Execution complete");
      }
   }
   
   public static void writeCombinedBytes(final File[] files, final File output, final boolean verbose)
         throws IOException
   {
      if(verbose)
      {
         System.err.println("FilesCombiner::writeCombinedBytes method invoked for " + files.length + " files to: "
               + output.getAbsolutePath());
      }
      final byte[] b = new byte[_MAX_BYTES];
      final BufferedOutputStream writer = new BufferedOutputStream(new FileOutputStream(output));
      for(final File f : files)
      {
         final BufferedInputStream reader = new BufferedInputStream(new FileInputStream(f));
         while(reader.available() > 0)
         {
            final int available = reader.available();
            if(available >= b.length)
            {
               reader.read(b);
               writer.write(b);
            }
            else
            {
               reader.read(b, 0, available);
               writer.write(b, 0, available);
            }
         }
         reader.close();
         if(verbose)
         {
            System.err.println("FilesCombiner::writeCombinedBytes File complete " + f.getAbsolutePath());
         }
      }
      writer.close();
      if(verbose)
      {
         System.err.println("FilesCombiner::writeCombinedBytes Execution complete");
      }
   }
   
   private final DragDropFilesList window;
   private final JButton button = new JButton("Execute");
   private final JFileChooser ofc;
   private final boolean asText;
   
   private boolean isComplete = false;
   
   private static final int _MAX_BYTES = 1024;
}
