package com.github.michaelaaronlevy.ork.util;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.Iterator;

import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;

/**
 * generic class that can display console output (System.out and/or System.err,
 * or another PrintStream) and also provide text input, one line at a time
 * (which can substitute for System.in.readLine()) so you can have the console
 * experience within a GUI window.
 * 
 * <P>
 * it also supports drag &amp; drop of files into the console display part of
 * the window through the {link:com.github.michaelaaronlevy.ork.util.FileDrop
 * FileDrop} class.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ConsoleGUI class) AND I AM PLACING IT
 * IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ConsoleGUI implements ActionListener, Iterator<String>
{
   /**
    * 
    * @param addSystemOut
    *           if true, this will call System.setOut to direct all such output
    *           to the display
    * @param addSystemErr
    *           if true, this will call System.setErr to direct all such output
    *           to the display
    */
   public ConsoleGUI(final boolean addSystemOut, final boolean addSystemErr, final OutputStream reRoute,
         final Font consoleFont, final Font inputFont)
   {
      area.setEditable(false);
      area.setFont(consoleFont);
      TextAreaOutputStream taos = new TextAreaOutputStream(area, 60);
      
      stream = new PrintStream(taos);
      if(addSystemOut)
      {
         System.setOut(stream);
      }
      if(addSystemErr)
      {
         System.setErr(stream);
      }
      
      field.addActionListener(this);
      field.setFont(inputFont);
      
      panel.setLayout(new BorderLayout());
      panel.add(new JScrollPane(area), BorderLayout.CENTER);
      panel.add(field, BorderLayout.SOUTH);
   }
   
   public void actionPerformed(ActionEvent arg0)
   {
      synchronized(this)
      {
         nextLine = field.getText();
         notify();
      }
   }
   
   public boolean hasNext()
   {
      return true;
   }
   
   public String next()
   {
      synchronized(this)
      {
         while(nextLine == null)
         {
            try
            {
               wait();
            }
            catch(final InterruptedException iex)
            {
               // do nothing
            }
         }
      }
      final String r = nextLine;
      nextLine = null;
      field.setText("");
      return r;
   }
   
   public void addFileDropListener(final FileDrop.Listener target)
   {
      new FileDrop(null, area, false, target);
   }
   
   public final PrintStream stream;
   public final JPanel panel = new JPanel();
   private final JTextArea area = new JTextArea();
   private final JTextField field = new JTextField();
   private String nextLine = null;
}
