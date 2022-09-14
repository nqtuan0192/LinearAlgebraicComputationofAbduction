package diagnosis.algorithms.hittingsetalg;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map.Entry;
import java.util.Vector;

/*
 * A few thoughts:
 * 	1. For the implementation of the h_function (a invertion method for boolean functions)
 * 		in this context, it is actually not important to keep the fact whether an elements is true or not.
 *	 		This because of the fact that each element starts negated when starting the algorithm.
 *			Applying the function results actually in a gradually taking away of these negative values,
 *			bring them out of the formula and continu the function recursive with another set with 
 *			only negative values. The inversion of these elements is thus unnecessary to implement.
 *			The result of this function is in other words not the real "logical and correct" implementation 
 *			of an inversion algorithm (although it would also work with such an implementation), but
 *			on the contrary specific for this situation.
 * @author fry
 *
 * To change the template for this generated type comment go to
 * Window - Preferences - Java - Code Generation - Code and Comments
 */

/**
 * TODO: Minimization funtion mu not implemented
 * @author 
 *
 */
public class BooleanSubSetContainer extends SubSetContainer{
	
	
	
	/*
	 * @return:	Assuming this Container contains BooleanSubSets with negative elements,
	 * 					a container with the negation of these elements is returned. 
	 */
	public void h_function(){
		String thisthingie =toString();
	    debugPrint("BooleanSubSetContainer::h_function with " + toString());
		
		SubSet tmp;
		//If the container is empty, the negation is the  TODO:check this
		if(size() == 0){
			return;
		}
		if(size() == 1){
			tmp = (SubSet)get(0);

			//H(0) = 1; H(1) = 0
			//
			if(tmp == BooleanSubSet.SUBSET_FALSE){
				clear();
				add(BooleanSubSet.SUBSET_TRUE);
				return;
			}else if (tmp == BooleanSubSet.SUBSET_TRUE){
				clear();
				add(BooleanSubSet.SUBSET_FALSE);
				return;
			}
			
 			//H(not e) = e
			//After returning the element of the BooleanSubSet is concerned positive. In
			// practice nothing happens though!!
			//
			else if(tmp.size() == 1){
				debugPrint("H(not e) = e");
			}//note: H(c) =e.H(C1) + H(C2) is still executed!!!! -> minimize.
			
			//H(not_e . C) = e + H(C)
			//
			else {
				debugPrint("H(not_e . C) = e + H(C)");
				debugPrint("  --> From: " + this.toString());
				//take the first element of the subset...
				Vector subset = getVector(0);
				Object element = subset.remove(0);
				//e is removed. Apply the algorithm to this container
				// H(not_e.C) ---> H(C) and e
				h_function();
				//<this> contains now H(C), so add <e> again.
				//H(c) ---> e + H(C)
				BooleanSubSet newSubSet = new BooleanSubSet();
				newSubSet.add(element);
				add(0,newSubSet);
				debugPrint("  --> To: " + this.toString());
				return;
			}
		}

			/*
//H(not_e + C) = e . H(C)
//Rem: Not really necessary, actually creates additional problems. Just here 
// because it was in the algorithm description. 
// TODO: delete above 4 lines ;)!!
*/
			
			//
			//H(C) = e . H(C1) + H(C2);
			//
//		else{  apply also for n=1
			debugPrint("H(C) = e . H(C1) + H(C2)");
			BooleanSubSetContainer BC1 = new BooleanSubSetContainer();
			BooleanSubSetContainer BC2 = new BooleanSubSetContainer();
			
			//Pick a random element from the booleanSubSet.
			//Object not_e = getVector(0).get(0);
			
			// Pick element increasing C2 & with minimal CS
			Object not_e = chooseElementToSplit();
			
			//C1=BC1 C2=BC2
			splitWith(BC2, BC1, not_e);
			debugPrint("true=" + BooleanSubSet.SUBSET_TRUE.toString() + "    false=" +BooleanSubSet.SUBSET_FALSE.toString() );
			debugPrint("C1:" + BC1.toString() + "\nC2: " + BC2.toString());
			//make the recursive call.
			BC1.h_function();
			BC2.h_function();
			//combine the elements of C1 with <not_e> (see algorithm)
			debugPrint("C1 was: " + BC1.toString());
			BC1.combination(not_e);
			debugPrint("C1 is combined with " + not_e + ": " + BC1.toString());
			//put all elements in this Container.
			clear();
			addAll(BC1);  //never special value like SUBSET_FALSE or SUBSET_TRUE
			if(BC2.getVector(0)!=BooleanSubSet.SUBSET_FALSE) 
			    addAll(BC2);
			
		//}
	}

