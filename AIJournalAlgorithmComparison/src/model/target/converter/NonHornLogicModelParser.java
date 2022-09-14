package model.target.converter;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;

import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.visitors.ConvertToCNF;
import diagnosis.data_structures.Hypothesis;
import model.logic.converter.ClausalToCNFConverter;
import model.logic.data_structures.DRule;
import model.logic.data_structures.Disjunction;


/**
 * Parses a logic model file or String representation of a logical model which has not a Horn structure.
 */
public class NonHornLogicModelParser {
	
	/** The original_model. */
	protected  String original_model;
	
	/** The logic_rules. */
	protected  ArrayList<DRule> logic_rules = new ArrayList<DRule>();


	/**
	 * Parses the model file.
	 *
	 * @param model_file the model_file
	 */
	protected void parseFile(String model_file){
		BufferedReader inputStream = null;
		StringBuilder theory = new StringBuilder();
		try {
			inputStream = 
					new BufferedReader(new FileReader(model_file));
			String str;
			while ((str = inputStream.readLine()) != null) {	
				theory.append(str + "\n");	                
			}
			parse(theory.toString());
		}catch(Exception e){
			e.printStackTrace();;
		}

	}

	/**
	 * Parses a String representation of the model.
	 *
	 * @param model the model
	 * @return the array list
	 */
	protected ArrayList<DRule> parse(String model){
		original_model = model;
		ArrayList<DRule> logic_rules = new ArrayList<>();
		
		//split with line seperator '.'
		String tokens[] = model.replace("\n", "").replace("\r", "").split("\\.");
		for(String rule: tokens){
			rule = rule.replace(".", "").replace("\n", "").replace("\r", "");
			
			String tokens2[] = rule.split("->");
			String hypstr = tokens2[0].trim();
			DRule drule = new DRule();
			 //matches A,B but not A(B,C)
			String hyp[] =  hypstr.split("[,|&](?![^(]*\\))",-1);
			
			ArrayList<Hypothesis> hypotheses = new ArrayList<>();
			for(String hyp_name:hyp){
				Hypothesis hypothesis = new Hypothesis(hyp_name.trim());
				hypotheses.add(hypothesis);
			}
			drule.setAntecendence(hypotheses);
			String disjunction = tokens2[1].trim();
			ArrayList<String> literals = new ArrayList<>();
			if(disjunction.contains("|")){
				String literals_str[] = disjunction.split("\\|");
				for(String l:literals_str){
					literals.add(l.trim());
				}
				Disjunction c = new Disjunction(literals);
				drule.consequence.add(c);
				logic_rules.add(drule);
			}
			
			else if(disjunction.contains(",")){
				String literals_str[] = disjunction.split("\\,");
				for(String l:literals_str){
					literals=new ArrayList<>();
					literals.add(l.trim());
					Disjunction c = new Disjunction(literals);
					drule.consequence.add(c);
					logic_rules.add(drule);
					drule = new DRule();
					drule.setAntecendence(hypotheses);
				}
			}
			else
			{
				literals.add(disjunction);
				Disjunction c = new Disjunction(literals);
				drule.consequence.add(c);
				logic_rules.add(drule);
			}
			
		}
	return logic_rules;

	}
}
