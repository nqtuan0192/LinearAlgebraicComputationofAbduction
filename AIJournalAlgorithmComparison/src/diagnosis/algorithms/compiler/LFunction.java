/**
 * LFunction: Implements an object representing a function
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public class LFunction extends LConstant {
	// Instance variables

	public LinkedList<LObject> arguments;

	// Instance creation and initialization

	LFunction() {
		identifier = null;
		arguments = new LinkedList<LObject>();
	}

	LFunction(String str, LinkedList<LObject> v) {
		identifier = str;
		arguments = v;
	}

	// Accessing methods

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(identifier);
		if (arguments.size() != 0) {
			str.append("(");
			int i = arguments.size();
			for (LObject o : arguments) {
				str.append(o.toString());
				i--;
				if (i > 0) {
					str.append(", ");
				}
			}
			str.append(")");
		}
		return str.toString();
	}
}
