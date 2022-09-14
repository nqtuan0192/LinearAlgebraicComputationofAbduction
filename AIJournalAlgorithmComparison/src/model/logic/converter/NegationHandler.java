/*
 * 
 */
package model.logic.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;
import java.util.Set;

import diagnosis.algorithms.compiler.*;
import model.source.data_structures.ModelEntry;



/**
 * The Class NegationHandler.
 * To ensure that for conflict driven methods the results are correct in case there are
 * negative observations, the model has to be adapted in such a way that the hypothesis
 * causing negative observations cannot be part of a diagnosis.
 * Thus for each negated observation, the corresponding clauses are removed involving the
 * causing hypothesis.
 * Example: A->b. C->b. D->e. C->e. Obs={b,-e}. For each observation the causes set is 
 * computed, i.e. causes(b)={A,C}, causes(e)={C,D}. Then each rule involving a variable
 * from the causes set of a negative observation is removed from the theory.
 * The resulting theory thus would be: A->b. (As all rules involving C and D had to be removed) 
 */
public class NegationHandler {

	/** The causes set mapping each effect variable to the set of hypothesis causing it. */
	private static HashMap<String,ArrayList<String>> causes;
	
	/** The hypothesis_to_lrule. */
	private static HashMap<String,ArrayList<LRule>> hypothesis_to_lrule;
	

//	private static ArrayList<Rule> simple_rules;
//	
//	
//	private static HashMap<HashSet<String>,HashSet<HashSet<String>>> causes2 = new HashMap<>();
	/**
	 * Removes the clauses corresponding to negative observations and their hypotheses.
	 *
	 * @param theory the model as String
	 * @param negatedObservations the set of negated observations
	 * @return the string new model
	 */
	public static String removeClausesNegatedObservations(String theory, HashSet<String> negatedObservations){
//		createCausesSets(theory);
//		String result = modelSetMinus(retrieveRulesToDismiss(negatedObservations),theory);
//		result = result.replaceAll("(?m)^[ \t]*\r?\n", "");
		
		 String result = nonHornSetMinus(theory,negatedObservations);
		
		return result;
	}
	public static String nonHornSetMinus(String model,HashSet<String> negatedObservations){
		String result = "";
		ArrayList<Rule> simple_rules = new ArrayList<>();
		HashMap<HashSet<String>,HashSet<HashSet<String>>> causes2 = new HashMap<>();
		simple_rules = new ArrayList<>();
		String tokens[] = model.replace("\n", "").replace("\r", "").split("\\.");
		for(String record:tokens){
			Rule new_rule = new Rule();
			new_rule.setRepresentation(record+".");
			String tokens2[] = record.split("->");
			String lhs = tokens2[0];
			String rhs = tokens2[1];
			String antecedents[] = lhs.split(",(?![^(]*\\))",-1); //matches A,B but not A(B,C)
			for(String antecedent:antecedents){
				new_rule.getBody().add(antecedent);
			}
			String consequences[] = rhs.split("[|]",-1);
			for(String consequence:consequences){
				new_rule.getHead().add(consequence);
			}
			simple_rules.add(new_rule);
		}
		
		HashMap<HashSet<String>,ArrayList<Rule>> hyp_to_rule = new HashMap<>();
		
		//createCauses
		for(Rule rule:simple_rules){
			HashSet<String> antecedents = new HashSet<>();
			antecedents.addAll(rule.getBody());
			if(hyp_to_rule.containsKey(antecedents)){
				ArrayList<Rule> rules = hyp_to_rule.get(antecedents);
				rules.add(rule);
				hyp_to_rule.put(antecedents, rules);
				
			}else{
				ArrayList<Rule> rules = new ArrayList<>();
				rules.add(rule);
				hyp_to_rule.put(antecedents, rules);
			}
			
			HashSet<String> consequences = new HashSet<>();
			consequences.addAll(rule.getHead());
			
			if(!causes2.containsKey(consequences)){
				HashSet<HashSet<String>> hypothese_sets = new HashSet<>();
				hypothese_sets.add(antecedents);
				causes2.put(consequences, hypothese_sets);
			}
			else{
				HashSet<HashSet<String>> hypothese_sets = causes2.get(consequences);
				hypothese_sets.add(antecedents);
				causes2.put(consequences, hypothese_sets);
			}
			
		}
		
		//check which hyps to remove
		HashSet<HashSet<String>> hyp_to_remove = new HashSet<>();
		for(Entry<HashSet<String>,HashSet<HashSet<String>>> entry:causes2.entrySet()){
			if(negatedObservations.containsAll(entry.getKey())){
				for(HashSet<String> hyps:entry.getValue()){
					hyp_to_remove.add(hyps);
				}
			}
		}

		//check which rules to skip
		HashSet<Rule> rules_to_skip = new HashSet<>();
		for(Entry<HashSet<String>,ArrayList<Rule>> entry: hyp_to_rule.entrySet()){
			HashSet<String> hyps = entry.getKey();
			for(HashSet<String> h:hyp_to_remove){
				//so that you skip also the supersets of the hyp to removes
				if(hyps.containsAll(h)){
					rules_to_skip.addAll(entry.getValue());
				}
			}
		}
		
		for(Rule r:simple_rules){
			if(!rules_to_skip.contains(r)){
				result += r.getRepresentation() +"\n";
			}
		}
		
		return result;
	}
	/**
	 * Creates a mapping, where the keyset comprises all effects. For each effect the value set contains
	 * the hypotheses, i.e. causes, implying said effect. Further a mapping is created relating each
	 * hypothesis to a set of LRules, where the hypothesis is in the antecedence.
	 * @param model String representation of the model
	 */
	private static void createCausesSets(String model){
		//causes(mi) Map <Effect,List of causes>
		causes = new HashMap<>();
		hypothesis_to_lrule = new HashMap<String, ArrayList<LRule>>();
		LogicParser lp = new LogicParser();
		lp.parse(model);
		LSentence l = (LSentence)lp.result();

		for(LRule r: l.rules){
			//head is LPredicate
			String head = r.head.toString();
			LinkedList<LObject> tail = r.tail;
			if(causes.containsKey(head)){
				// add tail to value set of key
				ArrayList<String> value = causes.get(head);
				for(LObject tail_element:tail){
					value.add(tail_element.toString());
				}
			}
			else{
				// add head to key and tail to values
				ArrayList<String> value = new ArrayList<>();
				for(LObject tail_element:tail){
					value.add(tail_element.toString());
				}
				causes.put(head, value);
			}
			for(LObject tail_element:tail){
				if(hypothesis_to_lrule.containsKey(tail_element.toString())){
					ArrayList<LRule> rules = hypothesis_to_lrule.get(tail_element.toString());
					rules.add(r);
					//hypothesis_to_lrule.put(tail_element.toString(), rules);
					
				}
				else{
					ArrayList<LRule> rules = new ArrayList<>();
					rules.add(r);
					hypothesis_to_lrule.put(tail_element.toString(), rules);
				}
			}
		}
	}

