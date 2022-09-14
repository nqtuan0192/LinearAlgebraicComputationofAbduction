/**
 * LRule: Implements an object representing a logical rule
 *
 * @version 0.1, DATE: 230.12.1998
 * @author Franz Wotawa
 *
 * This class is used for storing the information of a logical
 * rule. It is not intended to be used for implementing
 * logical operations or consistency checks. To do this convert
 * my instances to a more appropriate format.
 * 
 * V0.1: Creating the basic functionality (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public class LRule extends LObject {
	// Instance variables

	public LinkedList<LObject> tail;
	public LinkedList<LObject> head;

	// Instance creation and initialization

	LRule() {
		tail = new LinkedList<LObject>();
		head = null;
	}

	public LRule(LinkedList<LObject> v, LinkedList<LObject> h) {
		tail = v;
		head = h;
	}

	// Accessing methods

	public String toString() {
		StringBuffer str = new StringBuffer();
		int i = tail.size();
		for (LObject o : tail) {
			str.append(o.toString());
			i--;
			if (i > 0) {
				str.append(", ");
			}
		}
		if(tail.size()>0){
			str.append(" -> ");
		}
		
		
		i = head.size();
		for (LObject o : head) {
			str.append(o.toString());
			i--;
			if (i > 0) {
				str.append("| ");
			}
		}
		str.append(".");
		return str.toString();
	}

	public LinkedList<LObject> allPredicates(String str, int i, LinkedList<LObject> v) {
		for (LObject o: tail) {
			v = o.allPredicates(str, i, v);
		}
		for (LObject o: head) {
			v = o.allPredicates(str, i, v);
		}
		return v;
	}
}
