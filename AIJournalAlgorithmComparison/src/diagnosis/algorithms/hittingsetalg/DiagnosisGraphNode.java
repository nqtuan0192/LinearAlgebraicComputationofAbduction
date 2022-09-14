package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

/**
 * Implements a node of the HS tree implementing a diagnosis algorithm.
 *
 * @version 1.0, Date 14.03.2000
 * @author Franz Wotawa
 * @see modis.DiagnosisGraph
 */
public class DiagnosisGraphNode extends Object {

    protected int mark;
    protected int index;
    protected int min;
    protected DiagnosisGraphNode[] childs;
    protected DiagnosisGraphNode parents;
    protected int closedChilds;

    static protected int MINIMAL = 0;
    static protected int CLOSED  = -1;
    static protected int OPEN    = 1;

    /**
     * Returns a new instance of a graph node.
     * All instance variables are initialized to their default
     * values. The node is marked open, indices are set to 0, and
     * children and parents nodes are set to null.
     * @return a new instance
     */
    DiagnosisGraphNode () {
	super();
	mark = OPEN;
	index = 0;
	min = 0;
	childs = null;
	parents = null;
    }

    /**
     * Returns the number of children nodes that are currently not closed.
     * A value 0 indicates that all of my children are closed. Hence, myself
     * can be closed, too.
     * @return the number of nodes, NOT previously closed.
     */
    public int closedChilds() {
	return closedChilds;
    }

    /**
     * Decrements the number of not yet closed children nodes by 1.
     */
    public void decrClosedChilds() {
	closedChilds = closedChilds-1;
    }

    /**
     * Sets myself to the state MINIMAL. No children nodes are generated.
     * My hitting set is equal to a diagnosis.
     */
    public void setMinimal () {
	mark = MINIMAL;
    }

    /**
     * Sets myself to the state CLOSED. The closed children counter of
     * my parents is decremented and no children are generated for me.
     */
    public void setClosed () {
	mark = CLOSED;
	if (! this.isTopNode()) {
	    getParents().decrClosedChilds();
	}
    }

    /**
     * Sets myself to the state OPEN. It is possible that during further
     * computations I'm set to CLOSED or MINIMAL. 
     */
    public void setOpen () {
	mark = OPEN;
    }

    /**
     * Returns true if self is MINIMAL and false otherwise. This method is
     * called in order to determine my state.
     * @return a boolean indicating whether I'm minimal or not.
     */
    public boolean isMinimal () {
	return mark == MINIMAL;
    }

    /**
     * Returns true if self is CLOSED and false otherwise. This method is
     * called in order to determine my state.
     * @return a boolean indicating whether I'm closed or not.
     */
    public boolean isClosed () {
	return mark == CLOSED;
    }

    /**
     * Returns true if self is OPEN and false otherwise. This method is
     * called in order to determine my state.
     * @return a boolean indicating whether I'm open or not.
     */
    public boolean isOpen () {
	return mark == OPEN;
    }

    /**
     * Checks whether I'm the root node or not. A top node has no parents.
     * @return true, if self is the root and false, otherwise.
     */
    public boolean isTopNode () {
	return parents == null;
    }

    /**
     * Answers true if self has children nodes and false, otherwise.
     * @return a boolean indicating whether I've children or not.
     */
    public boolean hasChilds () {
	return childs != null;
    }

    /**
     * This method sets the minimum index to the given value.
     * @param i the minimum index
     */
    public void setMin (int i) {
	min = i;
    }

    /**
     * Reads my minimum index and returns it.
     * @return the minimum index.
     */
    public int getMin () {
	return min;
    }

    /**
     * This method sets the index to the given value.
     * @param i the index.
     */
    public void setIndex (int i) {
	index = i;
    }

    /**
     * Reads my index and returns it.
     * @return the index.
     */
    public int getIndex () {
	return index;
    }

    /**
     * This method is for reading my parents node.
     * @return the parents of the node.
     */
    public DiagnosisGraphNode getParents () {
	return parents;
    }

    /**
     * Sets the parents node to the given parameter.
     * @param n a DiagnosisGraphNode representing the parents.
     */
    public void setParents (DiagnosisGraphNode n) {
	parents = n;
    }

    /**
     * Computes the hitting set by stepping up from node to node until the
     * root node has been reached. The hitting set consists out of wrapper
     * objects having the same index than myself. Use this method for
     * computing the hitting set.
     * @param dict a dictionary storing the wrappers at their indices.
     * @return a Vector representing the hitting set.
     */
    public Vector hittingSet (DiagnosisCompWrapper[] dict) {
	Vector v = new Vector ();
	return hittingSet(v,dict);
    }

    /**
     * Computes the hitting set by stepping up from node to node until the
     * root node has been reached. The hitting set consists out of wrapper
     * objects having the same index than myself. Do not use this method
     * directly. Use the method hittingSet(dict) for computing the hitting 
     * set instead.
     * @param dict a dictionary storing the wrappers at their indices.
     * @param v the part of the hitting set found at previous steps.
     * @return a Vector representing the hitting set.
     */
    public Vector hittingSet (Vector v, DiagnosisCompWrapper[] dict) {
	if (this.isTopNode()) {
	    return v;
	} else {
	    v.addElement(dict[index-1]);
	    return parents.hittingSet(v,dict);
	}
    }

