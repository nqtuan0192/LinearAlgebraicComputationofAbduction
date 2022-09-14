/**
 * LPredicate: Implements an object representing a logical predicate
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * This class is used for storing the information of a logical
 * predicate. It is not intended to be used for implementing
 * logical operations or consistency checks. To do this convert
 * my instances to a more appropriate format.
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public class LPredicate extends LObject
{
    // Instance variables

    public LinkedList<LObject> arguments;
    public String identifier;

    // Instance creation and initialization

    LPredicate()
    {
	arguments = new LinkedList<LObject>();
	identifier = null;
    }

    public LPredicate(String str, LinkedList<LObject> v)
    {
	arguments = v;
	identifier = str;
    }

    // Accessing methods

    public String toString()
    {
	StringBuffer str = new StringBuffer();
	str.append(identifier);
	if (arguments.size() != 0) {
	  str.append("(");
	  int i = arguments.size();
	  for (LObject o: arguments) {
	    str.append(o.toString());
	    i--;
	    if (i>0) { str.append(", ");}
	  }
	  str.append(")");
	}
	return str.toString();
    }

    public LinkedList<LObject> allPredicates(String str, int i, LinkedList<LObject> v)
    {
	if (identifier.equals(str) && (i == arguments.size())) {
	    v.add(this);
	}
	return v;
    }
}
