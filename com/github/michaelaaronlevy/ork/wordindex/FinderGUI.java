package com.github.michaelaaronlevy.ork.wordindex;

import java.io.BufferedInputStream;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Scanner;
import java.util.TreeMap;
import java.util.TreeSet;

import javax.swing.JFrame;

import com.github.michaelaaronlevy.ork.OpenReviewKit;
import com.github.michaelaaronlevy.ork.util.AscendingStack;
import com.github.michaelaaronlevy.ork.util.ConsoleGUI;
import com.github.michaelaaronlevy.ork.util.FileDrop;
import com.github.michaelaaronlevy.ork.util.FontUtility;
import com.github.michaelaaronlevy.ork.util.Grid;
import com.github.michaelaaronlevy.ork.util.TokenParser;
import com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPDFs;
import com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPdfViewer;

/**
 * This is the user interface for the searchable word index. It supports both
 * the console as well as using a
 * {@link com.github.michaelaaronlevy.ork.util.ConsoleGUI ConsoleGUI} (a utility
 * class that more or less displays console output in a Swing component)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class FinderGUI implements Context, Runnable, FileDrop.Listener
{
   /**
    * constructor for a console-based interface (no GUI)
    * 
    * @param directory
    *           the directory containing the searchable word index files
    * @param name
    *           the name of the searchable word index (not including the file
    *           extension or the period preceding the file extension)
    * @throws IOException
    */
   public FinderGUI(final File directory, final String name) throws IOException
   {
      this(directory, name, System.out, System.err, new Scanner(System.in));
   }
   
   /**
    * constructor intended for use with
    * {@link com.github.michaelaaronlevy.ork.util.ConsoleGUI ConsoleGUI} (A
    * general-purpose utility class. This is the version you will see if you
    * create a searchable word index with the default Ork GUI.)
    * 
    * @param directory
    *           the directory containing the searchable word index files
    * @param name
    *           the name of the searchable word index (not including the file
    *           extension or the period preceding the file extension)
    * @param out
    * @param err
    * @param source
    * @throws IOException
    */
   public FinderGUI(final File directory, final String name, final PrintStream out, final PrintStream err,
         final Iterator<String> source) throws IOException
   {
      this.directory = directory;
      this.out = out;
      this.err = err;
      this.source = source;
      
      project = Indexer.buildProjectFromIndexFile(directory, name);
      
      final File coni = new File(directory, name + ".coni");
      final File conw = new File(directory, name + ".conw");
      
      BufferedInputStream bis = new BufferedInputStream(new FileInputStream(conw));
      
      Grid.readIntArray(bis);
      Grid.readStringArray(bis);
      words = Grid.readStringArray(bis);
      pages = new int[words.length][];
      
      bis.close();
      
      bis = new BufferedInputStream(new FileInputStream(coni));
      
      final int wordsPerConi = Grid.readInt(bis);
      if(wordsPerConi != words.length)
      {
         throw new RuntimeException("Error - number of words does not match " + wordsPerConi + " vs " + words.length);
      }
      
      final boolean isShort = Grid.readBoolean(bis);
      
      for(int i = 0; i < words.length; i++)
      {
         pages[i] = isShort ? Grid.readShortArrayToIntArray(bis) : Grid.readIntArray(bis);
      }
      bis.close();
      
      ppv = new ProjectPdfViewer(project, FontUtility.getFont("listFont"), FontUtility.getFont("mainFont"));
      frame.add(ppv.getComponent());
      
      out.println();
      out.println("Drag & Drop script files into this window to run them.");
      out.println("Or type commands into the text field below to run one command at a time.");
      out.println("quit() to exit; save() to save a script file of what happened in this session.");
      out.println("view() to open the viewer; integer(#) or range(#,#) to specify pages by number.");
      out.println("startsWith() to find which words in the index start with a particular prefix.");
      out.println();
   }
   
   public void run()
   {
      String line = null;
      while(source != null && (source instanceof Scanner || source.hasNext()))
      {
         if(line != null && line.length() > 0)
         {
            try
            {
               out.flush();
               final Command c = Command.parse(line);
               c.writeScript(err);
               err.println();
               err.flush();
               list.add(line);
               c.execute(this);
               out.flush();
               err.flush();
            }
            catch(final Exception exc)
            {
               err.println(exc.getClass() + ": " + exc.getMessage());
            }
         }
         
         try
         {
            Thread.sleep(100);
         }
         catch(final InterruptedException iex)
         {
            // do nothing
         }
         // out.print("input: ");
         if(source == null)
         {
            return;
         }
         line = source.next();
         out.println();
         out.flush();
         
         if(line != null)
         {
            line = line.trim();
         }
      }
   }
   
   private void runScript(final File f)
   {
      if(f == null)
      {
         return;
      }
      
      Object[][] tokens = null;
      try
      {
         tokens = TokenParser.parseAll(f);
      }
      catch(final Exception exc)
      {
         err.println(exc.getClass() + ": " + exc.getMessage());
         throw new RuntimeException(exc);
      }
      
      for(final Object[] line : tokens)
      {
         try
         {
            list.add(TokenParser.tokensToString(line));
            final Command c = Command.parse(line);
            err.println(c.getClass().toString().substring(45));
            c.writeScript(err);
            err.println();
            err.flush();
            c.execute(this);
            err.println();
            out.flush();
            err.flush();
         }
         catch(final Exception exc)
         {
            out.flush();
            err.println(exc.getClass() + ": " + exc.getMessage());
            err.flush();
            return;
         }
      }
   }
   
   public void echo(String message)
   {
      err.println("ECHO: " + message);
      err.flush();
   }
   
   public AscendingStack getPagesFor(String word)
   {
      AscendingStack as = null;
      final int index = Arrays.binarySearch(words, word);
      if(index < 0)
      {
         as = variables.get(word);
         if(as == null)
         {
            throw new RuntimeException("Error: Word is not in the index and it is not a variable: " + word);
         }
      }
      else
      {
         as = new AscendingStack(pages[index]);
      }
      as.makeDirty();
      return as;
   }
   
   /**
    * FinderGUI does not support any expressions that are not already
    * implemented through objects that subclass {@link Expression Expression}
    */
   public AscendingStack calculateFunction(String name, Expression[] arguments)
   {
      throw new RuntimeException("Function not recognized: " + name);
   }
   
   /**
    * This implementation of {@link Context Context} supports the following
    * commands:
    * <p>
    * "list" - display the pages associated with a particular expression;
    * <p>
    * "print" - print whatever text is in the parenthesis without evaluating it
    * as an expression;
    * <p>
    * "printProjectInfo" - display information about the PDFs that are behind
    * the searchable word index;
    * <p>
    * "quit" - System.exit(0);
    * <p>
    * "return" - go back to the text extraction view;
    * <p>
    * "save" - print the commands that have been issued, so far, to a script
    * file that can be edited and loaded later, e.g. if you quit for the day and
    * want to pick up where you left off without re-entering all variables;
    * <p>
    * "view" - open the graphical
    * {@link com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPdfViewer
    * ProjectPdfViewer} window. Put the search results in parenthesis and then
    * you can use the buttons in the
    * {@link com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPdfViewer
    * ProjectPdfViewer} to click through the search results.
    */
   public void runFunction(String name, Expression[] arguments)
   {
      if(name == null)
      {
         return;
      }
      else if(name.equals("list"))
      {
         if(arguments == null || arguments.length != 1)
         {
            throw new RuntimeException("invalid format for list function: must have exactly 1 argument.");
         }
         final AscendingStack as = arguments[0].getCalculation(this);
         try
         {
            as.sendDescription(out, Integer.MAX_VALUE);
            out.println();
            out.flush();
         }
         catch(final IOException iox)
         {
            // print stream should not throw IOException
         }
      }
      else if(name.equals("print"))
      {
         try
         {
            err.println("PRINTING:");
            for(final Expression e : arguments)
            {
               err.print('\t');
               e.writeScript(err);
               err.println();
            }
            err.flush();
         }
         catch(final IOException iox)
         {
            // PrintStream does not throw IOException
         }
      }
      else if(name.equals("printProjectInfo"))
      {
         err.println(project.toString());
         err.flush();
      }
      else if(name.equals(_SAVE_COMMAND))
      {
         try
         {
            File file = new File(directory, "console.txt");
            for(int i = 1; i < 999 && file.exists(); i++)
            {
               file = new File(directory, "console" + i + ".txt");
            }
            
            final BufferedWriter writer = new BufferedWriter(new FileWriter(file));
            for(final String line : list)
            {
               if(line.startsWith(_SAVE_COMMAND) && line.substring(_SAVE_COMMAND.length()).trim().startsWith("("))
               {
                  // skip it, we don't want this becoming part of the script
                  // later
               }
               else
               {
                  writer.write(line);
                  writer.newLine();
               }
            }
            writer.close();
         }
         catch(final IOException iox)
         {
            // PrintStream does not throw IOException
         }
      }
      else if(name.equals("quit"))
      {
         ppv.close();
         System.exit(0);
      }
      else if(name.equals("return"))
      {
         ppv.close();
         final OpenReviewKit ork = OpenReviewKit.getOrk();
         if(ork != null)
         {
            source = null; // end the run method
            ork.showExtractionGUI();
         }
      }
      else if(name.equals("startsWith"))
      {
         final TreeSet<String> set = new TreeSet<String>();
         for(final Expression e : arguments)
         {
            String s = null;
            if(e instanceof ExpressionWord)
            {
               final ExpressionWord ew = (ExpressionWord) e;
               s = ew.word;
            }
            if(s != null && s.length() > 0)
            {
               addStartsWith(set, s);
            }
         }
         err.print("STARTS WITH: ");
         final String[] w = new String[set.size()];
         set.toArray(w);
         if(w.length == 0)
         {
            err.println("Nothing in the word index starts with that.");
         }
         else
         {
            err.print(TokenParser.literalIfNeeded(w[0]));
            for(int i = 1; i < w.length; i++)
            {
               err.print(" | ");
               err.print(TokenParser.literalIfNeeded(w[i]));
            }
            err.println();
         }
         err.flush();
      }
      else if(name.equals("view"))
      {
         final int[] array;
         if(arguments == null || arguments.length == 0)
         {
            array = new int[0];
         }
         else if(arguments.length == 1)
         {
            array = arguments[0].getCalculation(this).integerArray();
         }
         else
         {
            array = new ExpressionOperation(1, arguments).getCalculation(this).integerArray();
         }
         ppv.setResults(array);
         frame.setVisible(true);
         frame.pack();
      }
      else
      {
         err.println("Function name not recognized: " + name);
         err.flush();
      }
   }
   
   public boolean functionReturnsValue(String name)
   {
      if("integer".equals(name) || "range".equals(name))
      {
         return true;
      }
      return false;
   }
   
   public void setVariable(String varName, AscendingStack as)
   {
      if(as == null)
      {
         throw new RuntimeException("ContextSimple does not permit deleting variables.");
      }
      else if(variables.get(varName) == null)
      {
         variables.put(varName, as);
      }
      else
      {
         throw new RuntimeException("ContextSimple does not permit overwriting variables.");
      }
   }
   
   public void displayValues(AscendingStack as)
   {
      final int[] array = as.integerArray();
      if(array.length == 0)
      {
         err.println("No pages match this criteria.");
      }
      else if(array.length == 1)
      {
         err.print("Exactly one match, at page ");
         err.print(array[0]);
         err.println(".");
      }
      else if(array.length <= _MAX_TO_DISPLAY_ALL)
      {
         final StringBuilder b = new StringBuilder(160);
         b.append("There are ").append(Integer.toString(array.length)).append(" matching pages: ").append(array[0]);
         for(int i = 1; i < array.length; i++)
         {
            b.append(", ");
            printToErr(b);
            b.append(Integer.toString(array[i]));
         }
         b.append(".");
         System.err.println(b.toString());
      }
      else
      {
         final StringBuilder b = new StringBuilder(160);
         b.append("There are ").append(Integer.toString(array.length)).append(" matching pages: ")
               .append(Integer.toString(array[0]));
         for(int i = 1; i < _MAX_TO_DISPLAY_ALL / 2; i++)
         {
            b.append(", ");
            printToErr(b);
            b.append(Integer.toString(array[i]));
         }
         b.append(" . . . ");
         printToErr(b);
         int i = array.length - (_MAX_TO_DISPLAY_ALL / 2);
         b.append(Integer.toString(array[i]));
         for(i = i + 1; i < array.length; i++)
         {
            b.append(", ");
            printToErr(b);
            b.append(Integer.toString(array[i]));
         }
         b.append(".");
         System.err.println(b.toString());
      }
      err.flush();
   }
   
   private void addStartsWith(final TreeSet<String> set, final String prefix)
   {
      int index = Arrays.binarySearch(words, prefix);
      if(index < 0)
      {
         index = -index - 1; // TODO: should this be +1, -1?
      }
      for(; index < words.length; index++)
      {
         final String s = words[index];
         if(s.startsWith(prefix))
         {
            set.add(s);
         }
         else
         {
            return;
         }
      }
   }
   
   /**
    * the method that is called when a user drags and drops a script file into
    * the window. It can also be called programmatically to carry out all of the
    * commands in a single arbitrary script file.
    */
   public void filesDropped(File[] files)
   {
      if(files == null || files.length != 1)
      {
         System.err.println("ERROR: Only one script file can be loaded at a time.");
         return;
      }
      final File file = files[0];
      final String name = file.getName();
      if(name.toLowerCase().startsWith("script") || name.toLowerCase().startsWith("console"))
      {
         runScript(file);
      }
      else
      {
         System.err.println("ERROR: can only load a script file if the file name begins with 'script' or 'console'");
      }
   }
   
   private void printToErr(final StringBuilder b)
   {
      if(b.length() >= _MAX_LINE_LENGTH)
      {
         System.err.print(b.substring(0, b.length() - 1));
         b.delete(0, b.length());
      }
   }
   
   private final File directory;
   private final PrintStream out;
   private final PrintStream err;
   private Iterator<String> source;
   private final String[] words;
   private final int[][] pages;
   private final TreeMap<String, AscendingStack> variables = new TreeMap<String, AscendingStack>();
   private final ArrayList<String> list = new ArrayList<String>();
   private final ProjectPDFs project;
   private final JFrame frame = new JFrame(OpenReviewKit._ORK_LONG_NAME + " PDF Viewer");
   private final ProjectPdfViewer ppv;
   
   private static final int _MAX_TO_DISPLAY_ALL = 200;
   private static final int _MAX_LINE_LENGTH = 150;
   
   private static final String _SAVE_COMMAND = "save";
   
   /**
    * this class is used to create a new FinderGUI for a specific searchable
    * word index (that has already been created). It supports drag and drop -
    * just pick any one of the files associated with the word index and drop it
    * into the window.
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   public static class Loader implements FileDrop.Listener
   {
      public Loader(final ConsoleGUI finder)
      {
         this.finder = finder;
      }
      
      public void filesDropped(File[] files)
      {
         if(isDone)
         {
            return;
         }
         if(files == null || files.length != 1)
         {
            return;
         }
         isDone = true;
         final File f = files[0];
         FinderGUI c = null;
         try
         {
            final File directory = f.getParentFile();
            String name = f.getName();
            name = name.substring(0, name.lastIndexOf('.'));
            c = new FinderGUI(directory, name, finder.stream, finder.stream, finder);
            finder.addFileDropListener(c);
         }
         catch(final IOException iox)
         {
            isDone = false;
         }
         if(c != null)
         {
            new Thread(c).start();
         }
      }
      
      private boolean isDone = false;
      private final ConsoleGUI finder;
   }
}
