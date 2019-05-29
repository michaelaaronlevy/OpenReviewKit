package com.github.michaelaaronlevy.ork.util;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.function.Predicate;

import javax.swing.DefaultListModel;
import javax.swing.JButton;
import javax.swing.JFileChooser;
import javax.swing.JList;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.ListSelectionModel;

/**
 * a utility class to support showing a list of files, which permits drag and
 * drop operations to add more files, as well as a series of buttons above to
 * add/remove files and reorder them.
 * 
 * <p>
 * This is used by the {@link com.github.michaelaaronlevy.ork.ExtractionGUI text
 * extraction GUI} as well as the {@link FilesCombiner FilesCombiner} utility
 * tool. In general, if you want users to be able to select arbitrary files
 * before pressing a "run" button (or perhaps a more complicated series of
 * buttons etc.) this is the class to use.
 * 
 * <p>
 * I AUTHORED THIS WORK BASED ON MATERIALS IN THE PUBLIC DOMAIN AND I AM PLACING
 * THIS WORK (the DragDropFilesList class) INTO THE PUBLIC DOMAIN
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class DragDropFilesList implements FileDrop.Listener
{
   public DragDropFilesList(final Component context, final JFileChooser jfc, final Predicate<File> gatekeeper,
         final Font buttonsFont, final Font listFont)
   {
      this.context = context;
      this.gatekeeper = gatekeeper;
      if(jfc == null)
      {
         this.jfc = new JFileChooser();
         this.jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
         this.jfc.setMultiSelectionEnabled(true);
      }
      else
      {
         this.jfc = jfc;
      }
      list.setSelectionMode(ListSelectionModel.MULTIPLE_INTERVAL_SELECTION);
      
      final Listener listener = new Listener();
      
      upButton.addActionListener(listener);
      downButton.addActionListener(listener);
      removeButton.addActionListener(listener);
      addButton.addActionListener(listener);
      
      upButton.setFocusable(false);
      downButton.setFocusable(false);
      removeButton.setFocusable(false);
      addButton.setFocusable(false);
      
      if(buttonsFont != null)
      {
         upButton.setFont(buttonsFont);
         downButton.setFont(buttonsFont);
         removeButton.setFont(buttonsFont);
         addButton.setFont(buttonsFont);
      }
      if(listFont != null)
      {
         list.setFont(listFont);
      }
      
      buttonsPanel.add(upButton);
      buttonsPanel.add(downButton);
      buttonsPanel.add(removeButton);
      buttonsPanel.add(addButton);
      
      mainPanel.setLayout(new BorderLayout());
      mainPanel.add(buttonsPanel, BorderLayout.NORTH);
      mainPanel.add(filesDisplay, BorderLayout.CENTER);
      
      new FileDrop(null, filesDisplay, false, this);
   }
   
   /**
    * add multiple files to the list. (This is the method called by
    * {@link FileDrop FileDrop} but it can be called by other classes too.)
    */
   public void filesDropped(final File[] files)
   {
      for(final File f : files)
      {
         addFile(f);
      }
   }
   
   private synchronized void addClicked()
   {
      final int x = jfc.showOpenDialog(context);
      if(x == JFileChooser.APPROVE_OPTION)
      {
         final File[] ff = jfc.getSelectedFiles();
         filesDropped(ff);
      }
   }
   
   private synchronized void removeClicked()
   {
      final int[] array = list.getSelectedIndices();
      for(int i = array.length - 1; i >= 0; i--)
      {
         final int toRemove = array[i];
         filesInList.remove(toRemove);
      }
      
      list.validate();
      for(final ActionListener listener : listeners)
      {
         listener.actionPerformed(new ActionEvent(this, -1, null));
      }
   }
   
   private synchronized void vertical(final boolean isUp)
   {
      final int[] array = list.getSelectedIndices();
      if(array.length == 0 || array.length == filesInList.size())
      {
         return;
      }
      
      // System.err.println("selected is: " + Arrays.toString(array));
      
      final int index = isUp ? array[0] - 1 : array[array.length - 1] + 2;
      // System.err.println("index is: " + index);
      
      final ArrayList<File> one = new ArrayList<File>();
      final ArrayList<File> two = new ArrayList<File>();
      final ArrayList<File> three = new ArrayList<File>();
      for(int i = 0; i < index; i++)
      {
         if(i < filesInList.size() && Arrays.binarySearch(array, i) < 0)
         {
            // System.err.println("Adding to One: " + i);
            one.add(filesInList.getElementAt(i));
         }
         else
         {
            // System.err.println("Not adding to One: " + i);
         }
      }
      for(int i : array)
      {
         // System.err.println("Adding to Two: " + i);
         two.add(filesInList.getElementAt(i));
      }
      for(int i = index; i < filesInList.size(); i++)
      {
         if(i >= 0 && Arrays.binarySearch(array, i) < 0)
         {
            // System.err.println("Adding to Three: " + i);
            three.add(filesInList.getElementAt(i));
         }
         else
         {
            // System.err.println("Not adding to Three: " + i);
         }
      }
      filesInList.removeAllElements();
      for(
      
      final File f : one)
      {
         filesInList.addElement(f);
      }
      for(final File f : two)
      {
         filesInList.addElement(f);
      }
      for(final File f : three)
      {
         filesInList.addElement(f);
      }
      list.setSelectionInterval(one.size(), one.size() + two.size() - 1);
   }
   
   /**
    * 
    * @param listener
    *           an ActionEvent is generated whenever files are added or removed.
    *           (Multiple files added/removed at the same time each cause a
    *           separate ActionEvent). ActionEvent format is: (a) reference to
    *           this; (b) 1 or -1 if added or removed; (c) a string with the
    *           absolute file path of the file in question
    */
   public synchronized void addActionListener(final ActionListener listener)
   {
      if(!listeners.contains(listener))
      {
         listeners.add(listener);
      }
   }
   
   /**
    * attempt to remove this file from the list.
    * 
    * @param f
    * @return false if it was not present in the list; true if it was removed
    *         (either way, it is not in the list after this method is called)
    */
   public synchronized boolean removeFile(final File f)
   {
      final Enumeration<File> e = filesInList.elements();
      final int size = filesInList.size();
      int index = 0;
      while(e.hasMoreElements())
      {
         final File ef = e.nextElement();
         if(ef.equals(f))
         {
            break;
         }
         index++;
      }
      if(index == size)
      {
         return false;
      }
      filesInList.remove(index);
      list.validate();
      for(final ActionListener listener : listeners)
      {
         listener.actionPerformed(new ActionEvent(this, -1, f.getAbsolutePath()));
      }
      return true;
   }
   
   /**
    * add a single file to the list
    * 
    * @param f
    *           the file to add
    * @return -1 if it was blocked by the GateKeeper, 0 if it is already in the
    *         list, or N if it was added, where N is the total number of files.
    */
   public int addFile(final File f)
   {
      if(filesInList.contains(f))
      {
         return 0;
      }
      else if((gatekeeper != null && !gatekeeper.test(f)))
      {
         return -1;
      }
      filesInList.addElement(f);
      list.validate();
      for(final ActionListener listener : listeners)
      {
         listener.actionPerformed(new ActionEvent(this, 1, f.getAbsolutePath()));
      }
      return filesInList.getSize();
   }
   
   public synchronized Enumeration<File> getFilesEnumeration()
   {
      return filesInList.elements();
   }
   
   public synchronized File[] getFilesArray()
   {
      final File[] r = new File[getNumberOfFiles()];
      final Enumeration<File> ef = filesInList.elements();
      for(int i = 0; i < r.length; i++)
      {
         r[i] = ef.nextElement();
      }
      return r;
   }
   
   public synchronized int getNumberOfFiles()
   {
      return filesInList.size();
   }
   
   /**
    * JPanel with a BorderLayout, buttons to the NORTH, the JScrollPane with the
    * files list in the CENTER. You can add your own components to the
    * WEST/EAST/SOUTH.
    */
   public final JPanel mainPanel = new JPanel();
   
   private Component context = null;
   
   private final DefaultListModel<File> filesInList = new DefaultListModel<File>();
   private final JList<File> list = new JList<File>(filesInList);
   
   private final JScrollPane filesDisplay = new JScrollPane(list);
   
   private final JButton upButton = new JButton(_UP_BUTTON);
   private final JButton downButton = new JButton(_DOWN_BUTTON);
   private final JButton removeButton = new JButton(_REMOVE_BUTTON);
   private final JButton addButton = new JButton(_ADD_BUTTON);
   private final JPanel buttonsPanel = new JPanel();
   
   private final JFileChooser jfc;
   private final Predicate<File> gatekeeper;
   
   private final ArrayList<ActionListener> listeners = new ArrayList<ActionListener>();
   
   private static final String _UP_BUTTON = "\u2191";
   private static final String _DOWN_BUTTON = "\u2193";
   private static final String _REMOVE_BUTTON = "-";
   private static final String _ADD_BUTTON = "+";
   
   private class Listener implements ActionListener
   {
      public void actionPerformed(final ActionEvent aev)
      {
         final Object source = aev.getSource();
         if(source == upButton || source == downButton)
         {
            vertical(source == upButton);
         }
         else if(source == addButton)
         {
            addClicked();
         }
         else if(source == removeButton)
         {
            removeClicked();
         }
      }
   }
}
