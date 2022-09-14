package model.logic.data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * The Class ConjunctiveFormula.
 */
public class ConjunctiveFormula {
    
    /** The literals. */
    private List<String> literals = new ArrayList<String>();

    /**
     * Instantiates a new conjunctive formula.
     * @param literals the literals to add
     */
    public ConjunctiveFormula(String... literals) {
        this.literals.addAll(Arrays.asList(literals));
    }

    /**
     * Adds a literal to the formula.
     * @param literal the literal to add
     */
    public void addLiteral(String literal) {
        literals.add(literal);
    }

    /**
     * Returns the literals of the formula.
     *
     * @return the literals of the formula
     */
    public List<String> getLiterals() {
        return literals;
    }


    public String toString() {
        StringBuilder sb = new StringBuilder();
        for (String literal : literals) {
            sb.append(literal);
        }
        return sb.toString();
    }
}