package com.github.michaelaaronlevy.ork.util;

/**
 * a simple interface to signal that the implementing class is not only Runnable
 * but that it uses a standardized format for saving/sharing information about
 * its status.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the RunnableWithStatus interface) AND I AM
 * PLACING IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public interface RunnableWithStatus extends Runnable
{
   public StatusReporter getStatus();
}
