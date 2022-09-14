package diagnosis.algorithms;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import aima.core.logic.propositional.inference.OptimizedDPLL;
import aima.core.logic.propositional.inference.WalkSAT;
import aima.core.logic.propositional.kb.data.Clause;
import aima.core.logic.propositional.kb.data.Model;
import aima.core.logic.propositional.parsing.PLParser;
import aima.core.logic.propositional.parsing.ast.PropositionSymbol;
import aima.core.logic.propositional.parsing.ast.Sentence;
import aima.core.logic.propositional.visitors.ClauseCollector;
import diagnosis.algorithms.theoremprover.ABTheoremProver;
import diagnosis.algorithms.theoremprover.Assumption;
import diagnosis.algorithms.theoremprover.LSentence;
import diagnosis.algorithms.theoremprover.Proposition;
import diagnosis.data_structures.ExploreClause;
import diagnosis.engines.EXPLorerEngine.MUSExtractionAlgorithm;
import support.Printer;

public class EXPLorerAlgorithm {
	public static ABTheoremProver theoremProver;
	public LSentence modelLSentence;
	private HashSet<Assumption> allAssumptions = new HashSet<>();
	private HashSet<ExploreClause> allAssumptionsClauses = new HashSet<ExploreClause>();
	private HashSet<HashSet<Assumption>> muses = new HashSet<>();
	public double deltaTime = -1;
	public boolean debug = false;
	public HashSet<Assumption> mus_assumptions = new HashSet<>();
	public ABTheoremProver mus_theoremProver;
	public int shrink_count = -1;
	public long max_model_time = -1;

	Assumption getAssumption(HashSet<Assumption> assumptions, String id) {
		for (Assumption a : assumptions) {
			if (id.equals(a.identifier)) {
				return a;
			}
		}
		return null;
	}

	public HashSet<HashSet<Assumption>> getMuses() {
		return this.muses;
	}

	HashSet<Assumption> getUnexplored(String s) {
		if (s.length() < 1) {
			return this.allAssumptions;
		}
		return null;
	}

	/**
	 * Arif M.F., Mencía C., Marques-Silva J. (2015) Efficient MUS Enumeration
	 * of Horn Formulae with Applications to Axiom Pinpointing. In: Heule M.,
	 * Weaver S. (eds) Theory and Applications of Satisfiability Testing -- SAT
	 * 2015. SAT 2015. Lecture Notes in Computer Science, vol 9340. Springer,
	 * Cham
	 * 
	 * @param F
	 * @return MUS
	 */
	public HashSet<Assumption> musExtractionLTUR(HashSet<Assumption> F) {
		HashSet<Assumption> S = new HashSet<>();
		HashSet<Assumption> M = new HashSet<>();

		ABTheoremProver newProver = new ABTheoremProver();

		newProver = this.modelLSentence.asABPropositionalSentence(newProver);
		ArrayList<Assumption> assumptions = newProver.getAssumptions();
		this.mus_assumptions = new HashSet<>(assumptions);
		this.mus_theoremProver = newProver;
		HashSet<Assumption> temp = new HashSet<Assumption>();
		for (Assumption a1 : F) {
			for (Assumption a2 : assumptions) {
				if (a2.identifier.equals(a1.identifier)) {
					temp.add(a2);
				}
			}
		}
		F = temp;

		Assumption transition_clause = null;
		while (true) {
			this.shrink_count++;
			if (transition_clause != null) {
				M.add(transition_clause);
				propagateTrue(M);
				if (!newProver.isConsistent()) {
					return M;
				}
			}

			S = new HashSet<>();
			while (true) {
				transition_clause = F.iterator().next();
				F.remove(transition_clause);
				S.add(transition_clause);

				HashSet<Assumption> M_cup_S = new HashSet<>();
				M_cup_S.addAll(S);
				M_cup_S.addAll(M);
				HashSet<Assumption> cr = new HashSet<>();
				cr.add(transition_clause);
				propagateTrue(cr);
				if (!newProver.isConsistent()) {
					F.clear();
					F.addAll(S);
					F.remove(transition_clause);
					propagateFalse(S);
					break;
				}
			}
		}

	}

