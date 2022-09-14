/**
 * ParserErrorException: 
 *
 * @version 0.1, DATE: 03.12.1998
 * @author Franz Wotawa
 *
 *
 * V0.1: Creating the basic functionality (29.12.1998)
 */

package diagnosis.algorithms.compiler;


public class ParserErrorException extends Exception
{

	private static final long serialVersionUID = 1L;
	
	public int position;

	// Instance creation and initialization

    public ParserErrorException() {
    	super();
    	position = 0;
    }

    public ParserErrorException(String str) {
       super(str);
       position = 0;
    }
    
    public ParserErrorException(String str, int pos) {
    	super(str);
    	position = pos;
    }
}
