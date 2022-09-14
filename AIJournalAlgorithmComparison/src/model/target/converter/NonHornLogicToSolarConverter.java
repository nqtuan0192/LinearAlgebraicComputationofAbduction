package model.target.converter;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Set;

import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Sets;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.data_structures.Hypothesis;
import model.logic.data_structures.DRule;
import model.logic.data_structures.Disjunction;
import model.target.converter.LogicToSOLARModelConverter.Encoding;
import model.target.converter.LogicToSOLARModelConverter.ExclusiveOr;

// TODO: Auto-generated Javadoc
/**
 * The Class NonHornLogicToSolarConverter which converts a non Horn model to a representation
 * suitable for the consequence finding tool SOLAR.
 */
public class NonHornLogicToSolarConverter extends LogicToSOLARModelConverter {
	
	/** The parser. */
	private  NonHornLogicModelParser parser;
	
	/** The logic_rules. */
	private  ArrayList<DRule> logic_rules;

	/* (non-Javadoc)
	 * @see model.target.converter.LogicToSOLARModelConverter#createClauses(java.lang.String)
	 */
	public void createClauses(String model){
		parser = new NonHornLogicModelParser();
		logic_rules = parser.parse(model);
		production_field = new HashSet<String>();
		effects = new HashSet<String>();
		clauses = new StringBuilder();
		createClausesCNFEncoding();
	}


	/**
	 * Creates the clauses' cnf encoding.
	 */
	void createClausesCNFEncoding(){
		for(DRule rule:logic_rules){
			StringBuilder clause= new StringBuilder("cnf(c");
			clause.append(clausecounter++);
			clause.append(",axiom,[");
			Iterator<Hypothesis> tail_iterator = rule.antecendence.iterator();
			while(tail_iterator.hasNext()){
				Hypothesis tail = tail_iterator.next();
				clause.append("-"+tail.toString().toLowerCase());
				clause.append(",");
				variable_mapping.put(tail.toString().toLowerCase().replace(" ", ""),tail.toString());
				//if(Character.isUpperCase(tail.toString().charAt(0))){
					production_field.add(tail.toString());
				//}
				
			}
			if(rule.consequence.get(0).literals.size()<2){
			////simple ANDs
				// hypotheses are represented by their negated integer
				// and the head, i.e. effect is represented by a positive integer
				for(Disjunction head : rule.consequence){
					Iterator<String> head_iterator = head.literals.iterator();
					while(head_iterator.hasNext()){
						String headElement = head_iterator.next();
						clause.append(headElement.toString().toLowerCase());
						effects.add(headElement);
						variable_mapping.put(headElement.toString().toLowerCase().replace(" ", ""),headElement.toString());
						if(head_iterator.hasNext()){
							clause.append(",");
						}
					}
				}
				clause.append("]).\n");
				clauses.append(clause.toString());
			}
			else{
				// ORs
				//create a temporary variables (i.e. additional_vars), which is put to of the rule and stored in map
				//e.g. A->b|c --> -A \vee k and map contains b:k, c:k
				String tmp = "tmp_"+variable_mapping.size()+1;
				variable_mapping.put(tmp, tmp);
				clause.append(tmp);
				clause.append("]).\n");
				clauses.append(clause.toString());
				for(Disjunction head : rule.consequence){
					Iterator<String> head_iterator = head.literals.iterator();
					while(head_iterator.hasNext()){
						String headElement = head_iterator.next();
						effects.add(headElement);
						variable_mapping.put(headElement.toString().toLowerCase().replace(" ", ""),headElement.toString());
						if(additional_vars.containsKey(headElement)){
							ArrayList<String> list =additional_vars.get(headElement);
							list.add(tmp);
							additional_vars.put(headElement, list);
						}
						else{
							ArrayList<String> list = new ArrayList<>();
							list.add(tmp);
							additional_vars.put(headElement, list);
						}
					}
				}
			}
		}
	}

	/* (non-Javadoc)
	 * @see model.target.converter.LogicToSOLARModelConverter#addObservations(java.util.HashSet)
	 */
	public boolean addObservations(HashSet<String> observations){ 
		if(effects.size()<observations.size()){
			return false;
		}
		//in case we have had ORs we need to create the observations string 
		//differently
		if(additional_vars.size()>1){
			//combination of all elements in all obs,i.e. permutation
			// e.g. Obs={o1,o2,o3}, causes(o1)={a,b} causes(o2)={c} causes(o3)={a,e}
			// list_1={a,c} list_2={b,c} list_3={a,e} list_4={b,e} list_5={c,e}
			ArrayList<ArrayList<String>> totalList = new ArrayList<>();
			for(String observation:observations){
				String obs_int = variable_mapping.get(observation);
				if(additional_vars.get(obs_int)!=null){
					ArrayList<String> vars = new ArrayList<>(additional_vars.get(obs_int));
					vars.add(obs_int);
					totalList.add(vars);
				}
				else{
					ArrayList<String> vars = new ArrayList<>();
					vars.add(obs_int);
					totalList.add(vars);
				}

			}
			ArrayList<ImmutableSet<String>> sets = new ArrayList<>();
			for(ArrayList<String> list:totalList){
				ImmutableSet<String> set = ImmutableSet.copyOf(list);
				sets.add(set);
			}

			Set<List<String>> final_list = Sets.cartesianProduct(sets);

			for(List<String> obs_list : final_list){
				clauses.append("cnf(c");
				clauses.append(clausecounter++);
				clauses.append(",axiom,[");
				Iterator<String> it = obs_list.iterator();
				while(it.hasNext()){
					clauses.append("-"+it.next());
					if(it.hasNext()){
						clauses.append(",");
					}
				}
				clauses.append("]).\n");
			}
		}
		else{
			clauses.append("cnf(c");
			clauses.append(clausecounter++);
			clauses.append(",axiom,[");
			Iterator<String> observation_iterator = observations.iterator();
			while(observation_iterator.hasNext()){
				String obs = observation_iterator.next();
				if(encoding==Encoding.SOLAR_ABD){
					clauses.append("conn(obs,"+obs.toString().toLowerCase()+")");
				}else{
					clauses.append("-"+obs.toString().toLowerCase());
				}

				if(observation_iterator.hasNext()){
					clauses.append(",");
				}
			}
			clauses.append("]).\n");
			observation_iterator = observations.iterator();
			while(observation_iterator.hasNext()){
				generateExclusiveOrs(observation_iterator.next());
			}
		}

		addProductionField();
		return true;
	}

}
