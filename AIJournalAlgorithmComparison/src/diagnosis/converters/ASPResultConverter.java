package diagnosis.converters;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import diagnosis.data_structures.Diagnosis;

public class ASPResultConverter implements ResultConverter {
	private HashSet<HashSet<String>> result = new HashSet<HashSet<String>>();
	/** The variable_mapping. */
	private HashMap<String, String> sclauseMapping;
	public Double time = -1.0;
	public Double solving_time = -1.0;
	public Double unsat_time = -1.0;

	public ASPResultConverter(HashMap<String, String> sclauseMapping) {
		this.sclauseMapping = sclauseMapping;
	}

	private void processFile(BufferedReader br) throws IOException {
		String currentLine;
		while ((currentLine = br.readLine()) != null) {
			processLine(currentLine);
		}
	}

	private void processLine(String line) {
		if (line.contains("solution")) {
			HashSet<String> solution = new HashSet<>();
			Matcher m = Pattern.compile("\\((.*?)\\)").matcher(line);
			while (m.find()) {
				String sol = m.group(1);
				solution.add(sol);
			}
			processSolution(solution);
		} else if (line.contains("Time") && !line.contains("CPU")) {
			int index_start = line.indexOf(":") + 1;
			int index_end = line.indexOf("s");
			String line1 = line.substring(index_start, index_end).replace(" ", "");
			this.time = Double.valueOf(line1);

			index_start = line.indexOf(":", index_end) + 1;
			index_end = line.indexOf("s ", index_start);
			String line2 = line.substring(index_start, index_end).replace(" ", "");
			this.solving_time = Double.valueOf(line2);

			index_start = line.indexOf("Unsat:", index_end) + 6;
			index_end = line.indexOf("s)", index_start);
			String line3 = line.substring(index_start, index_end).replace(" ", "");
			this.unsat_time = Double.valueOf(line3);
		}
	}

	private void processSolution(HashSet<String> solution) {
		HashSet<String> diagnosis = new HashSet<>();
		for (String sol : solution) {
			diagnosis.add(this.sclauseMapping.get(sol));
		}
		this.result.add(diagnosis);
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
			} catch (Exception ex) {
				ex.printStackTrace();
			}
		}
	}

	@Override
	public HashSet<Diagnosis> returnDiagnoses() {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public HashSet<HashSet<String>> returnResultAsStrings() {
		return this.result;
	}

}
