package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;

import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * this subclass of JPanel is a viewing pane to show a single page at a time
 * from a single PDF at a time. During the lifetime of this object, you can
 * change which PDF is being shown.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the SinglePdfWindow class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 * 
 * @see com.github.michaelaaronlevy.ork.util.pdfbox.SinglePdfViewer
 * 
 */
public class SinglePdfWindow extends JPanel
{
   public SinglePdfWindow()
   {
      super();
      empty();
   }
   
   public void reDraw()
   {
      if(doc == null)
      {
         return;
      }
      try
      {
         try
         {
            img = rend.renderImage(page - 1, zoom);
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
   public void empty()
   {
      doc = null;
      page = 0;
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
   public void setDocument(final PDDocument doc, final int initialPage)
   {
      if(doc != this.doc)
      {
         rotation = 0;
         this.doc = doc;
      }
      
      // set to an invalid page number (zero) so that when setPage is called two
      // lines later, it will refresh the view
      page = 0;
      rend = new PDFRenderer(doc);
      setPage(initialPage);
   }
   
   /**
    * 
    * @return the PDF page number that is being shown
    */
   public int getPage()
   {
      return page;
   }
   
   /**
    * 
    * @param newPage
    *           the PDF page number to show. (PDF page numbers start at 1, not
    *           0.)
    */
   public void setPage(int newPage)
   {
      if(doc == null || doc.getNumberOfPages() == 0)
      {
         empty();
         return;
      }
      else if(newPage < 1)
      {
         newPage = 1;
      }
      else if(newPage > doc.getNumberOfPages())
      {
         newPage = doc.getNumberOfPages();
      }
      
      if(page != newPage)
      {
         page = newPage;
         reDraw();
      }
   }
   
   public int getZoom()
   {
      return (int) (zoom * 100);
   }
   
   public void setZoom(float newZoom)
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
   
   public void changeZoom(final float factor)
   {
      setZoom(zoom * factor);
   }
   
   public int getRotation()
   {
      return rotation;
   }
   
   /**
    * 
    * @param newRotate
    *           the new rotation (0 is normal, 2 is upside-down)
    */
   public void setRotation(int newRotate)
   {
      newRotate %= 4;
      if(rotation != newRotate)
      {
         rotation = newRotate;
         reDraw();
      }
   }
   
   /**
    * rotate the display by 90 degrees (rotation will reset when the document
    * changes)
    */
   public void changeRotation()
   {
      rotation++;
      if(rotation == 4)
      {
         rotation = 0;
      }
   }
   
   /**
    * close the PDDocument object that is being shown in the window.
    */
   public void closeDocument()
   {
      if(doc != null)
      {
         try
         {
            doc.close();
         }
         catch(final IOException iox)
         {
            // do nothing
         }
         doc = null;
      }
   }
   
   private PDDocument doc;
   private int page;
   
   private PDFRenderer rend;
   private BufferedImage img;
   private float zoom;
   private int rotation;
   
   private static final float _DEFAULT_ZOOM = 1f;
   private static final float _MIN_ZOOM = .1f;
   private static final float _MAX_ZOOM = 9.99f;
}
