package com.github.michaelaaronlevy.ork.wordindex;

import com.github.michaelaaronlevy.ork.util.AscendingStack;

/**
 * the Java interface for an object that serves as an interface for the
 * searchable word index. Ork has only one implementation of this interface,
 * {@link FinderGUI FinderGUI}, which provides both a console- and GUI-based
 * interface (depending on which constructor is called).
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface Context
{
   void echo(String message);
   
   /**
    * get the pages associated with this word (either: in the index, or if this
    * is not in the index, it is a variable, so retrieve the value of the
    * variable, which is stored by the {@link Context Context} itself). The
    * {@link Context Context} object can have rules about whether a variable can
    * be named after a word that is already in the index or a variable that has
    * already been named.
    * 
    * @param word
    * @return
    */
   AscendingStack getPagesFor(String word);
   
   /**
    * the context object needs to perform the calculations associated with this
    * function. So the context object needs to interpret the function to decide
    * how to carry it out.
    * 
    * @param name
    * @param arguments
    * @return
    */
   AscendingStack calculateFunction(String name, Expression[] arguments);
   
   /**
    * The {@link Context Context} object is instructed to carry out this
    * function. That might involve performing calculations or it might not.
    * E.g., if the function is print("text to display") there would not be
    * calculations involved.
    * 
    * @param name
    * @param arguments
    */
   void runFunction(String name, Expression[] arguments);
   
   /**
    * 
    * @param name
    *           the name of the function
    * @return true if this function returns a value. Some functions return
    *         values. E.g., the function "or(a, b)" returns a result that can
    *         become the input for some other function. Other functions do not
    *         return values. E.g., print("hello") does not return a value that
    *         can be used by some other function. You can have "and(a, or(b,c))"
    *         but you cannot have "and(a, print("hello")".
    * 
    *         Implementations of the {@link Context Context} interface can have their own
    *         built-in functions which may or may not return values.
    */
   boolean functionReturnsValue(String name);
   
   /**
    * Set the value of a variable. The {@link Context Context} object can have rules about
    * variable naming, whether a variable can have the same name as a word in
    * the index, and whether a variable can be overwritten.
    * 
    * @param varName
    * @param as
    */
   void setVariable(String varName, AscendingStack as);
   
   /**
    * the {@link Context Context} object will display a list of pages. The format of this
    * display could vary between {@link Context Context}s. E.g., printing text to the console,
    * or putting a list of clickable page numbers into a GUI that the user can
    * click on to open up those PDF pages in a new window.
    * 
    * @param as
    */
   void displayValues(AscendingStack as);
}
