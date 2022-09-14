package diagnosis.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashSet;

import diagnosis.algorithms.theoremprover.ABTheoremProver;
import diagnosis.algorithms.theoremprover.Assumption;
import diagnosis.algorithms.theoremprover.LSentence;
import diagnosis.algorithms.theoremprover.LogicParser;
import diagnosis.algorithms.theoremprover.Proposition;
/**
 * Junker U. : QUICKXPLAIN: preferred explanations and relaxations for over-constrained problems
 * Proceeding AAAI'04 Proceedings of the 19th national conference on Artifical intelligence
 * Pages 167-172 
 * @author roxane
 */
public class QuickXplainLTUR {
	public static void main(String args[]) {
		System.out.println("----------------0----------------");
		String model = "X->x.A->b.D->k.B,D->x.C->b.x->false.";
		QuickXplainLTUR q = new QuickXplainLTUR(model);

		HashSet<Assumption> minimal_explanation2 = q.quickExplain();
		for (Assumption ex : minimal_explanation2) {
			System.out.println("EX:" + ex.toString());
		}
	}

	ABTheoremProver theoremprover;
	LinkedHashSet<Assumption> allAssumptions;
	public int shrink_counter = 0;

	public QuickXplainLTUR(ABTheoremProver theoremprover) {
		this.theoremprover = theoremprover;
		this.allAssumptions = new LinkedHashSet<Assumption>(theoremprover.getAssumptions());
	}

	public QuickXplainLTUR(LSentence modelLSentence) {
		this.theoremprover = new ABTheoremProver();
		this.theoremprover = modelLSentence.asABPropositionalSentence(this.theoremprover);
		this.allAssumptions = new LinkedHashSet<Assumption>(this.theoremprover.getAssumptions());
	}

	public QuickXplainLTUR(String model) {
		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parse(model);
		LSentence modelLSentence = (LSentence) parserNewModel.result();
		this.theoremprover = new ABTheoremProver();
		this.theoremprover = modelLSentence.asABPropositionalSentence(this.theoremprover);
		this.allAssumptions = new LinkedHashSet<Assumption>(this.theoremprover.getAssumptions());
	}

	private boolean isConsistent(HashSet<Assumption> assumptions) {
		propagateTrue(assumptions);
		boolean sat = this.theoremprover.isConsistent();
		propagateFalse(assumptions);
		return sat;
	}

	void propagateFalse(HashSet<Assumption> assumptions) {
		for (Assumption r : assumptions) {
			if (r.getLabel() != false) {
				ArrayList v = new ArrayList();
				v = r.propagateFalse(v);
				Iterator ve = v.iterator();
				while (ve.hasNext()) {
					Proposition p = (Proposition) ve.next();
					p.correctLabels();
				}
			}
		}
	}

	void propagateTrue(HashSet<Assumption> assumptions) {
		for (Assumption a : assumptions) {
			if (a.getLabel() != true) {
				a.setLabel(true);
				a.propagateTrue();
			}
		}
	}

	public HashSet<Assumption> quickExplain() {
		if ((this.allAssumptions != null && this.allAssumptions.size() > 0) || isConsistent(this.allAssumptions)) {
			return qX(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(this.allAssumptions));
		}
		return null;
	}

	public HashSet<Assumption> quickExplain(HashSet<Assumption> F) {
		LinkedHashSet<Assumption> assumptions = new LinkedHashSet<>();
		for (Assumption a : F) {
			for (Assumption ass : this.allAssumptions) {
				if (a.identifier.equals(ass.identifier)) {
					assumptions.add(ass);
				}
			}
		}

		if ((assumptions != null && assumptions.size() > 0) || isConsistent(assumptions)) {
			return qX(new LinkedHashSet<>(), new LinkedHashSet<>(), new LinkedHashSet<>(assumptions));
		}
		return null;
	}

	public LinkedHashSet<Assumption> qX(LinkedHashSet<Assumption> background, LinkedHashSet<Assumption> delta,
			LinkedHashSet<Assumption> constraints) {
		this.shrink_counter++;
		if (delta.size() > 0 && !isConsistent(background)) {
			return new LinkedHashSet<Assumption>();
		}
		if (constraints.size() == 1) {
			return constraints;
		}
		int k = split(constraints.size());
		LinkedHashSet<Assumption> c_1 = new LinkedHashSet<>();
		LinkedHashSet<Assumption> c_2 = new LinkedHashSet<>();


		constraints.stream().limit(k).forEachOrdered(c_1::add);
		constraints.stream().skip(k).forEachOrdered(c_2::add);

		LinkedHashSet<Assumption> b_cup_c1 = new LinkedHashSet<>();
		b_cup_c1.addAll(background);
		b_cup_c1.addAll(c_1);

		LinkedHashSet<Assumption> delta_2 = qX(b_cup_c1, c_1, c_2);
		LinkedHashSet<Assumption> b_cup_delta_2 = new LinkedHashSet<>();
		b_cup_delta_2.addAll(background);
		b_cup_delta_2.addAll(delta_2);

		LinkedHashSet<Assumption> delta_1 = qX(b_cup_delta_2, delta_2, c_1);

		delta_1.addAll(delta_2);
		return delta_1;
	}

	private int split(int n) {
		return (int) Math.floor(n / 2);
	}
}
