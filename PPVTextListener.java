package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JTextField;

/**
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVTextListener class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
class PPVTextListener implements PPVElement, ActionListener
{
   PPVTextListener(final ProjectPageState state, final int opCode)
   {
      this.state = state;
      this.opCode = opCode;
      field = new JTextField(2);
      field.addActionListener(this);
      state.addPPVListener(this);
   }
   
   public void updateView(final PageReference newRef)
   {
      int newValue = -1;
      int width = 1;
      if(newRef == null)
      {
         // do nothing, newValue is already -1
      }
      else if(opCode == 0)
      {
         newValue = newRef.projectPage;
         width = Integer.toString(newRef.getProjectPageCount()).length();
      }
      else if(opCode == 1)
      {
         newValue = newRef.pdfIndex;
         width = Integer.toString(newRef.getPdfCount()).length();
      }
      else if(opCode == 2)
      {
         newValue = newRef.pdfPage;
         width = Integer.toString(newRef.getPdfPageCount()).length();
      }
      else if(opCode == 3)
      {
         if(search != null)
         {
            newValue = search == null ? -1 : Arrays.binarySearch(search, newRef.projectPage) + 1;
            if(newValue <= 0 || newValue == search.length + 1)
            {
               newValue = -1;
            }
         }
         width = Integer.toString(search.length).length();
      }
      
      model = newValue == -1 ? "" : Integer.toString(newValue);
      width = ((width + 2) * 2) / 3;
      if(width > field.getColumns())
      {
         field.setColumns(width);
      }
      field.setText(model);
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      String text = field.getText();
      if(text == null)
      {
         field.setText(model);
         return;
      }
      text = text.trim();
      if(text.length() == 0)
      {
         field.setText(model);
         return;
      }
      
      int newValue = -1;
      try
      {
         newValue = Integer.parseInt(text);
      }
      catch(final NumberFormatException nfe)
      {
         field.setText(model);
         return;
      }
      
      field.setText(model);
      if(opCode == 0)
      {
         state.setFocusByProjectPage(newValue);
      }
      else if(opCode == 1)
      {
         state.setFocusByDocumentIndex(newValue, 1);
      }
      else if(opCode == 2)
      {
         state.setFocusByDocumentPage(newValue);
      }
      else if(opCode == 3)
      {
         state.setFocusBySearch(search, newValue);
      }
   }
   
   public JTextField getComponent()
   {
      return field;
   }
   
   public void setResults(final int[] results)
   {
      if(opCode != 3)
      {
         throw new RuntimeException("This PPVTextListener is not operating with search results.");
      }
      search = results;
   }
   
   private final JTextField field;
   
   private int[] search = null;
   private String model = "";
   
   private final ProjectPageState state;
   private final int opCode;
}
