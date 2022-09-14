package diagnosis.engines;

import java.io.File;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import diagnosis.algorithms.atms.ATMSLabel;
import diagnosis.algorithms.atms.ATMSNode;
import diagnosis.algorithms.atms.ATMSRule;
import diagnosis.algorithms.atms.ATMSTextInterface;
import diagnosis.algorithms.atms.Converter4ATMS;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;
import diagnosis.data_structures.Diagnosis;
import diagnosis.data_structures.Hypothesis;
import model.data_structures.FaultMode;
import support.Printer;

public class ATMSEngine implements DiagnosisEngine {

	public static class ATMSCaller implements Callable<ATMSTextInterface> {

		LinkedList<LinkedList<String>> list;
		ATMSTextInterface atms;

		private ATMSCaller(LinkedList<LinkedList<String>> list) {
			this.list = list;
		}

		@Override
		public ATMSTextInterface call() throws Exception {
			this.atms = ATMSTextInterface.create(this.list);
			return this.atms;
		}

	}

	private static int propagationCounterLast = 0;
	private static int minimizeCounterLast = 0;

	public static void main(String args[]) {
		String model = "A,B->c.\n A,F->g.";
		HashSet<String> obs = new HashSet<>();
		obs.add("c");
		ATMSEngine atms = new ATMSEngine();
		atms.startComputation(model, obs, null, -1);
		Printer.printCollection(atms.getDiagnoses());

	}

	private HashSet<HashSet<String>> diagnoses;
	private HashSet<Diagnosis> diag = new HashSet<Diagnosis>();
	private Set<ATMSNode> hypotheses = new HashSet<>();
	private String error;
	private StringBuilder stats = new StringBuilder();
	private boolean is_timeout = false;

	private double delta_time; // time in milliseconds

	private long timeout = -1;

	public Set<Set<String>> createComponentModeSet(ArrayList<FaultMode> faultmodes) {
		Set<Set<String>> component_mode = new HashSet<Set<String>>();
		for (Set<String> set : this.diagnoses) {
			Set<String> diagnosis = new HashSet<String>();
			for (String str : set) {
				String couple = retrieveOriginalComponentMode(faultmodes, str);
				if (couple != "") {
					diagnosis.add(couple);
				}
			}
			component_mode.add(diagnosis);
		}

		return component_mode;
	}

	private void createDiagnoses(HashSet<HashSet<String>> label) {
		for (Set<String> set : label) {
			Diagnosis d = new Diagnosis();
			HashSet<Hypothesis> hypos = new HashSet<Hypothesis>();
			for (String str : set) {
				Hypothesis hypo = new Hypothesis(str);
				hypos.add(hypo);
			}
			d.setHypotheses(hypos);
			this.diag.add(d);
		}
	}

	@Override
	public double getDeltaTime() {
		return this.delta_time;
	}

	@Override
	public HashSet<Diagnosis> getDiag() {
		return this.diag;
	}

	@Override
	public HashSet<HashSet<String>> getDiagnoses() {
		return this.diagnoses;
	}

	@Override
	public String getError() {
		return this.error;
	}

	@Override
	public String getStats() {
		// TODO Auto-generated method stub
		return this.stats.toString();
	}

	@Override
	public boolean isTimeout() {
		return this.is_timeout;
	}

	private void performDiagnosis(LSentence modelLSentence) {
		// Conversion
		LinkedList<LinkedList<String>> list = Converter4ATMS.convert(modelLSentence);

		// Diagnosis
		ATMSTextInterface atms = new ATMSTextInterface();
		ExecutorService e = Executors.newSingleThreadExecutor();
		ATMSCaller caller = new ATMSCaller(list);
		double time = System.nanoTime();
		try {
			Future<ATMSTextInterface> control = e.submit(caller);
			e.shutdown();
			if (this.timeout > 0) {
				atms = control.get(this.timeout, TimeUnit.MILLISECONDS);
			} else {
				atms = control.get();
			}
			this.delta_time = (System.nanoTime() - time) / 1000000;
			if (atms != null) {
				this.hypotheses = atms.atms.allAssumptions();

				Set<ATMSNode> atmsnodes = atms.atms.allNodesWithoutAssumptions();
				for (ATMSNode node : atmsnodes) {
					if (node.identifier.equals("explain")) {
						HashSet<HashSet<String>> label = new HashSet<HashSet<String>>();
						label.addAll((Collection<? extends HashSet<String>>) node.label.toSet());
						setDiagnoses(label);
						createDiagnoses(label);
						retrieveStats(node, modelLSentence);
					}
				}
			} else {
				this.is_timeout = true;
				setError("Timeout ATMS");
				e.shutdown();
			}
		} catch (CancellationException ex) {
			this.is_timeout = true;
			setError("Timeout ATMS");
			ex.printStackTrace();
			e.shutdown();
		} catch (InterruptedException ex) {
			this.is_timeout = true;
			ex.printStackTrace();
		} catch (ExecutionException ex) {
			this.is_timeout = true;
			ex.printStackTrace();
		} catch (Exception ex) {
			this.is_timeout = true;
			ex.printStackTrace();
		} finally {
			e.shutdown();
		}

	}

