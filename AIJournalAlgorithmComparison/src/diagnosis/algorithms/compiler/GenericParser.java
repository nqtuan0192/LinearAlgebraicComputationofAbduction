/**
 * GenericParser: Implementation of a generic parser
 *
 * @version 0.1, DATE: 03.12.1998
 * @author Franz Wotawa
 *
 * The GenericParser class implements a parser used
 * for parsing strings. This class only provides the
 * basic methods. The concrete implementation must
 * be given by my subclasses.
 *
 * V0.1: Creating the basic functionality (03.12.1998,28.12.1998)
 * V0.2: Introducing exception handling (29.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;

public class GenericParser extends Object {

	// Instance variables ...

	protected GenericScanner scanner;
	protected String source;
	protected GenericToken actualToken;
	protected Object result;
	protected String errorMessage;
	protected int errorPosition;

	// Instance creation and initialization

	GenericParser() {
		errorMessage = null;
		scanner = defaultScanner();
		source = "";
		actualToken = null;
		result = null;
	}

	GenericParser(String str) {
		errorMessage = null;
		scanner = defaultScanner();
		source = str;
		actualToken = null;
		result = null;
	}

	public GenericScanner defaultScanner() {
		return new GenericScanner();
	}

	// Accessing

	public Object result() {
		return result;
	}

	public String errorMessage() {
		return errorMessage;
	}
	
	public int errorPosition() {
		return errorPosition;
	}

	// Parsing

	public boolean parse(String str)
	// Returns true if the parser accepts the given string and
	// false otherwise
	{
		boolean noerror = true;
		errorMessage = null;
		errorPosition = 0;
		result = defaultResult();
		source = str;
		actualToken = scanner.scanSource(str);
		try {
			parse();
		} catch (ParserErrorException e) {
			errorMessage = e.getMessage();
			errorPosition = e.position;
			noerror = false;
			
		}
		return noerror;
	}
	
	public boolean parseThrow(String str) throws ParserErrorException
	// Returns true if the parser accepts the given string and
	// false otherwise
	{
		boolean noerror = true;
		errorMessage = null;
		errorPosition = 0;
		result = defaultResult();
		source = str;
		actualToken = scanner.scanSource(str);

			parse();
		
		return noerror;
	}

	public Object defaultResult() {
		return null;
	}

	public boolean parseFile(String file){
		FileInputStream freader;
		String str;
		try {
			freader = new FileInputStream(file);
		} catch (FileNotFoundException e) {
			return false;
		}
		try {
			byte[] chars = new byte[freader.available()];
			freader.read(chars);
			freader.close();
			str = new String(chars);
		} catch (IOException e) {
			return false;
		}
		return parse(str);
	}

	// Private parsing

	public void parse() throws ParserErrorException {
	}

	public void nextToken() throws ParserErrorException {
		if (actualToken.isEOI()) {
			// Do nothing after detecting the end of the input
		} else {
			actualToken = scanner.scanToken();
		}

		if (actualToken.isErrorToken()) {
			throw new ParserErrorException("Lexical Error at position " + actualToken.value() + " "
					+ Integer.toString(actualToken.position()), actualToken.position());
		}
	}

	public void errorDetected() throws ParserErrorException {
		throw new ParserErrorException("Parser Error [ " + actualToken.value()
				+ " ] at position " + Integer.toString(actualToken.position()),actualToken.position());
	}

	public void errorDetected(String str) throws ParserErrorException {
		throw new ParserErrorException("Parser Error [ " + actualToken.value()
				+ " ] at position " + Integer.toString(actualToken.position())
				+ " [ " + str + " ] ", actualToken.position());
	}

}
