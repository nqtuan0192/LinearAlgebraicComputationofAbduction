package diagnosis.engines;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.CancellationException;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import diagnosis.algorithms.ClingoInterface;
import diagnosis.algorithms.atms.ATMSNode;
import diagnosis.algorithms.atms.ATMSTextInterface;
import diagnosis.algorithms.compiler.LSentence;
import diagnosis.algorithms.compiler.LogicParser;
import diagnosis.algorithms.theoremprover.Assumption;
import diagnosis.converters.ASPResultConverter;
import diagnosis.data_structures.Diagnosis;
import diagnosis.engines.ATMSEngine.ATMSCaller;
import model.target.converter.HornLogicToClingoAbductionModelConverter;
import model.target.converter.HornLogicToDimacsModelConverter;
import model.target.converter.LogicToClingoAbductionModelConverter;
import model.target.converter.NonHornLogicToDimacsModelConverter;
import model.target.converter.NonHornToClingoAbductionModelConverter;
import support.Printer;

public class ASPEngine implements DiagnosisEngine{
	private static String heuristics_option = "--heuristic=Domain";
	private static String dom_option = "--dom-mod=5,16";
	private static String enumeration_option = "--enum-mod=domRec"; 
	private static String compute_all_models = "0"; 
	public String time_out = "--time-limit"; //--time-limit=t Force termination after t seconds.
	static String file_encoding = "abduction.dl";
	
	private static String execDir="lib/clingo/";
	
	private LogicToClingoAbductionModelConverter lTC;
	private ASPResultConverter result_converter;
	

	private String fileDir="";	
	private String filename;
	private HashSet<HashSet<String>> result = new HashSet<>();
	private long timeout = -1;
	private HashSet<HashSet<String>> diagnoses;
	private HashSet<Diagnosis> diag = new HashSet<Diagnosis>();
	private String error; 
	private StringBuilder stats = new StringBuilder();
	private boolean is_timeout = false;
	private double delta_time; //time in milliseconds
	public HashSet<String> facts;
	
	@Override
	public void startComputation(String model, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		this.timeout = timeout;
		time_out+="="+(String.valueOf((int) (timeout/1000))); //ms to seconds
		retrieveASPEncoding(model,observations);
		performASEnumeration();
		HashSet<HashSet<String>> as = new HashSet<HashSet<String>>();
		if(!is_timeout){
        	as.addAll(retrieveASPResult());
    		if(as.isEmpty()){
    			result.add(new HashSet<>());
    		}
    		this.diagnoses=as;
    	}
		retrieveStats(as);
	}

	@Override
	public void startComputation(File model_file, HashSet<String> observations, HashSet<String> negatedObservations,
			long timeout) {
		this.timeout = timeout;
		time_out+="="+(String.valueOf((int) (timeout/1000))); //ms to seconds
		this.filename = model_file.getName();
		this.fileDir = model_file.getParent()+"/";
		LogicParser parserNewModel = new LogicParser();
    	parserNewModel.parseFile(model_file.getAbsolutePath());
    	LSentence modelLSentence = (LSentence)parserNewModel.result();	    	
    	String modelAsString = modelLSentence.toString();
    	retrieveASPEncoding(modelAsString,observations);
    	performASEnumeration();
    	HashSet<HashSet<String>> as = new HashSet<HashSet<String>>();
    	if(!is_timeout){
        	as.addAll(retrieveASPResult());
    		if(as.isEmpty()){
    			result.add(new HashSet<>());
    		}
    		this.diagnoses=as;
    	}  
    	retrieveStats(as);
	}
	
	
	private String retrieveASPEncoding(String model,HashSet<String> observations){
		LogicParser parser = new LogicParser();
    	if (model.contains("|")) {
    		lTC = new NonHornToClingoAbductionModelConverter();
    	}
    	else{
    		lTC = new HornLogicToClingoAbductionModelConverter();
    	}
    	lTC.facts=facts;
    	lTC.compile(model);
    	lTC.addObservations(observations);
		filename = lTC.printToFile(fileDir, filename);
		return lTC.returnModelAsString();
	}
	
