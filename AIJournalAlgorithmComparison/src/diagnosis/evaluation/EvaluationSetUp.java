package diagnosis.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.Random;
import java.util.Set;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;
import diagnosis.evaluation.examplegenerators.TestCaseParser;

public class EvaluationSetUp {
	private static String TCfolder;
	private String dir;
	public int hypSize;
	public int effectSize;
	public int rulesSize;
	public ArrayList<HashSet<String>> observationsTC;
	private Set<String> effects = new HashSet<String>();
	private Set<String> hypotheses = new HashSet<String>();
	public long timeout_graph_metrics = 1200000;
	double metric_time_obs = -1;
	double metric_time_all = -1;
	HashMap<HashSet<String>, HashSet<String>> headToAntecedentMap;
	public HashSet<String> observationSet = new HashSet<>();

	/**
	 * Builds a diagnosis input for a given logical model and a set of
	 * observations
	 *
	 * @param model
	 * @param observations
	 * @return
	 */
	public String buildDiagnosisInput(String model, HashSet<String> observations) {
		if (observations != null) {
			this.observationSet = observations;
		} else {
			this.observationSet = new HashSet<>();
		}

		String explain;
		LogicParser parser = new LogicParser();
		if (!parser.parse(model)) {
			System.out.println("Parser error: " + model);
			return null;
		}
		LSentence lsentence = (LSentence) parser.result();
		this.rulesSize = lsentence.rules.size();
		if (lsentence != null) {
			this.headToAntecedentMap = retrieveHeadToAntecedentMap(lsentence);
			retrieveAntecedents(lsentence);
			this.hypSize = this.hypotheses.size();
			this.effectSize = this.effects.size();
			explain = createExplain(observations.size(), observations);
			model = model + "\n " + explain;
		}
		return model.toString();
	}

	/**
	 * Build a diagnosis input for a file with a particualr number of
	 * obervations.
	 *
	 * @param filename
	 * @param obsSize
	 * @param observations
	 * @return
	 */
	public String buildDiagnosisInput(String filename, int obsSize, HashSet<String> observations) {
		this.observationSet = new HashSet<>();
		LSentence lsentence;
		StringBuilder model = new StringBuilder();
		String explain;
		lsentence = parseFile(filename);
		this.rulesSize = lsentence.rules.size();
		if (lsentence != null) {
			this.headToAntecedentMap = retrieveHeadToAntecedentMap(lsentence);
			retrieveAntecedents(lsentence);
			this.hypSize = this.hypotheses.size();
			this.effectSize = this.effects.size();
			model.append(lsentence.toString());
			model.append("\n");
			if (observations != null) {
				this.observationSet = observations;
			}

			explain = createExplain(obsSize, observations);
			model.append(explain);
		}

		return model.toString();
	}

