package com.github.michaelaaronlevy.ork.util.pdfbox;

import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * PageReference has information about a particular page, including the ability
 * to open the PDF file associated with that page. But it avoids giving access
 * generally to the {@link ProjectPDFs ProjectPDFs} object, to limit the
 * potential for mischief. It is immutable and does not expose references to
 * anything mutable except the PDDocument.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the PageReference class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class PageReference implements Comparable<PageReference>
{
   public PageReference(final ProjectPDFs project, final int projectPage, final int pdfIndex, final int pdfPage)
   {
      this.project = project;
      this.projectPage = projectPage;
      this.pdfIndex = pdfIndex;
      this.pdfPage = pdfPage;
   }
   
   public PageReference newReference(final int newProjectPage)
   {
      return project.getReference(newProjectPage);
   }
   
   public PDDocument getPdf()
   {
      return project.getPdfByIndex(pdfIndex);
   }
   
   public int getPdfPageCount()
   {
      return project.getPdfPageCount(pdfIndex);
   }
   
   public int getProjectPageCount()
   {
      return project.getProjectPageCount();
   }
   
   public int getPdfCount()
   {
      return project.getPdfCount();
   }
   
   public boolean isSameProject(final PageReference that)
   {
      return this.project == that.project;
   }
   
   public boolean isSameProject(final ProjectPDFs project)
   {
      return this.project == project;
   }
   
   public String toString()
   {
      return "pp:\t" + projectPage + "\tfile:\t" + pdfIndex + "/" + getPdfCount() + "\tpdfPg:\t" + pdfPage + "/"
            + getPdfPageCount();
   }
   
   public boolean equals(final PageReference that)
   {
      return this.compareTo(that) == 0;
   }
   
   public int compareTo(final PageReference that)
   {
      if(this.project != that.project)
      {
         return Integer.compare(this.project.hashCode(), that.project.hashCode());
      }
      else
      {
         return Integer.compare(this.projectPage, that.projectPage);
      }
   }
   
   public String getPdfName()
   {
      return project.getPdfNameByIndex(pdfIndex);
   }
   
   private final ProjectPDFs project;
   
   public final int projectPage;
   public final int pdfIndex;
   public final int pdfPage;
}
