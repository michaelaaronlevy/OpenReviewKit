package com.github.michaelaaronlevy.ork.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Iterator;

/**
 * this class parses tokens from Strings (and writes tokens back to Strings)
 * according to some very, very simple rules. Tokens can be any of the
 * following: a word (as an instance of String), an operator (as an instance of
 * Character), or an array of other Tokens (as an instance of Object[]). An
 * array of tokens represents the contents of parenthesis.
 * 
 * <p>
 * The rules regarding what characters are permitted, how each character is
 * treated, and how the parser recognizes comments are handled by a
 * {@link TokenFormat TokenFormat} object. The {@link TokenFormat TokenFormat}
 * is set by default but it can be changed if you want different rules.
 * 
 * <p>
 * the parser recognizes text in the scripts as follows: it recognizes string
 * literals - each string literal is treated as a single word
 * 
 * <p>
 * outside of string literals, it recognizes each "operator" character as a
 * separate Operator token. It recognizes certain characters as dead space (in
 * the default implementation, whitespace is dead space) meaning it is not part
 * of any token. Everything else is treated as a "word"
 * 
 * <p>
 * For example:
 * 
 * <p>
 * a += cc-1;
 * 
 * <p>
 * Would be tokenized as: String("a") Character("+") Character("=") String("cc")
 * Character("-") String("1") Character(";")
 * 
 * <p>
 * a -(b + c)
 * 
 * <p>
 * Would be tokenized as: String("a") Character("-") Object[] {String("b"),
 * Character("+"), String("c")}
 * 
 * <p>
 * Also, (unless the {@link TokenFormat TokenFormat} object says otherwise)
 * string literals can contain certain special characters with the "escape"
 * marker. \b \t \0 \n \r \" \' \\ or \ u#### (where the four numbers are hex
 * digits). Using \' for single quotes is optional.
 * 
 * <p>
 * This class is meant to provide a very simple, general way to read scripts
 * into tokens so they can be interpreted at the next layer.
 * 
 * <p>
 * I AM THE SOLE AUTHOR OF THIS WORK (the TokenParser class) AND I AM PLACING
 * IT IN THE PUBLIC DOMAIN.
 * 
 * @author michaelaaronlevy@gmail.com
 * 
 * @see TokenFormatSimple TokenFormatSimple
 *
 */
public final class TokenParser
{
   public static Object[][] parseAll(final File f) throws ParseException, IOException
   {
      final BufferedReader b = new BufferedReader(new FileReader(f));
      String line = b.readLine();
      final ArrayList<String> content = new ArrayList<String>();
      while(line != null)
      {
         if(line.trim().length() > 0)
         {
            content.add(line);
         }
         line = b.readLine();
      }
      b.close();
      return parseAll(content.iterator());
   }
   
   public static Object[][] parseAll(final Iterator<String> lines) throws ParseException
   {
      final ArrayList<Object[]> r = new ArrayList<Object[]>();
      while(lines.hasNext())
      {
         final Object[] o = parse(lines.next());
         if(o != null)
         {
            r.add(o);
         }
      }
      return r.toArray(new Object[r.size()][]);
   }
   
