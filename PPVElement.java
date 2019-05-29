package com.github.michaelaaronlevy.ork.util.pdfbox;

import javax.swing.JComponent;

/**
 * each instance of this class is meant to represent a JComponent that is
 * updated whenever the page selected by a {@link ProjectPageState
 * ProjectPageState} object changes. Also, the setResults method can be invoked
 * when the {@link ProjectPageState ProjectPageState} loads new search results.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the PPVElement interface) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface PPVElement
{
   JComponent getComponent();
   
   void updateView(PageReference newRef);
   
   /**
    * 
    * @param results
    *           the search results
    * @throws RuntimeException
    *            if this does not support index operations
    */
   void setResults(final int[] results);
}
