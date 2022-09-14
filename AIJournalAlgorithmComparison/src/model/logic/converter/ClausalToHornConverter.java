package model.logic.converter;

import java.io.File;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.Objects;
import java.util.Set;

import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.visitors.ConvertToDNF;
import diagnosis.algorithms.compiler.*;
import diagnosis.data_structures.Hypothesis;
import diagnosis.engines.ATMSEngine;

import model.source.data_structures.*;
import model.logic.data_structures.CRule;
import model.logic.data_structures.Conjunction;


/**
 * The Class ClausalToHornConverter.
 */
public class ClausalToHornConverter extends SourceToLogicModelConverter{
	
	/** The model_crules. */
	public ArrayList<CRule> model_crules;

	/**
	 * Creates the horn theory.
	 *
	 * @param entries the entries
	 * @param bijuncive the bijuncive
	 * @return the string
	 */
	public String createHornTheory(ArrayList<ModelEntry> entries, boolean bijuncive){
		//create internal representation for AIMA
		String intermediate_model = "";
		int num_hyp_lhs = 0;
		for(ModelEntry entry: entries){
			String internal_fault_mode = "";
			Iterator<FaultMode> it = entry.faultmodes.iterator();
			while(it.hasNext()){
				num_hyp_lhs++;
				FaultMode fm = it.next();
				String internal_name;
				String external_name = fm.getId();
				if(internal_to_external.containsValue(external_name)){
					internal_name = getKeyByValue(internal_to_external,external_name);
				}else{
					internal_name = "FM"+faultmode_counter++;
					internal_to_external.put(internal_name, external_name);
				}
				internal_fault_mode = internal_fault_mode + internal_name;
				if(it.hasNext()){
					internal_fault_mode +=",";
				}
			}
			String internal_observation_logic = returnInternalEffectRep(entry);//returnEffectInternalRep(entry);


			intermediate_model += internal_fault_mode +"->"+ internal_observation_logic  +".\n";
		}		
		model_crules = retrieveCRules(intermediate_model);
		String horn_model;
		if(bijuncive)
			horn_model = retrieveBijunctiveModel(model_crules);
		else
			horn_model =retrieveHornModel(model_crules);
		return horn_model;
	}

