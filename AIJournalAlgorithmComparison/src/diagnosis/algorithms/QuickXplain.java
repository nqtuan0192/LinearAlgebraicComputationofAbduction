package diagnosis.algorithms;

import java.util.HashSet;
import java.util.LinkedHashSet;

import diagnosis.data_structures.ExploreClause;

/**
 * Junker U. : QUICKXPLAIN: preferred explanations and relaxations for over-constrained problems
 * Proceeding AAAI'04 Proceedings of the 19th national conference on Artifical intelligence
 * Pages 167-172 
 * @author roxane
 */
public class QuickXplain {
	private static String AND = "&";
	private static String NOT = "~";
	private static String OR = "|";

	public static void main(String args[]) {
		LinkedHashSet<ExploreClause> clauses = new LinkedHashSet<>();
		ExploreClause c1 = new ExploreClause();
		c1.addPositiveLiteral("A");
		c1.addNegativeLiteral("B");
		clauses.add(c1);

		ExploreClause c2 = new ExploreClause();
		c2.addPositiveLiteral("B");
		c2.addNegativeLiteral("C");
		clauses.add(c2);

		ExploreClause c3 = new ExploreClause();
		c3.addPositiveLiteral("C");
		c3.addNegativeLiteral("A");
		clauses.add(c3);

		LinkedHashSet<ExploreClause> background = new LinkedHashSet<>();
		QuickXplain x = new QuickXplain();
		HashSet<ExploreClause> minimal_explanation = x.quickExplain(background, clauses);
		for (ExploreClause ex : minimal_explanation) {
			System.out.println(ex.toString());
		}

		// -------------------------------------------------------------------------------
		LinkedHashSet<ExploreClause> constraints = new LinkedHashSet<>();
		ExploreClause cons1 = new ExploreClause();
		cons1.addPositiveLiteral("A");
		constraints.add(cons1);

		ExploreClause cons2 = new ExploreClause();
		cons2.addPositiveLiteral("C");
		constraints.add(cons2);

		ExploreClause cons3 = new ExploreClause();
		cons3.addNegativeLiteral("A");
		constraints.add(cons3);

		ExploreClause cons4 = new ExploreClause();
		cons4.addPositiveLiteral("D");
		constraints.add(cons4);

		ExploreClause cons5 = new ExploreClause();
		cons5.addPositiveLiteral("E");
		constraints.add(cons5);

		ExploreClause cons6 = new ExploreClause();
		cons6.addPositiveLiteral("F");
		constraints.add(cons6);

		ExploreClause cons7 = new ExploreClause();
		cons7.addPositiveLiteral("G");
		constraints.add(cons7);

		ExploreClause cons8 = new ExploreClause();
		cons8.addPositiveLiteral("H");
		constraints.add(cons8);

		ExploreClause cons9 = new ExploreClause();
		cons9.addPositiveLiteral("I");
		constraints.add(cons9);

		ExploreClause cons10 = new ExploreClause();
		cons10.addPositiveLiteral("J");
		constraints.add(cons10);

		LinkedHashSet<ExploreClause> B = new LinkedHashSet<>();
		QuickXplain xx = new QuickXplain();
		HashSet<ExploreClause> minimal_explanation2 = xx.quickExplain(B, constraints);
		for (ExploreClause ex : minimal_explanation2) {
			System.out.println("EX:" + ex.toString());
		}

	}

	SATSolverCall sat_caller;

	private boolean isConsistent(HashSet<ExploreClause> clauses) {

		this.sat_caller = new SATSolverCall();
		this.sat_caller.addClauses(clauses);
		return this.sat_caller.isSatisfiable();

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

	public HashSet<ExploreClause> quickExplain(LinkedHashSet<ExploreClause> background,
			LinkedHashSet<ExploreClause> constraints) {
		if ((constraints != null && constraints.size() > 0) || isConsistent(constraints)) {
			return qX(new LinkedHashSet<>(background), new LinkedHashSet<>(), new LinkedHashSet<>(constraints));
		}
		return null;
	}

	public LinkedHashSet<ExploreClause> qX(LinkedHashSet<ExploreClause> background, LinkedHashSet<ExploreClause> delta,
			LinkedHashSet<ExploreClause> constraints) {
		if (delta.size() > 0 && !isConsistent(background)) {
			return new LinkedHashSet<ExploreClause>();
		}
		if (constraints.size() == 1) {
			return constraints;
		}
		int k = split(constraints.size());
		LinkedHashSet<ExploreClause> c_1 = new LinkedHashSet<>();
		LinkedHashSet<ExploreClause> c_2 = new LinkedHashSet<>();
		constraints.stream().limit(k).forEachOrdered(c_1::add);
		constraints.stream().skip(k).forEachOrdered(c_2::add);
		;

		LinkedHashSet<ExploreClause> b_cup_c1 = new LinkedHashSet<>();
		b_cup_c1.addAll(background);
		b_cup_c1.addAll(c_1);

		LinkedHashSet<ExploreClause> delta_2 = qX(b_cup_c1, c_1, c_2);
		LinkedHashSet<ExploreClause> b_cup_delta_2 = new LinkedHashSet<>();
		b_cup_delta_2.addAll(background);
		b_cup_delta_2.addAll(delta_2);

		LinkedHashSet<ExploreClause> delta_1 = qX(b_cup_delta_2, delta_2, c_1);

		delta_1.addAll(delta_2);
		return delta_1;
	}

	private int split(int n) {
		return (int) Math.floor(n / 2);
	}

}
