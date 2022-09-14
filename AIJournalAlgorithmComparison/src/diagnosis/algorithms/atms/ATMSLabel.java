package diagnosis.algorithms.atms;

import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

/**
 * @author Franz Wotawa
 * The ATMSLabel class implements a label used in an assumption based truth maintenance
 * system (ATMS). The label stores all environments that allow for deriving its
 * corresponding node. This implementation implements a double-linked list for storing
 * the environments.
 */
public class ATMSLabel {
	
	public static int minimizeCounter = 0;

	ATMSLabelElement first;
	ATMSLabelElement last;
	
	// Methods
	
	public ATMSLabel() {
		first = null;
		last = null;
	}
	
	/**
	 * @param env
	 * The addEnvironment method adds the parameter env to the list of all environments
	 * stored in the label. Note that the environment is stored but not copied. Hence,
	 * modifications of env are also visible in the label.
	 */
	public void addEnvironment (Set<ATMSNode> env) {
		ATMSLabelElement nextNode = new ATMSLabelElement();
		nextNode.elements = env;
		if (first == null) {	
			first = nextNode;
		} else {
			last.next = nextNode;
			nextNode.prev = last;
		}
		last = nextNode;
	}
	
	/**
	 * @param nLabel
	 * Adds all element of nLabel to self.
	 */
	public void addAllEnvironments(ATMSLabel nLabel) {
		ATMSLabelElement el = nLabel.first;
		while (el != null) {
			addEnvironment(el.elements);
			el = el.next;
		}
	}
	
	/**
	 * @param env
	 * Adds the environment to this ATMS label and removes all supersets of env from 
	 * this ATMS label.
	 */
	public void addEnvironmentRemoveSupersets(Set<ATMSNode> env) {
		removeSupersets(env);
		addEnvironment(env);
	}

	
	/**
	 * @param env
	 * Removes all elements that are supersets or equal to env.
	 */
	public void removeSupersets(Set<ATMSNode> env) {
		minimizeCounter++;
		ATMSLabelElement el = first;
		while (el != null) {			
			if (subset(env,el.elements)) {
				if (el.prev != null) {
					(el.prev).next = el.next; }
				else {
					first = el.next;
				}
				if (el.next != null) {
					(el.next).prev = el.prev;
				} else {
					last = el.prev;
				}
			}
			el = el.next;
		}
	}
	
	/**
	 * @return
	 * Returns a set comprising all my elements. Note that the elements are not copied
	 * and any modification of the elements has an influence to myself.
	 */
	public Set<Set<ATMSNode>> environment() {
		Set<Set<ATMSNode>> envs = new LinkedHashSet<Set<ATMSNode>>();
		ATMSLabelElement el = first;
		while (el != null) {
			envs.add(el.elements);
			el = el.next;
		}
		return envs;
	}
	
	
	/**
	 * @return
	 * Returns true if there are no elements stored in this label, and false, otherwise.
	 */
	public boolean isEmpty() {
		return first==null;
	}
	
	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString() {
		String str = "[";
		ATMSLabelElement el = first;
		while (el != null) {
			str = str + " [";
	        for (Iterator<ATMSNode> noit = (el.elements).iterator(); noit.hasNext();) {
	        	str = str + (noit.next()).toString() + " ";
	        }
	        str = str + "]";
	        el = el.next;
		}
		str = str + "]";
		return str;
	}
	
	/** 
	 * @return A set of set of strings that represent the textual 
	 * representation of the label 
	 */
	public Set<Set<String>> toSet() {
		Set<Set<String>> list = new LinkedHashSet<Set<String>>();
		ATMSLabelElement el = first;
		while (el != null) {
			Set<String> env = new LinkedHashSet<String>();
	        for (Iterator<ATMSNode> noit = (el.elements).iterator(); noit.hasNext();) {
	        	env.add((noit.next()).toString());
	        }
	        list.add(env);
	        el = el.next;
		}
		return list;
	}
	
	/**
	 * @param label
	 * @return
	 * Combines two ATMS label comprising elements that are a combination of 
	 * elements of the given labels and self.
	 */
	public ATMSLabel combine(ATMSLabel label) {
		ATMSLabel newLabel = new ATMSLabel();
		ATMSLabelElement el = first;
		while (el != null) {
			ATMSLabelElement el2 = label.first;
			while (el2 != null) {
				Set<ATMSNode> newS = new LinkedHashSet<ATMSNode>();
				newS.addAll(el.elements);
				newS.addAll(el2.elements);
				if (!newLabel.existSubset(newS) ) {
					newLabel.addEnvironmentRemoveSupersets(newS);
				}
				el2 = el2.next;
			}
			el = el.next;
		}
		return newLabel;
	}
	
	/**
	 * @param env
	 * @return
	 * Returns true if self contains an environment that is a subset of env, and false,
	 * otherwise.
	 */
	public boolean existSubset(Set<ATMSNode> env) {
		ATMSLabelElement el = first;
		while (el != null) {
			if (subset(el.elements,env))
				return true;
			el = el.next;
		}
		return false;
	}
	
	/**
	 * @param s1
	 * @param s2
	 * @return
	 * Returns true if s1 is a subset of s2, and false, otherwise.
	 */
	public static boolean subset(Set<ATMSNode> s1, Set<ATMSNode> s2) {
		// Returns true if s1 is a subset of s2
		if (s1.size()<=s2.size()) {
			if (s2.containsAll(s1)) 
				return true;
			else
				return false;
		} else 
			return false;
	}
}
