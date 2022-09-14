package model.logic.converter;

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

import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.visitors.ConvertToDNF;
import diagnosis.data_structures.*;
import model.logic.data_structures.*;



public class ClausalToHornExtendedConverter {
	
	public ArrayList<CRule> model_crules;
	/**
	 * 
	 * @param model the model before converting the disjunctions represented as CRules
	 * @return the CRule ArrayLsit containing the model after the split of disjunctions.
	 */
	public ArrayList<CRule> transformModelToHorn(ArrayList<CRule> model){
		HashMap<String,Integer> combined_counter_map = new HashMap<>();
		ArrayList<CRule> newmodel = new ArrayList<>();
		for(CRule crule:model){
			ArrayList<Hypothesis> hyp = crule.antecendence;
			if(crule.consequence.size()>1){
				original_disj_num++;
				for(int i = 0; i < crule.consequence.size();i++){
					Conjunction c = crule.consequence.get(i);
					//create new hyp 
					ArrayList<Hypothesis> new_antecendences = new ArrayList<>();
					for(Hypothesis h:hyp){
						
						if(Character.isUpperCase(h.toString().charAt(0))){
							
							ArrayList<Hypothesis> map_value= new ArrayList<>();
							map_value.add(h);
							Hypothesis hyp_new = new Hypothesis();
							if(!mapContainsValue(map_value)){
								hyp_new.name = "Hyp_"+i+"_"+h.name.trim();
								hyp_new.name = hyp_new.name.trim();
								ArrayList<Hypothesis> list = new ArrayList<>();
								list.add(hyp_new);
								newhyp_to_hyp_map.put(list,map_value);		
								result_hyp_num++;
							}
							else{
								HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>> clone = (HashMap<ArrayList<Hypothesis>, ArrayList<Hypothesis>>) newhyp_to_hyp_map.clone();
								for(Entry<ArrayList<Hypothesis>,ArrayList<Hypothesis>> entry:clone.entrySet()){
									if(entry.getValue().equals(map_value)){
										ArrayList<Hypothesis> list = entry.getKey();
										Hypothesis last_added_hyp = list.get(list.size()-1);

										Integer counter = Integer.valueOf(last_added_hyp.name.replace("Hyp_", "").replace("_"+h.name.trim(), ""));
										hyp_new.name = "Hyp_"+(counter+1)+"_"+h.name.trim();
										hyp_new.name = hyp_new.name.trim();
										list.add(hyp_new);	
										newhyp_to_hyp_map.put(list,map_value);
									}
								}


							}
							new_antecendences.add(hyp_new);
						}
						else{
						
							new_antecendences.add(h);
						}

					}
					//make new crule with hyp -> conjunction
					CRule rule_new = new CRule();
					rule_new.antecendence=new_antecendences;
					rule_new.consequence.add(c);
					newmodel.add(rule_new);
				}
			}
			else{
				result_hyp_num++;
				newmodel.add(crule);
			}


		}

		return newmodel;
	}
	

	


	/**
	 * Gets the CRule for a String representation of a  rule.
	 *
	 * @param rule the String representation of a rule
	 * @return a CRule instance of that rule
	 */
	public CRule getCRuleforRule(String rule){
		rule = makeAIMASuitable(rule);
		CRule r = new CRule();
		String tokens[] = rule.split("->");

		String hypstr = tokens[0].trim();

		String tokens2[] = hypstr.split("&(?![^(]*\\))",-1); //matches A,B but not A(B,C)
		ArrayList<Hypothesis> hypotheses = new ArrayList<>();
		for(String hyp_name:tokens2){
			//if(Character.isUpperCase(hyp_name.charAt(0))){
				Hypothesis hyp = new Hypothesis(hyp_name);
				hypotheses.add(hyp);
				
			//}
			
		}
		r.setAntecendence(hypotheses);

		PLParser parser = new PLParser();
		Sentence nested = parser.parse(tokens[1]);
		Sentence transformed = ConvertToDNF.convert(nested);

		String conjunctions[] = transformed.toString().split("\\|");

		for(String conjunction: conjunctions){
			conjunction = conjunction.replace(" ", "");
			ArrayList<String> literals = new ArrayList<>();
			if(conjunction.contains("&")){
				String literals_str[] = conjunction.split("&");
				for(String l:literals_str){
					literals.add(l.replace(" ",""));
					//literals.add(l.replace(" ",""));
				}
				Conjunction c = new Conjunction(literals);
				r.consequence.add(c);
			}
			else
			{
				literals.add(conjunction);
				//literals.add(conjunction);
				Conjunction c = new Conjunction(literals);
				r.consequence.add(c);
			}

		}



		return r;
	}


	


	
	/**
	 * Retrieves the representation of the CRules required by the ATMS.
	 *
	 * @param model the model in CRules
	 * @return the String used as a model for the ATMS diagnosis
	 */
	public String retrieveHornModel(ArrayList<CRule> model){
		ArrayList<CRule> newmodel = transformModelToHorn(model);
		StringBuilder sb = new StringBuilder();
		for(CRule rule :newmodel){
			sb.append(rule.toATMSSyntax());
			result_rules_num =result_rules_num + rule.toATMSSyntax().split("\n").length;
			original_conj_num++;
		}
		return sb.toString();
	}



