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



import diagnosis.algorithms.EXPLorerAlgorithm;
import diagnosis.algorithms.theoremprover.ABTheoremProver;
import diagnosis.algorithms.theoremprover.Assumption;
import diagnosis.algorithms.theoremprover.LSentence;
import diagnosis.algorithms.theoremprover.LogicParser;
import diagnosis.algorithms.theoremprover.Proposition;
import diagnosis.data_structures.Diagnosis;


public class EXPLorerEngine implements DiagnosisEngine {

	public static class EXPLorerCaller implements Callable<EXPLorerAlgorithm> {
		private EXPLorerAlgorithm engine;
		private MUSExtractionAlgorithm algorithm;
		private LSentence model;
		private SEED seed;

		private EXPLorerCaller(EXPLorerAlgorithm engine, MUSExtractionAlgorithm algorithm, LSentence model, SEED seed) {
			this.algorithm = algorithm;
			this.engine = engine;
			this.model = model;
			this.seed = seed;
		}

		@Override
		public EXPLorerAlgorithm call() throws Exception {
			if (this.seed.equals(SEED.MAX_MODEL)) {
				this.engine.performDiagnosisWithMaxModel(this.model, this.algorithm);
			} else {
				this.engine.performDiagnosis(this.model, this.algorithm);
			}
			return this.engine;
		}

	}

	public enum MUSExtractionAlgorithm {
		SimpleInsertion, LTUR_like, LTUR_like_context, QUICKXPLAIN
	}

	public enum SEED {
		MAX_MODEL, ANY_ASSIGNMENT
	}


	EXPLorerAlgorithm explorer = new EXPLorerAlgorithm();
	public int diangnosisSize = -1;
	public Set<Set<String>> diagnoses;
	private Set<Diagnosis> diag = new HashSet<Diagnosis>();
	private String error;
	private StringBuilder stats = new StringBuilder();
	private boolean is_timeout = false;
	private double delta_time = -1;
	public int counter = 0;

	public boolean debug = true;

	private long timeout = -1;
	private LSentence modelLSentence;

	public SEED seed = SEED.ANY_ASSIGNMENT;

	public MUSExtractionAlgorithm algo = MUSExtractionAlgorithm.QUICKXPLAIN;

	String buildDiagnosisInput(String model, HashSet<String> observations) {
		model = model + ("\n");
		String[] lines = model.split(System.getProperty("line.separator"));
		for (int i = 0; i < lines.length; i++) {
			if (lines[i].contains("explain")) {
				model = model.replace(lines[i], "");
			}
		}
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
			printDebug("parse error");
			this.error = "parse error " + file;
			return null;
		}
		return allRules;
	}

	private void callThread(MUSExtractionAlgorithm algorithm, LSentence model, long timeout) throws Exception {
		ExecutorService e = Executors.newSingleThreadExecutor();
		this.explorer = new EXPLorerAlgorithm();
		EXPLorerCaller caller = new EXPLorerCaller(this.explorer, algorithm, model, this.seed);

		try {
			Future<EXPLorerAlgorithm> control = e.submit(caller);
			if (this.timeout > 0) {
				this.explorer = control.get(this.timeout, TimeUnit.MILLISECONDS);
			} else {
				this.explorer = control.get();
			}
			this.delta_time = this.explorer.deltaTime;

			// check if conflicts are consistent
			HashSet<HashSet<Assumption>> tmp = this.explorer.getMuses();
			ABTheoremProver theoremProver = new ABTheoremProver();
			theoremProver = this.modelLSentence.asABPropositionalSentence(theoremProver);
			HashSet<Assumption> assumptions = new HashSet<>(theoremProver.getAssumptions());
			propagateTrue(assumptions);
			Iterator<HashSet<Assumption>> iterator = tmp.iterator();
			while (iterator.hasNext()) {
				HashSet<Assumption> conflict = iterator.next();
				HashSet<Assumption> conflict_tmp = new HashSet<>();
				for (Assumption a1 : conflict) {
					for (Assumption a2 : assumptions) {
						if (a1.identifier.equals(a2.identifier)) {
							conflict_tmp.add(a2);
						}
					}
				}
				if (!isConsistent(conflict_tmp, theoremProver)) {
					iterator.remove();
				}
			}

			createDiagnoses(tmp);
			retrieveStats(this.diagnoses);
		} catch (CancellationException ex) {
			this.is_timeout = true;
			ex.printStackTrace();
			throw ex;
		} catch (InterruptedException ex) {
			this.is_timeout = true;
			ex.printStackTrace();
			throw ex;
		} catch (ExecutionException ex) {
			this.is_timeout = true;
			ex.printStackTrace();
			throw ex;
		} catch (Exception ex) {
			ex.printStackTrace();
			throw ex;
		}

	}

	private void createDiagnoses(HashSet<HashSet<Assumption>> muses) {
		this.diagnoses = new HashSet<Set<String>>();
		for (HashSet<Assumption> list : muses) {
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

	private boolean isConsistent(HashSet<Assumption> conflict, ABTheoremProver theoremProver) {
		propagateTrue(conflict);
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
			printDebug("error " + file);
			this.error = "parse error " + file;
			return null;
		}
		return (LSentence) parser.result();
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

	void propagateTrue(HashSet<Assumption> arrayList) {
		for (Assumption a : arrayList) {
			if (a.getLabel() != true) {
				a.setLabel(true);
				a.propagateTrue();
			}
		}
	}

	private void retrieveStats(Set<Set<String>> diagnoses2) {
		if (this.is_timeout) {
			this.stats.append("TIMEOUT \n\r");
			return;
		}
		int diagnosisSize = diagnoses2.size();
		int counterSingle = 0;
		int counterDouble = 0;
		int counterTriple = 0;
		int counterRest = 0;
		Iterator<Set<String>> iterator = diagnoses2.iterator();
		while (iterator.hasNext()) {
			Set<String> diagnosis = iterator.next();
			switch (diagnosis.size()) {
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
			}
		}
		this.stats.append(Double.toString(this.delta_time) + "," + diagnosisSize + "," + counterSingle + ","
				+ counterDouble + "," + counterTriple + "," + counterRest + "," + // "\n");
				(this.explorer.max_model_time / 1000000) + "," + this.explorer.shrink_count + "\n");
	}

	@Override
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		// Compilation
		this.timeout = timeout;
		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parseFile(model_file.getAbsolutePath());
		this.modelLSentence = (LSentence) parserNewModel.result();
		String model = buildDiagnosisInput(this.modelLSentence.toString(), observations);
		parserNewModel = new LogicParser();
		parserNewModel.parse(model);
		LSentence modelLSentence_with_obs = (LSentence) parserNewModel.result();
		try {
			callThread(this.algo, modelLSentence_with_obs, timeout);

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
	}

	@Override
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		this.timeout = timeout;
		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parse(model);
		this.modelLSentence = (LSentence) parserNewModel.result();
		String model_with_obs = buildDiagnosisInput(this.modelLSentence.toString(), observations);
		parserNewModel = new LogicParser();
		parserNewModel.parse(model_with_obs);
		LSentence modelLSentence_with_obs = (LSentence) parserNewModel.result();
		try {
			callThread(this.algo, modelLSentence_with_obs, timeout);

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
