/**
 * ErrorToken: Implements an error token class
 *
 * @version 1.0, DATE: 26.11.1998
 * @author Franz Wotawa
 *
 * 26.11.1998: First implementation
 */

package diagnosis.algorithms.compiler;

public class ErrorToken extends GenericToken
{
  public ErrorToken (String str, int i)
  {
    super(str,i);
  }

  // Testing...

  public boolean isErrorToken ()
  {
    return true;
  }

  // Printing...

  public String print ()
  {
    return "error(" + value + ")";
  }


}

