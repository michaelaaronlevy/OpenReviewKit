package com.github.michaelaaronlevy.ork.wordindex;

import java.util.TreeMap;
import java.util.TreeSet;
import java.util.function.UnaryOperator;

/**
 * a utility class to build a list of unique words, skipping words that are in
 * the skip list (which can be empty), skipping words that are shorter than
 * "min" characters, and truncating words that are longer than "max" characters.
 * Words are converted to lower case before they are saved.
 * 
 * I created this in order to support the searchable word index but you might
 * have another use for it.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class WordList implements UnaryOperator<String>
{
   /**
    * 
    * @param words
    *           all of the words in the skip list
    * @param min
    *           the minimum number of characters - if a word has less than this,
    *           it is skipped
    * @param max
    *           the maximum number of characters - if a word has more than this,
    *           it is shortened
    */
   public WordList(final String[] words, final int min, final int max)
   {
      minLength = min;
      maxLength = max;
      for(final String word : words)
      {
         if(word != null && word.length() >= min)
         {
            this.skips.add(word);
         }
      }
   }
   
   /**
    * 
    * @param word
    *           Can be null or empty. Can have uppercase letters.
    * @return true if this word must be skipped
    */
   public boolean isSkip(final String word)
   {
      if(word == null || word.length() < minLength)
      {
         return true;
      }
      else
      {
         return skips.contains(word.toLowerCase());
      }
   }
   
   /**
    * @param word
    *           cannot be null, must not contain uppercase letters
    * @return
    */
   private boolean isSkipUnchecked(final String word)
   {
      if(word.length() < minLength)
      {
         return true;
      }
      else
      {
         return skips.contains(word);
      }
   }
   
   /**
    * if this word is not to be skipped, and it is not already in the wordlist,
    * add it. Return null (if it should be skipped) or return a reference to the
    * unique version saved in the wordlist.
    */
   public String apply(String word)
   {
      word = word.toLowerCase();
      if(isSkipUnchecked(word))
      {
         return null;
      }
      if(word.length() > maxLength)
      {
         word = word.substring(0, maxLength) + "_";
      }
      final String r = uniques.get(word);
      if(r != null)
      {
         return r;
      }
      uniques.put(word, word);
      return word;
   }
   
   public boolean hasWord(String word)
   {
      word = word.toLowerCase();
      if(isSkipUnchecked(word))
      {
         return false;
      }
      if(word.length() > maxLength)
      {
         word = word.substring(0, maxLength) + "_";
      }
      return uniques.get(word) != null;
   }
   
   public String[] getWordList()
   {
      final String[] r = new String[uniques.size()];
      uniques.keySet().toArray(r);
      return r;
   }
   
   public String[] getSkipList()
   {
      final String[] r = new String[skips.size()];
      skips.toArray(r);
      return r;
   }
   
   /**
    * to free up memory
    */
   public void clear()
   {
      uniques.clear();
   }
   
   private final TreeSet<String> skips = new TreeSet<String>();
   private final TreeMap<String, String> uniques = new TreeMap<String, String>();
   
   public final int minLength;
   public final int maxLength;
}
