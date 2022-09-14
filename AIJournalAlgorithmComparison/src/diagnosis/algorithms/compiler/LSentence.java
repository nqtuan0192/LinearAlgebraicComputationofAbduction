/**
 * LSentence: Implements an object representing a logical sentence
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * This class is used for storing the information of a logical
 * sentence. It is not intended to be used for implementing
 * logical operations or consistency checks. To do this convert
 * my instances to a more appropriate format.
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public class LSentence extends LObject {
	// Instance variables

	public LinkedList<LRule> rules;

	// Instance creation and initialization

	LSentence() {
		rules = new LinkedList<LRule>();
	}

	LSentence(LinkedList<LRule> sentence) {
		rules = sentence;
	}

	// Accessing methods

	public void addRule(LRule rule) {
		rules.add(rule);
	}

	public String toString() {
		StringBuffer str = new StringBuffer();
		for (LObject o : rules) {
			str.append(o.toString());
			str.append("\n\r");
		}
		return str.toString();
	}

	// Returns a list of predicates of the form str(X)
	public LinkedList<LObject> allPredicates(String str) {
		return allPredicates(str, 1);
	}

	// Returns a list of predicates of the form str(X1,..,Xi)
	public LinkedList<LObject> allPredicates(String str, int i) {
		LinkedList<LObject> v = new LinkedList<LObject>();
		for (LObject o: rules) {
			v = o.allPredicates(str, i, v);
		}
		return v;
	}

}