	/**
	 * Creates the horn theory.
	 *
	 * @param theory the theory
	 * @return the string
	 */
	public String createHornTheory(String theory){
		//create internal representation for AIMA
		String intermediate_model = "";
		String tokens[] = theory.replace("\n", "").replace("\r", "").split("\\.");
		for(String rule: tokens){
			String new_rule = rule;
			String tokens2[] = rule.split("->");
			String antecedent = tokens2[0];
			String internal_name;
			if(internal_to_external.containsValue(antecedent)){
				internal_name = getKeyByValue(internal_to_external,antecedent);
			}else{
				internal_name = "FM"+faultmode_counter++;
				internal_to_external.put(internal_name, antecedent);

			}
			new_rule = new_rule.replace(antecedent, internal_name);	

			String consequences = tokens2[1];
			String tokens3[] = consequences.split(",|[|]");
			for(String eff:tokens3){
				eff = eff.replace("(","").replace(")","").trim();
				String internal_eff;
				if(internal_to_external.containsValue(eff)){
					internal_eff = getKeyByValue(internal_to_external,eff);
				}else{
					internal_eff = "e"+effect_counter++;
					internal_to_external.put(internal_eff, eff);
				}
				new_rule = new_rule.replace(eff, internal_eff);	
			}
			intermediate_model += new_rule.replace(" ", "") +".\n";
		}

		ArrayList<CRule> model_crules = retrieveCRules(intermediate_model);
		String horn_model = retrieveBijunctiveModel(model_crules);
		return horn_model;
	}




	
	/**
	 * Transform model to bijunctive.
	 *
	 * @param model the model before converting the disjunctions represented as CRules
	 * @return the CRule ArrayLsit containing the model after the split of disjunctions.
	 */
	public ArrayList<CRule> transformModelToBijunctive(ArrayList<CRule> model){
		HashMap<String,Integer> combined_counter_map = new HashMap<>();
		ArrayList<CRule> newmodel = new ArrayList<>();
		for(CRule crule:model){
			ArrayList<Hypothesis> hyp = crule.antecendence;

			if(hyp.size()>1 && crule.consequence.size()>1){
				Hypothesis combined = new Hypothesis();
				combined.name = "";
				ArrayList<Hypothesis> hypotheses = new  ArrayList<>();
				for(int j = 0; j < hyp.size();j++){
					Hypothesis h = hyp.get(j);
					hypotheses.add(h);
					combined.name += h.name.trim();
				}

				int offset = 0;
				//To ensure that in case we have seveal rules with the same hypothesis, we 
				//create the correct indices
				if(combined_counter_map.containsKey(combined.name)){
					Integer counter_val = combined_counter_map.get(combined.name);
					offset = counter_val.intValue();
					//make sure to account for the below created hypotheses
					combined_counter_map.put(combined.name, (offset+crule.consequence.size()));
				}
				else{
					combined_counter_map.put(combined.name, (offset+crule.consequence.size()));
				}

				for(int i = 0; i < crule.consequence.size();i++){
					Conjunction c = crule.consequence.get(i);
					Hypothesis hyp_new = new Hypothesis();
					hyp_new.name = "Hyp_"+(i+offset)+"_"+combined.name;
					hyp_new.name = hyp_new.name.trim();
					ArrayList<Hypothesis> new_antecendences = new ArrayList<>();
					new_antecendences.add(hyp_new);
					if(!mapContainsValue(hypotheses)){
						ArrayList<Hypothesis> list = new ArrayList<>();
						list.add(hyp_new);
						newhyp_to_hyp_map.put(list,hypotheses);								
					}
					else{
						HashMap<ArrayList<Hypothesis>,ArrayList<Hypothesis>> clone = (HashMap<ArrayList<Hypothesis>, ArrayList<Hypothesis>>) newhyp_to_hyp_map.clone();
						for(Entry<ArrayList<Hypothesis>,ArrayList<Hypothesis>> entry:clone.entrySet()){
							if(entry.getValue().equals(hypotheses)){
								ArrayList<Hypothesis> list = entry.getKey();
								Hypothesis last_added_hyp = list.get(list.size()-1);
								Integer counter = Integer.valueOf(last_added_hyp.name.replace("Hyp_", "").replace("_"+combined.name.trim(), ""));
								hyp_new.name = "Hyp_"+(counter+1)+"_"+combined.name.trim();
								hyp_new.name = hyp_new.name.trim();
								list.add(hyp_new);	
								newhyp_to_hyp_map.put(list,hypotheses);
							}
						}
					}
					CRule rule_new = new CRule();
					rule_new.antecendence=new_antecendences;
					rule_new.consequence.add(c);
					newmodel.add(rule_new);
				}
			}
			// we have ORs
			else if(crule.consequence.size()>1){
				for(int i = 0; i < crule.consequence.size();i++){
					Conjunction c = crule.consequence.get(i);
					//create new hyp 
					ArrayList<Hypothesis> new_antecendences = new ArrayList<>();
					for(Hypothesis h:hyp){
						ArrayList<Hypothesis> map_value= new ArrayList<>();
						map_value.add(h);
						Hypothesis hyp_new = new Hypothesis();
						hyp_new.name = "Hyp_"+i+"_"+h.name.trim();
						hyp_new.name = hyp_new.name.trim();
						if(!mapContainsValue(map_value)){
							ArrayList<Hypothesis> list = new ArrayList<>();
							list.add(hyp_new);
							newhyp_to_hyp_map.put(list,map_value);								
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
					//make new crule with hyp -> conjunction
					CRule rule_new = new CRule();
					rule_new.antecendence=new_antecendences;
					rule_new.consequence.add(c);
					newmodel.add(rule_new);
				}
			}
			else if(hyp.size()>1){
				Hypothesis combined = new Hypothesis();
				combined.name ="";
				ArrayList<Hypothesis> hypotheses = new  ArrayList<>();
				for(int j = 0; j < hyp.size();j++){
					Hypothesis h = hyp.get(j);
					hypotheses.add(h);
					combined.name += h.name.trim();
				}
				ArrayList<Hypothesis> combined_list = new ArrayList<>();
				combined_list.add(combined);
				newhyp_to_hyp_map.put(combined_list,hypotheses);
				CRule rule_new = new CRule();
				rule_new.consequence.add(crule.consequence.get(0));
				rule_new.antecendence = new ArrayList<Hypothesis>();
				rule_new.antecendence.add(combined);
				newmodel.add(rule_new);

			}
			else{
				newmodel.add(crule);
			}


		}

		return newmodel;
	}

	/**
	 * Transform model to horn.
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
				for(int i = 0; i < crule.consequence.size();i++){
					Conjunction c = crule.consequence.get(i);
					//create new hyp 
					ArrayList<Hypothesis> new_antecendences = new ArrayList<>();
					for(Hypothesis h:hyp){
						ArrayList<Hypothesis> map_value= new ArrayList<>();
						map_value.add(h);
						Hypothesis hyp_new = new Hypothesis();
						if(!mapContainsValue(map_value)){
							hyp_new.name = "Hyp_"+i+"_"+h.name.trim();
							hyp_new.name = hyp_new.name.trim();
							ArrayList<Hypothesis> list = new ArrayList<>();
							list.add(hyp_new);
							newhyp_to_hyp_map.put(list,map_value);		
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
					//make new crule with hyp -> conjunction
					CRule rule_new = new CRule();
					rule_new.antecendence=new_antecendences;
					rule_new.consequence.add(c);
					newmodel.add(rule_new);
				}
			}
			else{
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
			String hyp_original_name = internal_to_external.get(hyp_name);
			Hypothesis hypothesis = new Hypothesis(hyp_original_name);
			hypotheses.add(hypothesis);
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
					literals.add(internal_to_external.get(l.replace(" ","")));
					//literals.add(l.replace(" ",""));
				}
				Conjunction c = new Conjunction(literals);
				r.consequence.add(c);
			}
			else
			{
				literals.add(internal_to_external.get(conjunction));
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
	public String retrieveBijunctiveModel(ArrayList<CRule> model){
		ArrayList<CRule> newmodel = transformModelToBijunctive(model);
		StringBuilder sb = new StringBuilder();
		for(CRule rule :newmodel){
			sb.append(rule.toATMSSyntax());
		}
		return sb.toString();
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
		}
		return sb.toString();
	}



	/**
	 * Retrieve c rules.
	 *
	 * @param raw_model the raw_model
	 * @return the array list
	 */
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
		return model;
	}





	/* (non-Javadoc)
	 * @see model.logic.converter.SourceToLogicModelConverter#convertToLogicModel(java.util.ArrayList, boolean)
	 */
	@Override
	public String convertToLogicModel(ArrayList<ModelEntry> entries, boolean bijunctive) {
		return createHornTheory(entries, bijunctive);
	}


















}