	public ArrayList<CRule> retrieveCRules(String raw_model){
		ArrayList<CRule> model = new ArrayList<>();
		raw_model = splitEffectConjunction(raw_model);
		String tokens[] = raw_model.replace("\n", "").replace("\r", "").split("\\.");
		int num_of_disj = 0;
		int num_of_conj = 0;
		int num_of_elements = 0;
		for(String rule: tokens){
			rule = rule.replace(".", "").replace("\n", "").replace("\r", "");
			CRule r = getCRuleforRule(rule);
			if(r.getConsequence().size()>1){
				num_of_elements+=r.getConsequence().size();
				num_of_disj++;
			}
			model.add(r);
		}
		if(num_of_disj>0)
			avg_disj_size = num_of_elements/num_of_disj;
		return model;
	}
	
	/** The new hypothesis to old hypothesis mapping. */
	public HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>> newhyp_to_hyp_map = new HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>>();

	private static String AND = ",";
	private static String OR ="|";
	private static String AND_AIMA ="&";
	private static String OR_AIMA ="|";
	int faultmode_counter=1;
	int effect_counter=1;
	public HashMap<String,String> internal_to_external = new HashMap<>();
	//metric stf
	
	private StringBuilder stats = new StringBuilder();
	public double conversion_back_time = -1;
	public int subsetchecks=-1;
	public int result_diagnoses_size = 0;
	public int correct_diagnoses_size = 0;
	//original number of hypotheses
	public int original_hyp_num = 0;
	//original number of hypotheses
	public int original_eff_num = 0;
	
	//original number of hypotheses
	public int original_rules_num = 0;
	
	//original number of hypotheses
	public int result_hyp_num = 0;
	
	//original number of hypotheses
	public int result_rules_num = 0;
	
	//original number of hypotheses
	public int original_disj_num = 0;
	
	//average number of hypothese on lhs
	public double avg_disj_size = 0.0;
	
	//original number of hypotheses
	public int original_conj_num = 0;
	
	//average number of hypothese on lhs
	public double avg_hyp_lhs = 0.0;
	
	
	public static <T, E> T getKeyByValue(Map<T, E> map, E value) {
		for (Entry<T, E> entry : map.entrySet()) {
			if (Objects.equals(value, entry.getValue())) {
				return entry.getKey();
			}
		}
		return null;
	}
	
	
	
	public HashSet<HashSet<String>> convertBackToOrignalHypothesisSpace(HashSet<HashSet<String>> diagnoses){
		result_diagnoses_size = diagnoses.size();
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
					subsetchecks++;
					if(subset(set2,set1)){
						superset=true;
					}
				}
			}
			if(!superset){
				result.add(set1);
			}
		}
		conversion_back_time = System.currentTimeMillis() - time;
		correct_diagnoses_size = result.size();
		return result;
	}
	
	public boolean mapContainsValue(ArrayList<Hypothesis> list){
		for(Entry<ArrayList<Hypothesis>,ArrayList<Hypothesis>> entry:newhyp_to_hyp_map.entrySet()){
			ArrayList<Hypothesis> value_list = entry.getValue();
				if(value_list.containsAll(list)){
					return true; 
			}
		}
		return false;
	}
	
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
	

}
