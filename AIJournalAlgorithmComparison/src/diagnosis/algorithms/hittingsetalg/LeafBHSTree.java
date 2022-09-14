/*
 * Created on Dec 11, 2003
 */
package diagnosis.algorithms.hittingsetalg;


/**
 * Representation of a LeafBHSTree, i.e. this Treepart has no right or left branches. 
 * C is empty and H is a single number (never EMPTY).
 * 
 * @invar: 	left_branch_ == null
 * @invar: 	right_branch_ == null
 * @invar: 	getC().size() == 0
 * @invar: 	getH() != EMPTY
 * @author fry
 */
public class LeafBHSTree extends SubSetTree {

/************************************************
 * 				CONSTRUCTORS
 ************************************************/
	
	/**
	 * Create a new instance of a LeafBHSTree with given parameters.
	 * @param C
	 * @param setClusters
	 */
	protected LeafBHSTree(SubSetContainer C, Object setClusters) {
		super(C, setClusters);
	}

	/**
	 * Create a new instance of a LeafBHSTree with given parameter.
	 * @param C
	 */
	protected LeafBHSTree(SubSetContainer C) {
		super(C);
	}

/************************************************
 * 				METHODS
 ************************************************/
	
	protected int getDepth(int depth){
		return depth;
	}
	
	public int getDepth(){
		return 0;
	}

	public void print(int depth){

		for(int i=0; i<SubSetTree.NBLINES; i++){
			super.print(depth);
			System.out.println("|");
		}
		super.print(depth);
		System.out.print("+-- ");
		System.out.println(getC().toString("", "", "<", ">", true, "") + 
				getRemovedThroughMinimizing().toString("", "", "<", ">*",false,",") + " [" + getH() + "]");
	}
	
/************************************************
 * 			MINIMAL HITTING SET ALGORITHM
 ************************************************/
	
	/*
	 * The actual minimal hitting set method.
	 * If a node is a leaf, the minimal hitting set of the node is H.
	 */
	public SubSetContainer getMinimalHittingSet(){
		SubSet tmp = new SubSet();
		tmp.add(getH());
		SubSetContainer ret = new SubSetContainer();
		ret.add(tmp);
		return ret;   //return H
	}
}
