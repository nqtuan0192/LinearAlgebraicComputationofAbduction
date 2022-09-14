/**
 * FloatToken: Implements a character token class
 *
 * @version 1.0, DATE: 19.08.1998
 * @author Franz Wotawa
 *
 * 19.8.1998: First implementation
 */

package diagnosis.algorithms.compiler;

public class CharacterToken extends GenericToken
{
  public CharacterToken (String str, int i)
  {
    super(str,i);
  }

  // Testing...

  public boolean isCharacter ()
  {
    return true;
  }

  // Printing...

  public String print ()
  {
    return "character(" + value + ")";
  }


}

