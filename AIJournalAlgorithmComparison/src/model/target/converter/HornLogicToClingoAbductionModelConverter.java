package model.target.converter;

import java.util.HashSet;
import java.util.LinkedList;
import java.util.Map.Entry;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;

public class HornLogicToClingoAbductionModelConverter extends LogicToClingoAbductionModelConverter {
	
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
		createMapping();
		createClauses();
	}

	/**
	 * Creates the mapping to integer representations.
	 */
	private  void createMapping(){
		int value = 1;
		hypotheses = new HashSet<String>();
		for(LRule rule:logic_rules){
			for(LObject tail : rule.tail){
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
		for(LRule rule:logic_rules){
			String headElement = rule.head.get(0).toString();
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

	/**
	 * Creates the clauses.
	 */
	public  void createClauses(){
		for(Entry<String, Integer> entry: variable_mapping.entrySet()){
			clauses.append("variable("+entry.getValue()+").");
		}
		clauses.append("\n");
		int clausecounter = 1;
		for(LRule rule:logic_rules){
			String h_clause = "h"+clausecounter;
			clauses.append("hclause("+h_clause+ "). ");
			for(LObject tail : rule.tail){
				String tailElement = tail.toString();
				int tailElementIntValue = variable_mapping.get(tailElement);
				clauses.append("neg("+h_clause+","+tailElementIntValue+"). ");
			}

			String headElement = rule.head.get(0).toString();
			int headElementIntValue = (variable_mapping.get(headElement));
			clauses.append("pos("+h_clause+","+headElementIntValue+"). ");
			clauses.append("\n");  		
			clausecounter++;
		}
	}




}
