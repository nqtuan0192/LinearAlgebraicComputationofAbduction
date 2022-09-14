package diagnosis.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import diagnosis.algorithms.hittingsetalg.MinHittingSets;
import diagnosis.algorithms.hittingsetalg.MinHittingSets.Minimizer;
import diagnosis.algorithms.theoremprover.ABTheoremProver;
import diagnosis.algorithms.theoremprover.Assumption;
import diagnosis.algorithms.theoremprover.LSentence;
import diagnosis.algorithms.theoremprover.LogicParser;
import diagnosis.algorithms.theoremprover.Proposition;
import diagnosis.data_structures.Diagnosis;

public class ReiterConflictEngine implements DiagnosisEngine {

	public static class ReiterCaller implements Callable<MinHittingSets> {
		private MinHittingSets hs;

		private ReiterCaller(MinHittingSets hs) {
			this.hs = hs;
		}

		@Override
		public MinHittingSets call() throws Exception {
			this.hs.compute(100, -1);
			return this.hs;
		}

	}

	private static ABTheoremProver theoremProver;
	private static MinHittingSets hs;
	public int diangnosisSize = -1;
	public Set<Set<String>> diagnoses;
	private Set<Diagnosis> diag = new HashSet<Diagnosis>();
	private String error;
	private StringBuilder stats = new StringBuilder();
	private boolean is_timeout = false;
	private double delta_time = -1;
	private String original_model;
	public boolean minimizeInBetween = true;

	public Minimizer minimizer;

	String buildDiagnosisInput(String model, HashSet<String> observations) {
		model = model + ("\n");
		StringBuilder explain = new StringBuilder();
		Iterator<String> i = observations.iterator();
		while (i.hasNext()) {
			String obs = i.next();
			explain.append(obs);
			if (i.hasNext()) {
				explain.append(",");
			}
		}

		explain.append("->false.");
		model = model + (explain.toString());

		return model;
	}

	LSentence buildDiagnosisInputReiter(String file, HashSet<String> observations) {
		LSentence lsentence;
		StringBuilder model = new StringBuilder();
		lsentence = parseFile(file);
		if (lsentence != null) {
			model.append(lsentence.toString());
			model.append("\n");
			StringBuilder explain = new StringBuilder();
			Iterator<String> i = observations.iterator();
			while (i.hasNext()) {
				String obs = i.next();
				explain.append(obs);
				if (i.hasNext()) {
					explain.append(",");
				}
			}
			explain.append("->false.");
			model.append(explain.toString());
		}

		LogicParser parser = new LogicParser();
		LSentence allRules = new LSentence();
		parser.parse(model.toString());
		allRules = (LSentence) parser.result();
		if (allRules == null) {
			System.out.println("parse error");
			this.error = "parse error " + file;
			return null;
		}
		return allRules;
	}

	private void callThread(long timeout) throws Exception {
		ExecutorService e = Executors.newSingleThreadExecutor();
		ReiterCaller caller = new ReiterCaller(hs);
		double time = System.nanoTime();
		Future<MinHittingSets> control = e.submit(caller);
		e.shutdown();
		if (timeout > 0) {
			hs = control.get(timeout, TimeUnit.MILLISECONDS);
		} else {
			hs = control.get();
		}
		this.delta_time = (System.nanoTime() - time) / 1000000;
	}

	private void createDiagnoses(HashSet<ArrayList<Assumption>> conflictSet) {
		this.diagnoses = new HashSet<Set<String>>();
		for (ArrayList<Assumption> list : conflictSet) {
			HashSet<String> set = new HashSet<>();
			for (Assumption a : list) {
				String id = a.toString();
				set.add(id);
			}
			this.diagnoses.add(set);
		}

	}

	@Override
	public double getDeltaTime() {
		return this.delta_time;
	}

	@Override
	public HashSet<Diagnosis> getDiag() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashSet<HashSet<String>> getDiagnoses() {
		HashSet<HashSet<String>> temp = new HashSet<HashSet<String>>();
		temp.addAll((Collection<? extends HashSet<String>>) this.diagnoses);
		return temp;
	}

	@Override
	public String getError() {
		return this.error;
	}

	@Override
	public String getStats() {
		return this.stats.toString();
	}

	private boolean isConsistent(ArrayList<Assumption> conflict_tmp, ABTheoremProver theoremProver) {
		propagateFalse(conflict_tmp);
		boolean sat = theoremProver.isConsistent();
		return sat;
	}

	@Override
	public boolean isTimeout() {
		return this.is_timeout;
	}

	private LSentence parseFile(String file) {
		LogicParser parser = new LogicParser();
		System.currentTimeMillis();
		if (!parser.parseFile(file)) {
			System.out.println("error " + file);
			this.error = "parse error " + file;
			return null;
		}
		return (LSentence) parser.result();
	}