   public static Object[] parse(String line) throws ParseException
   {
      if(line == null)
      {
         return null;
      }
      for(int i = 0; i < line.length(); i++)
      {
         final char c = line.charAt(i);
         if(!Character.isWhitespace(c))
         {
            break;
         }
         if(_format.illegalCharacter(c))
         {
            throw new ParseException("TokenFormat does not permit this character to appear: " + (int) c + " " + c,
                  line);
         }
      }
      for(int i = line.length() - 1; i >= 0; i--)
      {
         final char c = line.charAt(i);
         if(!Character.isWhitespace(c))
         {
            break;
         }
         if(_format.illegalCharacter(c))
         {
            throw new ParseException("TokenFormat does not permit this character to appear: " + (int) c + " " + c,
                  line);
         }
      }
      
      line = line.trim() + " ";
      if(line.length() == 1 || _format.isCommentAt(line, 0))
      {
         return null;
      }
      else
      {
         final ArrayList<Object> tokens = new ArrayList<Object>();
         final StringBuilder word = new StringBuilder();
         boolean inQuote = false;
         
         for(int index = 0; index < line.length(); index++)
         {
            final char c = line.charAt(index);
            if(inQuote)
            {
               if(_format.illegalLiteralCharacter(c))
               {
                  throw new ParseException("TokenFormat does not permit this character to appear: " + (int) c + " " + c,
                        line);
               }
               
               if(c == '\"')
               {
                  addWord(tokens, word);
                  inQuote = false;
               }
               else if(c == '\\')
               {
                  final char d = line.charAt(index + 1);
                  index++;
                  switch (d)
                  {
                  case 'b':
                     word.append('\b');
                     break;
                  case 't':
                     word.append('\t');
                     break;
                  case '0':
                     word.append('\0');
                     break;
                  case 'n':
                     word.append('\n');
                     break;
                  case 'r':
                     word.append('\r');
                     break;
                  case '\"':
                     word.append('\"');
                     break;
                  case '\'':
                     word.append('\'');
                     break;
                  case '\\':
                     word.append('\\');
                     break;
                  case 'u':
                     if(line.length() - index < 5)
                     {
                        throw new ParseException("Unicode literal must be \\u followed by four hex digits.", line);
                     }
                     final String hex = line.substring(index, index + 4).toUpperCase();
                     index += 4;
                     for(int i = 0; i < 4; i++)
                     {
                        final char h = hex.charAt(i);
                        if(h < '0' || h > 'E' || (h > '9' && h < 'A'))
                        {
                           throw new ParseException("Unicode literal must be \\u followed by four hex digits.", line);
                        }
                        final int unicode = Integer.parseUnsignedInt(hex, 16);
                        word.append((char) unicode);
                     }
                     break;
                  default:
                     throw new ParseException("Escape sequence in quote is followed by invalid character: " + c, line);
                  }
               }
               else
               {
                  word.append(c);
               }
            }
            else
            {
               if(_format.illegalCharacter(c))
               {
                  throw new ParseException("TokenFormat does not permit this character to appear: " + (int) c + " " + c,
                        line);
               }
               
               if(_format.isEmpty(c))
               {
                  addWord(tokens, word);
               }
               else if(_format.isWord(c))
               {
                  word.append(c);
               }
               else
               {
                  addWord(tokens, word);
                  if(c == '\"')
                  {
                     inQuote = true;
                  }
                  else
                  {
                     if(_format.isCommentAt(line, index))
                     {
                        break;
                     }
                     tokens.add(getOperator(c));
                  }
               }
            }
         }
         if(inQuote)
         {
            throw new ParseException("Unclosed string literal at end of line.", line);
         }
         setParens(tokens, line);
         return objectify(tokens);
      }
   }
   
   private static void addWord(final ArrayList<Object> tokens, final StringBuilder word)
   {
      if(word.length() != 0)
      {
         tokens.add(word.toString());
         word.delete(0, word.length());
      }
   }
   
   private static Character getOperator(final char c)
   {
      if(c == '(')
      {
         return _OPEN;
      }
      else if(c == ')')
      {
         return _CLOSE;
      }
      else
      {
         return Character.valueOf(c);
      }
   }
   
   private static Object[] objectify(final ArrayList list)
   {
      for(int i = 0; i < list.size(); i++)
      {
         if(list.get(i) instanceof ArrayList)
         {
            final Object[] oo = objectify((ArrayList) list.get(i));
            list.set(i, oo);
         }
      }
      return list.toArray(new Object[list.size()]);
   }
   
   private static void setParens(final ArrayList<Object> tokens, final String line) throws ParseException
   {
      for(int i = 0; i < tokens.size(); i++)
      {
         Object token = tokens.get(i);
         if(token == _CLOSE)
         {
            throw new ParseException("Encountered close parenthesis token without corresponding open parenthesis.",
                  line);
         }
         else if(token == _OPEN)
         {
            int depth = 1;
            for(int j = i + 1; j < tokens.size(); j++)
            {
               token = tokens.get(j);
               if(token == _OPEN)
               {
                  depth++;
               }
               else if(token == _CLOSE)
               {
                  if(depth == 1)
                  {
                     final ArrayList<Object> internal = new ArrayList<Object>(j - i);
                     for(int k = i + 1; k < j; k++)
                     {
                        internal.add(tokens.remove(i + 1));
                     }
                     tokens.remove(i + 1);
                     tokens.set(i, internal);
                     setParens(internal, line);
                     depth = 0;
                     break;
                  }
                  else
                  {
                     depth--;
                  }
               }
            }
            if(depth != 0)
            {
               throw new ParseException("Parenthesis is not closed.", line);
            }
         }
      }
   }
   
   public static String tokensToString(final Object tokens)
   {
      final StringBuilder b = new StringBuilder();
      try
      {
         if(tokens instanceof Object[])
         {
            printTokens(b, (Object[]) tokens);
         }
         else if(tokens instanceof Iterator)
         {
            printTokens(b, (Iterator) tokens);
         }
         else if(tokens instanceof Collection)
         {
            printTokens(b, ((Collection) tokens).iterator());
         }
         else
         {
            System.err.println("Invalid Token format: " + tokens.getClass());
         }
      }
      catch(final IOException iox)
      {
         // StringBuilder does not throw IOException
      }
      return b.toString();
   }
   
   public static String tokensToString(final Object[] tokens, final int start, final int end)
   {
      final StringBuilder b = new StringBuilder();
      try
      {
         printTokens(b, tokens, start, end);
      }
      catch(final IOException iox)
      {
         // StringBuilder does not throw IOException
      }
      return b.toString();
      
   }
   
