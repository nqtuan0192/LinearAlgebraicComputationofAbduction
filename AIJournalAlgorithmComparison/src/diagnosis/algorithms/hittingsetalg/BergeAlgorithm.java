package diagnosis.algorithms.hittingsetalg;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;


// TODO: Auto-generated Javadoc
/**
 * The Class BergeEngine.
 */
public class BergeAlgorithm {
	
	/** The hittingsets. */
	private ArrayList<ArrayList<Integer>> hittingsets= new ArrayList<>();
	
	private HashMap<Integer,String> map= new HashMap<>();
	/**
	 * Computes the hitting sets for a given set of conflicts based on the Berge algorithm.
	 * Note 1) cs_set has to be sorted by cardinality 
	 * 		2) its subset are sorted alphabetically
	 * @param cs_set the set of conflicts represented as Strings
	 */
	public ArrayList<ArrayList<String>> computeHS(ArrayList<ArrayList<String>> cs_set_str){
		
		//convert to int
		ArrayList<ArrayList<Integer>> cs_set = new ArrayList<ArrayList<Integer>>(cs_set_str.size());
		for(ArrayList<String> cs: cs_set_str){
			cs_set.add(determineIntList(cs));
		}
		Collections.sort(cs_set, new Comparator<ArrayList<Integer> >(){
			public int compare(ArrayList<Integer> a1, ArrayList<Integer> a2) {
				return a1.size()-a2.size(); 
			}
		});

	
		//adds the first 
		addFirst(cs_set.get(0));
		for(int i = 1; i < cs_set.size();i++){
			addToHittingSets(cs_set.get(i));
		}
		
		ArrayList<ArrayList<String>> hittingsets_str = new ArrayList<ArrayList<String>>(hittingsets.size());
		for(ArrayList<Integer> hs:hittingsets){
			ArrayList<String> hs_str=determineStringList(hs);
			hittingsets_str.add(hs_str);
		}
		return hittingsets_str;

	}

	/**
	 * Adds the given conflict set to the already existing hitting sets. To add the set, for each
	 * existing hitting set, the set is duplicated by the cardinality of the conflict set.
	 * Then each conflict set element is added to one of the duplicated hitting sets to form new hitting sets.
	 * 
	 * The new hitting set is added if:
	 * 	  1) the conflict set is not a super set of any existing hitting set (checked before creation of duplicates)
	 * 	  2) the resulting hitting set is not a superset of any existing hitting set
	 *  
	 *
	 * @param cs a new conflict set to consider for the hitting sets
	 */
	private  void addToHittingSets(ArrayList<Integer> cs){
		ArrayList<ArrayList<Integer>> clone = (ArrayList<ArrayList<Integer>>) hittingsets.clone();
		for(int i = 0; i < clone.size();i++){
			ArrayList<Integer> hs_ = clone.get(i);
			if(intersection(hs_,cs).size()<1){
				ArrayList<ArrayList<Integer>> new_hitting_set = combineSets(hs_,cs);
				hittingsets.remove(hs_);
				for(ArrayList<Integer> set:new_hitting_set){
					//subsetcheck
					if(minimalHS(set)){
						hittingsets.add(set);
					}

				}
			}
		}
	}

	/**
	 * Computes the union of two lists.
	 *
	 * @param <T> the generic type
	 * @param list1 a list.
	 * @param list2 a list.
	 * @return the union of both lists.
	 */
	private  <T> List<T> union(List<T> list1, List<T> list2) {
		Set<T> set = new HashSet<T>();
		set.addAll(list1);
		set.addAll(list2);
		return new ArrayList<T>(set);
	}


	/**
	 * Checks whether list1 is a subset of list2. Note that we check if each element of list1 is in list2.
	 * By having sorted lists, we can return false as early as possible.
	 *
	 * @param <T> the generic type
	 * @param list1 the list to check whether it is a subset of list2.
	 * @param list2 the list to check whether list1 is a subset of said list.
	 * @return true, if list1 is a subset of list 2, false otherwise
	 */
	private  <T> boolean subset(List<T> list1, List<T> list2) {
		if(list1.size() >= list2.size()){
			return false;
		}
		else{
			for (T t : list1) {
				if(!list2.contains(t)) {
					return false;
				}
			}
			return true;
		}

	}

	


	/**
	 * Computes the intersection of two lists. 
	 *
	 * @param <T> the generic type
	 * @param list1 the list1
	 * @param list2 the list2
	 * @return the intersection. If the intersection is empty, the returned list has size 0. 
	 */
	private  <T> List<T> intersection(List<T> list1, List<T> list2) {
		List<T> list = new ArrayList<T>();
		for (T t : list1) {
			if(list2.contains(t)) {
				list.add(t);
			}
		}
		return list;
	}

