package com.github.michaelaaronlevy.ork;

import java.io.File;

import com.github.michaelaaronlevy.ork.ripping.TextPositionsParser;
import com.github.michaelaaronlevy.ork.ripping.Transformer;
import com.github.michaelaaronlevy.ork.util.Status;

/**
 * if you are writing a custom program based on {@link PdfToTextGrid PdfToTextGrid}, and you want to
 * be able to use the {@link ExtractionGUI ExtractionGUI} to run your program, you will need to
 * implement this interface. {@link com.github.michaelaaronlevy.ork.wordindex.ModeIndex ModeIndex} is one example of a custom program that
 * implements this interface (to generate a searchable word index).
 * {@link com.github.michaelaaronlevy.ork.sorcerer.ATTAccountStatement.Mode} is another example.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface ExtractionMode
{
   /**
    * 
    * @return the text to display on the Extraction GUI's mode button
    */
   public String buttonText();
   
   /**
    * 
    * @return the file extension for the output file. E.g., if your program is
    *         going to ultimately output a .csv, this should be ".csv";
    */
   public String getExtension();
   
   /**
    * 
    * @return the TextPositionsParser that will be passed to the {@link PdfToTextGrid PdfToTextGrid}'s
    *         constructor.
    */
   public TextPositionsParser getParser();
   
   /**
    * 
    * @return the Transformer that will be passed to the {@link PdfToTextGrid PdfToTextGrid}'s
    *         constructor.
    */
   public Transformer getTransformer();
   
   /**
    * 
    * @return the PageConsumer that will be passed to the {@link PdfToTextGrid PdfToTextGrid}'s
    *         constructor.
    */
   public PageConsumer getConsumer(final File targetOut);
   
   /**
    * this method is invoked at the very end of {@link PdfToTextGrid#run() PdfToTextGrid's run method}.
    * 
    * <p>For example, it can ask the user if they want to return to the text
    * extraction window or quit. Or it can change the interface to something
    * special. When a new searchable word index is created, the view changes to
    * a graphical user interface for the index. When a RangeFinder is set up,
    * the view changes to the RangeFinder display.
    * 
    * @param status
    *           showing whether PdfToTextGrid had errors.
    */
   public void post(final Status.ErrorStatus status);
}
