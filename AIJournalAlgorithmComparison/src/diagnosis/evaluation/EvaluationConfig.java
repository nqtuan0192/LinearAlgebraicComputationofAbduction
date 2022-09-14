package diagnosis.evaluation;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;

import diagnosis.algorithms.hittingsetalg.PyMBDInterface;

public class EvaluationConfig {

	private static long timeout = 1200000;
	
	private static void callArtificialConfig(String dir, String timeout_str){
		String ex_nr = new File(dir).getName();
		timeout= Long.valueOf(timeout_str).longValue();
		
		String model_dir = dir+"/";
		String output_dir = dir+"/";
		String output_filename = "Eval_meta_"+ ex_nr +".csv";
		aiConfigHSMeta(model_dir,output_dir,output_filename);
		
	}

	
	private static void aiConfigHSMeta(String model_dir, String output_dir, String output_filename){
		try {
			File resultsFile = new File(output_dir + output_filename);
			
			if (!resultsFile.exists()) {
				resultsFile.createNewFile();
			} 
			
			Evaluation.printToFile(output_dir, output_filename,"result,"+"PID,"+"filename" + "," + "hypotheses_size" + "," + "effect_size," +
					"rules"+ ","+ "obs_size" + ","
					+ "time"+"," + "diagnosis_size" + ","+  "single_fault"  +","+ "double_fault" +","+ "triple_fault" +","+ "rest_fault" +"," 
					+ "info1"+"," + "info2" + ","
					+"mem_consumption" + "\n", false); 

			Evaluation.ATMS = true;
			Evaluation.REITER = true;
			Evaluation.ASPClingo = true;
			Evaluation.EXPLORE = true;
			Evaluation.ConsequenceFinding = true;
			
			Evaluation.timeout = EvaluationConfig.timeout;
			File Winner = new File(output_dir + output_filename.replace(".csv", "") +"_winner.csv");
			
			
			if (!Winner.exists()) {
				Winner.createNewFile();
			} 
			Evaluation.printToFile(output_dir, Winner.getName(),"result,"+"PID,"+"filename" + "," + "hypotheses_size" + "," + "effect_size," +
					"rules"+ ","+ "obs_size" + ","
					+ "time"+"," + "diagnosis_size" + ","+  "single_fault"  +","+ "double_fault" +","+ "triple_fault" +","+ "rest_fault" +"," 
					+ "info1"+"," + "info2" + ","
					+"mem_consumption" + "\n", false); 
			
			Evaluation.performArtificialExamplesMeta(model_dir,output_dir,output_filename);


		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
			System.exit(-1);

		}
	}
	

	public static void main(String args[]){
		if(args.length<1){
			System.out.println(" Please call the program with \"<folder> [timeout in ms]\" ");
		}
		else if(args.length>1){
			callArtificialConfig(args[0],args[1]);
		}
		else{
			callArtificialConfig(args[0],"1200000");
		}
		
	}
}
