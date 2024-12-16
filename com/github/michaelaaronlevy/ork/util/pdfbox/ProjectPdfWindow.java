package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.awt.Color;
import java.awt.Component;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.geom.AffineTransform;
import java.awt.image.BufferedImage;
import java.io.IOException;
import javax.swing.JPanel;

import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.rendering.PDFRenderer;

/**
 * this class displays a single page corresponding to an arbitrary
 * {@link PageReference PageReference} object. The primary advantage of this
 * class over the similar "SinglePdfWindow" class is that a {@link PageReference
 * PageReference} can refer to a file that has been closed (to save memory) and
 * it (the {@link PageReference PageReference}, not the ProjectPdfWindow) can
 * re-open the PDF file as needed, whereas the {@link SinglePdfViewer
 * SinglePdfViewer} takes a PDDocument input and assumes that the PDDocument
 * will not be closed during its lifetime.
 * 
 * <p>
 * It is not named "PPVWindow," because it can be used outside of the
 * {@link ProjectPdfViewer ProjectPdfViewer} class.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ProjectPdfWindow class) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ProjectPdfWindow extends JPanel implements PPVElement, ComponentListener
{
   public ProjectPdfWindow()
   {
      super();
      empty();
      addComponentListener(this);
   }
   
   public void setContainer(final Component c)
   {
      container = c;
   }
   
   public void reDraw()
   {
      if(ref == null)
      {
         return;
      }
      try
      {
         try
         {
            img = rend.renderImage(page - 1, zoom);
            
            if(container != null)
            {
               final int h;
               final int w;
               
               if(rotation % 2 == 0)
               {
                  h = img.getHeight();
                  w = img.getWidth();
               }
               else
               {
                  w = img.getHeight();
                  h = img.getWidth();
               }
               
               final int maxH = container.getHeight();
               final int maxW = container.getWidth();
               
               if(h < maxH && w < maxW)
               {
                  float perc = Math.min(((float) maxH) / h, ((float) maxW) / w);
                  img = rend.renderImage(page - 1, perc * zoom);
               }
            }
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
   
   private void empty()
   {
      ref = null;
      page = 0;
      rend = null;
      img = null;
      zoom = _DEFAULT_ZOOM;
      rotation = 0;
      repaint();
   }
   
   public void updateView(final PageReference newRef)
   {
      if(newRef == null)
      {
         empty();
         return;
      }
      
      final PDDocument next = newRef.getPdf();
      if(next == null || next.getNumberOfPages() <= 0)
      {
         empty();
         return;
      }
      
      if(ref == null || ref.pdfIndex != newRef.pdfIndex)
      {
         rotation = 0;
         rend = new PDFRenderer(next);
      }
      ref = newRef;
      page = newRef.pdfPage;
      reDraw();
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
    * rotate the display by 90 degrees (it will reset when the document changes)
    */
   public void changeRotation()
   {
      rotation++;
      if(rotation == 4)
      {
         rotation = 0;
      }
   }
   
   public JPanel getComponent()
   {
      return this;
   }
   
   public void setResults(final int[] results)
   {
      throw new RuntimeException("Unsupported Operation for this Class");
   }
   
   public void componentResized(final ComponentEvent cev)
   {
      reDraw();
   }
   
   public void componentMoved(final ComponentEvent cev)
   {
      // do nothing
   }
   
   public void componentShown(final ComponentEvent cev)
   {
      reDraw();
   }
   
   public void componentHidden(final ComponentEvent cev)
   {
      // do nothing
   }
   
   private PageReference ref;
   private int page;
   private Component container = null;
   
   private PDFRenderer rend;
   private BufferedImage img;
   private float zoom;
   private int rotation;
   
   private static final float _DEFAULT_ZOOM = 1f;
   private static final float _MIN_ZOOM = .1f;
   private static final float _MAX_ZOOM = 9.99f;
}
