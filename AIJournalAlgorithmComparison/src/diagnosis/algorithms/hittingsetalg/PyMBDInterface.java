package diagnosis.algorithms.hittingsetalg;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.TreeSet;


public class PyMBDInterface {

	private static String inputFileName = "inputs.txt";
	private static String problemsFileName = "problems.txt";

	private static File resultsFile;
	private static BufferedWriter outputStream;

	//Sorted set of relevant sets
	private TreeSet<HashSet<Integer>> int_relevant_sets;

	private TreeSet<HashSet<String>> relevant_sets;
	//private static String dir = "/Users/roxane/Documents/AMOR/PyMBD_AMOR/benchmarks/experiment03-mhs/";
	private static String dir = "lib/PyMBD_AMOR/benchmarks/experiment03-mhs/";
	//private static String main_dir = "/Users/roxane/Documents/AMOR/PyMBD_AMOR/";
	private static String main_dir = "lib/PyMBD_AMOR/";
	private HashSet<HashSet<Integer>> int_diagnoses = new HashSet<>();

	//private HashSet<HashSet<String>> diagnoses = new HashSet<>();

	private String algorithm_name = "hsdag";
	private double delta_time = -1;
	private int ncs = 0;
	private int ncomp=0;
	private int nmhs= 0;
	private int  mhs = 0;
	private static StringBuilder stats;

	private HashMap<Integer,ArrayList<Double>> hsdagPIDMap = new HashMap<>();
	private HashMap<Integer,ArrayList<Double>> hstPIDMap = new HashMap<>();
	private HashMap<Integer,ArrayList<Double>> boolPIDMap = new HashMap<>();

	//problem id used in PyMBD
	private static int pid = 1;

	//determines which HS algorithms to use
	public boolean hsdag = true;
	public boolean bool = true;
	public boolean hst = true;

	public PyMBDAlgorithms algo =PyMBDAlgorithms.HSDAG;
	public enum PyMBDAlgorithms {
		HSDAG,
		BOOLEAN,
		HSTREE
	}

	/**
	 * Generates an input suitable for an experiment in PyMDB
	 * @param conflict_set
	 * @param directory
	 * @param num_comp
	 * @return
	 */
	public File createExperimentPyMBDInput(Set<Set<String>> conflict_set, String directory, int num_comp){

		//if directory supplied use it for output
		if(directory!=null){
			dir = directory;
		}

		relevant_sets = new TreeSet<HashSet<String>>(new Comparator<HashSet<String>>() {
			@Override
			public int compare(HashSet<String> o1, HashSet<String> o2) {
				if (o1.size() < o2.size()) {
					return -1;
				}
				return 1;
			}
		});

		relevant_sets.addAll((Collection<? extends HashSet<String>>) conflict_set);
		addToExperimentInputProblemsFile(num_comp);
		//createExperimentRunSh();
		return null;
	}

	/**
	 * Generates the input.txt file for PyMBD, i.e. a text file containing
	 * the sets to be used in a single line, sets separated by "|" and elements 
	 * separated by ",". The sets are stored in the file ordered by cardinality.
	 */
	private void addToExperimentInputProblemsFile(int num_comp){
		StringBuilder input = new StringBuilder();		
		Iterator<HashSet<String>> iterator_1 = relevant_sets.iterator();
		while(iterator_1.hasNext()){
			HashSet<String> cs = iterator_1.next();
			Iterator<String> iterator_2 = cs.iterator();
			while(iterator_2.hasNext()){
				String element = iterator_2.next();
				element = element.replace(",", ";");
				input.append(element);
				if(iterator_2.hasNext()){
					input.append(",");
				}
			}
			if(iterator_1.hasNext()){
				input.append("|");
			}
		}
		input.append("\n");
		//System.out.println(input.toString());

		// problems consists of pid, ncomp, ncs, neffc
		StringBuilder problems = new StringBuilder();
		problems.append(pid+"\t");
		problems.append(num_comp);
		problems.append("\t");
		problems.append(relevant_sets.size());
		problems.append("\t");
		problems.append(relevant_sets.size());
		problems.append("\n");

		//check if pid ==1
		// if true then create file new input & problem file
		// else add to file
		if(pid==1){
			printToFile(dir,inputFileName, input.toString(),false);
			printToFile(dir, problemsFileName, problems.toString(),false);
		}
		else{
			printToFile(dir,inputFileName, input.toString(),true);
			printToFile(dir, problemsFileName, problems.toString(),true);
		}

		//increase problem id counter
		pid++;

	}

