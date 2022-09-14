package diagnosis.algorithms;

import java.io.PrintWriter;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map.Entry;

import org.sat4j.core.VecInt;
import org.sat4j.minisat.SolverFactory;
import org.sat4j.specs.ContradictionException;
import org.sat4j.specs.IConstr;
import org.sat4j.specs.ISolver;
import org.sat4j.specs.TimeoutException;

import diagnosis.data_structures.ExploreClause;

public class SATSolverCall {

	HashMap<ExploreClause, IConstr> current_clauses;
	HashMap<String, Integer> int_values;
	ISolver solver;
	HashSet<VecInt> int_vector;
	int counter;
	boolean contradiction = false;
	public boolean debug = false;

	public SATSolverCall() {
		this.int_values = new HashMap<>();
		this.solver = SolverFactory.newSAT();
		this.int_vector = new HashSet<>();
		this.counter = 1;
		this.current_clauses = new HashMap<>();
	}

	public IConstr addClause(ExploreClause clause) {
		VecInt vector = createIVecFromExploreClause(clause, new VecInt());
		if (vector.size() > 0) {
			this.int_vector.add(vector);
			try {
				IConstr constraint = this.solver.addClause(vector);
				this.current_clauses.put(clause, constraint);
				return constraint;
			} catch (ContradictionException e) {
				this.contradiction = true;
				return null;
			}
		}
		return null;

	}



	public HashSet<IConstr> addClauses(HashSet<ExploreClause> clauses) {
		HashSet<IConstr> constraints = new HashSet<>();
		for (ExploreClause clause : clauses) {
			constraints.add(addClause(clause));
		}
		return constraints;
	}

	public VecInt createIVecFromExploreClause(ExploreClause clause, VecInt vector) {
		for (String positive : clause.positive_literals) {
			int int_value = 0;
			if (this.int_values.containsKey(positive)) {
				int_value = this.int_values.get(positive);
			} else {
				int_value = this.counter;
				this.int_values.put(positive, int_value);
				this.counter++;
			}
			vector.push(int_value);
		}

		for (String negative : clause.negative_literals) {
			int int_value = 0;
			if (this.int_values.containsKey(negative)) {
				int_value = this.int_values.get(negative);
			} else {
				int_value = this.counter;
				this.int_values.put(negative, int_value);
				this.counter++;
			}
			vector.push((int_value * (-1)));
		}
		return vector;
	}

	public String getIntStringModel() {
		if (this.contradiction) {
			return null;
		}
		return Arrays.toString(this.solver.model());
	}

	public HashMap<String, Boolean> getModel() {
		if (this.contradiction) {
			return null;
		}
		HashMap<String, Boolean> model = new HashMap<String, Boolean>();
		int[] int_model = this.solver.model();
		for (int original_int_value : int_model) {
			if (original_int_value < 0) {
				int positive_int_value = original_int_value * (-1);
				String original = getStringRepresentation(positive_int_value);
				model.put(original, false);

			} else {
				String original = getStringRepresentation(original_int_value);
				model.put(original, true);
			}
		}

		return model;
	}

	public HashMap<String, Boolean> getPositiveModel() {
		if (this.contradiction) {
			return null;
		}
		HashMap<String, Boolean> model = new HashMap<String, Boolean>();
		int[] int_model = this.solver.model();
		for (int original_int_value : int_model) {
			if (original_int_value < 0) {
				int positive_int_value = original_int_value * (-1);
				String original = getStringRepresentation(positive_int_value);
				model.put(original, false);

			} else {
				String original = getStringRepresentation(original_int_value);
				model.put(original, true);
			}
		}

		return model;
	}

	private String getStringRepresentation(int int_value) {
		for (Entry<String, Integer> entry : this.int_values.entrySet()) {
			if (entry.getValue().equals(Integer.valueOf(int_value))) {
				return entry.getKey();
			}
		}
		return "";
	}

	public boolean isSatisfiable() {
		if (this.contradiction) {
			return false;
		}
		try {
			return this.solver.isSatisfiable();
		} catch (TimeoutException e) {
			e.printStackTrace();
		}
		return false;
	}

	public void printDebug(String msg) {
		if (this.debug) {
			System.out.println(msg);
		}
	}

	public void removeClauses(HashSet<IConstr> constraints) {
		for (IConstr constraint : constraints) {
			this.solver.removeConstr(constraint);
		}
	}

	public void removeConstraintFromSolverByClause(ExploreClause clause) {
		IConstr constraint = this.current_clauses.get(clause);
		try {
			this.solver.removeConstr(constraint);
			this.current_clauses.remove(clause);
		} catch (Exception e) {
			printDebug("Current contraint" + constraint.toString());
			printDebug(new Integer(this.solver.nConstraints()).toString());
			this.solver.printInfos(new PrintWriter(System.out));

			e.printStackTrace();
			System.exit(-1);
		}
	}
}
