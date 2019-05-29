package com.github.michaelaaronlevy.ork.util;

/**
 * objects of this class contain status information (whether an object is
 * running and whether there are errors). This object is mutable.
 * 
 * <p>
 * By convention, the object whose status is being tracked should have a
 * reference to this object, but should only share references to the
 * {@link StatusReporter StatusReporter} associated with this Status object.
 * That prevents other objects from modifying the status but allows them easy
 * access to view the status.
 * 
 * <p>
 * There are three run statuses, and three error statuses
 * 
 * <p>
 * not started
 * 
 * <p>
 * running
 * 
 * <p>
 * finished
 * 
 * <p>
 * no errors detected
 * 
 * <p>
 * errors detected (nonfatal)
 * 
 * <p>
 * fatal error detected
 * 
 * <p>
 * By convention, if there is a fatal error, the object whose status is being
 * tracked should stop running.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the Status class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class Status
{
   public Status(final String name)
   {
      this.name = name;
      runStatus = Status.RunStatus.NOT_STARTED;
      errorStatus = Status.ErrorStatus.NO_ERROR;
      statusMessage = null;
      reporter = new StatusReporter(this);
   }
   
   public void declareStart()
   {
      runStatus = Status.RunStatus.RUNNING;
   }
   
   public void declareEnd()
   {
      runStatus = Status.RunStatus.DONE;
   }
   
   public void declareFatalError(final String message)
   {
      if(errorStatus != Status.ErrorStatus.FATAL_ERROR)
      {
         statusMessage = message;
         errorStatus = Status.ErrorStatus.FATAL_ERROR;
      }
   }
   
   public void declareError(final String message)
   {
      if(errorStatus == Status.ErrorStatus.NO_ERROR)
      {
         statusMessage = message;
         errorStatus = Status.ErrorStatus.HAS_ERROR;
      }
   }
   
   public void updateStatus(final String newStatus)
   {
      if(errorStatus == Status.ErrorStatus.NO_ERROR)
      {
         statusMessage = newStatus;
      }
   }
   
   String statusToString()
   {
      String r = name + " " + runStatus.message + ".  This has " + errorStatus.message;
      if(statusMessage != null)
      {
         r += ": " + statusMessage;
      }
      return r;
   }
   
   public final String name;
   public Status.RunStatus runStatus;
   public Status.ErrorStatus errorStatus;
   public String statusMessage;
   
   public final StatusReporter reporter;
   
   public enum RunStatus
   {
      NOT_STARTED("is not started"), RUNNING("is running"), DONE("is finished");
      
      private RunStatus(final String message)
      {
         this.message = message;
      }
      
      public final String message;
   }
   
   public enum ErrorStatus
   {
      NO_ERROR("no detected errors"), HAS_ERROR("error(s)"), FATAL_ERROR("a fatal error");
      
      private ErrorStatus(final String message)
      {
         this.message = message;
      }
      
      public final String message;
   }
}
