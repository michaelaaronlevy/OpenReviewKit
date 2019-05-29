package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Arrays;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

/**
 * The mainPanel for this class is a JPanel with a series of buttons on the top
 * for navigating between multiple PDFs, a window showing a page from one of
 * those PDFs, and on the bottom a JTextField that takes text input and sends it
 * to a text file along with information about whatever page was being viewed at
 * the time the note was typed. The buttons along the top include buttons to
 * move one page at a time, one document at a time, and one search result at a
 * time.
 * 
 * <p>
 * It was created for use with the Searchable Word Index. But you could also use
 * it to view one or more PDFs that are not part of a Searchable Word Index.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ProjectPdfViewer class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 * @see ProjectPdfWindow ProjectPdfWindow
 * @see SinglePdfWindow SinglePdfWindow
 *
 */
public class ProjectPdfViewer implements ActionListener, PPVElement
{
   /**
    * 
    * @param project
    *           the PDFs to display
    * @param buttonsFont
    *           optional - can be null
    * @param notesFont
    *           optional - can be null
    */
   public ProjectPdfViewer(final ProjectPDFs project, final Font buttonsFont, final Font notesFont)
   {
      state = new ProjectPageState(project);
      state.addPPVListener(window);
      mainPanel.setLayout(border);
      
      final PPVElement[] list = new PPVElement[16];
      
      list[0] = new PPVDeltaPages(state, -1, true, "<");
      list[1] = new PPVTextListener(state, 0);
      list[2] = new PPVBack(state, 0);
      list[3] = new PPVDeltaPages(state, 1, true, ">");
      list[4] = new PPVDeltaDocument(state, false, "<");
      list[5] = new PPVTextListener(state, 1);
      list[6] = new PPVBack(state, 1);
      list[7] = new PPVDeltaDocument(state, true, ">");
      list[8] = new PPVDeltaPages(state, -1, false, "<");
      list[9] = new PPVTextListener(state, 2);
      list[10] = new PPVBack(state, 2);
      list[11] = new PPVDeltaPages(state, 1, false, ">");
      list[12] = searchDec = new PPVDeltaSearch(state, false, "<");
      list[13] = searchText = new PPVTextListener(state, 3);
      list[14] = searchLabel = new PPVBack(state, 3);
      list[15] = searchInc = new PPVDeltaSearch(state, true, ">");
      
      for(final PPVElement e : list)
      {
         final JComponent jc = e.getComponent();
         buttonsPanel.add(jc);
      }
      
      zoomOutButton.addActionListener(this);
      zoomOutButton.setFocusable(false);
      buttonsPanel.add(zoomOutButton);
      
      zoomSetButton.setText(_DEFAULT_ZOOM_TEXT);
      zoomSetButton.addActionListener(this);
      buttonsPanel.add(zoomSetButton);
      
      zoomInButton.addActionListener(this);
      zoomInButton.setFocusable(false);
      buttonsPanel.add(zoomInButton);
      
      rotateButton.addActionListener(this);
      rotateButton.setFocusable(false);
      buttonsPanel.add(rotateButton);
      
      // final JScrollPane sp2 = new JScrollPane();
      // sp2.getViewport().add(buttonsPanel);
      // sp2.setHorizontalScrollBarPolicy(JScrollPane.HORIZONTAL_SCROLLBAR_AS_NEEDED);
      // mainPanel.add(sp2, BorderLayout.PAGE_START);
      
      mainPanel.add(buttonsPanel, BorderLayout.PAGE_START);
      
      final JScrollPane jsp = new JScrollPane();
      jsp.getViewport().setBackground(Color.CYAN);
      jsp.getViewport().add(window);
      window.setContainer(jsp.getViewport());
      
      mainPanel.add(jsp, BorderLayout.CENTER);
      
      notes = new PPVNotesExporter(project);
      mainPanel.add(notes.getComponent(), BorderLayout.PAGE_END);
      state.addPPVListener(notes);
      
      if(buttonsFont != null)
      {
         for(final Component c : buttonsPanel.getComponents())
         {
            c.setFont(buttonsFont);
         }
      }
      if(notesFont != null)
      {
         notes.getComponent().setFont(notesFont);
      }
      
      border.layoutContainer(mainPanel);
      totalProjectPages = project.getProjectPageCount();
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      final Object source = aev.getSource();
      if(source == zoomOutButton)
      {
         window.changeZoom(1.0f / _ZOOM_BUTTON_CHANGE);
         zoomPercentDisplay = window.getZoom() + "%";
      }
      else if(source == zoomSetButton)
      {
         String s = zoomSetButton.getText();
         if(s.equals(zoomPercentDisplay))
         {
            return;
         }
         if(!s.trim().equals(s))
         {
            s = s.trim();
         }
         if(s.length() == 0)
         {
            s = "100";
         }
         if(s.charAt(s.length() - 1) == '%')
         {
            s = s.substring(0, s.length() - 1).trim();
         }
         
         try
         {
            int z = Integer.parseInt(s);
            window.setZoom(z / 100f);
            zoomPercentDisplay = window.getZoom() + "%";
         }
         catch(final NumberFormatException nfe)
         {
            // do nothing
         }
      }
      else if(source == zoomInButton)
      {
         window.changeZoom(_ZOOM_BUTTON_CHANGE);
         zoomPercentDisplay = window.getZoom() + "%";
      }
      else if(source == rotateButton)
      {
         window.changeRotation();
      }
      
      zoomSetButton.setText(window.getZoom() + "%");
      window.reDraw();
      border.layoutContainer(mainPanel);
      mainPanel.revalidate();
   }
   
   public JPanel getComponent()
   {
      return mainPanel;
   }
   
   public void updateView(final PageReference newRef)
   {
      return;
   }
   
   public void setResults(int[] results)
   {
      if(results == null || results.length == 0 || results[0] > totalProjectPages)
      {
         results = new int[0];
      }
      else if(results[results.length - 1] > totalProjectPages)
      {
         // prevent the results array from including values that exceed the
         // maximum number of pages.
         // steps are taken elsewhere to prevent the results[] sent to this
         // method from having values <= 0.
         for(int i = 0; i < results.length; i++)
         {
            if(results[i] > totalProjectPages)
            {
               final int[] r = new int[i];
               System.arraycopy(results, 0, r, 0, i);
               results = r;
               System.err.println(Arrays.toString(results));
            }
         }
      }
      searchDec.setResults(results);
      searchText.setResults(results);
      searchLabel.setResults(results);
      searchInc.setResults(results);
      state.setFocusByProjectPage(results.length == 0 ? 1 : results[0]);
   }
   
   public void close()
   {
      notes.close();
   }
   
   private final JPanel mainPanel = new JPanel();
   private final JPanel buttonsPanel = new JPanel();
   private final BorderLayout border = new BorderLayout();
   private final ProjectPageState state;
   
   private final PPVDeltaSearch searchDec;
   private final PPVTextListener searchText;
   private final PPVBack searchLabel;
   private final PPVDeltaSearch searchInc;
   
   private final JButton zoomOutButton = new JButton("-");
   private final JTextField zoomSetButton = new JTextField(5);
   private final JButton zoomInButton = new JButton("+");
   private final JButton rotateButton = new JButton("r");
   private final ProjectPdfWindow window = new ProjectPdfWindow();
   private final PPVNotesExporter notes;
   
   private final int totalProjectPages;
   
   private String zoomPercentDisplay;
   
   private static final float _ZOOM_BUTTON_CHANGE = 1.2f;
   private static final String _DEFAULT_ZOOM_TEXT = "100%";
}
