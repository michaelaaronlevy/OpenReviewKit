package com.github.michaelaaronlevy.ork.util;

import java.awt.EventQueue;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.LinkedList;
import java.util.List;

import javax.swing.JTextArea;

/**
 * used to re-route System.out/System.err to a GUI element
 * 
 * <p>
 * published to StackOverflow by Cœur <br>
 * https://stackoverflow.com/questions/342990/create-java-console-inside-a-gui-panel
 * <br>
 * https://creativecommons.org/licenses/by-sa/4.0/
 * 
 * <p>
 * I (michaelaaronlevy@gmail.com) edited the run() method to add line breaks
 * automatically when lines are too long.
 * 
 * <p>
 * I am releasing this (the TextAreaOutputStream class) under the same creative
 * commons license <br>
 * https://creativecommons.org/licenses/by-sa/4.0/
 * 
 */
public class TextAreaOutputStream extends OutputStream
{
   // *************************************************************************************************
   // INSTANCE MEMBERS
   // *************************************************************************************************
   
   private byte[] oneByte; // array for write(int val);
   private Appender appender; // most recent action
   
   public TextAreaOutputStream(JTextArea txtara)
   {
      this(txtara, 1000);
   }
   
   public TextAreaOutputStream(JTextArea txtara, int maxlin)
   {
      if(maxlin < 1)
      {
         throw new IllegalArgumentException(
               "TextAreaOutputStream maximum lines must be positive (value=" + maxlin + ")");
      }
      oneByte = new byte[1];
      appender = new Appender(txtara, maxlin);
   }
   
   /** Clear the current console text area. */
   public synchronized void clear()
   {
      if(appender != null)
      {
         appender.clear();
      }
   }
   
   public synchronized void close()
   {
      appender = null;
   }
   
   public synchronized void flush()
   {
   }
   
   public synchronized void write(int val)
   {
      oneByte[0] = (byte) val;
      write(oneByte, 0, 1);
   }
   
   public synchronized void write(byte[] ba)
   {
      write(ba, 0, ba.length);
   }
   
   public synchronized void write(byte[] ba, int str, int len)
   {
      if(appender != null)
      {
         appender.append(bytesToString(ba, str, len));
      }
   }
   
   static private String bytesToString(byte[] ba, int str, int len)
   {
      try
      {
         return new String(ba, str, len, "UTF-8");
      }
      catch(UnsupportedEncodingException thr)
      {
         return new String(ba, str, len);
      } // all JVMs are required to support UTF-8
   }
   
   // *************************************************************************************************
   // STATIC MEMBERS
   // *************************************************************************************************
   
   static class Appender implements Runnable
   {
      private final JTextArea textArea;
      private final int maxLines; // maximum lines allowed in text area
      private final LinkedList<Integer> lengths; // length of lines within text
                                                 // area
      private final List<String> values; // values waiting to be appended
      
      private int curLength; // length of current line
      private boolean clear;
      private boolean queue;
      
      Appender(JTextArea txtara, int maxlin)
      {
         textArea = txtara;
         maxLines = maxlin;
         lengths = new LinkedList<Integer>();
         values = new ArrayList<String>();
         
         curLength = 0;
         clear = false;
         queue = true;
      }
      
      synchronized void append(String val)
      {
         values.add(val);
         if(queue)
         {
            queue = false;
            EventQueue.invokeLater(this);
         }
      }
      
      synchronized void clear()
      {
         clear = true;
         curLength = 0;
         lengths.clear();
         values.clear();
         if(queue)
         {
            queue = false;
            EventQueue.invokeLater(this);
         }
      }
      
      // MUST BE THE ONLY METHOD THAT TOUCHES textArea!
      public synchronized void run()
      {
         if(clear)
         {
            textArea.setText("");
         }
         for(String val : values)
         {
            while(val != null)
            {
               String toPut = null;
               if(curLength + val.length() > _MAX_LINE_LENGTH)
               {
                  int index = _MAX_LINE_LENGTH - curLength;
                  for(int j = 0; j > -10; j--)
                  {
                     int k = index + j;
                     if(k < 0)
                     {
                        break;
                     }
                     if(Character.isWhitespace(val.charAt(k)))
                     {
                        index = k;
                        break;
                     }
                  }
                  
                  toPut = val.substring(0, index);
                  if(!toPut.endsWith(EOL1) && !toPut.endsWith(EOL2))
                  {
                     toPut = toPut + EOL1;
                  }
                  val = val.substring(index);
               }
               else
               {
                  toPut = val;
                  val = null;
               }
               
               curLength += toPut.length();
               if(toPut.endsWith(EOL1) || toPut.endsWith(EOL2))
               {
                  if(lengths.size() >= maxLines)
                  {
                     textArea.replaceRange("", 0, lengths.removeFirst());
                  }
                  lengths.addLast(curLength);
                  curLength = 0;
               }
               textArea.append(toPut);
            }
         }
         values.clear();
         clear = false;
         queue = true;
      }
      
      static private final String EOL1 = "\n";
      static private final String EOL2 = System.getProperty("line.separator", EOL1);
      
      private static final int _MAX_LINE_LENGTH = 280;
   }
   
} /* END PUBLIC CLASS */
