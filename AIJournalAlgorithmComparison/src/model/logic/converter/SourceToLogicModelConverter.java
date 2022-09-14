package model.logic.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import diagnosis.algorithms.atms.ATMSNode;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.data_structures.Hypothesis;
import model.source.data_structures.ModelEntry;

// TODO: Auto-generated Javadoc
/**
 * The Class SourceToLogicModelConverter.
 */
public abstract class SourceToLogicModelConverter {

	/** The new hypothesis to old hypothesis mapping. */
	public HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>> newhyp_to_hyp_map = new HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>>();

	/** The and. */
	private static String AND = ",";
	
	/** The or. */
	private static String OR ="|";
	
	/** The and aima. */
	private static String AND_AIMA ="&";
	
	/** The or aima. */
	private static String OR_AIMA ="|";
	
	/** The faultmode_counter. */
	int faultmode_counter=1;
	
	/** The effect_counter. */
	int effect_counter=1;
	
	/** The internal_to_external. */
	public HashMap<String,String> internal_to_external = new HashMap<>();


	/**
	 * Gets the key by value.
	 *
	 * @param <T> the generic type
	 * @param <E> the element type
	 * @param map the map
	 * @param value the value
	 * @return the key by value
	 */
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}

	/**
	 * Return internal effect rep.
	 *
	 * @param entry the entry
	 * @return the string
	 */
	protected String returnInternalEffectRep(ModelEntry entry){
		String regex = "(\\w+)|([|])|([,])|([/(/)])";
		Matcher m = Pattern.compile(regex).matcher(entry.observation_logic);
		LinkedList<String> list_ori = new LinkedList();
		while (m.find()) {
			list_ori.add(m.group());
		}
		LinkedList<String> list = new LinkedList();
		list.addAll(list_ori);
		for(int i = 0; i < list.size();i++){
			String element = list.get(i);
			if(!element.matches("[|]") && !element.matches("[,]") && !element.matches("[/(]") && !element.matches("[/)]")){
				String internal_eff;
				if(internal_to_external.containsValue(element)){
					internal_eff = getKeyByValue(internal_to_external,element);
				}else{
					internal_eff = "e"+effect_counter++;
					internal_to_external.put(internal_eff, element);
				}
				list.set(i, internal_eff);
			}
		}

		StringBuilder sb = new StringBuilder();
		for(String str:list){
			sb.append(str).append(" ");
		}

		return sb.toString();

	}

	/**
	 * Convert back to orignal hypothesis space.
	 *
	 * @param diagnoses the diagnoses
	 * @return the hash set
	 */
	public HashSet<HashSet<String>> convertBackToOrignalHypothesisSpace(HashSet<HashSet<String>> diagnoses){
		double time = System.currentTimeMillis();
		HashSet<HashSet<String>> original_hypotheses = new HashSet<>();
		for(HashSet<String> s:diagnoses){
			HashSet<String> diagnosis = new HashSet<>();
			for(String ss:s){
				ss = ss.replaceAll("\\s+","");
				Hypothesis hypo = new Hypothesis(ss);
				ArrayList<Hypothesis> hyp =retrieveOrginialHypothesis(hypo);
				if(hyp!=null){
					if(hyp.size()>1){
						for(Hypothesis h:hyp){
							diagnosis.add(h.name);
						}
					}
					else
						diagnosis.add(hyp.get(0).toString());
				}
				else{
					diagnosis.add(ss);
				}

			}
			original_hypotheses.add(diagnosis);
		}
		//ensure minimality
		HashSet<HashSet<String>> result = new HashSet<>();
		for(HashSet<String> set1: original_hypotheses){
			boolean superset = false;
			for(HashSet<String> set2: original_hypotheses){
				if(!set1.equals(set2)){
					if(subset(set2,set1)){
						superset=true;
					}
				}
			}
			if(!superset){
				result.add(set1);
			}
		}
		return result;
	}

	/**
	 * Map contains value.
	 *
	 * @param list the list
	 * @return true, if successful
	 */
	public boolean mapContainsValue(ArrayList<Hypothesis> list){
		for(Entry<ArrayList<Hypothesis>,ArrayList<Hypothesis>> entry:newhyp_to_hyp_map.entrySet()){
			ArrayList<Hypothesis> value_list = entry.getValue();
			if(value_list.containsAll(list)){
				return true; 
			}
		}
		return false;
	}

	/**
	 * Retrieve orginial hypothesis.
	 *
	 * @param hypo the hypo
	 * @return the array list
	 */
	public ArrayList<Hypothesis> retrieveOrginialHypothesis(Hypothesis hypo){
		ArrayList<Hypothesis> result = new ArrayList<Hypothesis>();

		for(Entry<ArrayList<Hypothesis>,ArrayList<Hypothesis>> entry: this.newhyp_to_hyp_map.entrySet()){
			ArrayList<Hypothesis> value = entry.getValue();
			ArrayList<Hypothesis> key = entry.getKey();
			for(Hypothesis hyp:key){
				if(hyp.equals(hypo)){
					return value;
				}
			}
			//			if(key.contains(hypo)){
			//				return value;
			//			}
		}

		return null;
	}

	/**
	 * Split effect conjunction.
	 *
	 * @param raw_model the raw_model
	 * @return the string
	 */
	public String splitEffectConjunction(String raw_model){
		StringBuilder sb = new StringBuilder();
		String tokens[] = raw_model.replace("\n", "").replace("\r", "").split("\\.");
		for(String rule: tokens){
			String tokens2[] = rule.split("->");
			String antecedent = tokens2[0];
			String consequences = tokens2[1];
			if(!tokens2[1].contains("|")){
				//simple conjunction, i.e. split: A->b,c. --> A->b. A->c.
				String tokens3[] = tokens2[1].split(",");
				for(String eff:tokens3){
					sb.append(antecedent).append("->").append(eff).append(".\n");
				}
			}
			else{
				sb.append(rule).append(".\n");
			}
		}
		return sb.toString();
	}

	/**
	 * Makes replaces the logical operators by its necessary symbol for the usage of the AIMA library.
	 *
	 * @param rule the rule
	 * @return the the new string representation of the rule with the correct symbols
	 */
	public String makeAIMASuitable(String rule){
		rule = rule.replace(",", "&");
		return rule;
	}

	/**
	 * Checks whether set s1 is a subset of set s2.
	 *
	 * @param s1 the first set (checked whether it is a subset of s2)
	 * @param s2 the second set (checked whether s1 is a subset of it)
	 * @return Returns true if s1 is a subset of s2, and false, otherwise.
	 */
	public static boolean subset(Set<String> s1, Set<String> s2) {
		// Returns true if s1 is a subset of s2
		if (s1.size()<=s2.size()) {
			if (s2.containsAll(s1)) 
				return true;
			else
				return false;
		} else 
			return false;
	}

	/**
	 * Convert to logic model.
	 *
	 * @param entries the entries
	 * @param bijunctive the bijunctive
	 * @return the string
	 */
	public abstract String convertToLogicModel(ArrayList<ModelEntry> entries, boolean bijunctive);


}
