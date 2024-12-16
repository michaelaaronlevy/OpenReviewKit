package com.github.michaelaaronlevy.ork.util;

import java.awt.Graphics;

import javax.swing.BoxLayout;
import javax.swing.JLabel;
import javax.swing.JPanel;

import com.github.michaelaaronlevy.ork.util.Status.RunStatus;

/**
 * StatusReporter objects provide information about an underlying {@link Status
 * Status} object, without offering any opportunities to alter that
 * {@link Status Status}. This class also provides a Swing GUI element (a
 * JPanel) that displays status information.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the StatusReporter class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class StatusReporter
{
   StatusReporter(final Status s)
   {
      this.status = s;
   }
   
   public boolean hasError()
   {
      return status.errorStatus != Status.ErrorStatus.NO_ERROR;
   }
   
   public boolean hasFatalError()
   {
      return status.errorStatus == Status.ErrorStatus.FATAL_ERROR;
   }
   
   public Status.ErrorStatus getErrorStatus()
   {
      return status.errorStatus;
   }
   
   public boolean isRunning()
   {
      return status.runStatus == Status.RunStatus.RUNNING;
   }
   
   public boolean isFinished()
   {
      return status.runStatus == Status.RunStatus.DONE;
   }
   
   public Status.RunStatus getRunStatus()
   {
      return status.runStatus;
   }
   
   public String getErrorMessage()
   {
      return hasError() ? status.statusMessage : null;
   }
   
   public String getStatusMessage()
   {
      return status.statusMessage;
   }
   
   public String getFullStatus()
   {
      return status.statusToString();
   }
   
   public synchronized JPanel getComponent()
   {
      if(label == null)
      {
         label = new Label();
      }
      return label;
   }
   
   private final Status status;
   private Label label = null;
   
   private class Label extends JPanel implements Runnable
   {
      Label()
      {
         setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
         labelRun = new JLabel();
         labelErr = new JLabel();
         labelMes = new JLabel();
         this.add(labelRun);
         this.add(labelErr);
         this.add(labelMes);
         new Thread(this).start();
         this.validate();
      }
      
      /**
       * the run method will continue updating the GUI element until RunStatus
       * is done.
       */
      public void run()
      {
         while(status.runStatus != RunStatus.DONE)
         {
            try
            {
               Thread.sleep(50);
            }
            catch(final InterruptedException iex)
            {
               // do nothing
            }
            setLabels();
         }
         setLabels();
      }
      
      private void setLabels()
      {
         labelRun.setText(status.name + " " + status.runStatus.message);
         labelErr.setText("With " + status.errorStatus.message + ".");
         final String sm = getStatusMessage();
         labelMes.setText(sm == null ? "" : sm);
      }
      
      public void paint(final Graphics g)
      {
         setLabels();
         super.paint(g);
      }
      
      private final JLabel labelRun;
      private final JLabel labelErr;
      private final JLabel labelMes;
   }
}
