package diagnosis.evaluation.examplegenerators;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Random;

import diagnosis.algorithms.atms.ATMSNode;
import diagnosis.algorithms.atms.ATMSTextInterface;
import diagnosis.algorithms.atms.Converter4ATMS;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;

public class HornArtificialExampleGenerator extends Object {

	public static String dictionary = "./";
	public static String resultFilename = "results";

	/**
	 * Method runExperimentsVariant2 The purpose of this method is to perform
	 * test cases with a larger number of hypotheses that are still feasible.
	 * Currently these experiments can only be carried out up to 100_5_4_8
	 * examples!!
	 */
	public static void createArtificialExamples(int numPerHyp, int maxnHyp, int maxOverlap, int nRules, int maxObs,
			String sDir) {

		for (int nHyp = 10; nHyp <= maxnHyp; nHyp = nHyp + 10) {
			for (int counter = 0; counter <= numPerHyp; counter++) {
				Random rand = new Random();
				int overlap = rand.nextInt((maxOverlap - 1) + 1) + 1;
				rand = new Random();
				int numObs = rand.nextInt((maxObs - 1) + 1) + 1;
				rand = new Random();
				int numRules = rand.nextInt((nRules - 1) + 1) + 1;
				String str = generateHornExample(nHyp, numRules, overlap, numObs);
				if (str != null) {
					String example = "phcap_" + nHyp + "_" + numRules + "_" + overlap + "_" + numObs + ".atms";
					storeString(dictionary + sDir + "/" + example, str, false);
				}

			}
		}
	}

	/**
	 * Method generateExample
	 * 
	 * @param n
	 *            .. number of causes
	 * @param r
	 *            .. number of effects per cause
	 * @param o
	 *            .. overlap between effects that are connected with different
	 *            causes
	 * @param obs
	 *            .. number of effects to be explained
	 * @return A propositional horn clause abduction problem
	 */
	public static String generateExample(int n, int r, int o, int obs) {
		// Check input values
		if ((o > r) || (o < 0) || (n < 1) || (r < 0) || (obs < 0)) {
			return null;
		}

		// Input values are within range
		String str = new String();
		int nextEff = 1;
		HashSet<String> effects = new HashSet<String>();
		for (int hyp = 1; hyp <= n; hyp++) {
			String cause = "H_" + hyp;
			for (int eff = nextEff; eff < nextEff + r; eff++) {
				String effect = "e_" + eff;
				effects.add(effect);
				str = str + cause + " -> " + effect + ".\n";
			}
			nextEff = nextEff + r - o;
		}
		// Add rule for observation to be explained
		if (obs > 0) {
			int numbObs = Math.min(obs, effects.size());
			int val = (r + 1) % (effects.size()) + 1;
			HashSet<Integer> hs = new HashSet<Integer>();
			hs.add(1);
			for (int i = numbObs; i > 1; i--) {
				str = str + "e_" + val + ", ";
				val = ((val + r) % (effects.size())) + 1;
				while (hs.contains(val)) {
					val = val + 1;
				}
			}
			str = str + "e_1 -> obs.\n";
		}

		return str;
	}

	/**
	 * Method generateExample
	 * 
	 * @param n
	 *            .. number of causes
	 * @param r
	 *            .. number of effects per cause
	 * @param o
	 *            .. overlap between effects that are connected with different
	 *            causes
	 * @param obs
	 *            .. number of effects to be explained
	 * @return A propositional horn clause abduction problem
	 */
	public static String generateHornExample(int n, int r, int o, int obs) {
		// Check input values
		if ((o > r) || (o < 0) || (n < 1) || (r < 0) || (obs < 0)) {
			return null;
		}

		// Input values are within range
		String str = new String();

		int nextEff = 2;
		HashSet<String> count_eff = new HashSet<>();
		str = str + "H_1 -> e_1.\n";
		count_eff.add("1");

		HashSet<String> effects = new HashSet<String>();
		for (int hyp = 2; hyp <= n; hyp++) {
			String cause = "H_" + hyp;
			Random random = new Random();
			if (random.nextBoolean()) {
				int upper_bound = random.nextInt(5) + 1;
				for (int i = 0; i < upper_bound; i++) {
					cause += ",";
					hyp++;
					cause += "H_" + hyp;
				}

				hyp = hyp - upper_bound;
			}
			if (random.nextBoolean()) {
				int upper_bound = random.nextInt(2) + 1;
				for (int i = 0; i < upper_bound; i++) {
					cause += ",";
					cause += "e_" + nextEff;
					nextEff++;

				}

			}

			for (int eff = nextEff; eff < nextEff + r; eff++) {
				String effect = "e_" + eff;
				effects.add(effect);
				count_eff.add(String.valueOf(eff));
				str = str + cause + " -> " + effect + ".\n";
			}
			nextEff = nextEff + r - o;
		}

		// Add rule for observation to be explained
		if (obs > 0) {
			int numbObs = Math.min(obs, count_eff.size());
			ArrayList<String> count_eff_list = new ArrayList<>(count_eff);
			Random rand = new Random();

			HashSet<String> hs = new HashSet<String>();
			for (int i = numbObs; i > 1; i--) {
				int index = rand.nextInt(effects.size() - 1);
				String val = count_eff_list.get(index);
				if (hs.add(val)) {
					str = str + "e_" + val + ", ";
				} else {
					i++;
				}
			}
			str = str + "e_1 -> obs.\n";
		}

		System.out.println(str);

		return str;
	}

