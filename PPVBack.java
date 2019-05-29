package com.github.michaelaaronlevy.ork.util.pdfbox;

import javax.swing.JLabel;

/**
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVBack class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
class PPVBack implements PPVElement
{
   PPVBack(final ProjectPageState state, final int opCode)
   {
      this.opCode = opCode;
      setLabel(-1);
      state.addPPVListener(this);
   }
   
   private void setLabel(final int newMax)
   {
      if(max == newMax)
      {
         return;
      }
      max = newMax;
      label.setText(max == -1 ? "     " : _OUT_OF + max);
   }
   
   public void setResults(final int[] newSearch)
   {
      if(opCode != 3)
      {
         throw new RuntimeException("This PPVBack is not operating with search results.");
      }
      search = newSearch;
      setLabel(search == null ? -1 : search.length);
   }
   
   public void updateView(final PageReference ref)
   {
      int newMax = -1;
      if(ref == null)
      {
         newMax = -1;
      }
      else if(opCode == 0)
      {
         newMax = ref.getProjectPageCount();
      }
      else if(opCode == 1)
      {
         newMax = ref.getPdfCount();
      }
      else if(opCode == 2)
      {
         newMax = ref.getPdfPageCount();
      }
      else if(opCode == 3)
      {
         newMax = search == null ? -1 : search.length;
      }
      setLabel(newMax);
   }
   
   public JLabel getComponent()
   {
      return label;
   }
   
   private final JLabel label = new JLabel();
   private final int opCode;
   
   private int max = -1;
   private int[] search = null;
   
   private static final String _OUT_OF = "of ";
}
