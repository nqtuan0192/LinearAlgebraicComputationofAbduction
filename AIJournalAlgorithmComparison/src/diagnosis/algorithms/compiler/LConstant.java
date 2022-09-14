/**
 * LConstant: Implements an object representing a constant value
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

public class LConstant extends LObject {
	// Instance variables

	public String identifier;

	// Instance creation and initialization

	LConstant() {
		identifier = null;
	}

	LConstant(String str) {
		identifier = str;
	}

	// Accessing methods

	public String toString() {
		StringBuffer str = new StringBuffer();
		str.append(identifier);
		return str.toString();
	}

}