	private void createExperimentRunSh(){
		StringBuilder run = new StringBuilder();
		run.append("trap 'echo Control-C trap caught; exit 1' 2\n");
		run.append("cd "+main_dir+"benchmarks/experiment03-mhs/ \n");
		if(hsdag){
			run.append("./compute.py   -a hsdag -r run01 -m 0 | tee -a run.log 2>&1 \n");
		}
		if(hst){
			run.append("./compute.py   -a hst -r run01 -m 0 | tee -a run.log 2>&1 \n");
		}
		if(bool){
			run.append("./compute.py   -a bool-it-h5-stop  -r run01 -m 0 | tee -a run.log 2>&1 \n");
		}
		printToFile(main_dir+"benchmarks/experiment03-mhs/","run.sh", run.toString(),false);
	}

	public void readExperimentResult(String ending){

		if(hsdag){
			String filename_result = "results-hsdag"+ ending +".txt";
			//String filename_output = "output-hsdag"+ ending +".txt";
			readExperimentFile(dir+"run01/"+filename_result);
			//readExperimentFile(dir+"run01/"+filename_output);
		}
		if(hst){
			String filename_result = "results-hst"+ ending +".txt";
			String filename_output = "output-hst"+ ending +".txt";
			readExperimentFile(dir+"run01/"+filename_result);
			//readExperimentFile(dir+"run01/"+filename_output);
		}
		if(bool){
			String filename_result = "results-bool-it-h5-stop"+ ending +".txt";
			String filename_output = "output-bool-it-h5-stop"+ ending +".txt";
			readExperimentFile(dir+"run01/"+filename_result);
			//readExperimentFile(dir+"run01/"+filename_output);
		}

	}



