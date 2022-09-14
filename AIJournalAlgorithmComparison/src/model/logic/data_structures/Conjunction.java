package model.logic.data_structures;

import java.util.ArrayList;


/**
 * The Class Conjunction represents a logical conjunction of literals.
 */
public class Conjunction extends Junction{


	/**
	 * Instantiates a new conjunction.
	 *
	 * @param literals list of literals in the conjunction
	 */
	public Conjunction(ArrayList<String> literals){
		this.literals = literals;
	}
	
}