	/**
	 * Diagnosis using Explorer, i.e., exploration of the power set lattice,
	 * without maximal model as seed.
	 * 
	 * @param model
	 * @param algorithm
	 */
	public void performDiagnosis(LSentence model, MUSExtractionAlgorithm algorithm) {
		this.modelLSentence = model;
		PLParser parser = null;
		Sentence s = null;
		StringBuilder sb;
		new HashSet<ExploreClause>();
		if (this.modelLSentence != null) {
			theoremProver = new ABTheoremProver();
			theoremProver = this.modelLSentence.asABPropositionalSentence(theoremProver);
			if (theoremProver == null) {
				printDebug("TP null");
			} else {
				HashSet<Assumption> F = new HashSet<>();
				this.allAssumptions.addAll(theoremProver.getAssumptions());
				F.addAll(theoremProver.getAssumptions());
				sb = new StringBuilder("(");
				Iterator<Assumption> iterator = F.iterator();
				while (iterator.hasNext()) {
					Assumption assumption = iterator.next();
					sb.append(assumption.identifier);
					if (iterator.hasNext()) {
						sb.append("|");
					}
				}
				sb.append(")");
				double time = System.nanoTime();
				while (true) {
					parser = new PLParser();
					s = parser.parse(sb.toString());
					OptimizedDPLL dpplsat = new OptimizedDPLL();
					boolean sat = dpplsat.dpllSatisfiable(s);
					if (!sat) {
						this.deltaTime = (System.nanoTime() - time) / 1000000;
						return;
					} else {
						Set<Clause> clauseSet = ClauseCollector.getClausesFrom(s);
						WalkSAT walksat = new WalkSAT();
						Model mymodel = walksat.walkSAT(clauseSet, 0.5, -1);
						Set<PropositionSymbol> set_ps = mymodel.getAssignedSymbols();
						F = new HashSet<>();
						for (PropositionSymbol symbol : set_ps) {
							Boolean value = mymodel.getValue(symbol);
							if (value) {
								F.add(getAssumption(this.allAssumptions, symbol.getSymbol()));
							}
						}

						propagateTrue(F);
						boolean seed_sat;
						if (theoremProver.isConsistent()) {
							seed_sat = true;
						} else {
							seed_sat = false;
						}

						propagateFalse(F);

						if (!seed_sat) {
							HashSet<Assumption> mus;
							if (algorithm.equals(MUSExtractionAlgorithm.LTUR_like)) {
								mus = plain_insertion_based_LTUR_like(F);

							} else if (algorithm.equals(MUSExtractionAlgorithm.LTUR_like_context)) {
								mus = plain_insertion_based_LTUR_like_considering_context(F);
							} else if (algorithm.equals(MUSExtractionAlgorithm.SimpleInsertion)) {
								mus = plain_insertion_based(F);
							} else {
								mus = quickXplain(F);
							}
							this.muses.add(mus);

							StringBuilder negative_clause_str = new StringBuilder();
							Iterator<Assumption> i = mus.iterator();
							while (i.hasNext()) {
								Assumption a = i.next();
								negative_clause_str.append("~").append(a.identifier);
								if (i.hasNext()) {
									negative_clause_str.append("|");
								}

							}
							if (sb.length() > 1) {
								sb.append(" & ");
							}
							sb.append("(").append(negative_clause_str.toString()).append(")");

						}

						else {
							if (sb.length() > 1) {
								sb.append(" & ");
							}
							sb.append("(");
							Iterator<Assumption> it = this.allAssumptions.iterator();
							while (it.hasNext()) {
								Assumption a = it.next();
								if (!F.contains(a)) {
									sb.append(a.identifier);
									sb.append("|");
								}

							}
							String str = sb.toString().substring(0, sb.length() - 1);
							sb = new StringBuilder(str + ")");

						}
					}
				}

			}
		}

	}