   public static void printTokensToError(final Object tokens)
   {
      try
      {
         if(tokens instanceof Object[])
         {
            printTokens(System.err, (Object[]) tokens);
         }
         else if(tokens instanceof Iterator)
         {
            printTokens(System.err, (Iterator) tokens);
         }
         else if(tokens instanceof Collection)
         {
            printTokens(System.err, ((Collection) tokens).iterator());
         }
         else
         {
            System.err.println("Invalid Token format: " + tokens.getClass());
         }
      }
      catch(final IOException iox)
      {
         // PrintStream does not throw IOException
      }
   }
   
   public static void printTokens(final Appendable out, final Object[] tokens) throws IOException
   {
      printTokens(out, tokens, 0, tokens.length);
   }
   
   public static void printTokens(final Appendable out, final Object[] tokens, final int start, final int end)
         throws IOException
   {
      for(int i = start; i < end; i++)
      {
         final Object token = tokens[i];
         printToken(out, token);
         out.append(" ");
      }
   }
   
   public static void printTokens(final Appendable out, final Iterator<? extends Object> tokens) throws IOException
   {
      while(tokens.hasNext())
      {
         printToken(out, tokens.next());
         out.append(" ");
      }
   }
   
   public static void printToken(final Appendable out, final Object token) throws IOException
   {
      if(token == null)
      {
         out.append("null");
      }
      if(token instanceof String)
      {
         final String s = (String) token;
         out.append(literalIfNeeded(s));
      }
      else if(token instanceof Character)
      {
         out.append(((Character) token).charValue());
      }
      else if(token instanceof Object[])
      {
         out.append("( ");
         printTokens(out, (Object[]) token);
         out.append(")");
      }
      else
      {
         out.append("{invalid token:");
         out.append(token.getClass().getName());
         out.append("@");
         out.append(Integer.toHexString(token.hashCode()));
         out.append("}");
      }
   }
   
   public static class ParseException extends Exception
   {
      public ParseException(final String message, final String line)
      {
         this.message = message;
         this.line = line;
      }
      
      public ParseException(final String message, final Object[] line)
      {
         this(message, line, 0, line == null ? 0 : line.length);
      }
      
      public ParseException(final String message, final Object[] line, final int start, final int end)
      {
         this(message, tokensToString(line, start, end));
      }
      
      public String getMessage()
      {
         return "Parsing Error: " + message + System.lineSeparator() + "line: " + line;
      }
      
      public final String message;
      public final String line;
   }
   
   /**
    * test whether this word can be written to the script/display as-is, or if
    * it needs to be contained in a string literal
    * 
    * @param word
    * @return
    */
   public static boolean needsLiteral(final String word)
   {
      if(word == null)
      {
         return true;
      }
      for(int i = 0; i < word.length(); i++)
      {
         final char c = word.charAt(i);
         if(_format.illegalCharacter(c) || _format.isEmpty(c) || !_format.isWord(c))
         {
            return true;
         }
         switch (c)
         {
         case '\b':
         case '\0':
         case '\n':
         case '\r':
         case '\"':
         case '\\':
            return true;
         }
      }
      return false;
   }
   
   /**
    * make a string literal containing the contents of this String
    * 
    * @param word
    * @return
    */
   public static String literalVersion(final String word)
   {
      final StringBuilder b = new StringBuilder();
      b.append('\"');
      for(int i = 0; i < word.length(); i++)
      {
         final char c = word.charAt(i);
         if(_format.illegalLiteralCharacter(c))
         {
            b.append("\\u");
            String s = "000" + Integer.toHexString((int) c);
            s = s.substring(s.length() - 4, s.length());
            b.append(s);
         }
         else
         {
            switch (c)
            {
            case '\b':
               b.append("\\b");
               break;
            case '\t':
               b.append("\\t");
               break;
            case '\0':
               b.append("\\0");
               break;
            case '\n':
               b.append("\\n");
               break;
            case '\r':
               b.append("\\r");
               break;
            case '\"':
               b.append("\\\"");
               break;
            case '\\':
               b.append("\\\\");
               break;
            default:
               b.append(c);
            }
         }
      }
      b.append('\"');
      return b.toString();
   }
   
   /**
    * 
    * @param word
    * @return the word as-is if it can be displayed as-is, or as a string
    *         literal if that is necessary
    */
   public static String literalIfNeeded(final String word)
   {
      return needsLiteral(word) ? literalVersion(word) : word;
   }
   
   public static TokenFormat _format = new TokenFormatSimple("_$'", null, null, "//");
   
   private static final Character _OPEN = Character.valueOf('(');
   private static final Character _CLOSE = Character.valueOf(')');
}
