package diagnosis.engines;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

import diagnosis.algorithms.SOLARInterface;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;
import diagnosis.converters.SOLARResultConverter;
import diagnosis.data_structures.Diagnosis;
import diagnosis.data_structures.Hypothesis;
import model.target.converter.HornLogicToSolarConverter;
import model.target.converter.LogicToSOLARModelConverter;
import model.target.converter.NonHornLogicToSolarConverter;
import support.ModelFileFilter;

public class ConsquenceFindingEngine implements DiagnosisEngine {

	public static class CFCaller implements Callable<SOLARInterface> {

		String fileArg;
		String execDir;
		SearchStrategy strategy;
		long timeout;

		private CFCaller(String fileArg, String execDir, SearchStrategy strategy, long timeout) {
			this.fileArg = fileArg;
			this.execDir = execDir;
			this.strategy = strategy;
			this.timeout = timeout;

		}

		@Override
		public SOLARInterface call() throws Exception {
			SOLARInterface solar = new SOLARInterface();
			//solar.executeSolarDirectly(this.fileArg, this.execDir, this.strategy, this.timeout);
			
			solar.executeSolar(this.fileArg, this.execDir, this.strategy, this.timeout);
			
			
			
			return solar;
		}

	}

	public enum ConsequenceFindingAlgorithm {
		SOLAR, 
		SOLAR_eval, other
	}

	public enum SearchStrategy {
		DF, DFID, 
		DFIDR 
	}

	private LogicToSOLARModelConverter lTS;

	private SOLARResultConverter result_converter;
	private SearchStrategy strategy = SearchStrategy.DFID;
	private String execDir;
	private ConsequenceFindingAlgorithm algorithm = ConsequenceFindingAlgorithm.SOLAR;
	private HashSet<HashSet<String>> result = new HashSet<>();

	private HashSet<Diagnosis> diagnoses;
	private String fileDir;
	private String filename;
	private String error = "";
	private long timeout = -1;
	private long start;

	private StringBuilder stats = new StringBuilder();
	double delta_time;
	private boolean is_timeout = false;

	private int obs_size;

	public ConsquenceFindingEngine(String execDir, ConsequenceFindingAlgorithm algorithm, SearchStrategy strategy) {
		this.execDir = execDir;
		this.algorithm = algorithm;
		this.diagnoses = new HashSet<Diagnosis>();
		this.strategy = strategy;

	}

	@Override
	public double getDeltaTime() {
		return this.delta_time;
	}

	@Override
	public HashSet<Diagnosis> getDiag() {
		return this.diagnoses;
	}

	@Override
	public HashSet<HashSet<String>> getDiagnoses() {
		if (this.result.size() < 1) {
			HashSet<HashSet<String>> new_set_set = new HashSet<>();
			HashSet<String> new_set = new HashSet<>();
			new_set_set.add(new_set);
			return new_set_set;
		}
		return this.result;
	}

	@Override
	public String getError() {
		// TODO Auto-generated method stub
		return this.error;
	}

	public String getInputFile() {
		return this.filename;
	}

	@Override
	public String getStats() {
		// TODO Auto-generated method stub
		return this.stats.toString();
	}

	public SearchStrategy getStrategy() {
		return this.strategy;
	}

	public long getTimeout() {
		return this.timeout;
	}

	@Override
	public boolean isTimeout() {
		return this.is_timeout;
	}

	private HashSet<HashSet<String>> mapDiagnoses(HashSet<HashSet<String>> diagnoses) {
		HashSet<HashSet<String>> diag = new HashSet<>();
		for (HashSet<String> str : diagnoses) {
			HashSet<String> d = new HashSet<>();
			for (String s : str) {
				String original_hypo = this.lTS.variable_mapping.get(s);
				d.add(original_hypo);
			}
			diag.add(d);
		}
		return diag;
	}

	private void performConsequenceFinding() {
		this.start = System.currentTimeMillis();
		String output_cf = this.filename.replace(".cnf", ".sol");
		if (this.fileDir == null) {
			this.fileDir = "";
		}
		File output_file = new File(output_cf);
		try {
			if (!output_file.exists()) {
				output_file.createNewFile();
			}
			StringBuilder output = new StringBuilder();

			long time = System.nanoTime();

			SOLARInterface solar_i = new SOLARInterface();
			ExecutorService e = Executors.newSingleThreadExecutor();
			CFCaller caller = new CFCaller(this.fileDir + this.filename, this.execDir, this.strategy, this.timeout);
			try {
				Future<SOLARInterface> control = e.submit(caller);
				e.shutdown();
				if (this.timeout > 0) {
					solar_i = control.get(this.timeout, TimeUnit.MILLISECONDS);
				} else {
					solar_i = control.get();
				}

				output = solar_i.output;
				if (output == null) {
					return;
				}

				BufferedWriter outputStream = new BufferedWriter(new FileWriter(output_file));

				outputStream.write(output.toString());
				outputStream.close();

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
			} finally {
				if (solar_i.process != null) {
					solar_i.process.destroy();
				}

			}

			this.delta_time = (System.nanoTime() - time) / 1000000;

			if ((this.timeout > 0) && (this.delta_time > this.timeout)) {
				this.is_timeout = true;
				this.error = "Timeout";
			}

			BufferedWriter cfOutputStream = new BufferedWriter(new FileWriter(output_file));
			cfOutputStream.write(output.toString());
			cfOutputStream.close();

		} catch (IOException e) {
			this.error = "Error Solar Diagnosis " + e.getStackTrace().toString();
			e.printStackTrace();
		}
	}