	/**
	 * Build a diagnosis input for a particular test case
	 *
	 * @param file
	 * @param observations
	 * @return
	 */
	public String buildDiagnosisInputTC(String file, HashSet<String> observations) {
		LSentence lsentence;
		StringBuilder model = new StringBuilder();
		lsentence = parseFile(file);
		this.rulesSize = lsentence.rules.size();
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
			explain.append("->explain.");
			model.append(explain.toString());
		}
		return model.toString();
	}

	/**
	 * Generates the rule o_1,o_2,...,o_n->explain. Either from the set of
	 * observations or choses random observations based on the number of
	 * observations.
	 *
	 * @param obsSize
	 * @param observations
	 * @return
	 */
	private String createExplain(int obsSize, HashSet<String> observations) {
		StringBuilder explainBuilder = new StringBuilder();
		if (observations != null) {
			Iterator<String> iterator = observations.iterator();
			while (iterator.hasNext()) {
				String observation = iterator.next();
				explainBuilder.append(observation);
				if (iterator.hasNext()) {
					explainBuilder.append(",");
				}
			}

			this.observationSet = observations;

		} else {
			ArrayList<String> effects2 = new ArrayList<>();
			effects2.addAll(this.effects);
			long seed = System.nanoTime();
			Collections.shuffle(effects2, new Random(seed));
			Iterator<String> i = effects2.iterator();
			int counter = 0;
			while (i.hasNext() && counter < effects2.size()) {
				String obs = effects2.get(counter++);
				explainBuilder.append(obs);
				this.observationSet.add(obs);
				if (i.hasNext()) {
					explainBuilder.append(",");
				}
			}

		}
		explainBuilder.append("-> explain.\n");

		return explainBuilder.toString();
	}

	/**
	 * Parses a file and in case it is successful returns the LSentence
	 *
	 * @param file
	 * @return
	 */
	private LSentence parseFile(String file) {
		LogicParser parser = new LogicParser();
		System.currentTimeMillis();
		if (!parser.parseFile(file)) {
			System.out.println("error " + file);
			return null;
		}
		return (LSentence) parser.result();
	}

	/**
	 * Parses a testcase.
	 *
	 * @param test
	 * @param TC_dir
	 */
	void parseTestCase(int test, String TC_dir) {
		if (TC_dir != null) {
			EvaluationSetUp.TCfolder = TC_dir;
		}
		this.observationsTC = new ArrayList<HashSet<String>>();
		this.observationsTC = TestCaseParser.parseTestCase(test, EvaluationSetUp.TCfolder);

	}

	public void printModelToFile(LSentence resultNew, String filename, int obsSize, int test, int tests) {
		File resultFileTxt = new File(this.dir + "resultstxt" + filename + obsSize + "-" + tests + "_" + test + ".txt");
		try {
			if (!resultFileTxt.exists()) {
				resultFileTxt.createNewFile();
			}
			BufferedWriter outputStream2 = new BufferedWriter(new FileWriter(resultFileTxt));
			outputStream2.write(resultNew.toString());
			outputStream2.close();
		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	/**
	 * Return all antecendents of the given LSentence.
	 *
	 * @param result
	 * @return
	 */
	private HashSet<String> retrieveAntecedents(LSentence result) {
		HashSet<String> antecedents = new HashSet<String>();
		LinkedList<LRule> rules = result.rules;
		for (int index = 0; index < rules.size(); index++) {
			LinkedList<LObject> list = rules.get(index).tail;
			Iterator<LObject> iterator = list.iterator();
			while (iterator.hasNext()) {
				String tailElement = iterator.next().toString();
				if (Character.isUpperCase(tailElement.charAt(0))) {
					antecedents.add(tailElement);
				}
			}
		}
		return antecedents;
	}

	/**
	 * Returns a map containing for each rule antecedence its consequenes.
	 *
	 * @param result
	 * @return
	 */
	private HashMap<HashSet<String>, HashSet<String>> retrieveHeadToAntecedentMap(LSentence result) {
		HashMap<HashSet<String>, HashSet<String>> map = new HashMap<HashSet<String>, HashSet<String>>();
		LinkedList<LRule> rules = result.rules;
		for (int index = 0; index < rules.size(); index++) {

			LinkedList<LObject> head = rules.get(index).head;
			Iterator<LObject> i = head.iterator();
			HashSet<String> heads = new HashSet<>();
			while (i.hasNext()) {
				String headElement = i.next().toString();
				heads.add(headElement);
				if (Character.isUpperCase(headElement.charAt(0))) {
					this.hypotheses.add(headElement);
				} else {
					this.effects.add(headElement);
				}
			}
			if (!map.containsKey(heads)) {
				HashSet<String> tail = new HashSet<String>();
				map.put(heads, tail);
			}

			LinkedList<LObject> list = rules.get(index).tail;
			Iterator<LObject> iterator = list.iterator();
			while (iterator.hasNext()) {
				String tailElement = iterator.next().toString();
				if (Character.isUpperCase(tailElement.charAt(0))) {
					this.hypotheses.add(tailElement);
				} else {
					this.effects.add(tailElement);
				}
				map.get(heads).add(tailElement);
			}

		}
		return map;
	}

}
