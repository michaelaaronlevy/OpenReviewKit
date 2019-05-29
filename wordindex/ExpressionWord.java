package com.github.michaelaaronlevy.ork.wordindex;

import java.io.IOException;

import com.github.michaelaaronlevy.ork.util.AscendingStack;
import com.github.michaelaaronlevy.ork.util.TokenParser;

/**
 * this class represents either: a word in the word index or a variable. this
 * object does not know what it represents until it queries the {@link Context
 * Context}.
 * 
 * @author michaelaaronlevy@gmail.com
 *
 */
public class ExpressionWord extends Expression
{
   public ExpressionWord(final String word)
   {
      this.word = word;
   }
   
   protected AscendingStack calculate(final Context context)
   {
      context.getPagesFor(word);
      return context.getPagesFor(word);
   }
   
   public void writeScript(Appendable a) throws IOException
   {
      a.append(TokenParser.literalIfNeeded(word));
   }
   
   public final String word;
}
