package model.logic.data_structures;

import java.util.ArrayList;
import java.util.Iterator;

import diagnosis.data_structures.Hypothesis;

public class DRule {
	
	/** The antecendence of a rule is a Hypothesis */
public ArrayList<Hypothesis> antecendence;

/** The consequence is a list of conjunctions. */
public ArrayList<Disjunction> consequence;

/**
 * Instantiates a new CRule.
 */
public DRule(){
	this.consequence = new ArrayList<>();
	this.antecendence = new ArrayList<>();
}


/**
 * Synatx smilar to ATMS
 * A,B->c|d.
 *
 * @return the string
 */
public String toATMSSyntax(){
	StringBuilder sb = new StringBuilder();
	Iterator<Disjunction> i = consequence.iterator();
	while(i.hasNext()){
		Disjunction c = i.next();
		Iterator<Hypothesis> iterator = antecendence.iterator();
		while(iterator.hasNext()){
			sb.append(iterator.next().name);
			if(iterator.hasNext()){
				sb.append(",");
			}
		}
		sb.append("->");
		Iterator<String> it = c.literals.iterator();
		while(it.hasNext()){
			String literal = it.next();
			sb.append(literal);
			if(it.hasNext()){
				sb.append("|");
			}
		}
		sb.append(".\n");
	}
	return sb.toString();
	
}


public String toString(){
	StringBuilder sb = new StringBuilder();
	sb.append(antecendence);
	sb.append("->");
	Iterator<Disjunction> i = consequence.iterator();
	while(i.hasNext()){
		StringBuilder disjunction = new StringBuilder("(");
		Disjunction c = i.next();
		Iterator<String> it = c.literals.iterator();
		while(it.hasNext()){
			String literal = it.next();
			disjunction.append(literal);
			if(it.hasNext()){
				disjunction.append(" OR ");
			}
			else {
				disjunction.append(")");
			}
		}
		sb.append(disjunction.toString());
		if(i.hasNext()){
			sb.append(" AND ");
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
public ArrayList<Disjunction> getConsequence() {
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
public void setConsequence(ArrayList<Disjunction> consequence) {
	this.consequence = consequence;
}


}
