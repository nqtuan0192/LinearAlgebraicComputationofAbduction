package diagnosis.algorithms;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;

import org.sat4j.specs.IConstr;

import diagnosis.data_structures.ExploreClause;

/**
 * Arif M.F., Mencía C., Marques-Silva J. (2015) Efficient MUS Enumeration of
 * Horn Formulae with Applications to Axiom Pinpointing. In: Heule M., Weaver S.
 * (eds) Theory and Applications of Satisfiability Testing -- SAT 2015. SAT
 * 2015. Lecture Notes in Computer Science, vol 9340. Springer, Cham
 * 
 * @author roxane
 *
 */
public class MaximalModel {

	private static String AND = "&";
	private static String NOT = "~";
	private static String OR = "|";

	public static void main(String args[]) {

		HashSet<ExploreClause> clauses = new HashSet<>();
		ExploreClause c1 = new ExploreClause();
		c1.addPositiveLiteral("A");
		c1.addPositiveLiteral("B");
		c1.addPositiveLiteral("C");
		c1.addPositiveLiteral("D");
		c1.addPositiveLiteral("E");

		ExploreClause c2 = new ExploreClause();
		c2.addNegativeLiteral("C");
		c2.addNegativeLiteral("D");

		ExploreClause c3 = new ExploreClause();
		c3.addNegativeLiteral("B");

		ExploreClause c4 = new ExploreClause();
		c4.addPositiveLiteral("A");
		c4.addPositiveLiteral("B");
		c4.addPositiveLiteral("C");

		ExploreClause c5 = new ExploreClause();
		c5.addPositiveLiteral("B");
		c5.addPositiveLiteral("D");
		c5.addPositiveLiteral("E");

		ExploreClause c6 = new ExploreClause();
		c6.addNegativeLiteral("A");
		c6.addNegativeLiteral("D");

		ExploreClause c7 = new ExploreClause();
		c7.addNegativeLiteral("A");
		c7.addNegativeLiteral("E");

		clauses.add(c1);
		clauses.add(c2);
		clauses.add(c3);
		clauses.add(c4);
		clauses.add(c5);
		clauses.add(c6);
		clauses.add(c7);

		System.out.println("\n------------ CLAUSES SET -----------------");

	}

	HashSet<String> P = new HashSet<>();
	ArrayList<String> U = new ArrayList<>();
	HashSet<String> B = new HashSet<>();
	HashSet<ExploreClause> cnf_formula;

	SATSolverCall sat_instance;

	public ArrayList<String> countOccurences(HashSet<ExploreClause> cnf_formula) {
		HashMap<String, Integer> map = new HashMap<>();
		for (ExploreClause e : cnf_formula) {
			HashSet<String> neg = e.negative_literals;
			for (String literal : neg) {
				if (map.containsKey(literal)) {
					Integer int_val = map.get(literal);
					int_val = new Integer(int_val.intValue() + 1);
					map.put(literal, int_val);
				} else {
					Integer int_val = new Integer(1);
					map.put(literal, int_val);
				}
			}
		}

		ArrayList<String> U_sorted = new ArrayList<>(sortHashMapByValues(map));

		return U_sorted;
	}

	public void createSATInstance(HashSet<ExploreClause>... set) {
		this.sat_instance = new SATSolverCall();
		if (set != null) {
			for (HashSet<ExploreClause> clause_set : set) {
				for (ExploreClause clause : clause_set) {
					this.sat_instance.addClause(clause);
				}
			}
		}

	}

	public HashSet<ExploreClause> getClauses(HashSet<String> set) {
		HashSet<ExploreClause> clauses = new HashSet<>();
		for (String string : set) {
			ExploreClause clause = new ExploreClause();

			if (string.contains(NOT)) {
				clause.negative_literals.add(string.replace(NOT, ""));
			} else {
				clause.positive_literals.add(string);
			}
			clauses.add(clause);
		}

		return clauses;
	}

	public HashSet<String> getMaximalModel(HashSet<ExploreClause> cnf_formula) {
		this.cnf_formula = cnf_formula;
		initPUB(cnf_formula);
		createSATInstance(cnf_formula, getClauses(this.P));
		boolean isSAT = this.sat_instance.isSatisfiable();

		if (isSAT) {
			loop();
			return this.P;
		} else {
			return null;
		}
	}