	/**
	 * Diagnosis using Explorer, i.e., exploration of the power set lattice,
	 * with maximal model as seed.
	 * 
	 * @param model
	 * @param algorithm
	 */
	public void performDiagnosisWithMaxModel(LSentence model, MUSExtractionAlgorithm algorithm) {
		this.modelLSentence = model;
		HashSet<ExploreClause> clauses = new HashSet<ExploreClause>();
		if (this.modelLSentence != null) {
			theoremProver = new ABTheoremProver();
			theoremProver = this.modelLSentence.asABPropositionalSentence(theoremProver);
			if (theoremProver == null) {
				printDebug("TP null");
			} else {
				HashSet<Assumption> F = new HashSet<>();
				this.allAssumptions.addAll(theoremProver.getAssumptions());
				F.addAll(theoremProver.getAssumptions());
				ExploreClause init_clause = new ExploreClause();

				Iterator<Assumption> iterator = F.iterator();
				while (iterator.hasNext()) {
					Assumption assumption = iterator.next();
					String id = (String) assumption.identifier;
					init_clause.addPositiveLiteral(id);
				}
				clauses.add(init_clause);
				double time = System.nanoTime();
				while (true) {
					MaximalModel mm = new MaximalModel();
					long start_time_max = System.nanoTime();
					HashSet<String> max_model = mm.getMaximalModel(clauses);
					this.max_model_time += (System.nanoTime() - start_time_max);
					if (max_model != null) {
						F = new HashSet<>();
						for (String literal : max_model) {
							F.add(getAssumption(this.allAssumptions, literal));
						}

						HashSet<Assumption> negative = new HashSet<Assumption>(theoremProver.getAssumptions());
						HashSet<Assumption> positive = new HashSet<Assumption>(F);
						negative.removeAll(F);

						for (Assumption my_assumption : this.allAssumptions) {
							if (!my_assumption.label && negative.contains(my_assumption)) {
								negative.remove(my_assumption);
							} else if (my_assumption.label && F.contains(my_assumption)) {
								positive.remove(my_assumption);
							}
						}
						theoremProver.checkConsistency(new ArrayList<Assumption>(positive),
								new ArrayList<Assumption>(negative));

						boolean seed_sat;

						if (theoremProver.isConsistent()) {
							seed_sat = true;
						} else {
							seed_sat = false;
						}

						if (!seed_sat) {
							HashSet<Assumption> mus;
							if (algorithm.equals(MUSExtractionAlgorithm.LTUR_like)) {
								mus = plain_insertion_based_LTUR_like(F);

							} else if (algorithm.equals(MUSExtractionAlgorithm.LTUR_like_context)) {
								mus = plain_insertion_based_LTUR_like_considering_context(F);
							} else if (algorithm.equals(MUSExtractionAlgorithm.SimpleInsertion)) {
								mus = plain_insertion_based(F);
							} else {
								mus = quickXplain(F);
							}

							
							this.muses.add(mus);

							StringBuilder negative_clause_str = new StringBuilder();
							ExploreClause block_clause = new ExploreClause();
							Iterator<Assumption> i = mus.iterator();
							while (i.hasNext()) {
								Assumption a = i.next();
								negative_clause_str.append("~").append(a.identifier);
								block_clause.addNegativeLiteral((String) a.identifier);
								if (i.hasNext()) {
									negative_clause_str.append("|");
								}

							}
							clauses.add(block_clause);

						} else {
							ExploreClause block_clause = new ExploreClause();
							Iterator<Assumption> it = this.allAssumptions.iterator();
							while (it.hasNext()) {
								Assumption a = it.next();
								if (!F.contains(a)) {
									block_clause.addPositiveLiteral((String) a.identifier);
								}

							}
							clauses.add(block_clause);
						}

					} else {
						this.deltaTime = (System.nanoTime() - time) / 1000000;
						return;
					}

				}

			}
		}

	}

	/**
	 * Plain Insertion based MUS Extraction
	 *
	 * @param F
	 * @return
	 */
	HashSet<Assumption> plain_insertion_based(HashSet<Assumption> F) {
		ABTheoremProver newProver = new ABTheoremProver();
		newProver = this.modelLSentence.asABPropositionalSentence(newProver);
		ArrayList<Assumption> assumptions = newProver.getAssumptions();
		this.mus_assumptions = new HashSet<>(assumptions);
		this.mus_theoremProver = newProver;
		HashSet<Assumption> temp = new HashSet<Assumption>();
		for (Assumption a1 : F) {
			for (Assumption a2 : assumptions) {
				if (a2.identifier.equals(a1.identifier)) {
					temp.add(a2);
				}
			}
		}
		F = temp;

		HashSet<Assumption> S;
		HashSet<Assumption> M = new HashSet<>();
		Assumption cr;
		while (!F.isEmpty()) {
			this.shrink_count++;
			S = new HashSet<>();
			cr = null;
			HashSet<Assumption> M_cup_S = new HashSet<>();
			M_cup_S.addAll(S);
			M_cup_S.addAll(M);
			while (true) {
				propagateTrue(M_cup_S);
				if (newProver.isConsistent()) {
					if (F.iterator().hasNext()) {
						cr = F.iterator().next();
						F.remove(cr);
						S.add(cr);
						M_cup_S.add(cr);
					}
				} else {
					break;
				}
			}
			propagateFalse(M_cup_S);
			if (cr != null) {
				M.add(cr);
			}
			F.clear();
			F.addAll(S);
			F.remove(cr);

		}

		return M;
	}

