package model.target.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;
import java.util.Map.Entry;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.data_structures.Hypothesis;
import model.logic.data_structures.DRule;
import model.logic.data_structures.Disjunction;

public class NonHornToClingoAbductionModelConverter extends LogicToClingoAbductionModelConverter {
	/** The parser. */
	private  NonHornLogicModelParser parser;

	/** The logic_rules. */
	private  ArrayList<DRule> logic_rules;
	
	
	/** The additional_vars. */
	protected HashMap<Integer,ArrayList<Integer>> additional_vars;
	
	int clausecounter = 0;
	
	@Override
	public void compile(String model) {
		parser = new NonHornLogicModelParser();
		additional_vars = new HashMap<>();
		logic_rules = parser.parse(model);
		createMapping();
		createClauses();
		
	}
	
	/**
	 * Creates the mapping to integer representations.
	 */
	private  void createMapping(){
		int value = 1;
		hypotheses = new HashSet<String>();
		for(DRule rule:logic_rules){
			for(Hypothesis tail : rule.antecendence){
				String tailElement = tail.toString();
				if(!variable_mapping.containsKey(tailElement)){
					variable_mapping.put(tailElement,value);
					if(Character.isUpperCase(tailElement.charAt(0))){
						hypothesesIntValues.add(Integer.valueOf(value));
						hypotheses.add(tailElement);
					}
					value++;
				}
			}
		}	
		for(DRule rule:logic_rules){
			for(Disjunction head : rule.consequence){
				for(String headElement:head.literals){
					if(!variable_mapping.containsKey(headElement)){
						variable_mapping.put(headElement,value);
						if(Character.isUpperCase(headElement.charAt(0))){
							hypothesesIntValues.add(Integer.valueOf(value));
							hypotheses.add(headElement);
						}
						value++;
					}
				}
			}
		}	

	}
	
	/**
	 * Creates the clauses.
	 */
	public  void createClauses(){
		
		clauses.append("\n");
		for(DRule rule:logic_rules){
			String h_clause = "h"+(++clausecounter);
			clauses.append("hclause("+h_clause+ "). ");
			ArrayList<Integer> hyp_ints = new ArrayList<>();
			for(Hypothesis tail : rule.antecendence){
				String tailElement = tail.toString();
				int tailElementIntValue = variable_mapping.get(tailElement);
				clauses.append("neg("+h_clause+","+tailElementIntValue+"). ");
				hyp_ints.add(new Integer(tailElementIntValue));
			}
			for(Disjunction head : rule.consequence){
				//simple ANDs
				// hypotheses are represented by their negated integer
				// and the head, i.e. effect is represented by a positive integer
				if(head.literals.size()<2){
					for(String headElement:head.literals){
						int headElementIntValue = (variable_mapping.get(headElement));
						clauses.append("pos("+h_clause+","+headElementIntValue+"). ");	
					}				
				}
				else{
					Iterator<String> head_i = head.literals.iterator();
					String first = head_i.next();
					int headElementIntValue = (variable_mapping.get(first));
					clauses.append("pos("+h_clause+","+headElementIntValue+"). \n");	
					while(head_i.hasNext()){
						h_clause = "h"+(++clausecounter);
						String headElement = head_i.next();
						headElementIntValue = (variable_mapping.get(headElement));
						clauses.append("hclause("+h_clause+ "). ");
						for(Integer hyp_int:hyp_ints){
							clauses.append("neg("+h_clause+","+hyp_int.intValue()+"). ");
						}
						clauses.append("pos("+h_clause+","+headElementIntValue+").\n");	
					}
					
				}
			}
			clauses.append("\n");
		}

		clauses.append("\n");

		handleFacts();
	}
	
	public void handleFacts(){
		if(facts!=null){
			clauses.append("\n% facts\n");
			for(String fact:facts){
				String h_clause = "h"+(++clausecounter);
				clauses.append("hclause("+h_clause+ "). ");
				clauses.append("pos("+h_clause+","+variable_mapping.get(fact)+"). \n");
			}
		}
		
	}
	
