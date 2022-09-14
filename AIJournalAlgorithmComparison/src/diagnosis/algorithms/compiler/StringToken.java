/**
 * FloatToken: Implements a string token class
 *
 * @version 1.0, DATE: 19.08.1998
 * @author Franz Wotawa
 *
 * 19.8.1998: First implementation
 */

package diagnosis.algorithms.compiler;

public class StringToken extends GenericToken
{
  public StringToken (String str, int i)
  {
    super(str,i);
  }

  // Testing...

  public boolean isString ()
  {
    return true;
  }

  // Printing...

  public String print ()
  {
    return "string(" + value + ")";
  }

}

