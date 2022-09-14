package model.target.converter;


import java.util.LinkedList;

import diagnosis.algorithms.compiler.*;

/**
 * Parses a logic model file or String representation of a logical model which has a Horn structure.
 *
 */
public class HornLogicModelParser{
	protected  String original_model;
	protected  LinkedList<LRule> logic_rules = new LinkedList<LRule>();


	/**
	 * Parses a file using the ATMS' logic parser
	 * @param model_file
	 */
	protected void parseFile(String model_file){
		LogicParser parser = new LogicParser();
		if (!parser.parseFile(model_file)) {
			System.out.println("error " + model_file );
		}
		LSentence logic = (LSentence)parser.result();
		logic_rules = logic.rules;
	}

	/**
	 * Parses a String model using the ATMS' logic parser
	 * @param model_file
	 */
	protected LinkedList<LRule> parse(String model){
		original_model = model;
		LogicParser parser = new LogicParser();
		if (!parser.parse(original_model)) {
			System.out.println("error with model");
		}
		LSentence logic = (LSentence)parser.result();
		logic_rules = logic.rules;
		return logic_rules;
	}

}
