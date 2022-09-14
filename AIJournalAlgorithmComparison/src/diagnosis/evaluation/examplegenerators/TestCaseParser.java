package diagnosis.evaluation.examplegenerators;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class TestCaseParser {

	public static ArrayList<HashSet<String>> parseTestCase(int test, String directory) {
		String file = "FMEA_" + test + "_TC_10.txt";
		ArrayList<HashSet<String>> observationsTC = new ArrayList<HashSet<String>>();
		File inputFile = new File(file);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(directory + inputFile));
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				processLine(currentLine, observationsTC);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
		return observationsTC;
	}

	static void processLine(String line, ArrayList<HashSet<String>> observationsTC) {
		String[] tokens = line.split(";");
		HashSet<String> obsSet = new HashSet<>();
		for (String token : tokens) {
			obsSet.add(token);
		}
		observationsTC.add(obsSet);
	}
}
