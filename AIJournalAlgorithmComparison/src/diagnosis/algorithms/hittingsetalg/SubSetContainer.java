package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

/**
 * @invar: A container can not contain empty subsets.
 * 	   This does not mean of course that a subset cannot
 * 	   be empty. A container just cannot contain them.
 * @invar: A subset in this container with smaller index, has less than or 
 * 	   an equal nb of elements. I.e. SubSets with less elements come first.
 * @author fry
 */
public class SubSetContainer extends Vector{

/************************************************
 * 				CONSTRUCTORS
 ************************************************/
	//none for now
	
/************************************************
 * 			MEMBER VARIABLES
 ************************************************/
	
	static private boolean debug=false;
	
	
	//Minimize this SubSetContainer. Returned is the vector of SubSets that were not minimal.
	// @post: This SubSetContainer does not contain any subsets that would make the container
	// not minimal. 
	// <ret> is changed in that way that after execution of this method, the SubSets that were not minimal
	// are now contained in the Container.
	public void minimize(SubSetContainer ret){
		//OK
		ret.clear();
		debugPrint(" SUBSETCONTAINER: \n\tminimize()");
		if(size() != 0){
			Vector iterator;
			for(int i = size()-1; i>=0; i--){
				debugPrint("");
				iterator = getVector(i);
				debugPrint(" \t - picked subset" + iterator.toString());
				for(int j = 0; j<i; j++){
					if(iterator.size() <= getVector(j).size()){
						break;
					}else if(iterator.containsAll(getVector(j))){
						debugPrint(" \t ---> contained " + getVector(j).toString());
						ret.add(iterator);
						remove(i);
						break;
					} 
				}
			}
		}
	}

	/*
	 * Method to split this SubSetContainer with a certain element.
	 */
	public void splitWith(SubSetContainer subSetsThatContainedElement, 
				SubSetContainer subSetsThatDidNotContainElement, Object element){
		// Principal:
		// 1. Look in every SubSet in this container. 
		// 2. If it contained the number then:
		// 		- make a copy of the subset
		// 		- remove the number
		// 		- add it to subSetsThatContainedElement
		// 3. if not
		// 		- add it to subSetsThatDidNotContainElement

		debugPrint(" SUBSETCONTAINER: \n\tsplitWith\n");
		debugPrint(" \t - SubSetContainer = " + toString());
		debugPrint(" \t - Element to split on: " + element.toString());

		int c1=0,c2=0,empty = 0;
		int index;
		Vector tmp;
		for(int i = 0; i< size(); i++){
			tmp =getVector(i);
			index =tmp.indexOf(element);

			if(index >= 0){ //contains element
				tmp = (Vector)tmp.clone();
				tmp.remove(index);
				if(!tmp.isEmpty()){
					subSetsThatContainedElement.add(tmp);
					c1++; //for debug
				}
			}else{
				subSetsThatDidNotContainElement.add(tmp);
				c2++; //for debug
			}
		}
					
		//for debug purposes only
		debugPrint(" \t - Number of subsets that contained  " + element + ": " + c1);
		debugPrint(" \t - Number of subsets that did not contain  " + element + ": " + c2);
		if(debug){
			debugPrint(" \t - Copies of the subsets that contained, without " + element);
			for(int i=0;i<c1-empty; i++)
				System.out.println("\t\t" + subSetsThatContainedElement.get(i).toString());
			debugPrint(" \t - Copies of the subsets that did not contain " + element);
			for(int i=0;i<c2; i++)
				System.out.println("\t\t" + subSetsThatDidNotContainElement.get(i).toString());
		}
	}
	

/************************************************
 * 		GENERAL VECTOR METHODS
 ************************************************/
	/*
	 * Return the subset with a given index.
	 */
	public Vector getVector(int index){
		return (Vector)(super.get(index));
	}
	
	/*
	 * Adding method. Make sure Vectors with less elements come first.
	 */
	public void add(Vector v){
		int i = findIndexFor(v);
		add(i,v);
	}

	private int findIndexFor(Vector ss){
		//TODO: Change to binary algorithm
		for(int i = 0; i<size(); i++){
			if(ss.size() < getVector(i).size()){
				return i;
			}
		}
		return size();
	}
	

/************************************************
 * 			OTHER METHODS
 ************************************************/

	/*
	 * Return a string representation of this container.
	 */
	protected String toString(String openContainer, String closeContainer, String openSubset, //
			String closeSubset, boolean include_brackets_for_empty_container, String separator){

		StringBuffer s =new StringBuffer();
		s = s.append(openContainer);
		for(int i = 0; i<size()-1; i++){
			s.append(((SubSet)get(i)).toString(openSubset, closeSubset) + separator);
		}
		if(size()!=0){
			s.append(((SubSet)lastElement()).toString(openSubset, closeSubset));
		}else{
			if(include_brackets_for_empty_container) 
				s.append(openSubset + closeSubset);
		}
		s.append(closeContainer);
		return s.toString();
	}
	
	/*
	 * Cloning method, also cloning the elements.
	 */
	public Object cloneCopy(){
	    BooleanSubSetContainer ret = new BooleanSubSetContainer();
	    for(int i=0;i<size();i++){
	        ret.add(getVector(i).clone());
	    }
	    return ret;
	}

	/*
	 * Only print if debug is enabled.
	 */
	public void debugPrint(String s){
		if(debug) System.out.println(s);
	}

	/*
	 * Set debug-messages
	 */
	public static void setDebugEnabled(boolean enabled){
		debug = enabled;
	}

}
