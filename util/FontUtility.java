package com.github.michaelaaronlevy.ork.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Container;
import java.awt.Font;
import java.util.TreeMap;

import javax.swing.JButton;
import javax.swing.JFrame;

/**
 * Calculates the screen height/width, provides a central storage point for Font
 * objects, and has convenience methods to attempt application of a Font to a
 * JComponent (including all of its children, children's children, etc.)
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the FontUtility class) AND I AM PLACING IT
 * IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class FontUtility
{
   public static void calculateScreenSize()
   {
      final JFrame frame = new JFrame();
      final int es = frame.getExtendedState();
      final JButton button = new JButton("x");
      frame.add(button, BorderLayout.CENTER);
      frame.setExtendedState(frame.getExtendedState() | JFrame.MAXIMIZED_BOTH);
      frame.setVisible(true);
      frame.repaint();
      screenWidth = frame.getWidth();
      screenHeight = frame.getHeight();
      frame.setVisible(false);
      frame.setExtendedState(es);
      frame.remove(button);
   }
   
   public static int getScreenWidth()
   {
      if(screenWidth == -1)
      {
         calculateScreenSize();
      }
      return screenWidth;
   }
   
   public static int getScreenHeight()
   {
      if(screenHeight == -1)
      {
         calculateScreenSize();
      }
      return screenHeight;
   }
   
   public static void putFont(final String name, final Font font)
   {
      fonts.put(name, font);
   }
   
   public static Font getFont(final String name)
   {
      return fonts.get(name);
   }
   
   /**
    * 
    * @param c
    * @param font
    *           the font to apply recursively to the component and all of its
    *           child components
    */
   public static void setFonts(final Component c, final Font font)
   {
      final Component[] array = new Component[1];
      array[0] = c;
      setFonts(array, font);
   }
   
   /**
    * 
    * @param c
    * @param font
    *           the font to apply recursively to every component in the array,
    *           and all of their child components
    */
   public static void setFonts(final Component[] c, final Font font)
   {
      for(int x = 0; x < c.length; x++)
      {
         if(c[x] instanceof Container)
         {
            setFonts(((Container) c[x]).getComponents(), font);
         }
         if(c[x] instanceof JButton)
         {
            ((JButton) c[x]).setFocusPainted(false);
         }
         try
         {
            c[x].setFont(font);
         }
         catch(final Exception exc)
         {
            // do nothing
         }
      }
   }
   
   private static int screenWidth = -1;
   private static int screenHeight = -1;
   
   private static final TreeMap<String, Font> fonts = new TreeMap<String, Font>();
}