	/**
	 * Chooses an element e to split, which maximizes the C_2. And which is picked from 
	 * a CS with minimal cardinality.
	 * Map<Elementname,List<count,sizeCS>>
	 * @return
	 */
	private Object chooseElementToSplit(){
		HashMap<Object,ArrayList<Integer>> elementCount = new HashMap<>();
		for(int i=0; i < size();i++){
			SubSet s = (SubSet)get(i);
			for(int j= 0; j<s.size();j++){
				Object element = s.get(j);
				if(elementCount.containsKey(element)){
					ArrayList<Integer> list = new ArrayList<>();
					int counter = elementCount.get(element).get(0).intValue();
					counter++;
					list.add(counter);//counter					
					int cs_size = elementCount.get(element).get(1).intValue();
					if(cs_size>s.size()){
						list.add(Integer.valueOf(s.size()));//CS size
					}else{
						list.add(Integer.valueOf(cs_size));//CS size
					}
					elementCount.put(element, list);
				}else{
					ArrayList<Integer> list = new ArrayList<>();
					list.add(Integer.valueOf(1)); //counter
					list.add(s.size()); //CS size
					elementCount.put(element, list);
				}
			}
		}
		
		int max_counter = 0;
		int min_cs_size = Integer.MAX_VALUE;
		Object element = get(0);
		for(Entry<Object,ArrayList<Integer>> entry:elementCount.entrySet()){
			ArrayList<Integer> value = entry.getValue();
			if(value.get(0)>max_counter){
				if(value.get(1)<min_cs_size){
					min_cs_size = value.get(1);
					max_counter = value.get(0);
					element = entry.getKey();
				}
			}
			debugPrint("COMP" + entry.getKey() + "   Count:" + entry.getValue().get(0)+ "   CSsize:" + entry.getValue().get(1));
		}
		debugPrint("Chosen element: " + element);
		return element;
	}
	
	/*
	 * This function creates the C1 and C2 in the formula. C1 containing all subsets that
	 *	did not contain <element> and C2 the combination of all subsets that did contain it without
	 * the element in question and the ones that did not contain <element>.
	 * @see HittingSet.SubSetContainer#splitWith(HittingSet.SubSetContainer, HittingSet.SubSetContainer, java.lang.Object)
	 */
	public void splitWith(BooleanSubSetContainer thatContained, BooleanSubSetContainer thatDidNotContain, Object element){
		//super.splitWith(thatContained, thatDidNotContain, element); //super does not work, small adjustment necessary
	    // Principal:
	    // 1. Look in every SubSet in this container. 
	    // 2. If it contained the number then:
	    // 		- make a copy of the subset
	    // 		- remove the number
	    // 		- add it to subSetsThatContainedElement
	    // 3. if not
	    // 		- add it to subSetsThatDidNotContainElement

	    debugPrint(" BOOLEANSUBSETCONTAINER: \n\tsplitWith\n");
	    debugPrint(" \t - SubSetContainer = " + toString());
	    debugPrint(" \t - Element to split on: " + element.toString());

	    int index;
	    boolean always_true=false;
	    Vector tmp;
	    for(int i = 0; i< size(); i++){
	        tmp =getVector(i);
	        index =tmp.indexOf(element);

	        if(index >= 0){ //contains element
	            if(!always_true){
	                tmp = (Vector)tmp.clone();
	                tmp.remove(index);
	                if(!tmp.isEmpty()){
	                    thatContained.add(tmp);
	                }else{
	                    //if tmp.isEmpty() then we have the following situation:
	                    //H(c) = e.H(C1) + H(TRUE + .....)
	                    //The only necessary elements is in other words TRUE.
	                    always_true=true;
	                }
	            }
	        }else{
	            thatDidNotContain.add(tmp);
	        }
	    }
	    
		//if the container that did contained the element is empty, we give it the meaning of an always TRUE
		// boolean formula (see: e.H(C1) + H(C2) with C2 empty)
		// or: e.g. H(c) = H((-1)(-2)) = H((-1.TRUE)(-2)) =1.H((-2)) + H(TRUE)
		//in this case, it does not mather what we add to the container, the result will always 
		//be TRUE. Even better, we should not add, or we don't get a minimal formula.
		if(always_true){
		    thatContained.clear();
		    thatContained.add(BooleanSubSet.SUBSET_TRUE);
		    debugPrint("thatdidcontain is empty");
		}else{
		    //...otherwise we can just add them.
		    thatContained.addAllCopy(thatDidNotContain); //add copies!!
		}

		//if the container that did not contain is empty, we give it the meaning of an always true
		// boolean formula (see: e.H(C1) + H(C2) with C1 empty)
		if(thatDidNotContain.isEmpty()){
			thatDidNotContain.add(BooleanSubSet.SUBSET_FALSE);
			debugPrint("thatdidnotcontain is empty");
		}
	}
	
	/*
	 * Add all (copies/clones of the) elements of a collection to this container.
	 */
	public boolean addAllCopy(BooleanSubSetContainer c){
		for(int i =0; i<c.size(); i++){
			add(c.getVector(i).clone());
		}
		return true;
	}
	
	/*
	 * Combine the elements in a container with an element:
	 *    e.((1,2)(2,3)...) =((e,1,2)(e,2,3)).... 
	 */
	public void combination(Object element){
		//combination of element with TRUE is container with only this element.
		// e.(TRUE + some.other.stuff) = ((e))
	    int i=0;
	    while(i<size() && getVector(i).size()<=1){
	        if(getVector(i)==BooleanSubSet.SUBSET_TRUE){
	            clear(); //clear internal vector
	            BooleanSubSet b = new BooleanSubSet();
	            b.add(element);
	            add(b);
	            return;
	        }
	        i++;
		}
		//else just make the combination as described before.
		//  e.((1,2)(2,3)...) =((e,1,2)(e,2,3)....) 
		for(i=0;i<size();i++){
			getVector(i).add(0,element);
		}
	} 
	
}