	/**
	 * Creates the new hitting sets by combining each element of the cs with a hitting set. Note that
	 * for each hitting set we create |cs| duplicates to add the cs elements.
	 * E.g. hs={ab} cs={cd} the result is {abc}{abd}. 
	 *
	 * @param hs the hitting set
	 * @param cs the conflict set
	 * @return the newly created hitting sets
	 */
	private  ArrayList<ArrayList<Integer>> combineSets(ArrayList<Integer> hs, ArrayList<Integer> cs){
		ArrayList<ArrayList<Integer>> combined_set = new ArrayList<>();

		//split the cs into its elements
		for(Integer element:cs){
			//for each element add it to a duplicate of the hs
			ArrayList<Integer> new_set = new ArrayList<>();
			new_set.addAll(hs);
			new_set.add(element);
			//sort it
			Collections.sort(new_set, new Comparator<Integer>(){
				public int compare(Integer a1, Integer a2) {
					return a1.compareTo(a2);
				}
			});
			combined_set.add(new_set);
		}

		return combined_set;
	}

	/**
	 * Checks whether the given set is a subset minimal hitting set in comparison
	 * to the existing hitting sets.
	 *
	 * @param set the set to check
	 * @return true, if the set is a minimal hs, false otherwise
	 */
	public  boolean minimalHS(ArrayList<Integer> set){
		for(ArrayList<Integer> hs_: hittingsets){
			if(subset(hs_, set)){
				return false;
			}
		}
		return true;
	}

	/**
	 * Creates for the first conflict set, for each element its own hitting set.
	 * Note the first cs should be the cs with the smallest cardinality.
	 *
	 * @param cs_first the first conflict set
	 */
	private  void addFirst(ArrayList<Integer> cs_first){
		for(int i = 0; i < cs_first.size();i++){
			ArrayList<Integer> hittingset = new ArrayList<>();
			hittingset.add(cs_first.get(i));
			hittingsets.add(hittingset);
		}
	}



	/**
	 * Helper function for debugging. Creates a list representation of a String.
	 *
	 * @param str the str
	 * @return the array list
	 */
	public  ArrayList<String> strToArrayList(String str){
		ArrayList<String> list = new ArrayList<>();
		for(int i = 0; i < str.length();i++){
			char c = str.charAt(i);
			list.add(Character.toString(c));
		}

		return list;
	}

	/**
	 * Prints a list of a list for debuggin purposes.
	 *
	 * @param <T> the generic type
	 * @param list the list
	 */
	public  <T> void printList(List<ArrayList<T>> list){
		Iterator i = list.iterator();
		while(i.hasNext()){
			ArrayList<T> sublist = (ArrayList<T>) i.next();
			Iterator i2 = sublist.iterator();
			while(i2.hasNext()){
				System.out.print(i2.next());
				if(i2.hasNext()){
					System.out.print(", ");
				}
			}
			System.out.println();
		}

	}

	/**
	 * The main method.
	 *
	 * @param args the arguments
	 */
	public static void main(String args[]){
		BergeAlgorithm b = new BergeAlgorithm();

		ArrayList<String> cs_1 = b.strToArrayList("ab");
		Collections.sort(cs_1, new Comparator<String>(){
			public int compare(String a1, String a2) {
				return a1.compareToIgnoreCase(a2);
			}
		});
		ArrayList<String> cs_2 = b.strToArrayList("cd");
		Collections.sort(cs_1, new Comparator<String>(){
			public int compare(String a1, String a2) {
				return a1.compareToIgnoreCase(a2);
			}
		});
		ArrayList<String> cs_3 = b.strToArrayList("ac");
		Collections.sort(cs_1, new Comparator<String>(){
			public int compare(String a1, String a2) {
				return a1.compareToIgnoreCase(a2);
			}
		});

		ArrayList<ArrayList<String>> reference = new ArrayList<ArrayList<String>>();
		reference.add(cs_1);
		reference.add(cs_2);
		reference.add(cs_3);
		Collections.sort(reference, new Comparator<ArrayList<String> >(){
			public int compare(ArrayList<String> a1, ArrayList<String> a2) {
				return a1.size()-a2.size(); 
			}
		});
		b.printList(reference);
		
		
		
		b.computeHS(reference);
	}
	
	private ArrayList<String> determineStringList(ArrayList<Integer> list){
		ArrayList<String> newList = new ArrayList<>(list.size());
		for(Integer element:list){
			newList.add(determineStringValue(element));
		}
		return newList;
	}
	
	private String determineStringValue(Integer integer){
		if(map.containsKey(integer)){
			return map.get(integer);
		}
		else{
			return null;
		}
	}
	
	private ArrayList<Integer> determineIntList(ArrayList<String> list){
		ArrayList<Integer> newList = new ArrayList<>(list.size());
		for(String element:list){
			newList.add(determineIntValue(element));
		}
		return newList;
	}
	
	private Integer determineIntValue(String str){
		if(map.containsValue(str)){
			for(Entry<Integer, String> entry:map.entrySet()){
				if(entry.getValue().equals(str))
					return entry.getKey();
			}
		}
		else{
			map.put(map.size(),str);
			return map.size()-1;
		}
		return null;
	}
}
