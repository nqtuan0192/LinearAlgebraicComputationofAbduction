/*
 * 
 */
package model.logic.data_structures;


import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;


/**
 * The Class DisjunctiveFormula.
 */
public class DisjunctiveFormula {
    
    /** The literals of the formula. */
    private List<String> literals = new ArrayList<String>();

    /**
     * Instantiates a new disjunctive formula.
     *
     * @param literals the literals
     */
    public DisjunctiveFormula(String... literals) {
        this.literals.addAll(Arrays.asList(literals));
    }

    /**
     * Adds a literal to the formula.
     *
     * @param literal the literal
     */
    public void addLiteral(String literal) {
        literals.add(literal);
    }

    /**
     * Gets the literals.
     *
     * @return the literals
     */
    public List<String> getLiterals() {
        return literals;
    }
}