	/**
	 * Method runExperiments First experimental evaluation. The number of
	 * hypotheses ranges from 10 to 100. The number of connected effects to each
	 * hypothesis from 1 to 2 times the number of hypotheses. The overlap
	 * between connected effects is varies from 0 to 10. The latter holds as
	 * well as for the number of effects to be observed.
	 */
	public static void runExperiments() {
		for (int nHyp = 10; nHyp <= 100; nHyp = nHyp + 10) {
			int maxOverlap = 1;
			for (int overlap = 0; overlap <= maxOverlap; overlap++) {
				for (int numObs = 0; numObs <= 10; numObs++) {
					singleExperiment(nHyp, 1, overlap, numObs, "src");
				}
			}

			int maxRules = 2 * nHyp;
			int step = maxRules / 4;
			for (int nRules = step; nRules <= maxRules; nRules = nRules + step) {
				maxOverlap = Math.min(nRules, 10);
				for (int overlap = 0; overlap <= maxOverlap; overlap++) {
					for (int numObs = 0; numObs <= 10; numObs++) {
						singleExperiment(nHyp, nRules, overlap, numObs, "src");
					}
				}
			}
		}
	}

	/**
	 * Method runExperimentsVariant1 Currently these experiments can only be
	 * carried out up to 100_5_4_8 examples!!
	 */
	public static void runExperimentsVariant1() {
		int nHyp = 100;
		int maxRules = 10;
		int step = 5;
		int nRules = 1;
		while (nRules <= maxRules) {
			int maxOverlap = Math.min(nRules, 10);
			for (int overlap = 0; overlap <= maxOverlap; overlap = overlap + 1) {
				for (int numObs = 0; numObs <= 10; numObs = numObs + 2) {
					singleExperiment(nHyp, nRules, overlap, numObs, "srcV1");
				}
			}
			if (nRules == 1) {
				nRules = step;
			} else {
				nRules = nRules + step;
			}
		}
	}

	/**
	 * Method runExperimentsVariant2 The purpose of this method is to perform
	 * test cases with a larger number of hypotheses that are still feasible.
	 * Currently these experiments can only be carried out up to 100_5_4_8
	 * examples!!
	 */
	public static void runExperimentsVariant2() {
		storeString(dictionary + resultFilename + ".cvs",
				"PARAMETER , ATMS NODES , ATMS RULES , COMPILE TIME , CONVERSION TIME"
						+ ", PROVING TIME , NOGOOD LABEL SIZE , OBS NODE LABEL SIZE, Overlap, eh, covering \n",
				false);
		for (int nHyp = 10; nHyp <= 1000; nHyp = nHyp + 10) {
			int maxOverlap = 3;
			for (int overlap = 0; overlap <= maxOverlap; overlap = overlap + 1) {
				singleExperiment(nHyp, 6, overlap, 15, "srcV2");
			}
		}
	}

	public static void singleExperiment(int nHyp, int nRules, int overlap, int numObs, String sDir) {
		String str = generateExample(nHyp, nRules, overlap, numObs);
		String example = "phcap_" + nHyp + "_" + nRules + "_" + overlap + "_" + numObs + ".atms";
		System.out.println("Experiment: " + example);

		if (str == null) {
			System.out.println("Cannot generate example " + example);
		} else {
			String filename = dictionary + sDir + "/" + example;
			storeString(filename, str, false);
			long time = System.currentTimeMillis();
			// Start compilation here
			LogicParser parser = new LogicParser();
			if (!parser.parse(str)) {
				System.out.println("Error occured while parsing file " + filename);
				return;
			}
			LSentence result = (LSentence) parser.result();

			// Compilation finished here
			long compTime = System.currentTimeMillis() - time;

			time = System.currentTimeMillis();
			// Conversion starts here
			LinkedList<LinkedList<String>> list = Converter4ATMS.convert(result);
			// Conversion ends here
			long convTime = System.currentTimeMillis() - time;

			time = System.currentTimeMillis();
			// Start theorem proving here
			ATMSTextInterface atms = ATMSTextInterface.create(list);
			// Theorem proving finished here
			long provingTime = System.currentTimeMillis() - time;

			String resultFile = dictionary + resultFilename + ".cvs";
			ATMSNode obsNode = atms.nodeDict.get("obs");
			String obsNodeLabelSize = "-";
			if (obsNode != null) {
				obsNodeLabelSize = (new Integer(obsNode.label().toSet().size())).toString();
			}

			// FORMAT: PARAMETER + ATMS NODES + ATMS RULES + COMPILE TIME +
			// CONVERSION TIME
			// + PROVING TIME + NOGOOD LABEL SIZE + OBS NODE LABEL SIZE
			String resultString = example + "; " + nHyp + "; " + nRules + "; " + overlap + "; " + numObs + "; "
					+ atms.atms.nodes.size() + "; " + +atms.atms.rules.size() + "; " + compTime + "; " + convTime + "; "
					+ provingTime + "; " + atms.atms.nogoodLabel().toSet().size() + "; " + obsNodeLabelSize + ","
					+ "\n";
			storeString(resultFile, resultString, true);
		}
	}

	public static void storeString(String filename, String str, boolean append) {
		PrintWriter outputStream = null;
		try {
			File file = new File(filename);
			if (!file.exists()) {
				file.createNewFile();
			}

			outputStream = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
			outputStream.print(str);
			outputStream.flush();
		} catch (Exception e) {
			System.out.println("Cannot store in file " + filename);
			e.printStackTrace();
		} finally {
			if (outputStream != null) {
				try {
					outputStream.flush();
					outputStream.close();
				} catch (Exception e) {
					System.out.println("Could not close file " + filename);
				}
			}
		}
	}

	public HornArtificialExampleGenerator() {
	}

}