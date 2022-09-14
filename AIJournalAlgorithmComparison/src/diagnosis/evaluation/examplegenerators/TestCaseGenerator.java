package diagnosis.evaluation.examplegenerators;

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

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;

public class TestCaseGenerator {
	private static int numberOfTests = 20;
	private static String dir;
	private static BufferedWriter outputStream;
	private static File resultFileTxt;
	public static long deltaTime;
	public static LSentence resultSentence;
	public static HashMap<String, ArrayList<String>> map;
	public static HashMap<String, String> map_effect_old_new = new HashMap<>();
	public static HashMap<String, String> map_hyp_old_new = new HashMap<>();
	public static String outputSentence;

	static String generateObservationSet(String filename, int obsSize) throws IOException {
		ArrayList<String> effects = new ArrayList<String>();
		effects.addAll(map.keySet());
		StringBuilder sb = new StringBuilder();
		Random rand = new Random();
		ArrayList<String> effects2 = (ArrayList<String>) effects.clone();
		for (int counter = 0; (counter < obsSize) && (effects2.size() > 0); counter++) {
			int index = rand.nextInt(((effects2.size() - 1) - 0) + 1) + 0;
			System.out.println(filename + " " + obsSize + " " + counter + " ");
			String obs = effects2.get(index);

			sb.append(obs);
			effects2.remove(index);
			if (counter < obsSize - 1 && effects2.size() > 0) {
				sb.append(";");
			}

		}

		return sb.toString();
	}

	static String generateObservationString(int obsSize) throws IOException {
		StringBuilder sb = new StringBuilder();
		ArrayList<String> effects = new ArrayList<String>();
		effects.addAll(map_effect_old_new.values());
		long seed = System.nanoTime();
		Collections.shuffle(effects, new Random(seed));
		Iterator<String> i = effects.iterator();
		int counter = 0;
		while (i.hasNext() && counter < obsSize) {
			String obs = effects.get(counter++);
			sb.append(obs);
			if (i.hasNext() && counter < obsSize) {
				sb.append(",");
			}
		}
		sb.append("->explain.");

		return sb.toString();
	}

	private static void generatePHCAPs() throws IOException {
		String filename;
		String fmea = "FMEA_";
		String out_file = "";
		for (int test = 12; test < 13; test++) {
			filename = fmea + test + ".txt";
			StringBuilder result = new StringBuilder();
			System.out.println(
					"--------------------------------------------------------------------------------------------");
			LogicParser parser = new LogicParser();
			if (parser.parseFile(dir + filename)) {
				resultSentence = (LSentence) parser.result();

				map = retrieveHeadToAntecedentMap(resultSentence);

				for (int obsSize = 1; obsSize < 30; obsSize++) {
					if (obsSize <= map.keySet().size()) {
						result = new StringBuilder();
						result.append(outputSentence);
						result.append(generateObservationString(obsSize));
						out_file = fmea + test + "obs_file" + obsSize + ".atms";
						File resultFileTxt = new File(dir + "PHCAPs/" + out_file);
						if (!resultFileTxt.exists()) {
							resultFileTxt.createNewFile();
						}
						outputStream = new BufferedWriter(new FileWriter(resultFileTxt));
						outputStream.write(result.toString());
						outputStream.close();

					}

				}

			}
		}

	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		try {
			generatePHCAPs();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	public static void printObservationsToFile(String observations, String filename) {
		String text = filename.replace(".txt", "");
		resultFileTxt = new File(dir + "TC_test/" + text + "_TC_" + numberOfTests + ".txt");
		try {
			if (!resultFileTxt.exists()) {
				resultFileTxt.createNewFile();
			}
			outputStream = new BufferedWriter(new FileWriter(resultFileTxt));
			outputStream.write(observations);
			outputStream.close();
		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
			System.exit(-1);
		}
	}

	private static HashMap<String, ArrayList<String>> retrieveHeadToAntecedentMap(LSentence result) {
		HashMap<String, ArrayList<String>> map = new HashMap<String, ArrayList<String>>();
		HashSet<String> hyps = new HashSet<>();
		HashSet<String> effect = new HashSet<>();
		LinkedList<LRule> rules = result.rules;
		for (int index = 0; index < rules.size(); index++) {
			String head = rules.get(index).head.toString();
			System.out.println("HEAD " + head);
			if (!map.containsKey(head)) {
				ArrayList<String> tail = new ArrayList<String>();
				effect.add(head);
				map.put(head, tail);
			}

			LinkedList<LObject> list = rules.get(index).tail;
			Iterator<LObject> iterator = list.iterator();
			while (iterator.hasNext()) {
				String tailElement = iterator.next().toString();
				map.get(head).add(tailElement);
				hyps.add(tailElement);
				System.out.println("TAIL " + tailElement);
			}

		}

		// generate encoding maps
		int counter = 1;
		for (String hyp : hyps) {
			String new_hyp = "H_" + counter;
			map_hyp_old_new.put(hyp, new_hyp);
			counter++;
		}

		counter = 1;
		for (String eff : effect) {
			String new_eff = "e_" + counter;
			map_effect_old_new.put(eff, new_eff);
			counter++;
		}

		// generate new LSentence
		StringBuilder new_model = new StringBuilder();
		for (int index = 0; index < rules.size(); index++) {
			LinkedList<LObject> list = rules.get(index).tail;
			Iterator<LObject> iterator = list.iterator();
			while (iterator.hasNext()) {
				String tailElement = iterator.next().toString();
				new_model.append(map_hyp_old_new.get(tailElement));
			}
			new_model.append("->");

			String head = rules.get(index).head.toString();
			new_model.append(map_effect_old_new.get(head));
			new_model.append(".\n");
		}

		outputSentence = new_model.toString();

		return map;
	}
}
