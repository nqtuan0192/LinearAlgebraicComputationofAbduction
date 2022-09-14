package model.target.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Random;

import model.target.converter.LogicToSOLARModelConverter.Encoding;
import model.target.converter.LogicToSOLARModelConverter.ExclusiveOr;

public abstract class LogicToClingoAbductionModelConverter {
	/** The production_field. */
	protected  HashSet<String> hypotheses;
	
	/** The clauses. */
	protected StringBuilder clauses= new StringBuilder();
	
	/** The variable_mapping. */
	public HashMap<String,Integer> variable_mapping = new HashMap<>();
		
	/** The output_stream. */
	protected  BufferedWriter output_stream;
	
	/** The explanations int values. */
	protected  ArrayList<Integer> hypothesesIntValues = new ArrayList<>();
	
	//maps from an sclause e.g. s1, to its corresponding hyp, e.g. H_1
	public  HashMap<String,String> sclauseMapping= new HashMap<>();
	
	public HashSet<String> facts;
	
	/**
	 * Creates the clauses.
	 *
	 * @param model the model
	 */
	public abstract void compile(String model);
	
	
	/**
	 * Adds the observations.
	 *
	 * @param observations the observations
	 * @return true, if successful
	 */
	public boolean addObservations(HashSet<String> observations){ 
		addHypotheses();
		clauses.append("manifestation(m). ");
		Iterator<String> observation_iterator = observations.iterator();
		while(observation_iterator.hasNext()){
			String obs = observation_iterator.next();
			Integer value = variable_mapping.get(obs);
			clauses.append("pos(m,"+value+"). ");
		}
		return true;
	}

	/**
	 * Adds the production field.
	 */
	protected void addHypotheses(){
		int counter = 1;
		for(String hyp:hypotheses){
			Integer value = variable_mapping.get(hyp);
			String s_clause_id = "s"+counter;
			clauses.append("sclause(s"+counter+",1)."+" pos(s"+counter+","+value+").\n");
			sclauseMapping.put(s_clause_id, hyp);
			counter++;
		}
	}


	/**
	 * Prints the to file.
	 *
	 * @param directory the directory
	 * @param filename the filename
	 * @return the string
	 */
	public String printToFile(String directory, String filename) {
		if(clauses!=null){
			if(directory==null){
				directory = "";
			}
			if(filename == null){
				long time = System.currentTimeMillis();
				Random r = new Random();
				int min = 1;
				int max = 10000;
				int nxt = r.nextInt(max - min) + min;		
				filename = "asp_file"+time+nxt+".txt";
			}

			
			File asp_instance_encoding;
			String asp_filename = filename.replace(".atms", ".lp").replace(".txt", ".lp");
			if(directory==""){
				asp_instance_encoding = new File(asp_filename);
			}
			else{
				asp_instance_encoding = new File(directory + asp_filename);
			}

			try {
				if (!asp_instance_encoding.exists()) {
					asp_instance_encoding.createNewFile();
				} 
				output_stream = new BufferedWriter(new FileWriter(asp_instance_encoding));
				output_stream.write(clauses.toString());
				output_stream.close();
				return asp_filename;
			} catch (IOException e) {
				System.out.println("Fault while handling the results file");
				e.printStackTrace();
				return "";

			}

		}
		return "";
	}


	/**
	 * Return model as string.
	 *
	 * @return the string
	 */
	public String returnModelAsString() {		
		return clauses.toString();
	}






}
