package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVDeltaDocument class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
class PPVDeltaDocument implements PPVElement, ActionListener
{
   PPVDeltaDocument(final ProjectPageState state, final boolean forward, final String initialText)
   {
      this.state = state;
      this.forward = forward;
      button = new JButton(initialText);
      button.addActionListener(this);
      button.setFocusable(false);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      state.moveFocus1Doc(forward);
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
   private final boolean forward;
}
