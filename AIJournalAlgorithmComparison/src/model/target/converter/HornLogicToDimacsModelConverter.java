package model.target.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;
import java.util.Map.Entry;

import diagnosis.algorithms.compiler.*;


/**
 * The Class HornLogicToDimacsModelConverter converts a Horn logic model to its DIMACS
 * representation.
 */
public class HornLogicToDimacsModelConverter extends LogicToDimacsModelConverter{

	/** The parser. */
	private  HornLogicModelParser parser;
	
	/** The logic_rules. */
	private  LinkedList<LRule> logic_rules;


	/* (non-Javadoc)
	 * @see model.target.converter.LogicToDimacsModelConverter#compile(java.lang.String)
	 */
	public void compile(String model){
		parser = new HornLogicModelParser();
		logic_rules = parser.parse(model);
		clause_size = logic_rules.size();
		createMapping();
		createClauses();
	}

	/**
	 * Creates the mapping to integer representations.
	 */
	private  void createMapping(){
		int value = 1;
		explanations = new HashSet<String>();
		for(LRule rule:logic_rules){
			for(LObject tail : rule.tail){
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
			String headElement = rule.head.get(0).toString();
			if(!variableMapping.containsKey(headElement)){
				variableMapping.put(headElement,value);
				value++;
			}

		}	
	}

	/**
	 * Creates the clauses.
	 */
	public  void createClauses(){
		effects = new HashSet<String>();
		explanations = new HashSet<String>();
		StringBuilder clauses = new StringBuilder();
		int clausecounter = 1;
		for(LRule rule:logic_rules){
			String clause="";
			for(LObject tail : rule.tail){
				String tailElement = tail.toString();
				int tailElementIntValue = (variableMapping.get(tailElement))*(-1);
				clauses.append(tailElementIntValue + " ");
				explanations.add(tailElement);
				clause = clause + tailElement + " ";
			}

			String headElement = rule.head.get(0).toString();
			int headElementIntValue = (variableMapping.get(headElement));
			clauses.append(headElementIntValue + " ");
			effects.add(headElement);
			clause = clause + headElement;

			clauses.append("0 \n");
			clauseMapping.put(clause, clausecounter++);    		
		}
		model_builder = clauses;
	}


}
