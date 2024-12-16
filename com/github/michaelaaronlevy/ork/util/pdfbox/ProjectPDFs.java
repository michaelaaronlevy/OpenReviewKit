package com.github.michaelaaronlevy.ork.util.pdfbox;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;

/**
 * this class contains a list of PDF files, along with the number of pages in
 * each PDF, so that specific pages can be referenced either by the PDF + the
 * PDF page number, or by a single integer referring to a "project page #." The
 * "project page #" starts at 1 for the first page of the very first document
 * and increases for each PDF added. For every PDF added to ProjectPDFs, there
 * is a unique "project page #" for every page of that PDF and the highest valid
 * "project page #" is the same as the total number of pages in all of the PDFs
 * added to the ProjectPDFs.
 * 
 * <p>
 * the "project pdf #" is the same as the number listed as "total_page" in the
 * spreadsheet output and is the number kept in AscendingStack objects.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the ProjectPDFs class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public final class ProjectPDFs
{
   /**
    * 
    * @param directory
    *           for output such as notes files.
    * @param name
    *           the name of the project.
    * @param files
    *           the files comprising the project. If the list of pages is null,
    *           it will be necessary to open each one to count the number of
    *           pages.
    * @param pageCountPerFile
    *           an int[] listing the number of pages for each of the files.
    * @throws IOException
    */
   public ProjectPDFs(final File directory, final String name, final File[] files, final int[] pageCountPerFile)
         throws IOException
   {
      this.name = name;
      this.directory = directory;
      
      this.files = files.clone();
      names = new String[files.length];
      recentPDD = new RecencyHolder(_RECENCY_CAPACITY);
      this.pageCountPerFile = new int[files.length];
      pageEndPerFile = new int[files.length + 1];
      
      for(int i = 0; i < files.length; i++)
      {
         final File pdfFile = files[i];
         names[i] = pdfFile.getName();
         final int pdfPages;
         if(pageCountPerFile == null)
         {
            final PDDocument doc = Loader.loadPDF(files[i]);
            pdfPages = doc.getNumberOfPages();
            doc.close();
         }
         else
         {
            pdfPages = pageCountPerFile[i];
         }
         this.pageCountPerFile[i] = pdfPages;
         pageEndPerFile[i + 1] = pageEndPerFile[i] + pdfPages;
      }
   }
   
   public PageReference getReference(final int projectPage)
   {
      if(projectPage < 1 || projectPage > pageEndPerFile[files.length])
      {
         return null;
      }
      int r = Arrays.binarySearch(pageEndPerFile, projectPage);
      r = r < 0 ? -r - 1 : r - 0;
      final int pdfPage = projectPage - pageEndPerFile[r - 1];
      return new PageReference(this, projectPage, r, pdfPage);
   }
   
   /**
    * 
    * @param pdfIndex
    *           the index of the pdf in question, from 1...N (not zero)
    * @param pdfPage
    * @return the project page number associated with this page
    */
   public int getProjectPage(final int pdfIndex, final int pdfPage)
   {
      return pageEndPerFile[pdfIndex - 1] + pdfPage;
   }
   
   public int getPdfIndex(final File file)
   {
      if(file == null)
      {
         return -1;
      }
      for(int i = 0; i < files.length; i++)
      {
         if(files[i] != null && files[i].equals(file))
         {
            return i + 1;
         }
      }
      return -1;
   }
   
   public int getPdfIndex(final String fileName)
   {
      if(fileName == null)
      {
         return -1;
      }
      else if(fileName.indexOf('/') == -1 && fileName.indexOf('\\') == -1)
      {
         return getPdfIndex(new File(fileName));
      }
      else
      {
         for(int i = 0; i < files.length; i++)
         {
            if(files[i] != null && files[i].getName().equalsIgnoreCase(fileName))
            {
               return i + 1;
            }
         }
         return -1;
      }
   }
   
   public int getPdfIndex(final PDDocument doc)
   {
      if(doc == null)
      {
         return -1;
      }
      else
      {
         return recentPDD.getIndexForPdf(doc);
      }
   }
   
   /**
    * 
    * @param index
    *           1...N for the PDF
    * @return null if the PDDocument cannot be loaded
    */
   public synchronized PDDocument getPdfByIndex(final int index)
   {
      if(index < 1 || index > files.length)
      {
         return null;
      }
      try
      {
         PDDocument r = recentPDD.get(index);
         if(r != null)
         {
            return r;
         }
         r = Loader.loadPDF(files[index - 1]);
         recentPDD.add(index, r);
         return r;
      }
      catch(final IOException iox)
      {
         System.err.println(iox.getMessage());
         return null;
      }
   }
   
   public String getPdfNameByIndex(final int index)
   {
      return names[index - 1];
   }
   
   /**
    * 
    * @return the number of PDFs for which this object has mapped their pages to
    *         an ascending integer
    */
   public int getPdfCount()
   {
      return files.length;
   }
   
   /**
    * 
    * @param index
    *           1...N
    * @return the number of pages in this file
    */
   public int getPdfPageCount(final int index)
   {
      return pageCountPerFile[index - 1];
   }
   
   /**
    * 
    * @return the total number of PDF pages in the entire project
    */
   public int getProjectPageCount()
   {
      return pageEndPerFile[pageEndPerFile.length - 1];
   }
   
   public String getProjectName()
   {
      return name;
   }
   
   public File getProjectDirectory()
   {
      return directory;
   }
   
   public String toString()
   {
      final StringBuilder b = new StringBuilder();
      try
      {
         printDescription(b);
      }
      catch(final IOException iox)
      {
         // this will never happen because StringBuilder does not throw
      }
      return b.toString();
   }
   
   public void printDescription(final Appendable a) throws IOException
   {
      a.append("Project ");
      a.append(name);
      a.append(": ");
      a.append(Integer.toString(files.length));
      a.append(" files. {");
      for(int i = 0; i < files.length; i++)
      {
         a.append(names[i]);
         a.append("}[");
         a.append(Integer.toString(pageCountPerFile[i]));
         a.append(i == files.length - 1 ? "]." : "], {");
      }
   }
   
   private final File directory;
   private final String name;
   
   private final File[] files;
   private final String[] names;
   private final int[] pageCountPerFile;
   private final int[] pageEndPerFile;
   
   private final RecencyHolder recentPDD;
   
   private static final int _RECENCY_CAPACITY = 64;
}