	private String retrieveOriginalComponentMode(ArrayList<FaultMode> faultmodes, String str) {
		for (FaultMode fm : faultmodes) {
			if (fm.getId().equals(str.replace(" ", ""))) {
				return "Mode(" + fm.getOriginal_component() + ", " + fm.getOriginal_mode() + ")";
			}
		}
		return "";
	}

	private void retrieveStats(ATMSNode node, LSentence modelLSentence) {

		if (this.is_timeout) {
			this.stats.append("TIMEOUT");
			return;
		}
		int counterSingle = 0;
		int counterDouble = 0;
		int counterTriple = 0;
		int counterRest = 0;
		for (Set<String> set : node.label.toSet()) {
			switch (set.size()) {
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
		this.stats.append(Double.toString(this.delta_time) + "," + this.diagnoses.size() + "," + counterSingle + ","
				+ counterDouble + "," + counterTriple + "," + counterRest + "," + 
				(ATMSRule.propagateCounter - propagationCounterLast) + ","
				+ (ATMSLabel.minimizeCounter - minimizeCounterLast) + "\n");
		minimizeCounterLast = ATMSLabel.minimizeCounter;
		propagationCounterLast = ATMSRule.propagateCounter;
	}

	public void setDiag(HashSet<Diagnosis> diag) {
		this.diag = diag;
	}

	public void setDiagnoses(HashSet<HashSet<String>> set) {
		this.diagnoses = set;

	}

	public void setError(String error) {
		this.error = error;

	}

	@Override
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		// TODO Auto-generated method stub
		this.timeout = timeout;
		// Compilation
		LogicParser parserNewModel = new LogicParser();
		parserNewModel.parseFile(model_file.getAbsolutePath());
		LSentence modelLSentence = (LSentence) parserNewModel.result();

		String modelAsString = modelLSentence.toString();
		Iterator<String> i = observations.iterator();
		modelAsString = modelAsString + "\n";
		while (i.hasNext()) {
			modelAsString = modelAsString + i.next().toString();
			if (i.hasNext()) {
				modelAsString = modelAsString + ",";
			}
		}
		modelAsString = modelAsString + "->explain.";

		if (negatedObservations != null && negatedObservations.size() > 0) {
			Iterator<String> i2 = negatedObservations.iterator();
			modelAsString = modelAsString + "\n";
			while (i2.hasNext()) {
				modelAsString = modelAsString + i2.next().toString();
				if (i2.hasNext()) {
					modelAsString = modelAsString + ",";
				}
			}
			modelAsString = modelAsString + "->false.";
		}

		parserNewModel.parse(modelAsString);
		modelLSentence = (LSentence) parserNewModel.result();
		performDiagnosis(modelLSentence);
	}

	@Override
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {

		this.timeout = timeout;
		Iterator<String> i = observations.iterator();
		model = model + "\n";
		while (i.hasNext()) {
			model = model + i.next();
			if (i.hasNext()) {
				model = model + ",";
			}
		}
		model = model + "->explain.";
		if (negatedObservations != null && negatedObservations.size() > 0) {
			Iterator<String> i2 = negatedObservations.iterator();
			model = model + "\n";
			while (i2.hasNext()) {
				model = model + i2.next().toString();
				model = model + "->false.\n";
			}

		}

		// Start compilation here
		LogicParser parser = new LogicParser();
		if (!parser.parse(model)) {
			String error = "Error occured while parsing the text file for diagnosis: ";
			error = error + parser.errorMessage();
			setError(error);
		}
		LSentence result = (LSentence) parser.result();
		performDiagnosis(result);

	}

	public void startComputation(String model, long timeout) {
		this.timeout = timeout;
		// Start compilation here
		LogicParser parser = new LogicParser();
		if (!parser.parseFile(model)) {
			String error = "Error occured while parsing the text file for diagnosis: ";
			error = error + parser.errorMessage();
			setError(error);
		}
		LSentence result = (LSentence) parser.result();
		performDiagnosis(result);

	}

}
