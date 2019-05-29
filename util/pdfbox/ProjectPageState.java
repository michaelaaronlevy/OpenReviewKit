package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * this class is used to keep track of a reference to an arbitrary page within
 * the "project" (a group of PDFs), and making methods available to easily
 * manipulate the reference (e.g., moving to a specific page or document, or
 * moving to the next "search result") as well as keeping a list of listeners
 * which will be updated every time the page reference changes.
 * 
 * <p>
 * For example, it is used to control which page is shown by the
 * {@link ProjectPdfViewer ProjectPdfViewer}. Also it attempts to speed up
 * viewing by anticipating the PDFs that the user might want to view/read next.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ProjectPageState class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 * 
 * @see com.github.michaelaaronlevy.ork.util.pdfbox.ProjectPDFs ProjectPDFs
 * 
 */
public final class ProjectPageState
{
   public ProjectPageState(ProjectPDFs project)
   {
      this.project = project;
      this.focus = null;
   }
   
   /**
    * 
    * @param projectPageOfNewFocus
    *           the identifier or -1 for null
    */
   public synchronized void setFocusByProjectPage(final int projectPageOfNewFocus)
   {
      if(projectPageOfNewFocus == -1)
      {
         setFocusByPageReference(null);
      }
      else if(projectPageOfNewFocus < 1 || projectPageOfNewFocus > project.getProjectPageCount())
      {
         return;
      }
      else
      {
         setFocusByPageReference(project.getReference(projectPageOfNewFocus));
      }
   }
   
   public synchronized void setFocusByDocumentPage(final int pdfPageOfNewFocus)
   {
      if(focus == null || pdfPageOfNewFocus < 1 || pdfPageOfNewFocus > project.getPdfPageCount(focus.pdfIndex))
      {
         return;
      }
      setFocusByPageReference(project.getReference(project.getProjectPage(focus.pdfIndex, pdfPageOfNewFocus)));
   }
   
   public synchronized void setFocusByDocumentIndex(final int pdfIndexOfNewFocus, final int pdfPageOfNewFocus)
   {
      if(pdfIndexOfNewFocus < 1 || pdfIndexOfNewFocus > project.getPdfCount() || pdfPageOfNewFocus < 1
            || pdfPageOfNewFocus > project.getPdfPageCount(pdfIndexOfNewFocus))
      {
         return;
      }
      setFocusByPageReference(project.getReference(project.getProjectPage(pdfIndexOfNewFocus, pdfPageOfNewFocus)));
   }
   
   public synchronized void setFocusByPageReference(final PageReference newFocus)
   {
      if(newFocus != null && !newFocus.isSameProject(project))
      {
         throw new IllegalArgumentException("Cannot Use a PageReference based on a different ProjectPDFs");
      }
      focus = newFocus;
      for(final PPVElement c : components)
      {
         c.updateView(newFocus);
      }
      if(newFocus.pdfIndex > 1)
      {
         // pre-load the prior PDF for efficiency
         project.getPdfByIndex(newFocus.pdfIndex - 1);
      }
      if(newFocus.pdfIndex < project.getPdfCount())
      {
         // pre-load the next PDF for efficiency
         project.getPdfByIndex(newFocus.pdfIndex + 1);
      }
   }
   
   /**
    * if a valid search+index are given, update the page; otherwise, don't
    * change it
    * 
    * @param search
    * @param indexOfNewFocus
    */
   public synchronized void setFocusBySearch(final int[] search, int indexOfNewFocus)
   {
      if(search == null || indexOfNewFocus < 1 || indexOfNewFocus > search.length)
      {
         return;
      }
      int nextPage = search[indexOfNewFocus - 1];
      setFocusByProjectPage(nextPage);
      if(indexOfNewFocus != 1)
      {
         // pre-load the prior search result for efficiency
         project.getReference(search[indexOfNewFocus - 2]).getPdf();
      }
      if(indexOfNewFocus != search.length)
      {
         // pre-load the next search result for efficiency
         project.getReference(search[indexOfNewFocus]).getPdf();
      }
   }
   
   /**
    * 
    * @param delta
    *           positive or negative number of pages to move
    * @param canChangePdf
    *           if false, will only go as far as the start/end of a PDF and will
    *           not change to the next file
    */
   public synchronized void moveNPages(final int delta, final boolean canChangePdf)
   {
      if(focus == null || delta == 0)
      {
         return;
      }
      int newProjectPage = focus.projectPage + delta;
      if(newProjectPage < 1)
      {
         newProjectPage = 1;
      }
      else if(newProjectPage > project.getProjectPageCount())
      {
         newProjectPage = project.getProjectPageCount();
      }
      PageReference newRef = project.getReference(newProjectPage);
      if(canChangePdf || focus.pdfIndex == newRef.pdfIndex)
      {
         setFocusByPageReference(newRef);
      }
      else if(delta < 0)
      {
         setFocusByDocumentPage(1);
      }
      else
      {
         setFocusByDocumentPage(focus.getPdfPageCount());
      }
   }
   
   /**
    * 
    * @param forward
    *           if true, move to the next one (unless at the end); otherwise,
    *           try to move back a document. If the document changes (or if you
    *           are at the first document moving backwards) go to page 1 of the
    *           document.
    */
   public synchronized void moveFocus1Doc(final boolean forward)
   {
      if(focus == null)
      {
         return;
      }
      else if(forward)
      {
         if(focus.pdfIndex == project.getPdfCount())
         {
            return;
         }
         else
         {
            setFocusByDocumentIndex(focus.pdfIndex + 1, 1);
         }
      }
      else
      {
         if(focus.pdfIndex == 1)
         {
            setFocusByDocumentIndex(1, 1);
         }
         else
         {
            setFocusByDocumentIndex(focus.pdfIndex - 1, 1);
         }
      }
   }
   
   public synchronized void moveFocus1Search(final int[] search, final boolean isForward)
   {
      int currentProjectPage = focus == null ? 0 : focus.projectPage;
      int searchPoint = Arrays.binarySearch(search, currentProjectPage) + 1;
      if(searchPoint >= 0)
      {
         searchPoint += (isForward ? 1 : -1);
      }
      else
      {
         searchPoint = -searchPoint + (isForward ? 1 : 0);
      }
      
      if(searchPoint < 1)
      {
         searchPoint = 1;
      }
      else if(searchPoint > search.length)
      {
         searchPoint = search.length;
      }
      setFocusBySearch(search, searchPoint);
   }
   
   public synchronized PageReference getPageReference()
   {
      return focus;
   }
   
   public synchronized void addPPVListener(final PPVElement c)
   {
      if(!components.contains(c))
      {
         components.add(c);
      }
   }
   
   private final ProjectPDFs project;
   private PageReference focus = null;
   private final ArrayList<PPVElement> components = new ArrayList<PPVElement>();
}
