package support;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

public class EquivalenceChecker {
	
	public static HashSet<HashSet<String>> diagnoses_not_found;
	public static HashSet<HashSet<String>> diagnoses_too_much;

	
	/**
	 * Checks whether the diagnoses Set<Set<String>> of two different algorithms are equal, i.e. contain the same elements;
	 * @param resultATMS Reference diagnoses to compare the new solution to. 
	 * @param resultOther Diagnoses to check.
	 * @return true if the diagnoses are the same, false otherwise.
	 */
	public static boolean checkDiagnoses(HashSet<HashSet<String>> resultATMS, HashSet<HashSet<String>> resultOther){
		diagnoses_not_found= new HashSet<>();
		diagnoses_too_much= new HashSet<>();
		if(resultATMS!=null && resultOther!=null){
			for(Set<String> innerSet1:resultOther){
				if(!checkDiagnosis(innerSet1, resultATMS)){
					diagnoses_too_much.add(new HashSet<String>(innerSet1));
				}
			}
			for(Set<String> innerSet1:resultATMS){
				if(!checkDiagnosis(innerSet1, resultOther)){
					diagnoses_not_found.add(new HashSet<String>(innerSet1));
				}
			}
			if(diagnoses_too_much.size()>0||diagnoses_not_found.size()>0){
				if(diagnoses_too_much.size()>0){
					System.out.println("Too much:");
					Printer.printHashSetHashSet(diagnoses_too_much);
				}
				if(diagnoses_not_found.size()>0){
					System.out.println("Not found:");
					Printer.printHashSetHashSet(diagnoses_not_found);
				}
				
				
				return false;
			}
			return true; 
		}
		return false;
	}



	/**
	 * Checks whether each set within the test diagnoses is contained within the reference diagnoes. 
	 * @param innerSet1 Reference set.
	 * @param resultOther Test set of sets.
	 * @return true if innerSet1 is in resultOther, false otherwise.
	 */
	public static boolean checkDiagnosis(Set<String> innerSet1, HashSet<HashSet<String>> resultOther){
		for(Set<String> innerSet2:resultOther){
			if(equalElements(innerSet1,innerSet2)){
				return true;
			}
		}
		return false;
	}



	/**
	 * Checks whether the sets are equal, i.e. have the same elements.
	 * @param innerSet1 First set.
	 * @param innerSet2 Second set.
	 * @return true if they are equal, false otherwise.
	 */
	public static boolean equalElements(Set<String> innerSet1, Set<String> innerSet2){
		if ( innerSet1.size() != innerSet2.size() ) {
			return false;
		}
		HashSet<String> clone = new HashSet<String>(innerSet2);
		Iterator<String> it = innerSet1.iterator();
		while (it.hasNext() ){
			String A= (String) it.next();
			if (clone.contains(A)){ 
				clone.remove(A);
			} else {
				return false;
			}
		}
		return true;
	}
}