	public boolean addObservations(HashSet<String> observations){ 
		clauses.append("\n %Hyps \n");
		addHypotheses();
		if( (variable_mapping.size()-hypotheses.size())<observations.size()){
			return false;
		}

		clauses.append("\n% o1,o2,...->ex.\n");
		int observation_var = variable_mapping.size()+1;
		variable_mapping.put(new String(), observation_var);
		
		String h_clause = "h"+(++clausecounter);
		clauses.append("hclause("+h_clause+ "). ");
		clauses.append("pos("+h_clause+","+observation_var+"). ");
		for(String obs:observations){
			int obs_int = variable_mapping.get(obs);
			clauses.append("neg("+h_clause+","+obs_int+"). ");
		}
		
		clauses.append("\n manifestation(m). ");
		clauses.append("pos(m,"+observation_var+"). ");
		

		for(Entry<String, Integer> entry: variable_mapping.entrySet()){
			clauses.insert(0, "variable("+entry.getValue()+").");
		}
		//System.out.println(clauses.toString());
		return true;
		
	}
	
	
//	/**
//	 * Creates the clauses.
//	 */
//	public  void createClauses(){
//		
//		clauses.append("\n");
//		for(DRule rule:logic_rules){
//			String h_clause = "h"+(++clausecounter);
//			clauses.append("hclause("+h_clause+ "). ");
//			for(Disjunction head : rule.consequence){
//				//simple ANDs
//				// hypotheses are represented by their negated integer
//				// and the head, i.e. effect is represented by a positive integer
//				if(head.literals.size()<2){
//					for(String headElement:head.literals){
//						int headElementIntValue = (variable_mapping.get(headElement));
//						clauses.append("pos("+h_clause+","+headElementIntValue+"). ");	
//					}				
//				}
//				else{
//					// ORs
//					//create a temporary variables, which is put to of the rule and stored in map
//					//e.g. A->b|c --> -A \vee k and map contains b:k, c:k
//					int tmp_val = variable_mapping.size()+1;
//					variable_mapping.put("tmp"+tmp_val, tmp_val);
//					clauses.append("pos("+h_clause+","+tmp_val+"). ");
//					ArrayList<Integer> disj_elements = new ArrayList<>();
//					for(String headElement:head.literals){
//						int headElementIntValue = (variable_mapping.get(headElement));
//						disj_elements.add(new Integer(headElementIntValue));
//					}
//					additional_vars.put(new Integer(tmp_val), disj_elements);
//					/*for(String headElement:head.literals){
//						int headElementIntValue = (variable_mapping.get(headElement));
//						if(additional_vars.containsKey(headElementIntValue)){
//							ArrayList<Integer> tmp_so_far = additional_vars.get(headElementIntValue);
//							tmp_so_far.add(new Integer(tmp_val));
//							additional_vars.put(headElementIntValue, tmp_so_far);
//						}
//						else{
//							ArrayList<Integer> tmp_so_far = new ArrayList<>();
//							tmp_so_far.add(new Integer(tmp_val));
//							additional_vars.put(headElementIntValue, tmp_so_far);
//						}
//					}*/
//				}
//			}
//			
//			
//			
//			
//			for(Hypothesis tail : rule.antecendence){
//				String tailElement = tail.toString();
//				int tailElementIntValue = variable_mapping.get(tailElement);
//				clauses.append("neg("+h_clause+","+tailElementIntValue+"). ");
//			}
//			clauses.append("\n");
//		}
//
//		clauses.append("\n");
//
//	}
	
//	public boolean addObservations(HashSet<String> observations){ 
//		clauses.append("\n %Hyps \n");
//		addHypotheses();
//		if( (variable_mapping.size()-hypotheses.size())<observations.size()){
//			return false;
//		}
//		
//		HashSet<Integer> obs_int = new HashSet<>();
//		
//		clauses.append("\n% o1,o2,...->ex. tmp_i->ex. tmp_i+1->ex\n");
//		String h_clause = "h"+(++clausecounter);
//		int observation_var = variable_mapping.size()+1;
//		variable_mapping.put(new String(), new Integer(observation_var));
//		clauses.append("hclause("+h_clause+ "). ");
//		clauses.append("pos("+h_clause+","+observation_var+"). ");
//		
//		for(String observation:observations){
//			int tailElementIntValue = variable_mapping.get(observation);
//			clauses.append("neg("+h_clause+","+tailElementIntValue+"). ");
//			obs_int.add(new Integer(tailElementIntValue));
//		}
//		clauses.append("\n");
//		
//		HashSet<Integer> already_considered = new HashSet();
//		for(Entry<Integer, ArrayList<Integer>> entry: additional_vars.entrySet()){
//			for(Integer i: entry.getValue()){
//				if(obs_int.contains(i) && !already_considered.contains(entry.getKey())){
//					h_clause = "h"+(++clausecounter);
//					clauses.append("hclause("+h_clause+ "). ");
//					clauses.append("pos("+h_clause+","+observation_var+"). ");
//					clauses.append("neg("+h_clause+","+entry.getKey()+"). ");
//					clauses.append("\n");
//					already_considered.add(entry.getKey());
//				}
//			}
//			
//		}
//		
//		clauses.append("manifestation(m). ");
//		clauses.append("pos(m,"+observation_var+"). ");
//		
//		
//
//		for(Entry<String, Integer> entry: variable_mapping.entrySet()){
//			clauses.insert(0, "variable("+entry.getValue()+").");
//		}
//		System.out.println(clauses.toString());
//		return true;
//		
//	}

}
