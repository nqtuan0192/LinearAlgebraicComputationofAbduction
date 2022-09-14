/**
 * LVariable: Implements an object representing a variable
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

public class LVariable extends LConstant {
	// Instance creation and initialization

	LVariable() {
		identifier = null;
	}

	LVariable(String str) {
		identifier = str;
	}
}
