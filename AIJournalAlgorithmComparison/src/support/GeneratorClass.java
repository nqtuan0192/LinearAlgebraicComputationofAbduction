/**
 * 
 */
package support;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;

/**
 * @author fwotawa
 *
 */
public class GeneratorClass {

	/**
	 * The generateSubset methods returns a linked list containing all subsets of the 
	 * given set.
	 * @param set
	 * @return all subsets of set
	 */
	public static LinkedList<HashSet<Object>> generateSubsets(Set<Object> set) {
		LinkedList<HashSet<Object>> result = new LinkedList<HashSet<Object>>();
		generateSubsets(new LinkedList<Object>(set),result);
		return result;
	}
	
	public static void generateSubsets(LinkedList<Object> list, LinkedList<HashSet<Object>> result) {
		if (list.isEmpty()) {
			result.add(new HashSet<Object>());
		} else {
			 Object el = list.getFirst();
			 LinkedList<HashSet<Object>> interresult = new LinkedList<HashSet<Object>>(); 
			 list.remove(el);
			 generateSubsets(list, interresult);
			 result.addAll(interresult);
			 for (HashSet<Object> subset : interresult) {
				 HashSet<Object> subset2 = new HashSet<Object>(subset);
				 subset2.add(el);
				 result.add(subset2);
			}
		}
	}
	
	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		HashSet<Object> set = new HashSet<Object>();
		Integer el1 = 7;
		Integer el2 = 8;
		Integer el3 = 13;
		Integer el4 = 17;
		Integer el5 = 18;
		Integer el6 = 19;
		Integer el7 = 20;
		set.add(el1);
		set.add(el2);
		set.add(el3);
		set.add(el4);
		set.add(el5);
		set.add(el6);
		set.add(el7);
		
		// Compute Subsets...
		LinkedList<HashSet<Object>> result = generateSubsets(set);
		System.out.println(result.size() + " subsets generated!");

		// Remove Subsets that are no diagnoses...		
		LinkedList<HashSet<Object>> allDiags = new LinkedList<HashSet<Object>>();
		for (HashSet<Object> diag : result) {
			if (diag.contains(el4)) {
				allDiags.add(diag);
			} else if (diag.contains(el5)) {
				allDiags.add(diag);
			} else if (diag.contains(el1) && diag.contains(el3)) {
				allDiags.add(diag);
			} else if (diag.contains(el1) && diag.contains(el7)) {
				allDiags.add(diag);
			} else if (diag.contains(el2) && diag.contains(el3)) {
				allDiags.add(diag);
			} else if (diag.contains(el2) && diag.contains(el7)) {
				allDiags.add(diag);
			} else if (diag.contains(el6) && diag.contains(el3)) {
				allDiags.add(diag);
			} else if (diag.contains(el6) && diag.contains(el7)) {
				allDiags.add(diag);
			}
		}
		System.out.println(allDiags.size() + " elements remain after removing subsets that are no diagnoses");

		// Compute statement probabilities...
		for (Object stmnt : set) {
			Double probability = 0.0;
			for (HashSet<Object> diag : allDiags) {
				if (diag.contains(stmnt)) {
					Double p = Math.pow((22.0/23.0), (23.0 - diag.size()));
					p = p * Math.pow(1.0/23.0, diag.size());
					probability = probability + p;
				}
			}
			System.out.println("Probability of " + stmnt + " is " + probability);
		}
	}

}