	public void readExperimentFile(String filename){
		File inputFile = new File(filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			if(filename.contains("results")){
				processExperimentResultFile(br);
			}
			else{
				if(filename.contains("hsdag")){
					processExperimentOutputFile(br,"hsdag");
				}
				else if(filename.contains("hst")){
					processExperimentOutputFile(br,"hst");
				}
				else if(filename.contains("bool-it-h5-stop")){
					processExperimentOutputFile(br,"bool-it-h5-stop");
				}

			}


		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	private void processExperimentResultFile(BufferedReader br) throws IOException{
		String currentLine;
		int counter = 0;
		while ((currentLine = br.readLine()) != null) {
			if(counter > 0){
				processExperimentResultLine(currentLine);
			}
			counter++;
		}
	}

	private void processExperimentResultLine(String line){
		System.out.println(line);
		String[] tokens = line.split(",");
		pid = Integer.valueOf(tokens[3]).intValue();
		algorithm_name = tokens[4];
		ncs =  Integer.valueOf(tokens[5]).intValue();
		ncomp =  Integer.valueOf(tokens[6]).intValue();
		delta_time =Double.valueOf(tokens[9])*1000;

		mhs = Integer.valueOf(tokens[16]).intValue();


		ArrayList<Double> information = new ArrayList<>();
		information.add(Double.valueOf(delta_time));
		information.add(Double.valueOf(ncs));
		information.add(Double.valueOf(ncomp));
		information.add(Double.valueOf(mhs));

		if(algorithm_name.equals("hsdag"))
			hsdagPIDMap.put(pid, information);
		if(algorithm_name.equals("hst"))
			hstPIDMap.put(pid, information);
		if(algorithm_name.equals("bool-it-h5-stop"))
			boolPIDMap.put(pid, information);

	}

	private void processExperimentOutputFile(BufferedReader br,String algo) throws IOException{
		String currentLine;
		while ((currentLine = br.readLine()) != null) {
			System.out.println(currentLine);
			processExperimentOutputLine(currentLine,algo);
		}
	}

	private void processExperimentOutputLine(String line, String algo){
		String[] tokens = line.split("\t");
		HashSet<HashSet<String>> diagnoses = new HashSet<>();
		int pid = Integer.valueOf(tokens[1]).intValue();

		//No Diagnosis aka timeout
		if(tokens.length<3){
			ArrayList<Double> list= new ArrayList<>();

			if(algo.equals("hsdag")){
				list= hsdagPIDMap.get(pid);

			}
			else if(algo.equals("hst")){
				list= hstPIDMap.get(pid);

			}
			else if(algo.equals("bool-it-h5-stop")){
				list= boolPIDMap.get(pid);
			}
			//delta time should be -1 (since timeout)
			list.add(0, (double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);
			list.add((double) -1);

		}
		else{
			String output = tokens[2];
			String delims = "[|]+"; //split at |
			String[] tokens2 = output.split(delims);
			double singlefault = 0;
			double doublefault = 0;
			double triplefault = 0;
			double rest = 0;
			for(String diagnosis:tokens2){

				String[] elements = diagnosis.split(",");
				HashSet<String> diag = new HashSet<>();
				for(String element:elements){
					String i = String.valueOf(element);
					diag.add(i);		
				}
				if(diag.size()==1){
					singlefault++;
				}
				else if(diag.size()==2){
					doublefault++;
				}
				else if(diag.size()==3){
					triplefault++;
				}
				else{
					rest++;
				}
				diagnoses.add(diag);
			}

			//Add to information
			ArrayList<Double> list= new ArrayList<>();
			if(algo.equals("hsdag")){
				list= hsdagPIDMap.get(pid);
			}
			else if(algo.equals("hst")){
				list= hstPIDMap.get(pid);
			}
			else if(algo.equals("bool-it-h5-stop")){
				list= boolPIDMap.get(pid);
			}
			list.add((double) diagnoses.size());
			list.add(singlefault);
			list.add(doublefault);
			list.add(triplefault);
			list.add(rest);
		}


	}


	public void createStats(){
		stats = new StringBuilder();
		int pid = -1;
		String algorithm_name ="";
		double diagsize=0;
		double singlefault = 0;
		double doublefault = 0;
		double triplefault = 0;
		double rest = 0;
		double delta_time=0;
		double ncs = 0;
		double ncomp = 0;
		double mhs = 0;

		if(hsdag){
			for(Entry<Integer, ArrayList<Double>> entry: hsdagPIDMap.entrySet()){
				pid = entry.getKey();
				algorithm_name = "hsdag";
				ArrayList<Double> list= entry.getValue();
				delta_time = list.get(0);
				ncs = list.get(1);
				ncomp = list.get(2);
				mhs = list.get(3);
				/*diagsize = list.get(4);
				singlefault = list.get(5);
				doublefault = list.get(6);
				triplefault = list.get(7);
				rest = list.get(8);*/

				//stats.append(","+","+","+","+","+","+","+","+","+","+",");
				stats.append(delta_time+","+ mhs +","+ singlefault +","+ doublefault +","+triplefault+","+rest+",");
				stats.append(ncs+","+ncomp+","+mhs+",");
				stats.append(algorithm_name+","+pid+"\n");
			}

		}
		if(hst){
			for(Entry<Integer, ArrayList<Double>> entry: hstPIDMap.entrySet()){
				pid = entry.getKey();
				algorithm_name = "hst";
				ArrayList<Double> list= entry.getValue();
				delta_time = list.get(0);
				ncs = list.get(1);
				ncomp = list.get(2);
				mhs = list.get(3);
				/*diagsize = list.get(4);
				singlefault = list.get(5);
				doublefault = list.get(6);
				triplefault = list.get(7);
				rest = list.get(8);*/

				//stats.append(","+","+","+","+","+","+","+","+","+","+",");
				stats.append(delta_time+","+ diagsize +","+ singlefault +","+ doublefault +","+triplefault+","+rest+",");
				stats.append(ncs+","+ncomp+","+mhs+",");
				stats.append(algorithm_name+","+pid+"\n");
			}

		}
		if(bool){
			for(Entry<Integer, ArrayList<Double>> entry: boolPIDMap.entrySet()){
				pid = entry.getKey();
				algorithm_name = "bool";
				ArrayList<Double> list= entry.getValue();
				delta_time = list.get(0);
				ncs = list.get(1);
				ncomp = list.get(2);
				mhs = list.get(3);
				/*diagsize = list.get(4);
				singlefault = list.get(5);
				doublefault = list.get(6);
				triplefault = list.get(7);
				rest = list.get(8);*/

				//stats.append(","+","+","+","+","+","+","+","+","+","+",");
				stats.append(delta_time+","+ diagsize +","+ singlefault +","+ doublefault +","+triplefault+","+rest+",");
				stats.append(ncs+","+ncomp+","+mhs+",");
				stats.append(algorithm_name+","+pid+"\n");
			}

		}


	}

	//------------------------------------ SINGLE EXPERIMENT-------------------------------------------------------------


	/**
	 * Generates an input suitable for a single experiment in PyMDB
	 * @param relevant
	 * @param directory
	 * @param num_comp
	 * @return
	 */
	public File createSingleExperimentPyMBDInput(HashSet<HashSet<Integer>> relevant, String directory, int num_comp){
		int_relevant_sets = new TreeSet<HashSet<Integer>>(new Comparator<HashSet<Integer>>() {
			@Override
			public int compare(HashSet<Integer> o1, HashSet<Integer> o2) {
				if (o1.size() < o2.size()) {
					return -1;
				}
				return 1;
			}
		});

		int_relevant_sets.addAll(relevant);
		createSingleExperimentInputProblemsFile(num_comp);

		return null;
	}

	/**
	 * Generates the input.txt file for PyMBD, i.e. a text file containing
	 * the sets to be used in a single line, sets separated by "|" and elements 
	 * separated by ",". The sets are stored in the file ordered by cardinality.
	 */
	private void createSingleExperimentInputProblemsFile(int num_comp){
		StringBuilder input = new StringBuilder();		
		Iterator<HashSet<Integer>> iterator_1 = int_relevant_sets.iterator();
		while(iterator_1.hasNext()){
			HashSet<Integer> cs = iterator_1.next();
			Iterator<Integer> iterator_2 = cs.iterator();
			while(iterator_2.hasNext()){
				Integer element = iterator_2.next();
				input.append(element);
				if(iterator_2.hasNext()){
					input.append(",");
				}
			}
			if(iterator_1.hasNext()){
				input.append("|");
			}
		}

		//System.out.println(input.toString());

		// problems consists of pid, ncomp, ncs, neffc
		StringBuilder problems = new StringBuilder();
		problems.append("1\t");
		problems.append(num_comp);
		problems.append("\t");
		problems.append(int_relevant_sets.size());
		problems.append("\t");
		problems.append(int_relevant_sets.size());

		//System.out.println(problems.toString());
		printToFile(dir,"inputs.txt", input.toString(), false);
		printToFile(dir, "problems.txt", problems.toString(), false);
	}


	public void runSingleExperiment() throws IOException{
		createSingleExperimentRunSh();
		executeSingleExperimentPyMBD();
		readSingleExperimentResult();
	}

	private void executeSingleExperimentPyMBD() throws IOException{
		InputStream is;
		String line;
		ProcessBuilder processbuilder;
		List<String> commands = new ArrayList<String>();
		commands.add("/bin/sh");
		commands.add("-c");
		commands.add("osascript "+main_dir+"apple_run.scpt");
		try {
			processbuilder = new ProcessBuilder(commands);
			Process process = processbuilder.start();
			//System.out.println("Waiting for batch file ...");
			process.waitFor();
			//System.out.println("Batch file done.");

			is = process.getInputStream();
			InputStreamReader isr = new InputStreamReader(is);
			BufferedReader br = new BufferedReader(isr);
			while (((line = br.readLine()) != null)){
				//System.out.println(line+"\n");
			}

			is = process.getErrorStream();
			InputStreamReader isr2 = new InputStreamReader(is);
			BufferedReader br2 = new BufferedReader(isr2);
			while (((line = br2.readLine()) != null)){
				//System.out.println(line+"\n");
			}

			process.destroy();	
		} catch (IOException | InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}

	}

	private void createSingleExperimentRunSh(){
		StringBuilder run = new StringBuilder();

		run.append("trap 'echo Control-C trap caught; exit 1' 2\n");
		run.append("SOURCE=\"${BASH_SOURCE[0]}\" \n while [ -h \"$SOURCE\" ]; do \n");
		run.append("DIR=\"$( cd -P \"$( dirname \"$SOURCE\" )\" && pwd )\" \n");
		run.append("SOURCE=\"$(readlink \"$SOURCE\")\" \n");
		run.append(" [[ $SOURCE != /* ]] && SOURCE=\"$DIR/$SOURCE\"  \n");
		run.append("done \n");
		run.append("DIR=\"$( cd -P \"$( dirname \"$SOURCE\" )\" && pwd )\" \n");
		run.append("echo $DIR \n");
		run.append("cd $DIR \n");
		run.append("cd benchmarks/experiment03-mhs/ \n");
		switch(algo){
		case HSDAG:
			run.append("./compute.py   -a hsdag -m 0 -r run01  | tee -a run.log 2>&1 \n");         
			algorithm_name = "hsdag";
			break;
		case HSTREE:
			run.append("./compute.py   -a hst -m 0  -r run01  | tee -a run.log 2>&1 \n");
			algorithm_name = "hst";
			break;
		case BOOLEAN:
			run.append("./compute.py   -a bool-it-h5-stop  -m 0  -r run01  | tee -a run.log 2>&1 \n");
			algorithm_name = "bool-it-h5-stop";
			break;
		}
		run.append("cd ../../ \n");
		printToFile(main_dir,"run.sh", run.toString(),false);
	}


	private void readSingleExperimentResult(){
		String filename_result = "results-"+algorithm_name+".txt";
		String filename_output = "output-"+algorithm_name+".txt";
		readFile(dir+"run01/"+filename_result);
		readFile(dir+"run01/"+filename_output);
	}


	public void readFile(String filename){
		File inputFile = new File(filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(inputFile));
			if(filename.contains("results"))
				processResultFile(br);
			else
				processOutputFile(br);

		} catch (IOException e) {
			e.printStackTrace();
		} finally {
			try {
				if (br != null)br.close();
			} catch (IOException ex) {
				ex.printStackTrace();
			}
		}
	}
	private void processResultFile(BufferedReader br) throws IOException{
		String currentLine;
		int counter = 0;
		while ((currentLine = br.readLine()) != null) {
			if(counter > 0){
				processLine(currentLine);
			}
			counter++;
		}
	}

	private void processLine(String line){
		String[] tokens = line.split(",");
		delta_time = Double.valueOf(tokens[9]);
		//System.out.println(delta_time);


	}

	private void processOutputFile(BufferedReader br) throws IOException{
		String currentLine;
		while ((currentLine = br.readLine()) != null) {
			processOutputLine(currentLine);
		}
	}

	private void processOutputLine(String line){
		String[] tokens = line.split("\t");
		String output = tokens[2];
		String delims = "[|]+"; //split at |
		String[] tokens2 = output.split(delims);
		for(String diagnosis:tokens2){

			String[] elements = diagnosis.split(",");
			HashSet<Integer> diag = new HashSet<>();
			for(String element:elements){
				Integer i = Integer.valueOf(element);
				diag.add(i);		
			}
			int_diagnoses.add(diag);
		}
	}


	/**
	 * Prints content to a file
	 * @param directory
	 * @param filename
	 * @param content
	 */
	public static void printToFile(String directory, String filename,String content, boolean append_to_end) {
		File file = new File(directory+filename);	
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

	public HashSet<HashSet<Integer>> getInt_diagnoses() {
		return int_diagnoses;
	}

	public double getDelta_time() {
		return delta_time;
	}



	public static StringBuilder getStats() {
		return stats;
	}

	public static void readPYMBDResults(){
		String filename = "run01_AI_all_newAI_experiments.csv";

		try {
			resultsFile = new File(main_dir + filename);
			stats = new StringBuilder();
			if (!resultsFile.exists()) {
				resultsFile.createNewFile();
			} 
			outputStream = new BufferedWriter(new FileWriter(resultsFile));


			outputStream.write("Time"+"," + "Diagnosis Size" + ","+  "Single Fault"  +","+ "Double Fault" +","+ "Triple Fault" +","+ "Rest Fault" +","  + 
					"#ncs #1ATMSprop "+ ","+"#ncomp #2(ATMS min)"+ ",mhs,"+
					"Result,"+"PID,"+"\n");

			outputStream.close();
			PyMBDInterface engine = new PyMBDInterface();
			engine.readExperimentResult("");
			engine.createStats();
			printToFile(main_dir,filename,stats.toString(),true);
			//call

		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
			System.exit(-1);

		}


	}

	public static void main(String[] args) {




		resultsFile = new File(dir+"assa");
		stats = new StringBuilder();
		if (!resultsFile.exists()) {
			try {
				resultsFile.createNewFile();
			} catch (IOException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		} 


	}


}
