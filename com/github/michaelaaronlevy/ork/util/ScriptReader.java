package com.github.michaelaaronlevy.ork.util;

import java.io.BufferedReader;
import java.io.IOException;
import java.util.ArrayList;

/**
 * convenience file for reading script files and similar text files. Takes a
 * BufferedReader as input. Output is: strips out comments (all characters after
 * "//") and returns the lines that are not empty.
 *
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ScriptReader class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ScriptReader implements RunnableWithStatus
{
   /**
    * 
    * @param r
    * @param stringLiterals
    *           if true, will ignore a comment marker ("//") that appears inside
    *           of an apparent string literal.
    */
   public ScriptReader(final BufferedReader r, final boolean stringLiterals)
   {
      this.stringLiterals = stringLiterals;
      source = r;
      status = new Status("ScriptReader");
      loadNextLine();
   }
   
   /**
    * 
    * @return the next line, or null if finished
    */
   public String nextLine()
   {
      if(status.reporter.isFinished())
      {
         return null;
      }
      else if(nextLine == null)
      {
         status.declareEnd();
         return null;
      }
      else
      {
         final String r = nextLine;
         loadNextLine();
         return r;
      }
   }
   
   private void loadNextLine()
   {
      nextLine = null;
      String line = null;
      try
      {
         line = source.readLine();
      }
      catch(final IOException iox)
      {
         status.declareFatalError(iox.getMessage());
         return;
      }
      if(line == null)
      {
         try
         {
            source.close();
         }
         catch(final IOException iox)
         {
            // do nothing - ignore this type of error
         }
         return;
      }
      line = line.trim();
      if(line.length() == 0 || line.startsWith(_COMMENT))
      {
         loadNextLine();
         return;
      }
      
      nextLine = line;
      if(line.indexOf(_COMMENT) == -1) // there is no comment, so we are done
      {
         return;
      }
      else if(!stringLiterals) // find the first comment marker and trim
      {
         line = line.substring(line.indexOf(_COMMENT)).trim();
         return;
      }
      else // look for the first comment marker that does not begin inside a
           // string literal
      {
         final int max = line.length() - 1;
         boolean inQuote = false;
         for(int i = 0; i < max; i++)
         {
            final char c0 = line.charAt(i);
            final char c1 = line.charAt(i + 1);
            if(c0 == _QUOTE)
            {
               if(stringLiterals)
               {
                  if(inQuote)
                  {
                     if(c1 == _QUOTE)
                     {
                        i++; // skip the next character because this is a
                             // literal
                     }
                     else
                     {
                        inQuote = false;
                     }
                  }
                  else
                  {
                     inQuote = true;
                  }
               }
            }
            else if(c0 == _SLASH && !inQuote && c1 == _SLASH)
            {
               line = line.substring(0, i).trim();
               nextLine = line;
               return;
            }
            else if(c0 == _BACKSLASH)
            {
               if(c1 == _BACKSLASH)
               {
                  i++; // skip the next character
               }
               else if(c1 == _QUOTE)
               {
                  i++;
                  
                  /*
                   * skip the next character because this is a literal. note:
                   * this will treat this as a literal whether or not we are
                   * inside a quote meaning that the double quotes character at
                   * c1 will NOT open a new quote
                   */
               }
            }
         }
      }
   }
   
   public boolean hasNext()
   {
      if(nextLine == null)
      {
         status.declareEnd();
         return false;
      }
      else
      {
         return true;
      }
   }
   
   public void run()
   {
      if(output == null)
      {
         output = toStringArray();
      }
   }
   
   /**
    * 
    * @return the remaining lines, as a String[] array, or null if there was an
    *         error.
    */
   public String[] toStringArray()
   {
      if(status.reporter.hasError())
      {
         return null;
      }
      final ArrayList<String> a = new ArrayList<String>();
      while(nextLine != null)
      {
         a.add(nextLine);
         loadNextLine();
      }
      if(status.reporter.hasError())
      {
         return null;
      }
      final String[] returner = new String[a.size()];
      a.toArray(returner);
      return returner;
   }
   
   public StatusReporter getStatus()
   {
      return status.reporter;
   }
   
   public final boolean stringLiterals;
   public String[] output = null;
   
   private final BufferedReader source;
   private String nextLine = null;
   
   private final Status status;
   
   private static final char _SLASH = '/';
   private static final String _COMMENT = "//";
   private static final char _QUOTE = '\"';
   private static final char _BACKSLASH = '\\';
}