	/**
	 * Arif M.F., Mencía C., Marques-Silva J. (2015) Efficient MUS Enumeration
	 * of Horn Formulae with Applications to Axiom Pinpointing. In: Heule M.,
	 * Weaver S. (eds) Theory and Applications of Satisfiability Testing -- SAT
	 * 2015. SAT 2015. Lecture Notes in Computer Science, vol 9340. Springer,
	 * Cham
	 * 
	 * @param F
	 * @return
	 */
	HashSet<Assumption> plain_insertion_based_LTUR_like(HashSet<Assumption> F) {
		HashSet<Assumption> S = new HashSet<>();
		HashSet<Assumption> M = new HashSet<>();

		ABTheoremProver newProver = new ABTheoremProver();
		newProver = this.modelLSentence.asABPropositionalSentence(newProver);
		ArrayList<Assumption> assumptions = newProver.getAssumptions();
		this.mus_assumptions = new HashSet<>(assumptions);
		this.mus_theoremProver = newProver;
		HashSet<Assumption> temp = new HashSet<Assumption>();
		for (Assumption a1 : F) {
			for (Assumption a2 : assumptions) {
				if (a2.identifier.equals(a1.identifier)) {
					temp.add(a2);
				}
			}
		}
		F = temp;

		Assumption transition_clause = null;
		while (true) {
			this.shrink_count++;
			if (transition_clause != null) {
				M.add(transition_clause);
				propagateTrue(M);
				if (!newProver.isConsistent()) {
					return M;
				}
			}

			S = new HashSet<>();
			while (true) {
				transition_clause = F.iterator().next();
				F.remove(transition_clause);
				S.add(transition_clause);

				HashSet<Assumption> M_cup_S = new HashSet<>();
				M_cup_S.addAll(S);
				M_cup_S.addAll(M);
				propagateTrue(M_cup_S);
				if (!newProver.isConsistent()) {
					F.clear();
					F.addAll(S);
					F.remove(transition_clause);
					propagateFalse(M_cup_S);
					break;
				}
			}
		}

	}

	/**
	 * Arif M.F., Mencía C., Marques-Silva J. (2015) Efficient MUS Enumeration
	 * of Horn Formulae with Applications to Axiom Pinpointing. In: Heule M.,
	 * Weaver S. (eds) Theory and Applications of Satisfiability Testing -- SAT
	 * 2015. SAT 2015. Lecture Notes in Computer Science, vol 9340. Springer,
	 * Cham
	 * 
	 * @param F
	 * @return
	 */

	HashSet<Assumption> plain_insertion_based_LTUR_like_considering_context(HashSet<Assumption> F) {
		HashSet<Assumption> S = new HashSet<>();
		HashSet<Assumption> M = new HashSet<>();

		ABTheoremProver newProver = new ABTheoremProver();

		newProver = this.modelLSentence.asABPropositionalSentence(newProver);
		ArrayList<Assumption> assumptions = newProver.getAssumptions();
		this.mus_assumptions = new HashSet<>(assumptions);
		this.mus_theoremProver = newProver;
		HashSet<Assumption> temp = new HashSet<Assumption>();
		for (Assumption a1 : F) {
			for (Assumption a2 : assumptions) {
				if (a2.identifier.equals(a1.identifier)) {
					temp.add(a2);
				}
			}
		}
		F = temp;

		Assumption transition_clause = null;
		while (true) {
			this.shrink_count++;
			if (transition_clause != null) {
				M.add(transition_clause);
				propagateTrue(M);
				if (!newProver.isConsistent()) {
					return M;
				}
			}

			S = new HashSet<>();
			while (true) {
				transition_clause = F.iterator().next();
				F.remove(transition_clause);

				S.add(transition_clause);

				HashSet<Assumption> M_cup_S = new HashSet<>();
				M_cup_S.addAll(S);
				M_cup_S.addAll(M);
				HashSet<Assumption> cr = new HashSet<>();
				cr.add(transition_clause);
				propagateTrue(cr);

				if (!newProver.isConsistent()) {

					F.clear();
					F.addAll(S);
					F.remove(transition_clause);
					propagateFalse(S);
					break;
				}
			}
		}

	}

	public void printDebug(String msg) {
		if (this.debug) {
			System.out.println(msg);
		}
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

	HashSet<Assumption> quickXplain(HashSet<Assumption> F) {
		QuickXplainLTUR qx = new QuickXplainLTUR(this.modelLSentence);
		this.mus_assumptions = qx.allAssumptions;
		this.mus_theoremProver = qx.theoremprover;
		this.shrink_count = qx.shrink_counter;
		return qx.quickExplain(F);
	}

	HashSet<Assumption> quickXplainSAT(HashSet<Assumption> F) {
		QuickXplain qx = new QuickXplain();
		qx.parseStringToCNFFormula(this.modelLSentence.toString());
		return null;
	}

}