    /**
     * Removes all my children. During tree generation some branches
     * are not necessary and can be removed.
     */
    public void removeChilds() {
	childs = null;
    }

    /**
     * Initializes the array storing my children. The number of children
     * is determined by my index and my minimum index.
     * This method must be called before callind the child(i,n) method.
     */
    public void newChilds () {
	int diff = index - min + 1;
	childs = new DiagnosisGraphNode[diff];
	closedChilds = diff -1;
    }

    /**
     * Add the given graph as i-th child to myself. Use newChilds()
     * before calling this method!
     * @param i index of the child
     * @param n the child node
     */
    public void child (int i, DiagnosisGraphNode n) {
	if ((i >= min)&&(i<index)) {
	    childs[index-i-1] = n;
	}
    }

    /**
     * Returns my i-th child. If it does not exists null is returned.
     * @param i the child index
     * @return the child specified by the index
     */
    public DiagnosisGraphNode getChild(int i) {
	if ((i >= min)&&(i<index)) {
	    try {
		return childs[index-i-1];
	    } catch (NullPointerException e) {
		return null;
	    }
	}
	return null;
    }

    /**
     * Computes a vector containing the hitting sets of all minimal nodes
     * @param ds a vector storing the hitting sets, i.e., the diagnoses.
     * @param dict a dictionary storing the wrapper at their indices.
     */
    public void allMinimalNodes(Vector ds, DiagnosisCompWrapper[] dict) {
	if (this.isMinimal()) {
	    ds.addElement(this.hittingSet(dict));
	} else {
	    for(int i=min; i<index; i++) {
		DiagnosisGraphNode childNode = this.getChild(i);
		if (childNode != null) {
		    childNode.allMinimalNodes(ds,dict);
		}
	    }
	}
    }

    /**
     * Check the close condition. Return true if self can be closed,
     * and false, otherwise.
     * @param array, an array for storing the subset to be considered.
     * @param iVal, the last entry of the given array.
     * @return boolean, true if the close condition is fulfilled.
     */
    public boolean checkClose(int[] array, int iVal) {
	if (this.isTopNode()) {
	    return false;
	} else {
	    if ((this.getParents()).isTopNode()) {
		return false;
	    } else {
		DiagnosisGraphNode p = (this.getParents()).getParents();
		array[iVal+1] = index;
		if (p.checkTree(array,iVal+1)) {
		    return true;
		} else {
		    return (this.getParents()).checkClose(array,iVal+1);
		}
	    }
	}
    }

    /**
     * This method is called during checking the close condition.
     * See a description of the HS tree algorithm for more details.
     * @param array, an array for storing the currently considered subset
     * @param iVal, the last entry of the array.
     * @return boolean, true if the check was successfull.
     */
    public boolean checkTree(int[] array, int iVal) {
	int j;
	for (int i=0; i <= iVal; i++) {
	    j = array[i];
	    if ((j >= min)&&(j<=index)) {
		if (getChild(j) != null) {
		    if (getChild(j).isMinimal()) {
			return true;
		    } else if (getChild(j).isOpen()) {
			return getChild(j).checkTree(array,iVal - 1);
		    } else {
			return false;
		    }
		}
	    }
	}
	return false;
    }

    /**
     * This method checks the pruning condition. A node can be CLOSED
     * if it was open and if all children have been closed before.
     * @return boolean, true if self can be closed.
     */
    public boolean checkPrune() {
	if ((! this.isTopNode())&&(this.isClosed())) {
	    DiagnosisGraphNode p = getParents();
	    if (p.closedChilds() <= 0) {
		p.removeChilds();
		p.setClosed();
		p.checkPrune();
		return true;
	    } else {
		return false;
	    }
	}
	return false;
    }

    /**
     * Converts self to a string representation
     * @return a string
     */
    public String toString() {
	StringBuffer buf = new StringBuffer();

	buf.append("[" +
		   (new Integer(min)).toString() +
		   "," +
		   (new Integer(index)).toString() +
		   "]");
	if (isOpen()) {
	    buf.append(" OPEN");
	} else if (isClosed()) {
	    buf.append(" CLOSED");
	} else if (isMinimal()) {
	    buf.append(" MINIMAL");
	} else {
	    buf.append(" ERROR");
	}
	return buf.toString();
    }

    /**
     * Converts self to a string representation
     * @param dict, the wrapper array used for accessing the right wrapper
     * for the given index.
     * @return a string
     */
    public String toString(DiagnosisCompWrapper[] dict) {
	StringBuffer buf = new StringBuffer();

	buf.append(toString());
	if (index <= dict.length) {
	    buf.append(" " + (dict[index-1].toString()));
	}
	return buf.toString();
    }

    /**
     * Converts self to a string representation using a string buffer.
     * @param buf, the string buffer storing the string
     * @param dict, the wrapper array used for accessing the right wrapper
     * for the given index.
     */
    public void toStringBuffer(StringBuffer buf, int level, DiagnosisCompWrapper[] dict) {
	for (int j=1; j <= level; j++) {
	    buf.append("\t");
	}
	buf.append(toString(dict));
	buf.append("\n");

	for(int i=min; i<index; i++) {
	    DiagnosisGraphNode childNode = this.getChild(i);
	    if (childNode != null) {
		childNode.toStringBuffer(buf, level + 1,dict);
	    }
	}
    }

}


