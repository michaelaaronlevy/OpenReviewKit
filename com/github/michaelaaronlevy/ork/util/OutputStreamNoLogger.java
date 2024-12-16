package com.github.michaelaaronlevy.ork.util;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;

/**
 * this class is intended to filter out any output that comes from a Java Logger
 * (i.e., any code from a package in java.logging) Messages from other sources
 * are still printed. This is because I can't figure out how to disable PDFBox
 * from printing error messages to System.err but I don't think (at least when
 * they are using OpenReviewKit) people actually want to read those error
 * messages. The messages from the Logger are instead discarded (if reRoute ==
 * null) or sent to the OutputStream reRoute.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the OutputStreamNoLogger class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class OutputStreamNoLogger extends OutputStream
{
   /**
    * 
    * @param target
    *           the PrintStream that this class will write the filtered output
    *           to
    * @param reRoute
    *           the PrintStream that this class will write the rejected output
    *           to (if this is null, the rejected output will be discarded)
    */
   public OutputStreamNoLogger(final PrintStream target, final OutputStream reRoute)
   {
      out = target;
      this.reRoute = reRoute;
      in = new PrintStream(this);
   }
   
   public void write(final int b) throws IOException
   {
      if(check())
      {
         out.write(b);
      }
      else if(reRoute != null)
      {
         reRoute.write(b);
      }
   }
   
   public void write(final byte[] b) throws IOException
   {
      if(check())
      {
         out.write(b);
      }
      else if(reRoute != null)
      {
         reRoute.write(b);
      }
   }
   
   public void write(final byte[] b, final int off, final int len)
   {
      if(check())
      {
         out.write(b, off, len);
      }
      else if(reRoute != null)
      {
         try
         {
            reRoute.write(b, off, len);
         }
         catch(final IOException iox)
         {
            // do nothing
         }
      }
   }
   
   private boolean check()
   {
      final StackTraceElement[] stack = Thread.currentThread().getStackTrace();
      final String twelve = stack.length <= 12 ? null : stack[12].toString();
      return twelve == null || !twelve.startsWith("java.logging");
   }
   
   /**
    * filtered output is sent to this Object (filtered to exclude logging)
    */
   public final PrintStream out;
   
   /**
    * other methods will print to this PrintStream, which provides the input for
    * this class
    */
   public final PrintStream in;
   
   /**
    * if reRoute is not null, the filtered output is sent to reRoute, instead of
    * being discarded
    */
   public final OutputStream reRoute;
}
