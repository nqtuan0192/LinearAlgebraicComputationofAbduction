package model.logic.data_structures;

import java.util.ArrayList;

public class Disjunction extends Junction{


	/**
	 * Instantiates a new disjunction.
	 *
	 * @param literals list of literals in the conjunction
	 */
	public Disjunction(ArrayList<String> literals){
		this.literals = literals;
	}

}
