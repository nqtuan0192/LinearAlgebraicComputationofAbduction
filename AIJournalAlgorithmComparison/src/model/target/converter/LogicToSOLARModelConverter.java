package model.target.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;

import diagnosis.algorithms.compiler.*;
import diagnosis.engines.ConsquenceFindingEngine;
import diagnosis.engines.ConsquenceFindingEngine.ConsequenceFindingAlgorithm;
import diagnosis.engines.ConsquenceFindingEngine.SearchStrategy;

// TODO: Auto-generated Javadoc
/**
 * The Class LogicToSOLARModelConverter convertes a logic representation to a representation suitable for the
 * consequence finding tool SOLAR.
 */
public abstract class LogicToSOLARModelConverter {

	/** The production_field. */
	protected  HashSet<String> production_field;
	
	/** The effects. */
	protected  HashSet<String> effects;
	
	/** The clauses. */
	protected StringBuilder clauses;
	
	/** The variable_mapping. */
	public HashMap<String,String> variable_mapping = new HashMap<>();
	
	/** The clausecounter. */
	public int clausecounter = 0;
	
	/** The encoding*/
	protected Encoding encoding = Encoding.CNF_ABD;
	
	/** The xors. */
	protected ArrayList<ExclusiveOr> xors = new ArrayList<>();
	
	/** The output_stream. */
	protected  BufferedWriter output_stream;
	
	/** The additional_vars. */
	protected HashMap<String,ArrayList<String>> additional_vars = new HashMap<>();

	/**
	 * The Enum Encoding.
	 */
	public enum Encoding{
		
		/** The SOLAR encoding. */
		SOLAR_ABD,
		
		/** The CNF encoding. */
		CNF_ABD
	}

	/**
	 * Creates the clauses.
	 *
	 * @param model the model
	 */
	abstract  public void createClauses(String model);

	/**
	 * Adds the observations.
	 *
	 * @param observations the observations
	 * @return true, if successful
	 */
	public boolean addObservations(HashSet<String> observations){ 
		if(effects.size()<observations.size()){
			return false;
		}

		clauses.append("cnf(c");
		clauses.append(clausecounter++);
		clauses.append(",axiom,[");
		Iterator<String> observation_iterator = observations.iterator();
		while(observation_iterator.hasNext()){
			String obs = observation_iterator.next();
			if(encoding==Encoding.SOLAR_ABD){
				clauses.append("conn(obs,"+obs.toString().toLowerCase()+")");
			}else{
				clauses.append("-"+obs.toString().toLowerCase());
			}

			if(observation_iterator.hasNext()){
				clauses.append(",");
			}
		}
		clauses.append("]).\n");
		observation_iterator = observations.iterator();
		while(observation_iterator.hasNext()){
			generateExclusiveOrs(observation_iterator.next());
		}

		addProductionField();
		return true;
	}

	/**
	 * Adds the production field.
	 */
	protected void addProductionField(){
		clauses.append("pf([");
		Iterator iterator = production_field.iterator();
		while(iterator.hasNext()){
			String str = iterator.next().toString();
			if(encoding==Encoding.SOLAR_ABD){
				clauses.append("-abd("+str.toLowerCase()+")");
			}
			else{
				clauses.append("-"+str.toLowerCase());
			}
			if(iterator.hasNext()){
				clauses.append(",");
			}
		}
		clauses.append("]).");

	}

	/**
	 * Generate exclusive ors.
	 *
	 * @param observation the observation
	 */
	protected void generateExclusiveOrs(String observation){
		for(ExclusiveOr xor:this.xors){
			if(xor.heads.contains(observation)){
				for(String head:xor.heads){
					if(!head.equals(observation)){
						for(String tail:xor.tails){
							if(encoding==Encoding.SOLAR_ABD){
								clauses.append("cnf(c");
								clauses.append(clausecounter++);
								clauses.append(",axiom,[-caused("+head.toLowerCase()+","+tail.toLowerCase()+")]).\n");
							}
							else{
								clauses.append("cnf(c");
								clauses.append(clausecounter++);
								clauses.append(",axiom,[-"+ head.toLowerCase()+"]).\n");
							}
						}
					}
				}
			}
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
			File result_SOLAR;
			if(directory==null){
				directory = "";
			}
			if(filename == null){
				filename = "default.txt";
			}
			String solar_filename = filename.replace(".txt", ".sol").replace(".atms", ".sol");
			if(directory==""){
				result_SOLAR = new File(solar_filename);
			}
			else{
				result_SOLAR = new File(directory + solar_filename);
			}

			try {
				if (!result_SOLAR.exists()) {
					result_SOLAR.createNewFile();
				} 
				output_stream = new BufferedWriter(new FileWriter(result_SOLAR));
				output_stream.write(clauses.toString());
				output_stream.close();
				return solar_filename;
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

	/**
	 * Gets the encoding.
	 *
	 * @return the encoding
	 */
	public Encoding getEncoding() {
		return encoding;
	}

	/**
	 * Sets the encoding.
	 *
	 * @param encoding the new encoding
	 */
	public void setEncoding(Encoding encoding) {
		this.encoding = encoding;
	}


	/**
	 * The Class ExclusiveOr.
	 */
	public class ExclusiveOr {
		
		/** The heads. */
		protected HashSet<String> heads = new HashSet<>();
		
		/** The tails. */
		public HashSet<String> tails= new HashSet<>();
	}



}