	private void performASEnumeration(){
		if(filename==null){
			return;
		}
		String output_asp = filename.replace(".lp", "_result.lp");
		File output_file = new File(output_asp);
		try {
			if (!output_file.exists()) {
				output_file.createNewFile();
			}    
			StringBuilder output = new StringBuilder();
			
			List<String> commands = new ArrayList<String>();
			commands.add(execDir+"clingo");
			commands.add(execDir+file_encoding);
			commands.add(fileDir+filename);
			commands.add(heuristics_option);
			commands.add(dom_option);
			commands.add(enumeration_option);
			commands.add(compute_all_models);
			if(timeout>0){
				commands.add(time_out);
			}
			ClingoInterface clingo= new ClingoInterface();
			ExecutorService e = Executors.newSingleThreadExecutor();
			ASPCaller caller = new ASPCaller(commands);
			try{
				Future<ClingoInterface> control = e.submit(caller);
				e.shutdown();
				if(timeout>0){ 
					clingo = control.get(timeout, TimeUnit.MILLISECONDS);
				}
				else{
					clingo = control.get();
				}
				
				output = clingo.output;
				if(output==null){
					return;
				}
				
				BufferedWriter aspOutputStream = new BufferedWriter(new FileWriter(output_file));
				
				aspOutputStream.write(output.toString());
				aspOutputStream.close();
				
			} catch (CancellationException ex) {
				this.is_timeout = true;
				ex.printStackTrace();
			}
			catch (InterruptedException ex) {
				this.is_timeout = true;
				ex.printStackTrace();
			} catch (ExecutionException ex) {
				this.is_timeout = true;
				ex.printStackTrace();
			} 
			catch (Exception ex) {
				this.is_timeout = true;
				ex.printStackTrace();
			} 	
			finally{
				if(clingo.process!=null){
					clingo.process.destroy();
				}
				
			}
		} catch (IOException e) {
			error = "Error ASP Diagnosis " + e.getStackTrace().toString();
			e.printStackTrace();
		}
		
	}
	
	
	private void deleteInputAndOutputLPFile(String filename){
		 try {
	         File input = new File(filename);
	         input.delete();    
	      } catch(Exception e) {
	         e.printStackTrace();
	      }
	}
	
	private HashSet<HashSet<String>> retrieveASPResult(){
		result_converter = new ASPResultConverter(lTC.sclauseMapping);
		filename = filename.replace(".lp", "_result.lp");
		result_converter.readFile(filename);
		if(result_converter.time>0){
			this.delta_time = result_converter.time*1000;
		}
		return result_converter.returnResultAsStrings();
	}
	
private void retrieveStats(HashSet<HashSet<String>> diagnoses){	
		if(is_timeout){			
			stats.append("TIMEOUT \n\r");
			return;
		}
		int maxSize = 0;
			int counterSingle = 0;
			int counterDouble = 0;
			int counterTriple = 0;
			int counterRest = 0;
			for (HashSet<String> diagnosis: diagnoses) {  
	    		switch(diagnosis.size()){
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
	    	stats.append(delta_time +"," +diagnoses.size() + "," + counterSingle + "," + counterDouble + ","+ counterTriple + "," + counterRest +","+//"\n");
	    	(result_converter.solving_time*1000)+","+(result_converter.unsat_time*1000)+"\n");
	    	
	    	deleteInputAndOutputLPFile(filename);
			filename = filename.replace("_result.lp",".lp");
			deleteInputAndOutputLPFile(filename);
}

	@Override
	public String getError() {
		return error;
	}

	@Override
	public HashSet<HashSet<String>> getDiagnoses() {
		return diagnoses;
	}

	@Override
	public HashSet<Diagnosis> getDiag() {
		return diag;
	}

	@Override
	public String getStats() {
		return stats.toString();
	}

	@Override
	public boolean isTimeout() {
		return is_timeout;
	}

	@Override
	public double getDeltaTime() {
		return delta_time;
	}
	
	public static void main(String args[]){

		
		File model_file = new File("/Users/roxane/Documents/workspace/phcap_2.atms");
		HashSet<String> observations = new HashSet<>();
		observations.add("e_5");
		
		ASPEngine asp = new ASPEngine();
		asp.startComputation(model_file, observations, null, 600000);
		
	}
	
	public static class ASPCaller implements Callable<ClingoInterface> {

		List<String> commands;
		ClingoInterface clingo;

		private ASPCaller(List<String> commands) {
			this.commands = commands;
			clingo = new ClingoInterface();
		}

		@Override
		public ClingoInterface call() throws Exception {
			clingo.executeClingo(commands);
			return clingo;
		}

	}
	

}
