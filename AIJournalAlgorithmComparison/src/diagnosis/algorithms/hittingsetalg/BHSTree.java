/*
 * Created on Dec 11, 2003
*/
package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

import support.*;



/**
 * An extension to the general Tree-class. A BHS-Tree is the Binary Hitting Set Tree
 * It has both a left and right branch containing new trees. 
 *
 * @see: 	super
 * @invar:	left_ != null and right_ != null	
 * @author 	fry
 */
public class BHSTree extends SubSetTree {

/************************************************
 * 				CONSTRUCTORS
 ************************************************/
	
	/**
	 * @param MCS
	 * @param setClusters
	 */
	protected BHSTree(SubSetContainer C, Object setClusters) {
		super(C, setClusters);   //Adds the minimisation effect already.
		
		debugPrint(" - Creating tree from C:\n\t" + C.toString());
		//
		//C is set, no only left and right tree have to be created.
		//take a random element out of the whole bunch of elements out of which the 
		//subsets are created. 
		Object rnd = getRandomElement(C);

		debugPrint(" - Selected element " + rnd.toString() + " from this subset");

		SubSetContainer left = new SubSetContainer(),
						right = new SubSetContainer();
		//Create copies of the elements in the set, apply the tree creation algorithm to the
		//set, split on the given random number and and put them in the left and right
		//H of the new left and right branches.
		C.splitWith(left, right, rnd); 
		debugPrint("Left.isEmpty():" + left.isEmpty());
		debugPrint("Right.isEmpty():" + right.isEmpty());
		
		
		debugPrint(" - Creating the left branch:");
		if(left.isEmpty()){ 
			//if left branch is empty
		    //This means, the clusterSet is rnd and the C is empty.
		    //The left branch has to be a leafTree in this case.
   		    left_branch_ = new LeafBHSTree(left, rnd);
		    debugPrint(" \t - Left tree is empty, created left leaftree:\n\t" + left.toString("","","<",">",false,",") + 
					"[" + left_branch_.getH().toString() + "]");
		}else{  
			//In the case the left branch is not empty, create a normal subtree
			//recursively.

		    debugPrint(" \t -Creating LeftTree recursively!! \n\t ** RECURSION **"); 
			left_branch_ = new BHSTree(left, rnd);
		    debugPrint(" \t ** AFTER RECURSION **"); 
		}

		debugPrint(" - Creating the right branch:");
		if(right.isEmpty()){   
			//Right branch is empty
		    right_branch_ = new EmptyBHSTree(right,SubSetTree.EMPTY);
		    debugPrint(" - Right tree is empty, created empty BHSTree.");
		}else{  
			//In the case the right branch is not empty, create a normal subtree
			//recursively. The clusterSet is always empty for a right branch.
		    debugPrint(" \t -Creating RightTree recursively!! \n\t ** RECURSION **"); 
			right_branch_ = new BHSTree(right, SubSetTree.EMPTY);
		    debugPrint(" \t ** AFTER RECURSION **"); 
		}

		debugPrint("BHSTree created.");
	}

	/**
	 * @param C
	 */
	public BHSTree(SubSetContainer C) {
	    this(C, SubSetTree.EMPTY);
	}

/************************************************
 * 				METHODS
 ************************************************/

	protected int getDepth(int depth){
		int left_depth = left_branch_.getDepth(depth + 1);
		int right_dept = right_branch_.getDepth(depth + 1);
		if (left_depth < right_dept) return right_dept;
		return left_depth;
	}

	public int getDepth(){
		return getDepth(0);
	}

/************************************************
 * 			MINIMAL HITTING SET ALGORITHM
 ************************************************/

	/*
	 * Actual minimal hitting set method. 
	 */
	public SubSetContainer getMinimalHittingSet(){
		SubSetContainer left = left_branch_.getMinimalHittingSet();
		SubSetContainer right = right_branch_.getMinimalHittingSet();
		SubSetContainer ret = new SubSetContainer();

		Vector tmp;
		for(int i=0; i<left.size(); i++){
			for(int j=0; j<right.size(); j++){
				tmp = ((SubSet)left.get(i)).combineWith(((SubSet)right.get(j)));
				ret.add(tmp);
			}
		}
		if(left.isEmpty()){
			for(int j=0; j<right.size(); j++){
				tmp =  right.getVector(j);
				ret.add(tmp);
			}
		}else if(right.isEmpty()){
			for(int i=0; i<left.size(); i++){
				tmp = left.getVector(i);
				ret.add(tmp);
			}
		}
	
		if(getH() != SubSetTree.EMPTY){
			tmp = new SubSet();
			tmp.add(getH());
			ret.add(tmp);
		}
			
		//<debugging>	
		debugPrint("\ngetMinimalHittingSet: left is : " + left.toString());
		debugPrint("getMinimalHittingSet: right is : " + right.toString());
		debugPrint("getMinimalHittingSet: Combination is: " + ret.toString());
		//<\debugging>
		
		SubSetContainer prunedSubSets = new SubSetContainer();
		ret.minimize(prunedSubSets);
		debugPrint("getMinimalHittingSet: Minimized combination is: " + ret.toString());
		return ret;
	}

/************************************************
 * 				PRINTING METHODS
 ************************************************/
	protected void print(int depth){
		if(depth != 0){
			for(int i=0; i<SubSetTree.NBLINES; i++){
				super.print(depth);
				System.out.println("|");
			}
		}
		super.print(depth);
		if(depth != 0){
			System.out.print("+-- ");
		}else{
			System.out.print("    ");
		}

		System.out.println(getC().toString("", "", "<", ">", true, "") +
				getRemovedThroughMinimizing().toString("", "", "<", ">*", false, ",") + " " + "[" + getH().toString() + "]");

		if(right_branch_.getDepth() >= 0)
			set_printline(depth + 1, true);
		left_branch_.print(depth + 1);
		set_printline(depth + 1, false);
		right_branch_.print(depth + 1);
	}

	public void print(){
		init_printlines(getDepth() + 1);
		print(0);
	}
}
