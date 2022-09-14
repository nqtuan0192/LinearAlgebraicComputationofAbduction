package model.logic.data_structures;

import java.util.ArrayList;
import java.util.Iterator;

import diagnosis.data_structures.Hypothesis;


/**
 * The Class CRule represents a rule with only conjunctions on the LHS.
 */
public class CRule {

	/** The antecendence of a rule is a Hypothesis */
	public ArrayList<Hypothesis> antecendence;
	
	/** The consequence is a list of conjunctions. */
	public ArrayList<Conjunction> consequence;

	/**
	 * Instantiates a new CRule.
	 */
	public CRule(){
		this.consequence = new ArrayList<>();
		this.antecendence = new ArrayList<>();
	}
	
	
	/**
	 * Creates a syntax for ATMS
	 * e.g. A->b. A->c.
	 *
	 * @return the string
	 */
	public String toATMSSyntax(){
		StringBuilder sb = new StringBuilder();
		Iterator<Conjunction> i = consequence.iterator();
		while(i.hasNext()){
			Conjunction c = i.next();
			Iterator<String> it = c.literals.iterator();
			while(it.hasNext()){
				Iterator<Hypothesis> iterator = antecendence.iterator();
				while(iterator.hasNext()){
					sb.append(iterator.next().name);
					if(iterator.hasNext()){
						sb.append(",");
					}
				}
				sb.append("->");
				String literal = it.next();
				sb.append(literal);
				sb.append(".\n");
			}
		}
		return sb.toString();
		
	}
	
	
	public String toString(){
		StringBuilder sb = new StringBuilder();
		sb.append(antecendence);
		sb.append("->");
		Iterator<Conjunction> i = consequence.iterator();
		while(i.hasNext()){
			StringBuilder conjunction = new StringBuilder("(");
			Conjunction c = i.next();
			Iterator<String> it = c.literals.iterator();
			while(it.hasNext()){
				String literal = it.next();
				conjunction.append(literal);
				if(it.hasNext()){
					conjunction.append(" AND ");
				}
				else {
					conjunction.append(")");
				}
			}
			sb.append(conjunction.toString());
			if(i.hasNext()){
				sb.append(" OR ");
			}
		}
		
		return sb.toString();
	}



	/**
	 * Gets the antecendence.
	 *
	 * @return the antecendence
	 */
	public ArrayList<Hypothesis>  getAntecendence() {
		return antecendence;
	}



	/**
	 * Gets the consequence.
	 *
	 * @return the consequence
	 */
	public ArrayList<Conjunction> getConsequence() {
		return consequence;
	}



	/**
	 * Sets the antecendence.
	 *
	 * @param antecendence the new antecendence
	 */
	public void setAntecendence(ArrayList<Hypothesis>  antecendence) {
		this.antecendence = antecendence;
	}



	/**
	 * Sets the consequence.
	 *
	 * @param consequence the new consequence
	 */
	public void setConsequence(ArrayList<Conjunction> consequence) {
		this.consequence = consequence;
	}
	
}