	private HashSet<HashSet<String>> retrieveCFResult() {
		this.result_converter = new SOLARResultConverter();
		switch (this.algorithm) {
		case SOLAR:
			this.result_converter.readFile(this.filename);
			break;
		case SOLAR_eval:
			this.result_converter.evaluation = true;
			this.result_converter.readFile(this.filename);
		default:

		}
		deleteInputAndOutputLPFile(this.filename);
		this.result = mapDiagnoses(this.result_converter.returnResultAsStrings());
		retrieveStats(this.result);
		
		return this.result;
	}

	private void deleteInputAndOutputLPFile(String filename){
		 try {
	         File input = new File(filename);
	         input.delete();    
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
	}
	private String retrieveSolarModel(String model, HashSet<String> observations) {
		this.obs_size = observations.size();
		new LogicParser();
		if (model.contains("|")) {
			this.lTS = new NonHornLogicToSolarConverter();
		} else {
			this.lTS = new HornLogicToSolarConverter();
		}

		this.lTS.createClauses(model);
		this.lTS.addObservations(observations);
		this.filename = this.lTS.printToFile(this.fileDir, this.filename);
		return this.lTS.returnModelAsString();
	}

	private void retrieveStats(HashSet<HashSet<String>> diagnosesList) {
		this.delta_time = this.result_converter.time * 1000; // conversion from
																// seconds to
																// milliseconds
		diagnosesList.size();
		System.currentTimeMillis();
		if (this.is_timeout) {
			this.stats.append("TIMEOUT" + "," + "\n");
			return;
		}

		String diag = "";
		ArrayList<ArrayList<String>> diagnosesStringValue = new ArrayList<ArrayList<String>>();
		int single_fault = 0;
		int double_fault = 0;
		int triple_fault = 0;
		int rest = 0;
		this.result = new HashSet<HashSet<String>>();
		for (HashSet<String> diagnosis : diagnosesList) {
			Diagnosis diagnosis_class = new Diagnosis();
			diagnosis_class.setHypotheses(new HashSet<Hypothesis>());
			ArrayList<String> diagnosisString = new ArrayList<>();

			switch (diagnosis.size()) {
			case 1:
				single_fault++;
				break;
			case 2:
				double_fault++;
				break;
			case 3:
				triple_fault++;
				break;
			default:
				rest++;
			}
			for (String element : diagnosis) {
				String elementStringValue = element;
				Hypothesis hypo = new Hypothesis(elementStringValue);
				diagnosis_class.getHypotheses().add(hypo);
				diagnosisString.add(elementStringValue);
				diag = diag + " " + elementStringValue;
			}
			diag = diag + ",";
			HashSet d = new HashSet<String>(diagnosisString);
			this.result.add(d);
			this.diagnoses.add(diagnosis_class);
			diagnosesStringValue.add(diagnosisString);
		}
		if (this.delta_time < 0) {
			this.is_timeout = true;
		}

		this.stats.append(Double.toString(this.delta_time) + "," + String.valueOf(diagnosesList.size()) + ","
				+ single_fault + "," + double_fault + "," + triple_fault + "," + rest + "," + 
				this.result_converter.executation_info[0] + "," + this.result_converter.executation_info[1] + "\n");


	}

	public void setInputFile(String inputFile) {
		this.filename = inputFile;
	}

	public void setStrategy(SearchStrategy strategy) {
		this.strategy = strategy;
	}

	public void setTimeout(long timeout) {
		this.timeout = timeout;
	}

	@Override
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		this.timeout = timeout;
		this.filename = model_file.getName();
		this.fileDir = model_file.getParent() + "/";

		if (ModelFileFilter.getExtension(model_file).equals(ModelFileFilter.cnf)) {
			/* TODO: Add that you can also use cnf files directly */
		} else {
			LogicParser parserNewModel = new LogicParser();
			parserNewModel.parseFile(model_file.getAbsolutePath());
			LSentence modelLSentence = (LSentence) parserNewModel.result();
			String modelAsString = modelLSentence.toString();

			retrieveSolarModel(modelAsString, observations);
			performConsequenceFinding();
			retrieveCFResult();

		}

	}

	@Override
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		this.timeout = timeout;
		retrieveSolarModel(model, observations);
		performConsequenceFinding();
		retrieveCFResult();
	}

}
