package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;

/**
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVDeltaSearch class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
class PPVDeltaSearch implements PPVElement, ActionListener
{
   PPVDeltaSearch(final ProjectPageState state, final boolean isForward, final String initialText)
   {
      this.state = state;
      this.isForward = isForward;
      search = null;
      button = new JButton(initialText);
      button.addActionListener(this);
      button.setFocusable(false);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      if(search != null && search.length != 0)
      {
         state.moveFocus1Search(search, isForward);
      }
   }
   
   public void setResults(final int[] newSearch)
   {
      search = newSearch;
   }
   
   public void updateView(final PageReference newRef)
   {
      return;
   }
   
   public JButton getComponent()
   {
      return button;
   }
   
   private final JButton button;
   private final ProjectPageState state;
   private final boolean isForward;
   
   private int[] search;
}
