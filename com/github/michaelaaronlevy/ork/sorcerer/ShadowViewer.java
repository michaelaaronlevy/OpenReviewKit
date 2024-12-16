package com.github.michaelaaronlevy.ork.sorcerer;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Font;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTextField;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.state.PDExtendedGraphicsState;
import org.apache.pdfbox.rendering.PDFRenderer;
import org.apache.pdfbox.util.Matrix;

import com.github.michaelaaronlevy.ork.ExtractionMode;
import com.github.michaelaaronlevy.ork.MemenPage;
import com.github.michaelaaronlevy.ork.MemenText;
import com.github.michaelaaronlevy.ork.OpenReviewKit;
import com.github.michaelaaronlevy.ork.MemenText.IsEntirelyInBox;
import com.github.michaelaaronlevy.ork.PageConsumer;
import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.util.FontUtility;
import com.github.michaelaaronlevy.ork.util.Status;

/**
 * Display a single PDF (one page at a time) with a resizable shadow over it
 * (defaulting to a yellow, transparent shadow). This is a graphical tool to
 * quickly determine the coordinates of PDF contents, so you can build programs
 * that look for content within a specific part of a PDF. Within the
 * {@link com.github.michaelaaronlevy.ork.ExtractionGUI ExtractionGUI}, this
 * tool is referred to as the RangeFinder
 * 
 * <p>
 * An example of a program that uses coordinates to locate information in the
 * PDF is the AT&amp;T Account Statements parser. This graphical tool assisted
 * with finding and checking the coordinates used by that code, to speed up
 * development.
 * 
 * <p>
 * There is a panel on the right side of the window, which displays the text
 * that falls entirely within the boundaries of the yellow rectangle. These
 * coordinates correspond to the four arguments passed to the
 * {@link com.github.michaelaaronlevy.ork.MemenText.IsEntirelyInBox
 * IsEntirelyInBox} constructor.
 * 
 * <p>
 * (The RangeFinder will not permit more than one PDF to be loaded at a time)
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ShadowViewer extends JPanel implements PageConsumer
{
   public static class Mode implements ExtractionMode
   {
      /**
       * Information for the
       * {@link com.github.michaelaaronlevy.ork.ExtractionGUI ExtractionGUI} so
       * it can display a button to run this function.
       * 
       * @author michaelaaronlevy@gmail.com
       *
       */
      public Mode(final TextPositionsParser tpp, final Transformer t)
      {
         parser = tpp;
         transformer = t;
      }
      
      public String buttonText()
      {
         return "RangeFinder";
      }
      
      public String getExtension()
      {
         return null;
      }
      
      public TextPositionsParser getParser()
      {
         return parser;
      }
      
      public Transformer getTransformer()
      {
         return transformer;
      }
      
      public PageConsumer getConsumer(File targetOut)
      {
         viewer = new ShadowViewer();
         return viewer;
      }
      
      public void post(final Status.ErrorStatus status)
      {
         final OpenReviewKit ork = OpenReviewKit.getOrk();
         if(status == Status.ErrorStatus.NO_ERROR)
         {
            viewer.setDocument(viewer.toSet);
            if(ork != null)
            {
               ork.updateView(viewer, true);
            }
         }
         else
         {
            ork.continueOption("Error: Text Extraction Failed.",
                  "RangeFinder cannot function due to an error during text extraction.  Try again?",
                  JOptionPane.ERROR_MESSAGE);
         }
      }
      
      private final TextPositionsParser parser;
      private final Transformer transformer;
      private ShadowViewer viewer = null;
   }
   
   public ShadowViewer()
   {
      border = new BorderLayout();
      setLayout(border);
      
      final JPanel buttonsPanel = new JPanel();
      
      prevButton = new JButton("<");
      buttonsPanel.add(prevButton);
      
      jumpButton = new JTextField(4);
      jumpButton.setText("0");
      buttonsPanel.add(jumpButton);
      
      nextButton = new JButton(">");
      buttonsPanel.add(nextButton);
      
      zoomOutButton = new JButton("-");
      buttonsPanel.add(zoomOutButton);
      
      zoomSetButton = new JTextField(4);
      zoomSetButton.setText(_DEFAULT_ZOOM_TEXT);
      buttonsPanel.add(zoomSetButton);
      
      zoomInButton = new JButton("+");
      buttonsPanel.add(zoomInButton);
      
      rotateButton = new JButton("r");
      buttonsPanel.add(rotateButton);
      
      add(buttonsPanel, BorderLayout.PAGE_START);
      
      window = new ShadowWindow();
      final JScrollPane jsp = new JScrollPane();
      jsp.getViewport().setBackground(Color.CYAN);
      jsp.getViewport().add(window);
      
      add(jsp, BorderLayout.CENTER);
      
      xS = new JTextField(Integer.toString(rectXS), 8);
      xE = new JTextField(Integer.toString(rectXE), 8);
      yS = new JTextField(Integer.toString(rectYS), 8);
      yE = new JTextField(Integer.toString(rectYE), 8);
      
      prevButton.addActionListener(window);
      jumpButton.addActionListener(window);
      nextButton.addActionListener(window);
      zoomOutButton.addActionListener(window);
      zoomSetButton.addActionListener(window);
      zoomInButton.addActionListener(window);
      rotateButton.addActionListener(window);
      
      xS.addActionListener(window);
      xE.addActionListener(window);
      yS.addActionListener(window);
      yE.addActionListener(window);
      
      xS.addFocusListener(window);
      xE.addFocusListener(window);
      yS.addFocusListener(window);
      yE.addFocusListener(window);
      
      final JPanel fieldsPanel = new JPanel();
      
      fieldsPanel.add(new JLabel("IsEntirelyInBox ( Top:"));
      fieldsPanel.add(yS);
      fieldsPanel.add(new JLabel("Bottom:"));
      fieldsPanel.add(yE);
      fieldsPanel.add(new JLabel("Left:"));
      fieldsPanel.add(xS);
      fieldsPanel.add(new JLabel("Right:"));
      fieldsPanel.add(xE);
      fieldsPanel.add(new JLabel(" )"));
      
      add(fieldsPanel, BorderLayout.SOUTH);
      
      final JScrollPane jspList = new JScrollPane();
      jspList.getViewport().setBackground(Color.CYAN);
      jspList.getViewport().add(list);
      add(jspList, BorderLayout.EAST);
      
      final Font big = FontUtility.getFont("mainFont");
      final Font small = FontUtility.getFont("listFont");
      if(big != null)
      {
         FontUtility.setFonts(buttonsPanel, big);
         FontUtility.setFonts(fieldsPanel, big);
      }
      if(small != null)
      {
         FontUtility.setFonts(list, small);
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
   private void setDocument(final File file)
   {
      window.setDocument(file, 1);
      pageNumberDisplay = "" + window.getPage();
      jumpButton.setText(pageNumberDisplay);
      zoomPercentDisplay = window.getZoom() + "%";
      zoomSetButton.setText(zoomPercentDisplay);
      window.reDraw();
   }
   
   public void close()
   {
      window.closeDocument();
   }
   
   public void startProject(File[] files, int[] pageCounts) throws IOException
   {
      if(files.length > 1)
      {
         throw new IOException("ERROR: ShadowViewer can only view a single PDF at a time.");
      }
      memenPages.clear();
   }
   
   public void startFile(File file, int fileNumber, int pageCount) throws IOException
   {
      toSet = file;
   }
   
   public void takePage(int firstId, int totalPage, MemenPage page) throws IOException
   {
      memenPages.add(page);
   }
   
   public void endOfFile() throws IOException
   {
      // do nothing?
   }
   
   public void endOfProject() throws IOException
   {
      // do nothing
   }
   
   /**
    * the color of the rectangle
    */
   public Color color = Color.YELLOW;
   
   /**
    * the opacity of the rectangle
    */
   public float opacity = 0.4f;
   
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
   private final ShadowWindow window;
   
   private final JTextField xS;
   private final JTextField xE;
   private final JTextField yS;
   private final JTextField yE;
   
   private final ArrayList<MemenPage> memenPages = new ArrayList<MemenPage>();
   private final ArrayList<MemenText> words = new ArrayList<MemenText>();
   
   private final DefaultListModel<String> wordsInList = new DefaultListModel<String>();
   private final JList<String> list = new JList<String>(wordsInList);
   
   private File toSet = null;
   
   private static final float _ZOOM_BUTTON_CHANGE = 1.2f;
   
   private static final String _DEFAULT_ZOOM_TEXT = "100%";
   
   private int rectXS = 40000;
   private int rectXE = 80000;
   private int rectYS = 80000;
   private int rectYE = 120000;
   
   /**
    * displays a page from a PDF with a rectangular yellow shadow on it
    * 
    * @author michaelaaronlevy@gmail.com
    *
    */
   private class ShadowWindow extends JPanel implements ActionListener, FocusListener
   {
      private ShadowWindow()
      {
         super();
         empty();
      }
      
      private void reDraw()
      {
         if(docFile == null)
         {
            return;
         }
         
         try
         {
            if(template == null)
            {
               template = Loader.loadPDF(docFile);
            }
            if(shadowDoc != null)
            {
               shadowDoc.close();
            }
         }
         catch(final IOException iox)
         {
            // do nothing
         }
         
         try
         {
            words.clear();
            memenPages.get(page - 1).getWords(words, new IsEntirelyInBox(rectYS, rectYE, rectXS, rectXE));
            
            wordsInList.clear();
            wordsInList.addElement(words.size() + " Text Items Within Range.");
            wordsInList.addElement(
                  "----------------------------------------------------------------------------------------------------");
            int counter = 1;
            for(final MemenText mt : words)
            {
               String s = mt.text;
               if(s.length() > 80)
               {
                  s = s.substring(0, 80) + "...";
               }
               String num;
               if(counter < 10)
               {
                  num = "00" + counter;
               }
               else if(counter < 100)
               {
                  num = "0" + counter;
               }
               else
               {
                  num = Integer.toString(counter);
               }
               counter++;
               wordsInList.addElement(num + " " + s);
            }
            
            shadowDoc = new PDDocument();
            PDPage shadowPage = template.getPage(page - 1);
            shadowPage = shadowDoc.importPage(shadowPage);
            shadowDoc.addPage(shadowPage);
            shadowPage = shadowDoc.getPage(1);
            
            PDExtendedGraphicsState extendedGraphicsState = new PDExtendedGraphicsState();
            extendedGraphicsState.setNonStrokingAlphaConstant(opacity);
            
            final PDRectangle cropBox = shadowPage.getCropBox();
            final PDPageContentStream stream = new PDPageContentStream(shadowDoc, shadowPage,
                  PDPageContentStream.AppendMode.APPEND, false);
            stream.transform(
                  new Matrix(new AffineTransform(1, 0, 0, -1, cropBox.getLowerLeftX(), cropBox.getUpperRightY())));
            // thanks to StackOverflow user mkl for posting code similar to this
            // https://stackoverflow.com/questions/28093537/in-pdfbox-how-to-change-the-origin-0-0-point-of-a-pdrectangle-object
            // https://stackoverflow.com/users/1729265/mkl
            // https://creativecommons.org/licenses/by-sa/4.0/
            
            stream.setGraphicsStateParameters(extendedGraphicsState);
            
            stream.setNonStrokingColor(color);
            stream.addRect(rectXS / 1000.0f, rectYS / 1000.0f, (rectXE - rectXS) / 1000.0f,
                  (rectYE - rectYS) / 1000.0f);
            stream.fill();
            // thanks to Maruan Sahyoun for posting code that I found in
            // pdfbox-users mailing list archives
            
            stream.close();
            rend = new PDFRenderer(shadowDoc);
         }
         catch(final IOException iox)
         {
            iox.printStackTrace();
            empty();
            return;
         }
         
         try
         {
            try
            {
               img = rend.renderImage(0, zoom);
            }
            catch(final RuntimeException rte)
            {
               rte.printStackTrace(System.err);
               img = new BufferedImage(32, 32, BufferedImage.TYPE_INT_ARGB);
            }
            
            if(rotation % 2 == 0)
            {
               setSize(img.getWidth(), img.getHeight());
            }
            else
            {
               setSize(img.getHeight(), img.getWidth());
            }
            setPreferredSize(getSize());
            repaint();
         }
         catch(final IOException iox)
         {
            iox.printStackTrace(System.err);
            empty();
         }
      }
      
      public void paintComponent(final Graphics g)
      {
         g.setColor(Color.GRAY);
         g.fillRect(-100, -100, this.getWidth() + 100, this.getHeight() + 100);
         
         if(img != null)
         {
            if(g instanceof Graphics2D)
            {
               final Graphics2D g2 = (Graphics2D) g;
               AffineTransform at = new AffineTransform();
               at.translate(getWidth() / 2, getHeight() / 2);
               at.rotate((Math.PI / 2) * rotation);
               at.translate(-img.getWidth() / 2, -img.getHeight() / 2);
               g2.drawImage(img, at, null);
            }
            else
            {
               g.drawImage(img, 0, 0, null);
            }
         }
      }
      
      /**
       * show an empty pane.
       */
      private void empty()
      {
         docFile = null;
         shadowDoc = null;
         page = -1;
         
         rend = null;
         img = null;
         zoom = _DEFAULT_ZOOM;
         rotation = 0;
      }
      
      /**
       * 
       * @param doc
       *           the document to display
       * @param initialPage
       *           the first page number (1..N where N is the number of pages,
       *           there is no "page zero")
       */
      private synchronized void setDocument(final File newFile, final int initialPage)
      {
         docFile = newFile;
         try
         {
            template = Loader.loadPDF(newFile);
         }
         catch(final IOException iox)
         {
            iox.printStackTrace();
            empty();
            return;
         }
         
         rotation = 0;
         
         // set to an invalid page number (zero) so that when setPage is called
         // two lines later, it will refresh the view
         page = 0;
         rend = null;
         setPage(initialPage);
      }
      
      private int getPage()
      {
         return page;
      }
      
      private synchronized void setPage(int newPage)
      {
         if(template == null || template.getNumberOfPages() == 0)
         {
            empty();
            return;
         }
         else if(newPage < 1)
         {
            newPage = 1;
         }
         else if(newPage > template.getNumberOfPages())
         {
            newPage = template.getNumberOfPages();
         }
         
         if(page != newPage)
         {
            page = newPage;
            reDraw();
         }
      }
      
      private int getZoom()
      {
         return (int) (zoom * 100);
      }
      
      private void setZoom(float newZoom)
      {
         if(newZoom < _MIN_ZOOM)
         {
            newZoom = _MIN_ZOOM;
         }
         if(newZoom > _MAX_ZOOM)
         {
            newZoom = _MAX_ZOOM;
         }
         
         if(zoom != newZoom)
         {
            zoom = newZoom;
            
            reDraw();
         }
      }
      
      private void changeZoom(final float factor)
      {
         setZoom(zoom * factor);
      }
      
      /**
       * rotate the display by 90 degrees (it will reset when the document
       * changes)
       */
      private void changeRotation()
      {
         rotation++;
         if(rotation == 4)
         {
            rotation = 0;
         }
      }
      
      /**
       * close the document that is being shown in the window.
       */
      private void closeDocument()
      {
         if(template != null)
         {
            try
            {
               template.close();
            }
            catch(final IOException iox)
            {
               // do nothing
            }
            template = null;
         }
         if(shadowDoc != null)
         {
            try
            {
               shadowDoc.close();
            }
            catch(final IOException iox)
            {
               // do nothing
            }
            shadowDoc = null;
         }
      }
      
      public void focusGained(final FocusEvent fev)
      {
         // do nothing
      }
      
      public void focusLost(final FocusEvent fev)
      {
         actionPerformed(new ActionEvent(fev.getSource(), 0, ""));
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
            String s = jumpButton.getText().trim();
            try
            {
               int p = Integer.parseInt(s);
               if(p < 1)
               {
                  p = 1;
               }
               if(window.getPage() != p)
               {
                  window.setPage(p);
               }
            }
            catch(final NumberFormatException nfe)
            {
               // do nothing
            }
         }
         else if(source == nextButton)
         {
            window.setPage(window.getPage() + 1);
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
         else if(source == yS || source == yE || source == xS || source == xE)
         {
            final JTextField src = ((JTextField) source);
            final String text = src.getText();
            int i;
            try
            {
               i = Integer.parseInt(text);
            }
            catch(final NumberFormatException nfe)
            {
               i = source == yS ? rectYS : (source == yE ? rectYE : (source == xS ? rectXS : rectXE));
            }
            src.setText(Integer.toString(i));
            
            boolean flag = false;
            if(source == yS)
            {
               if(rectYS != i)
               {
                  rectYS = i;
                  flag = true;
               }
            }
            else if(source == yE)
            {
               if(rectYE != i)
               {
                  rectYE = i;
                  flag = true;
               }
            }
            else if(source == xS)
            {
               if(rectXS != i)
               {
                  rectXS = i;
                  flag = true;
               }
            }
            else if(source == xE)
            {
               if(rectXE != i)
               {
                  rectXE = i;
                  flag = true;
               }
            }
            
            if(rectYS > rectYE)
            {
               int temp = rectYS;
               rectYS = rectYE;
               rectYE = temp;
            }
            if(rectXS > rectXE)
            {
               int temp = rectXS;
               rectXS = rectXE;
               rectXE = temp;
            }
            
            yS.setText(Integer.toString(rectYS));
            yE.setText(Integer.toString(rectYE));
            xS.setText(Integer.toString(rectXS));
            xE.setText(Integer.toString(rectXE));
            
            if(flag)
            {
               reDraw();
            }
            // System.err.println("yS=" + rectYS + ", yE=" + rectYE + ", xS=" +
            // rectXS + ", xE=" + rectXE);
         }
         
         jumpButton.setText("" + window.getPage());
         zoomSetButton.setText(window.getZoom() + "%");
         reDraw();
         final ShadowViewer parent = ShadowViewer.this;
         border.layoutContainer(parent);
         parent.revalidate();
      }
      
      private File docFile = null;
      private PDDocument template = null;
      private PDDocument shadowDoc = null;
      private int page = -1;
      
      private PDFRenderer rend = null;
      private BufferedImage img = null;
      private float zoom = _DEFAULT_ZOOM;
      private int rotation = 0;
      
      private static final float _DEFAULT_ZOOM = 1f;
      private static final float _MIN_ZOOM = .1f;
      private static final float _MAX_ZOOM = 9.99f;
   }
}
