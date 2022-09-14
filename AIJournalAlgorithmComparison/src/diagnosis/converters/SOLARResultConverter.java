package diagnosis.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashSet;

import diagnosis.data_structures.Diagnosis;

public class SOLARResultConverter implements ResultConverter {

	private HashSet<HashSet<String>> result = new HashSet<HashSet<String>>();
	public Double time = -1.0;

	public boolean evaluation = false;

	/*
	 * 0: #inferences 1: #skips 2: #extension 3: #skip factoring 4: #reductions
	 * 5: #skip-minimization 6: #lfc hits 7: #fails 8:#merge 9: #clauses
	 */
	public String[] executation_info = new String[10];

	private void processFile(BufferedReader br) throws IOException {
		String currentLine;
		Boolean carc_clauses = false;
		while ((currentLine = br.readLine()) != null) {

			currentLine = currentLine.trim();
			if (currentLine.contains("CHARACTERISTIC CLAUSES")) {
				carc_clauses = true;
			} else if (carc_clauses && currentLine.contains("[-")) {

				processLine(currentLine);

			} else if (currentLine.contains("CPU time")) {
				currentLine = currentLine.replace("CPU time     : ", "").replace("s", "").replace(",", ".");
				this.time = Double.parseDouble(currentLine);
			} else if (currentLine.contains("inferences   :")) {
				currentLine = currentLine.replace("inferences   : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[0] = currentLine;
			} else if (currentLine.contains("skips        :")) {
				currentLine = currentLine.replace("skips        : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[1] = currentLine;
			} else if (currentLine.contains("extensions   :")) {
				currentLine = currentLine.replace("extensions   : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[2] = currentLine;
			} else if (currentLine.contains("factorings   :")) {
				currentLine = currentLine.replace("factorings   : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[3] = currentLine;
			} else if (currentLine.contains("reductions   :")) {
				currentLine = currentLine.replace("reductions   : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[4] = currentLine;
			} else if (currentLine.contains("skip-min     :")) {
				currentLine = currentLine.replace("skip-min     : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[5] = currentLine;
			} else if (currentLine.contains("lfc hits     :")) {
				currentLine = currentLine.replace("lfc hits     : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[6] = currentLine;
			} else if (currentLine.contains("fails        :")) {
				currentLine = currentLine.replace("fails        : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[7] = currentLine;
			} else if (currentLine.contains("merge        :")) {
				currentLine = currentLine.replace("merge        : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[8] = currentLine;
			} else if (currentLine.contains("clauses      :")) {
				currentLine = currentLine.replace("clauses      : ", "").replaceAll("\\(.*?\\)", "").replaceAll("\\s+",
						"");
				this.executation_info[9] = currentLine;
			}
		}
		if (this.evaluation) {
			HashSet<HashSet<String>> tmp = new HashSet<HashSet<String>>();
			for (HashSet<String> set : this.result) {
				HashSet<String> tmp_set = new HashSet<String>();
				for (String s : set) {
					if (!s.startsWith("e")) {
						tmp_set.add(s);
					}
				}
				tmp.add(tmp_set);
			}
			this.result = retrieveMinimalDiagnoses(tmp);

		}

	}

	private void processLine(String currentLine) {

		currentLine = currentLine.replace("[", "");
		currentLine = currentLine.replace("],", "");
		currentLine = currentLine.replace("]", "");

		String tokens[] = currentLine.split("-");
		HashSet<String> resultSet = new HashSet<>();
		for (String string : tokens) {
			string = string.trim();
			// System.out.println(string.length() + "** " +
			// string.lastIndexOf(','));
			if (string.length() - 1 == string.lastIndexOf(',') && string.lastIndexOf(',') > 0) {
				string = string.substring(0, string.lastIndexOf(','));
				// System.out.println(string);
			}
			if (!string.isEmpty()) {
				resultSet.add(string);
			}
		}
		this.result.add(resultSet);
	}

	public void readFile(String filename) {
		File inputFile = new File(filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			processFile(br);
		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null) {
					br.close();
				}
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}

	private HashSet<HashSet<String>> retrieveMinimalDiagnoses(HashSet<HashSet<String>> diagnoses) {
		HashSet<HashSet<String>> minDiag = new HashSet<HashSet<String>>(diagnoses);
		for (HashSet<String> conflict1 : diagnoses) {
			for (HashSet<String> conflict2 : diagnoses) {

				if (!conflict1.equals(conflict2)) {
					if (subset(conflict1, conflict2)) {
						minDiag.remove(conflict2);
					} else if (subset(conflict2, conflict1)) {
						minDiag.remove(conflict1);
					}
				}
			}
		}
		HashSet<HashSet<String>> minConflictSet = new HashSet<>(minDiag);
		return minConflictSet;
	}

	@Override
	public HashSet<Diagnosis> returnDiagnoses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashSet<HashSet<String>> returnResultAsStrings() {
		// TODO Auto-generated method stub
		return this.result;
	}

	private boolean subset(HashSet<String> s1, HashSet<String> s2) {
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
