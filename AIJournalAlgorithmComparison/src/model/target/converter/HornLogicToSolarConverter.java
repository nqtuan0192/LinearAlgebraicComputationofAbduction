package model.target.converter;

import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import model.target.converter.LogicToSOLARModelConverter.Encoding;
import model.target.converter.LogicToSOLARModelConverter.ExclusiveOr;


/**
 * The Class HornLogicToSolarConverter.
 */
public class HornLogicToSolarConverter extends LogicToSOLARModelConverter {
	
	/** The parser. */
	private  HornLogicModelParser parser;
	
	/** The logic_rules. */
	private  LinkedList<LRule> logic_rules;

	/* (non-Javadoc)
	 * @see model.target.converter.LogicToSOLARModelConverter#createClauses(java.lang.String)
	 */
	public void createClauses(String model){
		parser = new HornLogicModelParser();
		logic_rules = parser.parse(model);
		production_field = new HashSet<String>();
		effects = new HashSet<String>();
		clauses = new StringBuilder();
		if(encoding==Encoding.SOLAR_ABD){
			createClausesSOLAREncoding();
		}
		else{
			createClausesCNFEncoding();
		}
	}

	/**
	 * Creates the clauses cnf encoding.
	 */
	void createClausesCNFEncoding(){
		ExclusiveOr xor = new ExclusiveOr();
		for(LRule rule:logic_rules){
			xor = new ExclusiveOr();
			StringBuilder clause= new StringBuilder("cnf(c");
			clause.append(clausecounter++);
			clause.append(",axiom,[");
			Iterator<LObject> tail_iterator = rule.tail.iterator();
			while(tail_iterator.hasNext()){
				LObject tail = tail_iterator.next();
				clause.append("-"+tail.toString().toLowerCase());
				clause.append(",");
				variable_mapping.put(tail.toString().toLowerCase().replace(" ", ""),tail.toString());
				if(Character.isUpperCase(tail.toString().charAt(0))){
					production_field.add(tail.toString());
				}
				

			}
			LObject head = rule.head.get(0); //Horn only 1 head element
			clause.append(head.toString().toLowerCase());
			variable_mapping.put(head.toString().toLowerCase().replace(" ", ""),head.toString());
			clause.append("]).\n");
			effects.add(rule.head.toString());
			clauses.append(clause.toString());
		}
	}

	/**
	 * Creates the clauses solar encoding.
	 */
	void createClausesSOLAREncoding(){
		StringBuilder clause= new StringBuilder("cnf(c");
		clause.append(clausecounter++);
		clause.append(",axiom,[caused(X,Y),-conn(X,Y)]).\n");
		clause.append("cnf(c");
		clause.append(clausecounter++);
		clause.append(",axiom,[caused(X,Y),-conn(X,Z),-caused(Z,Y)]).\n");
		clause.append("cnf(c");
		clause.append(clausecounter++);
		clause.append(",axiom,[caused(X,X),-abd(X)]).\n");
		clause.append("cnf(c");
		clause.append(clausecounter++);
		clause.append(",top_clause,[-caused(obs,X),-abd(X)]).\n");
		clauses.append(clause.toString());
		ExclusiveOr xor = new ExclusiveOr();

		// do not handle rules such as A,B->c|d
		for(LRule rule:logic_rules){
			Iterator<LObject> tail_iterator = rule.tail.iterator();
			if(rule.tail.size()>=1){
				LObject head = rule.head.get(0); //Horn only one head element
				while(tail_iterator.hasNext()){
					LObject tail = tail_iterator.next();
					clause.append("conn("+head.toString().toLowerCase());
					clause.append(",");
					clause.append(tail.toString().toLowerCase()+")");
					variable_mapping.put(head.toString().toLowerCase().replace(" ", ""),head.toString());
					if(tail_iterator.hasNext()){
						clause.append(",");
					}
					variable_mapping.put(tail.toString().toLowerCase().replace(" ", ""),tail.toString());
					if(Character.isUpperCase(tail.toString().charAt(0))){
						production_field.add(tail.toString());
					}
				}
				clause.append("]).\n");
				clauses.append(clause.toString());
				effects.add(rule.head.toString());
			}
		}
	}
}
