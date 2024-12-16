package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * display a single page from a single PDF (in a {@link SinglePdfWindow
 * SinglePdfWindow}), along with buttons at the top for moving between pages and
 * changing the zoom or rotation.
 * 
 * <p>
 * With the constructor, you can add your own components to the left, right, and
 * below the viewing window, so it is easy to use this class as part of a more
 * complicated application. You can use an instance of this class to view a
 * single PDF (one page at a time, which you can navigate through using the
 * buttons that appear above the viewing window), and you can use your own
 * custom components on the left, right, and below the viewing window, to add
 * additional functionality specific to your application).
 * 
 * <p>
 * If you want to be able to navigate through multiple PDFs at the same time (as
 * opposed to: only ever navigating one, or only navigating through one at a
 * time), consider the {@link ProjectPdfViewer ProjectPdfViewer} class instead.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the SinglePdfViewer class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 * 
 * @see com.github.michaelaaronlevy.ork.util.pdfbox.SinglePdfWindow
 *
 */
public class SinglePdfViewer extends JPanel implements ActionListener
{
   /**
    * 
    * @param south
    *           any JComponent. It will be displayed underneath the PDF page.
    * @param east
    *           any JComponent. It will be displayed left of the PDF page.
    * @param west
    *           any JComponent. It will be displayed right of the PDF page.
    */
   public SinglePdfViewer(final JComponent south, final JComponent east, final JComponent west)
   {
      docPages = -1;
      
      border = new BorderLayout();
      setLayout(border);
      
      final JPanel buttonsPanel = new JPanel();
      
      prevButton = new JButton("<");
      prevButton.addActionListener(this);
      buttonsPanel.add(prevButton);
      
      jumpButton = new JTextField(4);
      jumpButton.setText("0");
      jumpButton.addActionListener(this);
      buttonsPanel.add(jumpButton);
      
      nextButton = new JButton(">");
      nextButton.addActionListener(this);
      buttonsPanel.add(nextButton);
      
      zoomOutButton = new JButton("-");
      zoomOutButton.addActionListener(this);
      buttonsPanel.add(zoomOutButton);
      
      zoomSetButton = new JTextField(4);
      zoomSetButton.setText(_DEFAULT_ZOOM_TEXT);
      zoomSetButton.addActionListener(this);
      buttonsPanel.add(zoomSetButton);
      
      zoomInButton = new JButton("+");
      zoomInButton.addActionListener(this);
      buttonsPanel.add(zoomInButton);
      
      rotateButton = new JButton("r");
      rotateButton.addActionListener(this);
      buttonsPanel.add(rotateButton);
      
      add(buttonsPanel, BorderLayout.PAGE_START);
      
      window = new SinglePdfWindow();
      final JScrollPane jsp = new JScrollPane();
      jsp.getViewport().setBackground(Color.CYAN);
      jsp.getViewport().add(window);
      
      add(jsp, BorderLayout.CENTER);
      
      if(south != null)
      {
         add(south, BorderLayout.PAGE_END);
      }
      if(east != null)
      {
         add(east, BorderLayout.LINE_START);
      }
      if(west != null)
      {
         add(west, BorderLayout.LINE_END);
      }
      
      border.layoutContainer(this);
   }
   
   /**
    * 
    * @param doc
    *           the PDF to display
    * @param page
    *           the page number to start at
    */
   public void setDocument(final PDDocument doc, final int page)
   {
      docPages = (doc == null ? -1 : doc.getNumberOfPages());
      window.setDocument(doc, page);
      window.setPage(page);
      pageNumberDisplay = "" + window.getPage();
      jumpButton.setText(pageNumberDisplay);
      zoomPercentDisplay = window.getZoom() + "%";
      zoomSetButton.setText(zoomPercentDisplay);
      window.reDraw();
   }
   
   public void actionPerformed(final ActionEvent aev)
   {
      final Object source = aev.getSource();
      if(source == prevButton)
      {
         if(window.getPage() > 1)
         {
            window.setPage(window.getPage() - 1);
         }
      }
      else if(source == jumpButton)
      {
         String s = jumpButton.getText();
         if(!s.trim().equals(s))
         {
            s = s.trim();
         }
         if(!s.equals(pageNumberDisplay))
         {
            try
            {
               int p = Integer.parseInt(s);
               if(p < 1)
               {
                  p = 1;
               }
               if(p > docPages)
               {
                  p = docPages;
               }
               if(window.getPage() != p)
               {
                  pageNumberDisplay = s;
                  window.setPage(p);
               }
            }
            catch(final NumberFormatException nfe)
            {
               // do nothing
            }
         }
      }
      else if(source == nextButton)
      {
         if(window.getPage() < docPages)
         {
            window.setPage(window.getPage() + 1);
         }
      }
      else if(source == zoomOutButton)
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
      
      jumpButton.setText("" + window.getPage());
      zoomSetButton.setText(window.getZoom() + "%");
      window.reDraw();
      border.layoutContainer(this);
      revalidate();
   }
   
   /**
    * close the PDDocument object that is being shown in the window.
    */
   public void closeDocument()
   {
      window.closeDocument();
   }
   
   private int docPages;
   private String pageNumberDisplay;
   private String zoomPercentDisplay;
   
   private final BorderLayout border;
   private final JButton prevButton;
   private final JTextField jumpButton;
   private final JButton nextButton;
   private final JButton zoomOutButton;
   private final JTextField zoomSetButton;
   private final JButton zoomInButton;
   private final JButton rotateButton;
   private final SinglePdfWindow window;
   
   private static final float _ZOOM_BUTTON_CHANGE = 1.2f;
   
   private static final String _DEFAULT_ZOOM_TEXT = "100%";
}
