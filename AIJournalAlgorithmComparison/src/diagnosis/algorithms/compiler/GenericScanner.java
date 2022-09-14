/**
 * GenericScanner: Implementation of a generic scanner
 *
 * @version 0.1, DATE: 17.08.1998
 * @author Franz Wotawa
 *
 * The GenericScanner class implements a scanner used
 * for converting strings into tokens.
 *
 * V0.1: Creating the basic functionality (17.08.1998)
 */

package diagnosis.algorithms.compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;


public class GenericScanner extends Object
{

  // Instance variables ...

  protected String source;
  protected char nextChar;
  protected int position;
  protected StringBuffer charBuffer;

  // Instance creation and initialization

  public GenericScanner()
  {
    super();
    initialize();
  }

  public GenericScanner(String str)
  {
    super();
    initialize();
    source = str;
    getNextChar();
  }

  public static GenericScanner parseFile (String file)
  {
    FileInputStream freader;
    try{
      freader = new FileInputStream(file);}
    catch (FileNotFoundException e) {
      return null;
    }

    try {
      byte[] chars = new byte[freader.available()];
      freader.read(chars);
      freader.close();
      String str = new String(chars);
      return new GenericScanner(str);}
    catch (IOException e){
      return null;
    }
  }

  public void initialize()
  {
    source = "";
    nextChar = endOfInput();
    position = 0;
    charBuffer = new StringBuffer();
  }

  // Accessing

  public String source()
  {
    return source;
  }

  public int position()
  {
    return position;
  }


  // Private methods

  public char endOfInput ()
  {
    return 0;
  }

  public boolean isEndOfInput ()
  {
    return (nextChar == endOfInput());
  }

  public boolean isDelimiter (char c)
  {
    return false; // This method should be overwritten by my subclasses..
  }

  public String bufferValue ()
  {
    return charBuffer.toString();
  }

  // Public methods

  public void initSource (String str)
  {
    source = str;
    position = 0;
    getNextChar();
  }

  public char getNextChar ()
  {
      if (position < source.length()) {
	  nextChar = source.charAt(position);
	  charBuffer.append(nextChar);
	  position = position + 1; }
      else {
	  charBuffer.append('\u0000');
	  nextChar = endOfInput(); }
      return nextChar;
  }

  public GenericToken scanSource(String str)
  {
      initSource(str);
      return scanToken();
  }

  public void overReadSpaces()
  {
    if (isEndOfInput()) { return; }
    while (Character.isWhitespace(nextChar)) {
      getNextChar();
      if (isEndOfInput()) { return; }}
    charBuffer = new StringBuffer((new Character(nextChar)).toString());
  }

  public GenericToken scanToken ()
  {
    GenericToken actualToken;
    String str;
    
    overReadSpaces();

    // Ignore comments

    if (nextChar == '%') {
	while ((nextChar != '\n') && (nextChar != '\r')) {
	    getNextChar();
	    if (isEndOfInput()) { return new EOIToken("EOI",position); }
	}
	charBuffer = new StringBuffer();
	getNextChar();
	return scanToken();
    }

    // Classify tokens

    int oldPos = position;

    if (isEndOfInput()) {
      return new EOIToken("EOI",oldPos); }

    if (Character.isDigit(nextChar)) {
      return scanNumber(); }

    if (Character.isLetter(nextChar)) {
      return scanIdOrKeyword(); }

    if ((nextChar == ',') || (nextChar == '.') ||
	(nextChar == ')') || (nextChar == '(') || (nextChar == '|')) {
      str = bufferValue();
      actualToken = new DelimiterToken(str,oldPos);
      charBuffer = new StringBuffer();
      getNextChar();
      return actualToken;}

    if (nextChar == ':') {
      getNextChar();
      if (nextChar == '-') {
	actualToken = new DelimiterToken(bufferValue(),oldPos);
	charBuffer = new StringBuffer();
	getNextChar();
      }
      else {
	// Lexical Error detected
	actualToken = new ErrorToken(":- expected", oldPos);
	charBuffer = new StringBuffer();
	getNextChar();
      }
      return actualToken; }

    if (nextChar == '-') {
      getNextChar();
      if (nextChar == '>') {
	actualToken = new DelimiterToken(bufferValue(),oldPos);
	charBuffer = new StringBuffer();
	getNextChar();
      }
      else {
	if (Character.isDigit(nextChar)) {
	  actualToken = scanNumber();
	} else {
	  // Lexical Error detected
	  charBuffer = new StringBuffer();
	  getNextChar();
	  actualToken = new ErrorToken("-> or number expected", oldPos);
	}
      }
      return actualToken; }

    if (nextChar == '"') {
      return scanString(); }
    
    if (nextChar == '\'') {
      return scanCharacter(); }
    
    // Lexical Error detected
    charBuffer = new StringBuffer();
    getNextChar();
    return new ErrorToken("No token recognized", oldPos);
  }

  public GenericToken scanNumber ()
  {
    boolean floatFlag = false;
    String str;
    int oldPos = position;
    getNextChar();
    while (Character.isDigit(nextChar)) {
      getNextChar(); }
    if (nextChar == '.') {
      floatFlag = true;
      getNextChar();
      while (Character.isDigit(nextChar)) {
	getNextChar(); } }
    str = bufferValue().substring(0,bufferValue().length() - 1);
    charBuffer = new StringBuffer((new Character(nextChar)).toString());
    if (floatFlag) {
      return new FloatToken(str, oldPos);
    } else {
      return new IntegerToken(str, oldPos);
    }
  }

  public GenericToken scanIdOrKeyword ()
  {
    String str;
    int oldPos = position;
    getNextChar();
    while (Character.isLetterOrDigit(nextChar) || (nextChar == '_')) {
      getNextChar(); }
    str = bufferValue().substring(0,bufferValue().length() - 1);
    charBuffer = new StringBuffer((new Character(nextChar)).toString());
    return new IdentifierToken(str, oldPos);
  }

  public GenericToken scanString ()
  {
    String str;
    int oldPos = position;
    boolean flag = true;
    while (flag ) {
	getNextChar();
	if (nextChar == '\"') {
	    getNextChar();
	    if (nextChar != '\"') {
		flag = false; 
	    }
	}
	if (isEndOfInput()) {
	    return new ErrorToken("eoi detected while scanning string", oldPos);
	}
    }
  
    str = bufferValue().substring(1,bufferValue().length() - 2);
    charBuffer = new StringBuffer((new Character(nextChar)).toString());
    return new StringToken(str, oldPos);
  }

  public GenericToken scanCharacter ()
  {
	int oldPos = position;
    getNextChar();
    char ch = nextChar;
    getNextChar();
    if (nextChar == '\'') {
      charBuffer = new StringBuffer();
      getNextChar();
      return new CharacterToken(new Character(ch).toString(),oldPos);
    } else {
      // Lexical error detected
      charBuffer = new StringBuffer();
      getNextChar();
      return new ErrorToken("character token expected",oldPos);
    }
  }

}