	/**
	 * Retrieves the rules, which have to be dismissed due to the negated observations by retrieving
	 * the hypotheses causing the negative observation and determining all LRules containing each hypotheses. 
	 *
	 * @param negatedObservations the negated observations
	 * @return the ArrayList<LRule> comprising of the rules which have to be removed
	 */
	private static ArrayList<LRule> retrieveRulesToDismiss(HashSet<String> negatedObservations){
		ArrayList<LRule> rules_to_dismiss = new ArrayList<>();
		for(Entry<String,ArrayList<String>> entry:causes.entrySet()){
			if(negatedObservations.contains(entry.getKey())){
				for(String hypothesis: entry.getValue()){
					rules_to_dismiss.addAll(hypothesis_to_lrule.get(hypothesis));		
				}
			}
		}
		return rules_to_dismiss;
	}

	
	/**
	 * Creates the new model by removing the rules which have to be dismissed from the original model.
	 *
	 * @param rules_to_dismiss the rules_to_dismiss
	 * @param original_model the original_model
	 * @return the string
	 */
	private static String modelSetMinus(ArrayList<LRule> rules_to_dismiss, String original_model){
		for(LRule rule:rules_to_dismiss){
			original_model = original_model.replace(rule.toString(), "");
			original_model= original_model.replaceAll("(?m)^[ \t]*\r?\n", "");
		}
		return original_model;
	}

	public static void main(String args[]){
		String theory = "A,B->e.D->f|g.A->f.C->e.X->y.B->g.";
		HashSet<String> obs_neg = new HashSet<>();
		//obs_neg.add("f");
		obs_neg.add("e");
		String result = NegationHandler.removeClausesNegatedObservations(theory,obs_neg);
		System.out.println(result);
	}

}
