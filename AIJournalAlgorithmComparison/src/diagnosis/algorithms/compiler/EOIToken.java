/**
 * FloatToken: Implements an EOI token class
 *
 * @version 1.0, DATE: 19.08.1998
 * @author Franz Wotawa
 *
 * 19.8.1998: First implementation
 */

package diagnosis.algorithms.compiler;


public class EOIToken extends GenericToken
{
  public EOIToken (String str, int i)
  {
    super (str, i);
  }

  // Testing...

  public boolean isEOI ()
  {
    return true;
  }

  // Printing...

  public String print ()
  {
    return "EOI";
  }

}

