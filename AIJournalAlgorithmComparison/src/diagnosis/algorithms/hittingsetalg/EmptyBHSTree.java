package diagnosis.algorithms.hittingsetalg;

/**
 * Representation of the EmptyBHSTree. Both right and left branch are null.
 * C is empty and H is EMPTY. This EmptyBHSTree is created from the constructor of 
 * BHSTree, i.e. all possible BHSTrees are created through BHSTree(...).
 *
 * @invar:	left_branch_ == null
 * @invar:	right_branch_ == null
 * @invar:	getC().size() == 0
 * @invar:	getH() == Tree.EMPTY
 * @author fry
 */
public class EmptyBHSTree extends SubSetTree {

/************************************************
 * 				CONSTRUCTORS
 ************************************************/
	
	
	/**
	 * @param C
	 * @param setClusters
	 * @see: super
	 */
	protected EmptyBHSTree(SubSetContainer C, Object setClusters) {
		super(C, setClusters);
	}

	/**
	 * @param C
	 * @see: super
	 */
	protected EmptyBHSTree(SubSetContainer C) {
		super(C);
	}

	protected int getDepth(int depth){
		return depth-1;
	}

	public int getDepth(){
		return (-1);
	}

/************************************************
 * 				METHODS
 ************************************************/
	public void print(int depth){}

	public SubSetContainer getMinimalHittingSet(){
		return new SubSetContainer();
	}

}
