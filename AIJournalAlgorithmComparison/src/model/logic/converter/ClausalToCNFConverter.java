package model.logic.converter;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Map;
import java.util.Objects;
import java.util.Map.Entry;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.visitors.ConvertToCNF;
import diagnosis.data_structures.Hypothesis;
import model.target.converter.*;
import model.source.data_structures.*;
import model.logic.data_structures.DRule;
import model.logic.data_structures.Disjunction;

/**
 * The Class ClausalToCNFConverter.
 */
public class ClausalToCNFConverter extends SourceToLogicModelConverter {
	
	/** The model_drules. */
	public ArrayList<DRule> model_drules;

	/**
	 * Creates the cnf theory.
	 *
	 * @param entries the entries
	 * @return the string
	 */
	public String createCNFTheory(ArrayList<ModelEntry> entries){
		//create internal representation for AIMA
		String intermediate_model = "";
		for(ModelEntry entry: entries){
			String internal_fault_mode = "";
			Iterator<FaultMode> it = entry.faultmodes.iterator();
			while(it.hasNext()){
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

		model_drules = retrieveDRules(intermediate_model);
		String horn_model= retrieveModel(model_drules);
		return horn_model;
	}

	/**
	 * Retrieves the representation of the CRules required by the ATMS.
	 *
	 * @param model the model in CRules
	 * @return the String used as a model for the ATMS diagnosis
	 */
	public String retrieveModel(ArrayList<DRule> model){
		StringBuilder sb = new StringBuilder();
		for(DRule rule :model){
			sb.append(rule.toATMSSyntax());
		}
		return sb.toString();
	}

	/**
	 * Retrieve d rules.
	 *
	 * @param raw_model the raw_model
	 * @return the array list
	 */
	public ArrayList<DRule> retrieveDRules(String raw_model){
		ArrayList<DRule> model = new ArrayList<>();
		raw_model = splitEffectConjunction(raw_model);
		String tokens[] = raw_model.replace("\n", "").replace("\r", "").split("\\.");
		for(String rule: tokens){
			rule = rule.replace(".", "").replace("\n", "").replace("\r", "");
			DRule r = getDRuleforRule(rule);
			model.add(r);
		}
		return model;
	}

	/**
	 * Gets the CRule for a String representation of a  rule.
	 *
	 * @param rule the String representation of a rule
	 * @return a CRule instance of that rule
	 */
	public DRule getDRuleforRule(String rule){
		rule = makeAIMASuitable(rule);
		DRule r = new DRule();
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
		Sentence transformed = ConvertToCNF.convert(nested);

		String disjunctions[] = transformed.toString().split("\\&");

		for(String disjunction: disjunctions){
			disjunction = disjunction.replace(" ", "").replace("(", "").replace(")", "");
			ArrayList<String> literals = new ArrayList<>();
			if(disjunction.contains("|")){
				String literals_str[] = disjunction.split("\\|");
				for(String l:literals_str){
					literals.add(internal_to_external.get(l.replace(" ","")));
					//literals.add(l.replace(" ",""));
				}
				Disjunction c = new Disjunction(literals);
				r.consequence.add(c);
			}
			else
			{
				literals.add(internal_to_external.get(disjunction));
				//literals.add(conjunction);
				Disjunction c = new Disjunction(literals);
				r.consequence.add(c);
			}
		}
		return r;
	}

	/* (non-Javadoc)
	 * @see model.logic.converter.SourceToLogicModelConverter#convertToLogicModel(java.util.ArrayList, boolean)
	 */
	@Override
	public String convertToLogicModel(ArrayList<ModelEntry> entries, boolean bijunctive) {
		return createCNFTheory(entries);
	}

}
