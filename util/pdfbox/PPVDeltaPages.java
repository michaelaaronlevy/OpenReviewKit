package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVDeltaPages class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
class PPVDeltaPages implements PPVElement, ActionListener
{
   PPVDeltaPages(final ProjectPageState state, final int delta, final boolean canChangePdf, final String initialText)
   {
      this.state = state;
      this.delta = delta;
      this.canChangePdf = canChangePdf;
      button = new JButton(initialText);
      button.addActionListener(this);
      button.setFocusable(false);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      state.moveNPages(delta, canChangePdf);
   }
   
   public void updateView(final PageReference newRef)
   {
      return;
   }
   
   public JButton getComponent()
   {
      return button;
   }
   
   public void setResults(final int[] results)
   {
      throw new RuntimeException("Unsupported Operation for this Class");
   }
   
   private final JButton button;
   private final ProjectPageState state;
   private final int delta;
   private final boolean canChangePdf;
}
