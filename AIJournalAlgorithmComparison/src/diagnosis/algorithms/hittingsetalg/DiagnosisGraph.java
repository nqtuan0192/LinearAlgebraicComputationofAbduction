package diagnosis.algorithms.hittingsetalg;

import java.util.Enumeration;
import java.util.Stack;
import java.util.Vector;

/**
 * This class implements the HS tree algorithm.
 * The HS tree algorithm is used for computing a hitting set for a 
 * collection of conflict sets. This is equal to the computation of
 * diagnoses from the same conflict sets.
 *
 * @version 1.0, Date 14.03.2000
 * @author Franz Wotawa
 * @see modis.DiagnosisGraphNode
 * @see modis.DiagnosisSystem
 * @see modis.PDiagnosisSystem
 */
public class DiagnosisGraph extends Object {

    protected DiagnosisGraphNode top;
    protected DiagnosisGraphSystem system;
    protected int index;
    protected int min;
    protected DiagnosisCompWrapper[] indexDict;
    protected int[] indexArray;
    protected int numDiagnoses;
    protected int diagSize;

    /**
     * Creates a new instance for the given diagnosis system and intializes
     * the instance variables to their default values.
     * @return the new instance
     */
    public DiagnosisGraph (DiagnosisGraphSystem system) {
	super();
	this.system = system;
	top = new DiagnosisGraphNode();
	min = (system.diagnosisComponents()).size();
	index = min + 1;
	indexDict = new DiagnosisCompWrapper[min];
	indexArray = new int[min];
	numDiagnoses = system.maxNumberOfDiagnoses();
	diagSize = system.maxDiagnosisSize();
    }

    /**
     * This method is for computing the hitting set tree. Use this method
     * for computing diagnoses.
     */
    public void computeHST() {
	Stack nodes = new Stack();
	top.setIndex(index);
	nodes.push(top);
	computeHST(nodes,new Stack(),0);
	numDiagnoses = system.maxNumberOfDiagnoses();
    }

    /**
     * This method implements the hitting set algorithm. Do not use it
     * directly for computing the diagnoses. Use computeHST() instead.
     * @param nodes a stack of nodes to be considered in the next step.
     * @param level the maximum depth of the generated HS tree.
     */
    public void computeHST(Stack nodes, Stack newNodes, int level) {
	Enumeration e;
	Vector co;
	// Stack newNodes = new Stack();

		while (! nodes.empty()) {
		    DiagnosisGraphNode n = (DiagnosisGraphNode)nodes.pop();
		    System.out.println("LEVEL " + level +" CMP " + n.toString());
		    if (! n.checkClose(indexArray,-1)) {
				co = nextConflictFor(n.hittingSet(indexDict));
				if (co == null) {
				    n.setMinimal();
				    numDiagnoses = numDiagnoses - 1;
				    if (numDiagnoses <= 0) {
					return;
				    }
				} else {
					
				    n.setMin(min+1);
				    if (n.getMin() < n.getIndex()) {
					n.newChilds();
					for(int i=n.getMin(); i < n.getIndex(); i++) {
					    DiagnosisGraphNode newN = new DiagnosisGraphNode();
					    newN.setParents(n);
					    n.child(i,newN);
					    newN.setIndex(i);
					    newNodes.push(newN);
					    System.out.println("newN pushed " + newN.toString());
					}
				    } else {
					n.setClosed();
					n.checkPrune();
				    }
				}
		    }  else {
				n.setClosed();
				n.checkPrune();
		    }
		}
		
		if ((! newNodes.empty())&&(diagSize>level)) {
		    computeHST(newNodes, nodes, level+1);
		}
    }

    /**
     * Computes the next conflict. The diagnosis wrapper are assigned a
     * new index if they were not been used before.
     * @param hs, a Vector storing a hitting set.
     */
    public Vector nextConflictFor(Vector hs) {
	Vector co = system.computeConflicts(hs);
	if (co != null) {
	    Enumeration e = co.elements();
	    while (e.hasMoreElements()) {
		DiagnosisCompWrapper comp = (DiagnosisCompWrapper)e.nextElement();
		if (! comp.hasIndex()) {
		    comp.setIndex(min);
		    indexDict[min-1] = comp;
		    min = min - 1; 
		}
	    }
	}
	return co;
    }

    /**
     * Returns all hitting set, e.g., diagnoses. 
     * @return the hitting sets
     */
    public Vector returnDiagnoses() {
	Vector result = new Vector ();
	if (top != null) {
	    top.allMinimalNodes(result,indexDict);
	}
	return result;
    }

    /**
     * Converts self to a string represenation.
     * @return a string representing the tree.
     */
    public String toString() {
	StringBuffer buf = new StringBuffer();
	if (top != null) {
	    top.toStringBuffer(buf,0,indexDict);
	}
	return buf.toString();
    }
}


