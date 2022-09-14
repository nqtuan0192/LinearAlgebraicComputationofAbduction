package diagnosis.evaluation;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.PrintWriter;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import diagnosis.algorithms.hittingsetalg.MinHittingSets.Minimizer;
import diagnosis.engines.ASPEngine;
import diagnosis.engines.ATMSEngine;
import diagnosis.engines.ConsquenceFindingEngine;
import diagnosis.engines.ConsquenceFindingEngine.ConsequenceFindingAlgorithm;
import diagnosis.engines.ConsquenceFindingEngine.SearchStrategy;
import diagnosis.engines.EXPLorerEngine;
import diagnosis.engines.EXPLorerEngine.MUSExtractionAlgorithm;
import diagnosis.engines.EXPLorerEngine.SEED;
import diagnosis.engines.ReiterConflictEngine;
import support.Printer;

public class Evaluation {

	private static String model_dir;
	private static String artificial_dir;
	private static String exec_dir_solar = "lib/SOLAR/";

	private static File resultFile;

	public static long timeout = 600000;

	public static HashSet<String> observationSet = new HashSet<>();
	public static long deltaTime;
	public static int hypSize;
	public static int effectSize;
	public static int rulesSize;
	public static ArrayList<HashSet<String>> observationsTC;

	public static boolean NONHORN = false;

	/**
	 * Diagnosis Algorithms
	 */
	public static boolean ATMS = false;
	public static boolean REITER = false;
	public static boolean ConsequenceFinding = false;
	public static boolean EXPLORE = false;
	public static boolean ASPClingo = false;

	private static StringBuilder stats = new StringBuilder();
	private static EvaluationSetUp setup;


	/**
	 * Set containing the wrong diagnoses in case of an error
	 */
	private static HashSet<HashSet<String>> diagnoses_not_found;
	private static HashSet<HashSet<String>> diagnoses_too_much;

	/**
	 * Problem id
	 */
	private static int pid = 1;

	private static double min_delta_time = Integer.MAX_VALUE;
	private static String min_delat_time_stats = "";

