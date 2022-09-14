package model.target.converter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.data_structures.Hypothesis;
import model.logic.data_structures.DRule;
import model.logic.data_structures.Disjunction;


/**
 * The Class NonHornLogicToDimacsModelConverter, which converts a non Horn model
 * into its DIMACS representation, i.e. a CNF formula
 */
public class NonHornLogicToDimacsModelConverter extends LogicToDimacsModelConverter {

	/** The parser. */
	private  NonHornLogicModelParser parser;

	/** The logic_rules. */
	private  ArrayList<DRule> logic_rules;


	/** 
	 * @see model.target.converter.LogicToDimacsModelConverter#compile(java.lang.String)
	 */
	public void compile(String model){
		parser = new NonHornLogicModelParser();
		additional_vars = new HashMap<>();
		logic_rules = parser.parse(model);
		clause_size =0;
		createMapping();
		createClauses();
	}

	/**
	 * Creates the mapping from String variables to their
	 * integer representation and also determines which
	 * integers represent the hypotheses, i.e. explanations
	 */
	private  void createMapping(){
		int value = 1;
		explanations = new HashSet<String>();
		for(DRule rule:logic_rules){
			for(Hypothesis tail : rule.antecendence){
				String tailElement = tail.toString();
				if(!variableMapping.containsKey(tailElement)){
					variableMapping.put(tailElement,value);
					if(Character.isUpperCase(tailElement.charAt(0))){
						explanationsIntValues.add(Integer.valueOf(value));
						explanations.add(tailElement);
					}
					value++;
				}
			}
			for(Disjunction head : rule.consequence){
				for(String headElement:head.literals){
					if(!variableMapping.containsKey(headElement)){
						variableMapping.put(headElement,value);
						value++;
					}
				}
			}
		}	
	}

	/**
	 * Creates the clauses set given the theory.
	 */
	public  void createClauses(){
		effects = new HashSet<String>();
		
		StringBuilder clauses = new StringBuilder();
		int clausecounter = 1;
		for(DRule rule:logic_rules){
			String tailElement_str_clause_internal_mapping="";
			String tailElementIntValues_for_DIMACS="";
			Iterator<Hypothesis> hypotheses_it = rule.antecendence.iterator();
			while(hypotheses_it.hasNext()){
				Hypothesis tail = hypotheses_it.next();
				tailElement_str_clause_internal_mapping+= tail.toString();
				tailElementIntValues_for_DIMACS+= String.valueOf((variableMapping.get(tail.toString()))*(-1));
				if(hypotheses_it.hasNext()){
					tailElement_str_clause_internal_mapping+=",";
					tailElementIntValues_for_DIMACS+=" ";
				}
			}	
				for(Disjunction head : rule.consequence){
					//simple ANDs
					// hypotheses are represented by their negated integer
					// and the head, i.e. effect is represented by a positive integer
					if(head.literals.size()<2){
						for(String headElement:head.literals){
							String clause="";
							clause = clause + tailElement_str_clause_internal_mapping + " ";
							clauses.append(tailElementIntValues_for_DIMACS + " ");
							int headElementIntValue = (variableMapping.get(headElement));
							clauses.append(headElementIntValue + " ");
							effects.add(headElement);
							clause = clause + headElement;
							clauses.append("0 \n");
							clause_size++;
							clauseMapping.put(clause, clausecounter++); 
						}

					}
					else{
						// ORs
						//create a temporary variables, which is put to of the rule and stored in map
						//e.g. A->b|c --> -A \vee k and map contains b:k, c:k
						int tmp_val = variableMapping.size()+1;
						variableMapping.put("tmp"+tmp_val, tmp_val);
						String clause="";
						clause = clause + tailElement_str_clause_internal_mapping + " ";
						clauses.append(tailElementIntValues_for_DIMACS + " ");
						clauses.append(tmp_val + " ");
						clause = clause + " tmp"+tmp_val;
						clauses.append("0 \n");
						clause_size++;
						clauseMapping.put(clause, clausecounter++); 
						for(String headElement:head.literals){
							int headElementIntValue = (variableMapping.get(headElement));
							if(additional_vars.containsKey(headElementIntValue)){
								ArrayList<Integer> tmp_so_far = additional_vars.get(headElementIntValue);
								tmp_so_far.add(new Integer(tmp_val));
								additional_vars.put(headElementIntValue, tmp_so_far);
							}
							else{
								ArrayList<Integer> tmp_so_far = new ArrayList<>();
								tmp_so_far.add(new Integer(tmp_val));
								additional_vars.put(headElementIntValue, tmp_so_far);
							}
						}


					}
				}
			   		
		}
		model_builder = clauses;
	}

	/* (non-Javadoc)
	 * @see model.target.converter.LogicToDimacsModelConverter#addObservations(java.util.HashSet)
	 */
	public boolean addObservations(HashSet<String> observations){ 	
		try{addExplanations();
		if( (variableMapping.size()-explanations.size())<observations.size()){
			return false;
		}
		int clausecounter = clauseMapping.size()+1;
		
		//in case we have had ORs we need to create the observations string 
		//differently
		if(additional_vars.size()>1){
			//combination of all causes of all observations
			ArrayList<ArrayList<Integer>> totalList = new ArrayList<>();
			for(String observation:observations){
				Integer obs_int = variableMapping.get(observation);
				if(additional_vars.get(obs_int)!=null){
					ArrayList<Integer> vars = new ArrayList<>(additional_vars.get(obs_int));
					vars.add(obs_int);
					totalList.add(vars);
				}
				else{
					ArrayList<Integer> vars = new ArrayList<>();
					vars.add(obs_int);
					totalList.add(vars);
				}

			}
			
			ArrayList<ImmutableSet<Integer>> sets = new ArrayList<>();
			for(ArrayList<Integer> list:totalList){
				ImmutableSet<Integer> set = ImmutableSet.copyOf(list);
				sets.add(set);
			}

			Set<List<Integer>> final_list = Sets.cartesianProduct(sets);

			for(List<Integer> obs_list : final_list){
				for(Integer i:obs_list){
					model_builder.append((i.intValue()*(-1)) + " ");
				}
				model_builder.append("0\n");
				clauseMapping.put(obs_list.toString(), clausecounter++);
				clause_size++;
			}
		}
		else{
			for(String obs: observations){
				Integer value = variableMapping.get(obs);
				// Add \neg o to DIMACS
				model_builder.append(value*(-1) + " ");

			}
			clauseMapping.put(observations.toString(), clausecounter++);
			clause_size++;
			model_builder.append("0\n");
		}
		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}

}

