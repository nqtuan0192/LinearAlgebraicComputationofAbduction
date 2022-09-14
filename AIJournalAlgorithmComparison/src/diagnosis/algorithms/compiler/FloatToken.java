/**
 * FloatToken: Implements an float token class
 *
 * @version 1.0, DATE: 19.08.1998
 * @author Franz Wotawa
 *
 * 19.8.1998: First implementation
 */

package diagnosis.algorithms.compiler;

public class FloatToken extends GenericToken
{
  public FloatToken (String str, int i)
  {
    super(str,i);
  }

  // Testing...

  public boolean isFloat ()
  {
    return true;
  }

  // Printing...

  public String print ()
  {
    return "float(" + value + ")";
  }

}