	/**
	 * Checks whether the diagnoses Set<Set<String>> of two different algorithms
	 * are equal, i.e. contain the same elements;
	 * 
	 * @param resultATMS
	 *            Reference diagnoses to compare the new solution to.
	 * @param resultOther
	 *            Diagnoses to check.
	 * @return true if the diagnoses are the same, false otherwise.
	 */
	private static boolean checkDiagnoses(HashSet<HashSet<String>> resultATMS, HashSet<HashSet<String>> resultOther) {
		diagnoses_not_found = new HashSet<>();
		diagnoses_too_much = new HashSet<>();
		if (resultATMS != null && resultOther != null) {
			for (Set<String> innerSet1 : resultOther) {
				if (!checkDiagnosis(innerSet1, resultATMS)) {
					diagnoses_too_much.add(new HashSet<String>(innerSet1));
				}
			}
			for (Set<String> innerSet1 : resultATMS) {
				if (!checkDiagnosis(innerSet1, resultOther)) {
					diagnoses_not_found.add(new HashSet<String>(innerSet1));
				}
			}
			if (diagnoses_too_much.size() > 0 || diagnoses_not_found.size() > 0) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Checks whether each set within the test diagnoses is contained within the
	 * reference diagnoes.
	 * 
	 * @param innerSet1
	 *            Reference set.
	 * @param resultOther
	 *            Test set of sets.
	 * @return true if innerSet1 is in resultOther, false otherwise.
	 */
	private static boolean checkDiagnosis(Set<String> innerSet1, HashSet<HashSet<String>> resultOther) {
		for (Set<String> innerSet2 : resultOther) {
			if (equalElements(innerSet1, innerSet2)) {
				return true;
			}
		}
		return false;
	}

	/**
	 * Creates the stats of the current diagnosis problem from the setup.
	 * 
	 * @param filename
	 *            Name of the file containing the current diagnosis problem.
	 * @param computePathsInHypergraph
	 *            true if the average maximum path in the hypergraph is to be
	 *            computed.
	 * @param computeDiagProblems
	 *            Number of independent diagnosis subproblems.
	 */
	private static void constructStats(String filename, boolean computePathsInHypergraph, boolean computeDiagProblems) {
		stats.append(pid++).append(",");
		stats.append(filename).append(",");
		stats.append(setup.hypSize).append(",");
		stats.append(setup.effectSize).append(",");
		stats.append(setup.rulesSize).append(",");
		stats.append(setup.observationSet.size()).append(",");
	}

	/**
	 * Checks whether the sets are equal, i.e. have the same elements.
	 * 
	 * @param innerSet1
	 *            First set.
	 * @param innerSet2
	 *            Second set.
	 * @return true if they are equal, false otherwise.
	 */
	private static boolean equalElements(Set<String> innerSet1, Set<String> innerSet2) {
		if (innerSet1.size() != innerSet2.size()) {
			return false;
		}
		HashSet<String> clone = new HashSet<String>(innerSet2);
		Iterator<String> it = innerSet1.iterator();
		while (it.hasNext()) {
			String A = it.next();
			if (clone.contains(A)) {
				clone.remove(A);
			} else {
				return false;
			}
		}
		return true;
	}

	public static void executeArtificialExampleAlgorithms(String filename, String model, HashSet<String> observations) {
		HashSet<HashSet<String>> resultATMS = null;
		DateFormat dateFormat = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss");
		Date date = new Date();
		if (ATMS) {
			System.out.println("");
			System.out.println(filename + " ATMS: Run_start: " + dateFormat.format(date));
			ATMSEngine atms = new ATMSEngine();
			Runtime runtime = Runtime.getRuntime();
			atms.startComputation(model, observations, null, timeout);
			StringBuilder statsATMS = new StringBuilder(stats);
			statsATMS.insert(0, "ATMS,");
			if (!atms.isTimeout()) {
				resultATMS = atms.getDiagnoses();
				System.out.print("Delta: ");
				Printer.printCollection(resultATMS);
				statsATMS.append(atms.getStats());
				runtime.gc();
				statsATMS.replace(statsATMS.length() - 1, statsATMS.length(), ",")
						.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");

				if (atms.getDeltaTime() < min_delta_time) {
					min_delta_time = atms.getDeltaTime();
					min_delat_time_stats = statsATMS.toString();
				}
			} else {
				statsATMS.append("Timeout\n");
				System.out.println("timeout ATMS");
				resultATMS = null;
			}
			if (resultATMS == null) {
			} else {
				if (resultATMS.size() < 1) {
				}
				if (resultATMS.isEmpty()) {
					System.out.println(filename + " ATMS: EMPTY: " + dateFormat.format(date));
				}
			}

			printToFile(resultFile, statsATMS.toString(), true);
			date = new Date();
			System.out.println(filename + " ATMS: Run_ende: " + dateFormat.format(date));

		}

		if (!(resultATMS != null && resultATMS.isEmpty())) {
			if (EXPLORE) {
				date = new Date();
				System.out.println("");
				System.out.println(filename + " XPLORER: Run_start: " + dateFormat.format(date));
				EXPLorerEngine explorer = new EXPLorerEngine();
				explorer.seed = SEED.MAX_MODEL;
				explorer.algo = MUSExtractionAlgorithm.LTUR_like_context;
				Runtime runtime = Runtime.getRuntime();
				explorer.startComputation(model, observations, null, timeout);
				StringBuilder statsExplorer = new StringBuilder(stats);
				statsExplorer.insert(0, "XPLORER,");
				HashSet<HashSet<String>> result_ex = null;
				if (!explorer.isTimeout()) {
					result_ex = explorer.getDiagnoses();
					System.out.print("Delta: ");
					Printer.printCollection(result_ex);
					statsExplorer.append(explorer.getStats());
					runtime.gc();
					statsExplorer.replace(statsExplorer.length() - 1, statsExplorer.length(), ",")
							.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");
					if (explorer.getDeltaTime() < min_delta_time) {
						min_delta_time = explorer.getDeltaTime();
						min_delat_time_stats = statsExplorer.toString();
					}
				} else {
					if (result_ex != null) {
						result_ex = explorer.getDiagnoses();
						statsExplorer.append("Timeout," + result_ex.size() + "\n");
					} else {
						statsExplorer.append("Timeout\n");
					}
					System.out.println("timeout XPLORER");

				}

				printToFile(resultFile, statsExplorer.toString(), true);
				date = new Date();
				System.out.println(filename + " XPLORER: Run_ende: " + dateFormat.format(date));
				if (resultATMS != null) {
					if (result_ex != null) {
						if (!checkDiagnoses(resultATMS, result_ex)) {
							handleError(resultATMS, result_ex, "XPLORER_LTUR", String.valueOf(pid));
						}
					}

				}
				// max model
				date = new Date();
				System.out.println("");
				System.out.println(filename + " XPLORER_QX: Run_start: " + dateFormat.format(date));
				explorer = new EXPLorerEngine();
				explorer.seed = SEED.MAX_MODEL;
				explorer.algo = MUSExtractionAlgorithm.QUICKXPLAIN;
				runtime = Runtime.getRuntime();
				explorer.startComputation(model, observations, null, timeout);
				statsExplorer = new StringBuilder(stats);
				statsExplorer.insert(0, "XPLORER_QX,");
				result_ex = null;
				if (!explorer.isTimeout()) {
					result_ex = explorer.getDiagnoses();
					System.out.print("Delta: ");
					Printer.printCollection(result_ex);
					statsExplorer.append(explorer.getStats());
					runtime.gc();
					statsExplorer.replace(statsExplorer.length() - 1, statsExplorer.length(), ",")
							.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");
					if (explorer.getDeltaTime() < min_delta_time) {
						min_delta_time = explorer.getDeltaTime();
						min_delat_time_stats = statsExplorer.toString();
					}
				} else {
					statsExplorer.append("Timeout\n");
					System.out.println("timeout XPLORER_QX");
				}
				printToFile(resultFile, statsExplorer.toString(), true);
				date = new Date();
				System.out.println(filename + " XPLORER_QX: Run_ende: " + dateFormat.format(date));
				if (resultATMS != null) {
					if (result_ex != null) {
						if (!checkDiagnoses(resultATMS, result_ex)) {
							handleError(resultATMS, result_ex, "XPLORER_QX", String.valueOf(pid));
						}
					}

				}
			}

			if (ASPClingo) {
				File f = new File("lib/clingo/clingo");
				File f2 = new File("lib/clingo/abduction.dl");
				if(f.exists() && !f.isDirectory()&&f2.exists() && !f2.isDirectory()) { 
					date = new Date();
					System.out.println("");
					System.out.println(filename + " ASP: Run_start: " + dateFormat.format(date));
					Runtime runtime = Runtime.getRuntime();
					ASPEngine asp = new ASPEngine();
					asp.startComputation(model, observations, null, timeout);
					HashSet<HashSet<String>> result_asp = null;
					StringBuilder statsAsp = new StringBuilder(stats);
					statsAsp.insert(0, " ASP" + ",");
					if (!asp.isTimeout()) {
						result_asp = asp.getDiagnoses();
						System.out.print("Delta: ");
						Printer.printCollection(result_asp);
						statsAsp.append(asp.getStats());
						runtime.gc();
						statsAsp.replace(statsAsp.length() - 1, statsAsp.length(), ",")
								.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");

						if (asp.getDeltaTime() < min_delta_time) {
							min_delta_time = asp.getDeltaTime();
							min_delat_time_stats = statsAsp.toString();
						}
					} else {
						statsAsp.append("Timeout\n");
						System.out.println("timeout ASP");
					}

					printToFile(resultFile, statsAsp.toString(), true);

					date = new Date();
					System.out.println(filename + " ASP: Run_ende: " + dateFormat.format(date));
					if (resultATMS != null) {
						if (result_asp != null) {
							if (!checkDiagnoses(resultATMS, result_asp)) {
								handleError(resultATMS, result_asp, "ASP", String.valueOf(pid));
							}
						}

					}
				}
				else{
					System.out.println(filename + " ASP:  Missing file \"clingo\" or \"abduction.dl\" in folder \"lib/clingo/\"" + dateFormat.format(date));
				}
		
			}

			if (REITER) {
				System.out.println("");
				date = new Date();
				System.out.println(filename + " HS-DAG: Run_start: " + dateFormat.format(date));
				Runtime runtime = Runtime.getRuntime();
				ReiterConflictEngine reiter = new ReiterConflictEngine();
				reiter.minimizeInBetween = false;
				reiter.startComputation(model, observations, null, timeout);

				HashSet<HashSet<String>> result_reiter = null;
				StringBuilder statsBip = new StringBuilder(stats);
				statsBip.insert(0, "HS-DAG" + ",");

				if (!reiter.isTimeout()) {
					result_reiter = reiter.getDiagnoses();
					System.out.print("Delta: ");
					Printer.printCollection(result_reiter);
					statsBip.append(reiter.getStats());
					runtime.gc();
					statsBip.replace(statsBip.length() - 1, statsBip.length(), ",")
							.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");

					if (reiter.getDeltaTime() < min_delta_time) {
						min_delta_time = reiter.getDeltaTime();
						min_delat_time_stats = statsBip.toString();
					}
				} else {
					statsBip.append("Timeout\n");
					System.out.println("timeout HS-DAG");
				}

				printToFile(resultFile, statsBip.toString(), true);
				date = new Date();
				System.out.println(filename + " HS-DAG: Run_ende: " + dateFormat.format(date));

				if (resultATMS != null) {
					if (result_reiter != null) {
						if (!checkDiagnoses(resultATMS, result_reiter)) {
							handleError(resultATMS, result_reiter, "HS-DAG", String.valueOf(pid));
						}
					}
				}

				date = new Date();
				System.out.println("");
				System.out.println(filename + " HS-DAG_QX: Run_start: " + dateFormat.format(date));

				runtime = Runtime.getRuntime();
				reiter = new ReiterConflictEngine();
				reiter.minimizeInBetween = true;
				reiter.minimizer = Minimizer.QUICKXPLAIN;
				reiter.startComputation(model, observations, null, timeout);

				result_reiter = null;
				statsBip = new StringBuilder(stats);
				statsBip.insert(0, "HS-DAG_QX" + ",");

				if (!reiter.isTimeout()) {
					result_reiter = reiter.getDiagnoses();
					System.out.print("Delta: ");
					Printer.printCollection(result_reiter);
					statsBip.append(reiter.getStats());
					runtime.gc();
					statsBip.replace(statsBip.length() - 1, statsBip.length(), ",")
							.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");

					if (reiter.getDeltaTime() < min_delta_time) {
						min_delta_time = reiter.getDeltaTime();
						min_delat_time_stats = statsBip.toString();
					}
				} else {
					statsBip.append("Timeout\n");
					System.out.println("timeout HS-DAG_QX");
				}

				printToFile(resultFile, statsBip.toString(), true);
				date = new Date();
				System.out.println(filename + " HS-DAG_QX: Run_ende: " + dateFormat.format(date));

				if (resultATMS != null) {
					if (result_reiter != null) {
						if (!checkDiagnoses(resultATMS, result_reiter)) {
							handleError(resultATMS, result_reiter, "HS-DAG_QX", String.valueOf(pid));
						}
					}
				}

			}
			if (ConsequenceFinding) {
				File f = new File(exec_dir_solar + "solar2-build315.jar");
				if(f.exists() && !f.isDirectory()) { 
					date = new Date();
					System.out.println("");
					System.out.println(filename + " CF: Run_start: " + dateFormat.format(date));
					ConsquenceFindingEngine cf = new ConsquenceFindingEngine(exec_dir_solar,
							ConsequenceFindingAlgorithm.SOLAR, SearchStrategy.DFID);
					cf.setInputFile(filename);
					Runtime runtime = Runtime.getRuntime();
					cf.startComputation(model, observations, null, timeout);
					HashSet<HashSet<String>> result_cf = null;
					StringBuilder statsBip = new StringBuilder(stats);
					statsBip.insert(0, "CF,");
					if (!cf.isTimeout()) {
						result_cf = cf.getDiagnoses();
						runtime.gc();
						System.out.print("Delta: ");
						Printer.printCollection(result_cf);
						statsBip.append(cf.getStats());
						statsBip.replace(statsBip.length() - 1, statsBip.length(), ",")
								.append(runtime.totalMemory() - runtime.freeMemory()).append("\n");

						if (cf.getDeltaTime() < min_delta_time) {
							min_delta_time = cf.getDeltaTime();
							min_delat_time_stats = statsBip.toString();
						}
					} else {
						statsBip.append("Timeout\n");
						System.out.println("timeout CF");
					}

					printToFile(resultFile, statsBip.toString(), true);
					date = new Date();
					System.out.println(filename + " CF: Run_ende: " + dateFormat.format(date));

					if (resultATMS != null) {
						if (result_cf != null) {
							if (!checkDiagnoses(resultATMS, result_cf)) {
								handleError(resultATMS, result_cf, "CF", String.valueOf(pid));
							}
						}
					}
				}
				else{
					System.out.println(filename + " CF: Missing \"solar2-build315.jar\" folder \"lib/SOLAR/\": " + dateFormat.format(date));
				}


			}

		}
	}

	private static void handleError(HashSet<HashSet<String>> result_ref, HashSet<HashSet<String>> result_test,
			String algo, String model_nr) {
		System.out.println("CHECK NOT SOUND");
		for (HashSet<String> set : result_ref) {
			for (String s : set) {
				System.out.print(s + ",");
			}
			System.out.println();
		}
		System.out.println(".........................." + algo + ".............................");
		for (HashSet<String> set : result_test) {
			for (String s : set) {
				System.out.print(s + ",");
			}
			System.out.println();
		}
		storeError(model_nr, algo, setup.observationSet, result_ref, result_test);

	}

	/**
	 * Performs artificial examples.
	 * 
	 * @throws IOException
	 */
	public static void performArtificialExamplesMeta(String ai_dir, String output_dir, String result_name)
			throws IOException {
		if (ai_dir != null) {
			model_dir = ai_dir;
		} else {
			model_dir = artificial_dir;
		}

		resultFile = new File(output_dir + result_name);
		File resultFile_winner = new File(output_dir + result_name.replace(".csv", "_winner.csv"));
		EvaluationSetUpArtificialEx e = new EvaluationSetUpArtificialEx();
		File dir = new File(model_dir);
		File[] directoryListing = dir.listFiles();
		if (directoryListing != null) {
			for (File child : directoryListing) {
				String filename = child.getName();
				if (filename.contains(".atms")) {

					e.processAritificalExample(filename, model_dir);
					System.out.println(filename + "," + pid);
					setup = new EvaluationSetUp();

					setup.buildDiagnosisInput(e.getModel(), e.getObservations());

					for (int i = 1; i < 11; i++) {
						min_delta_time = Integer.MAX_VALUE;
						min_delat_time_stats = "";
						stats = new StringBuilder();
						constructStats(filename, true, false);
						executeArtificialExampleAlgorithms(filename, e.getModel(), e.getObservations());
						printToFile(resultFile_winner, min_delat_time_stats, true);

					}

				}
			}
		}
	}

	

	/**
	 * Prints content to a file
	 * 
	 * @param file
	 * @param content
	 */
	public static void printToFile(File file, String content, boolean append_to_end) {

		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, append_to_end));
			outputStream.write(content);
			outputStream.close();

		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
		}
	}

	/**
	 * Prints content to a file
	 * 
	 * @param directory
	 * @param filename
	 * @param content
	 */
	public static void printToFile(String directory, String filename, String content, boolean append_to_end) {
		File file = new File(directory + filename);
		try {
			if (!file.exists()) {
				file.createNewFile();
			}
			BufferedWriter outputStream = new BufferedWriter(new FileWriter(file, append_to_end));
			outputStream.write(content);
			outputStream.close();

		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
		}
	}

	public static String readFile(File file) {
		String model = "";
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(file));
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				model += currentLine;

			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return model;
	}

	/**
	 * Stores the details of the diagnosis process, i.e. observations, diagnoses
	 * and number of independent diagnosis subproblems.
	 * 
	 * @param algo
	 *            Name of the diagnosis algorithm
	 * @param observations
	 *            Set of observations
	 * @param diagnoses
	 *            Set of diagnoses
	 * @param row_counter
	 *            Problem row in csv result
	 * @param diagProb
	 *            Number of independent diagnosis subproblems
	 */
	public static void storeDetails(String algo, HashSet<String> observations, HashSet<HashSet<String>> diagnoses,
			int row_counter, String diagProb) {

		StringBuilder str = new StringBuilder("OBS:\n");

		for (String obs : observations) {
			str.append(obs + ",");
		}
		str.append("\n");
		str.append("DIAG:");
		for (HashSet<String> diag : diagnoses) {
			for (String element : diag) {
				str.append(element + ",");
			}
			str.append("\n");
		}
		str.append("\n");
		str.append("DIAGPROB: " + diagProb + "\n");
		storeString(resultFile.getParent() + "/diagresult_" + algo + "_" + row_counter + ".txt", str.toString(), false);
	}

	/**
	 * Stores the error details, i.e. test case number, observations, reference
	 * diagnoses and actual diagnoses
	 * 
	 * @param model_nr
	 *            Test case number
	 * @param algo
	 *            Diagnosis algorithm
	 * @param observations
	 *            Set of observations
	 * @param diagnosesATMS
	 *            Reference diagnoses
	 * @param diagnoses
	 *            Actual diagnoses
	 */
	public static void storeError(String model_nr, String algo, HashSet<String> observations,
			HashSet<HashSet<String>> diagnosesATMS, HashSet<HashSet<String>> diagnoses) {
		StringBuilder str = new StringBuilder("FMEA " + model_nr + ":\n");
		str.append("Observations:");
		for (String obs : observations) {
			str.append(obs + ",");
		}
		str.append("\n");
		str.append("DIAG ATMS (" + diagnosesATMS.size() + "):\n");
		for (HashSet<String> diag : diagnosesATMS) {
			for (String element : diag) {
				str.append(element + ",");
			}
			str.append("\n");
		}
		str.append("\n");

		str.append("\n");
		str.append("DIAG " + algo + "(" + diagnoses.size() + "):\n");
		for (HashSet<String> diag : diagnoses) {
			for (String element : diag) {
				str.append(element + ",");
			}
			str.append("\n");
		}
		str.append("\n");

		str.append("DIAG TOO MUCH (" + diagnoses_too_much.size() + "):\n");
		for (HashSet<String> diag : diagnoses_too_much) {
			for (String element : diag) {
				str.append(element + ",");
			}
			str.append("\n");
		}
		str.append("\n");

		str.append("DIAG NOT FOUND (" + diagnoses_not_found.size() + "):\n");
		for (HashSet<String> diag : diagnoses_not_found) {
			for (String element : diag) {
				str.append(element + ",");
			}
			str.append("\n");
		}
		str.append("\n");

		storeString(resultFile.getParent() + "/error_" + algo + "_" + model_nr + "_" + observations.size() + "_"
				+ diagnosesATMS.size() + ".txt", str.toString(), false);

	}

	/**
	 * Stores a string to a file.
	 * 
	 * @param filename
	 *            Name of the file.
	 * @param str
	 *            String to store.
	 * @param append
	 *            Whether to append the str to the file or not.
	 */
	public static void storeString(String filename, String str, boolean append) {
		PrintWriter outputStream = null;
		try {
			outputStream = new PrintWriter(new BufferedWriter(new FileWriter(filename, append)));
			outputStream.print(str);
			outputStream.flush();
		} catch (Exception e) {
			System.out.println("Cannot store in file " + filename);
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
}