	public void performDiagnosis(String model, HashSet<String> observations, long timeout) {
		this.original_model = model;
		model = buildDiagnosisInput(model, observations);
		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parse(model);
		LSentence modelLSentence = (LSentence) parserNewModel.result();
		ArrayList<ArrayList<Assumption>> conflicts = null;
		HashSet<ArrayList<Assumption>> conflictSet = new HashSet<>();

		if (modelLSentence != null) {
			theoremProver = new ABTheoremProver();
			theoremProver = modelLSentence.asABPropositionalSentence(theoremProver);
			if (theoremProver == null) {
				System.out.println("TP null");
				this.error = "TP null";
			} else {
				if (!this.minimizeInBetween) {
					hs = new MinHittingSets(true, theoremProver);
					hs.minimizeInBetween = false;
				} else {
					hs = new MinHittingSets(true, theoremProver, modelLSentence, this.minimizer, true);
				}
				if (timeout > 0) {
					hs.timeout = timeout;
				}

				try {
					
					callThread(timeout);
				} catch (CancellationException ex) {
					this.is_timeout = true;
					ex.printStackTrace();
				} catch (InterruptedException ex) {
					this.is_timeout = true;
					ex.printStackTrace();
				} catch (ExecutionException ex) {
					this.is_timeout = true;
					ex.printStackTrace();
				} catch (Exception ex) {
					this.is_timeout = true;
					ex.printStackTrace();
				}

				//// double time = System.nanoTime();
				//// int returnValue = hs.compute(100, -1);
				//// delta_time = (System.nanoTime()-time)/1000000;
				// if(returnValue==hs.CS_TIMEOUT_REACHED){
				// error = "Timeout";
				// is_timeout = true;
				//
				// }
				conflicts = hs.getConflictsAsAss();
				// if not minimized during HS computing, than minimize
				// afterwards
				if (!this.minimizeInBetween) {
					if (conflicts.size() > 0) {
						double time = System.nanoTime();
						conflictSet = retrieveMinimalConflicts(conflicts);
						this.delta_time += ((System.nanoTime() - time) / 1000000);
					}
				} else {
					conflictSet = new HashSet<>(conflicts);
				}

				// check if conflicts are consistent
				parserNewModel = new LogicParser();
				parserNewModel.parse(this.original_model);
				LSentence modelLSentence_original = (LSentence) parserNewModel.result();
				ABTheoremProver theoremProver2 = new ABTheoremProver();
				theoremProver2 = modelLSentence_original.asABPropositionalSentence(theoremProver2);
				ArrayList<Assumption> assumptions = new ArrayList<>(theoremProver2.getAssumptions());
				propagateTrue(assumptions);
				HashSet<ArrayList<Assumption>> tmp = new HashSet<>(conflictSet);

				Iterator<ArrayList<Assumption>> iterator = tmp.iterator();
				while (iterator.hasNext()) {
					ArrayList<Assumption> conflict = iterator.next();
					ArrayList<Assumption> conflict_tmp = new ArrayList<>();
					for (Assumption a1 : conflict) {
						for (Assumption a2 : assumptions) {
							if (a1.identifier.equals(a2.identifier)) {
								conflict_tmp.add(a2);
							}
						}
					}
					if (!isConsistent(conflict_tmp, theoremProver2)) {
						iterator.remove();
					}
				}

				// for(ArrayList<Assumption> conflict:tmp){
				// if (!isConsistent(conflict)) {
				// conflictSet.remove(conflict);
				// }
				// }

			}
		}
		if (conflicts != null) {
			createDiagnoses(conflictSet);
			this.diangnosisSize = conflictSet.size();
			retrieveStats(conflictSet);

		} else {
			this.error = "No diagnoses";
		}

	}

	void propagateFalse(ArrayList<Assumption> assumptions) {
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

	void propagateTrue(ArrayList<Assumption> assumptions) {
		for (Assumption a : assumptions) {
			if (a.getLabel() != true) {
				a.setLabel(true);
				a.propagateTrue();
			}
		}
	}

	private HashSet<ArrayList<Assumption>> retrieveMinimalConflicts(ArrayList<ArrayList<Assumption>> conflicts) {
		ArrayList<ArrayList<Assumption>> minConflicts = new ArrayList<ArrayList<Assumption>>(conflicts);
		for (int i = 0; i < conflicts.size(); i++) {

			ArrayList<Assumption> conflict1 = conflicts.get(i);
			for (int j = i + 1; j < conflicts.size(); j++) {
				ArrayList<Assumption> conflict2 = conflicts.get(j);
				// if(i!=j){
				if (!conflict1.equals(conflict2)) {
					if (subset(conflict1, conflict2)) {
						minConflicts.remove(conflict2);
					} else if (subset(conflict2, conflict1)) {
						minConflicts.remove(conflict1);
					}
				}
			}
		}
		HashSet<ArrayList<Assumption>> minConflictSet = new HashSet<>(minConflicts);
		return minConflictSet;
	}

	private void retrieveStats(Set<ArrayList<Assumption>> conflicts) {

		if (this.is_timeout) {
			this.stats.append("TIMEOUT \n\r");
			return;
		}
		int maxSize = 0;
		int counterSingle = 0;
		int counterDouble = 0;
		int counterTriple = 0;
		int counterRest = 0;
		for (ArrayList<Assumption> conflict : conflicts) {
			switch (conflict.size()) {
			case 1:
				counterSingle++;
				break;
			case 2:
				counterDouble++;
				break;
			case 3:
				counterTriple++;
				break;
			default:
				counterRest++;
				if (maxSize < conflict.size()) {
					maxSize = conflict.size();
				}
			}
		}
		this.stats.append(this.delta_time + "," + this.diagnoses.size() + "," + counterSingle + "," + counterDouble
				+ "," + counterTriple + "," + counterRest + // +"\n");//
															// hs.quick_xplain_time);
				"," + maxSize + "," + hs.call_TP_counter + "\n");
	}

	@Override
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		// Compilation

		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parseFile(model_file.getAbsolutePath());
		this.original_model = parserNewModel.result().toString();
		LSentence modelLSentence = (LSentence) parserNewModel.result();
		performDiagnosis(modelLSentence.toString(), observations, timeout);

	}

	@Override
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		performDiagnosis(model, observations, timeout);

	}

	private boolean subset(ArrayList<Assumption> s1, ArrayList<Assumption> s2) {
		// Returns true if s1 is a subset of s2
		if (s1.size() <= s2.size()) {
			if (s2.containsAll(s1)) {
				return true;
			} else {
				return false;
			}
		} else {
			return false;
		}
	}
}
