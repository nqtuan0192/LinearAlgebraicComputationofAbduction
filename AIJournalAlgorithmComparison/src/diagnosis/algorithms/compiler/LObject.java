/**
 * LObject: Abstract class for representing logical sentences
 *
 * @version 0.1, DATE: 30.12.1998
 * @author Franz Wotawa
 *
 *
 * V0.1: Implementing the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public abstract class LObject extends Object {

	public void addRule(LRule rule) {
	}

	// Printing methods

	public abstract String toString();

	public LinkedList<LObject> allPredicates(String str, int i,
			LinkedList<LObject> v) {
		return v;
	}
}