	public void initPUB(HashSet<ExploreClause> cnf_formula) {
		for (ExploreClause clause : cnf_formula) {
			HashSet<String> positive_literals = clause.positive_literals;
			this.P.addAll(positive_literals);
			this.U.addAll(clause.negative_literals);
		}

		this.U = countOccurences(cnf_formula);

		for (String negative_literal : this.U) {
			this.P.remove(negative_literal);
		}
	}

	public ArrayList<String> intersection(ArrayList<String> set1, HashSet<String> set2) {
		ArrayList<String> set = new ArrayList<>();
		for (String t : set1) {
			if (set2.contains(t)) {
				set.add(t);
			}
		}

		return set;
	}

	public void loop() {
		while (this.U.size() > 0) {
			String l_literal = this.U.iterator().next();
			// -----------
			this.U.remove(l_literal);

			ExploreClause l_clause = new ExploreClause();
			l_clause.addPositiveLiteral(l_literal);
			HashSet<ExploreClause> l_set = new HashSet<>();
			l_set.add(l_clause);

			HashSet<ExploreClause> p_clauses = getClauses(this.P);
			HashSet<ExploreClause> b_clauses = getClauses(this.B);
			updateSATInstance(this.cnf_formula, p_clauses, b_clauses, l_set); 
			boolean isSAT = this.sat_instance.isSatisfiable();

			if (isSAT) {
				HashMap<String, Boolean> model = this.sat_instance.getModel();
				HashSet<String> L = new HashSet<>();
				for (Entry<String, Boolean> entry : model.entrySet()) {
					if (entry.getValue()) {
						this.P.add(entry.getKey());
					} else {
						L.add(entry.getKey());
					}
				}
				this.U = intersection(this.U, L);
			} else {
				this.B.add(NOT + l_literal);
			}
		}
	}

	public HashSet<ExploreClause> parseStringToCNFFormula(String str_cnf) {
		HashSet<ExploreClause> cnf = new HashSet<>();
		String str_clauses[] = str_cnf.split(AND);
		for (String str_clause : str_clauses) {
			str_clause = str_clause.replace("(", "").replace(")", "").replace(" ", "");
			ExploreClause clause = new ExploreClause();
			String str_literals[] = str_clause.split("\\" + OR);
			for (String str_literal : str_literals) {
				if (str_literal.startsWith(NOT)) {
					clause.addNegativeLiteral(str_literal);
				} else {
					clause.addPositiveLiteral(str_literal);
				}
			}
			cnf.add(clause);
		}

		return cnf;
	}

	public Set<String> sortHashMapByValues(HashMap<String, Integer> passedMap) {
		List<String> mapKeys = new ArrayList<>(passedMap.keySet());
		List<Integer> mapValues = new ArrayList<>(passedMap.values());
		Collections.sort(mapValues);
		Collections.sort(mapKeys);

		LinkedHashMap<String, Integer> sortedMap = new LinkedHashMap<>();

		Iterator<Integer> valueIt = mapValues.iterator();
		while (valueIt.hasNext()) {
			Integer val = valueIt.next();
			Iterator<String> keyIt = mapKeys.iterator();

			while (keyIt.hasNext()) {
				String key = keyIt.next();
				Integer comp1 = passedMap.get(key);
				Integer comp2 = val;

				if (comp1.equals(comp2)) {
					keyIt.remove();
					sortedMap.put(key, val);
					break;
				}
			}
		}
		Set<String> U = sortedMap.keySet();
		return U;
	}

	public String union(HashSet<ExploreClause>... set) {
		StringBuilder sb = new StringBuilder();
		if (set != null) {
			for (HashSet<ExploreClause> clause_set : set) {
				for (ExploreClause clause : clause_set) {
					sb.append(clause.toString());
					sb.append(AND);
				}
			}

		}

		sb.replace(sb.length() - 1, sb.length(), "");
		return sb.toString();
	}

	public void updateSATInstance(HashSet<ExploreClause>... set) {
		if (set != null) {
			HashSet<ExploreClause> combined = new HashSet<>();
			for (HashSet<ExploreClause> new_clause_set : set) {
				combined.addAll(new_clause_set);
			}
			HashMap<ExploreClause, IConstr> tmp = new HashMap<>(this.sat_instance.current_clauses);
			for (ExploreClause old_clauses : tmp.keySet()) {
				if (!combined.contains(old_clauses)) {
					this.sat_instance.removeConstraintFromSolverByClause(old_clauses);
				}
			}
			for (ExploreClause new_clause : combined) {
				if (!this.sat_instance.current_clauses.keySet().contains(new_clause)) {
					this.sat_instance.addClause(new_clause);
				}
			}
		}
	}

}
