package model.target.converter;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Random;
import java.util.Set;

import com.google.common.collect.*;

import java.util.Map.Entry;


/**
 * The Class LogicToDimacsModelConverter.
 */
public abstract class LogicToDimacsModelConverter {
	
	/** The result dimacs file. */
	protected static File resultDimacs;
	
	/** The variable mapping. */
	protected  HashMap<String,Integer> variableMapping= new HashMap<>();
	
	/** The filename. */
	protected  String filename;
	
	/** The model_builder. */
	protected  StringBuilder model_builder;
	
	/** The effects. */
	protected  HashSet<String> effects;
	
	/** The explanations. */
	protected  HashSet<String> explanations;
	
	/** The explanations int values. */
	protected  HashSet<Integer> explanationsIntValues = new HashSet<>();
	
	/** The clause mapping. */
	protected  HashMap<String,Integer> clauseMapping= new HashMap<>();
	
	/** The output_stream. */
	protected  BufferedWriter output_stream;
	
	/** The clause_size. */
	protected int clause_size;

	/** The additional_vars. */
	protected HashMap<Integer,ArrayList<Integer>> additional_vars;

	/**
	 * Compile.
	 *
	 * @param model the model
	 */
	public abstract void compile(String model);

	/**
	 * Adds the explanations.
	 */
	protected void addExplanations(){
		/*insert Explanation to create inconsistency -- T \wedge \neg o \wedge E -> \bot*/
		int clausecounter = clauseMapping.size()+1;
		//System.out.println(" CLAUSECOUNTER " +  clauseMapping.size() + " " + clausecounter);
		for(String explanation : explanations){
			int tailElementIntValue = (variableMapping.get(explanation));
			//System.out.println(" EXPLANATION " + explanation + " " + clausecounter);
			clauseMapping.put(explanation, clausecounter++);
			model_builder.append(tailElementIntValue + " 0 \n");
		}

	}

	/**
	 * Adds the observations.
	 *
	 * @param observations the observations
	 * @return true, if successful
	 */
	public boolean addObservations(HashSet<String> observations){ 	
		try{addExplanations();
		if( (variableMapping.size()-explanations.size())<observations.size()){
			return false;
		}
		int clausecounter = clauseMapping.size()+1;
		for(String obs: observations){
			Integer value = variableMapping.get(obs);
			model_builder.append(value*(-1) + " ");

		}
		clauseMapping.put(observations.toString(), clausecounter++);
		clause_size++;
		model_builder.append("0\n");

		}
		catch(Exception e){
			e.printStackTrace();
			return false;
		}
		return true;
	}






	/**
	 * Adds the random observations.
	 *
	 * @param numObs the num obs
	 * @return true, if successful
	 */
	public  boolean addRandomObservations(int numObs){
		//maximum number of possible observations
		if( (variableMapping.size()-explanations.size())<numObs){
			return false;
		}

		// add all explanations as true
		addExplanations();
		int clausecounter = clauseMapping.size()+1;
		//add random observations (\beg o)
		ArrayList<String> headsList = new ArrayList<String>();
		headsList.addAll(effects); 
		Set<Integer> indices = new HashSet<Integer>();
		int i=0; 
		while(i<numObs){
			Random rand = new Random();
			int index = rand.nextInt(((effects.size()-1) - 0) + 1) + 0;
			//System.out.println("INDEX" +index);
			if(!indices.contains(Integer.valueOf(index))){
				indices.add(index);
				//System.out.println("INDEX__" +index);
				Integer value = variableMapping.get(headsList.get(index));
				//System.out.println("*** CLAUSE " + headsList.get(index) + " " + clausecounter);
				clauseMapping.put(headsList.get(index), clausecounter++);
				// Add \neg o to DIMACS
				model_builder.append(value*(-1) + " ");
				i++;
			}	
		}
		model_builder.append("0\n");
		return true;
	}

	/**
	 * Prints the to file.
	 *
	 * @param directory the directory
	 * @param filename the filename
	 * @return the string
	 */
	public  String printToFile(String directory, String filename){
		if(directory==null){
			directory = "";
		}
		if(filename == null){
			filename = "dimacs_file.txt";
		}
		String header = "p cnf " + variableMapping.size() + " " + (clause_size  + explanations.size())+ " \n";
		model_builder.insert(0,header);
		String cnfFilename = filename.replace(".pl", ".cnf").replace(".txt", ".cnf");
		if(directory==""){
			resultDimacs = new File(cnfFilename);
		}
		else{
			resultDimacs = new File(directory + cnfFilename);
		}

		try {
			if (!resultDimacs.exists()) {
				resultDimacs.createNewFile();
			} 
			output_stream = new BufferedWriter(new FileWriter(resultDimacs));
			output_stream.write(model_builder.toString());
			output_stream.close();
			return cnfFilename;
		} catch (IOException e) {
			System.out.println("Fault while handling the results file");
			e.printStackTrace();
			System.exit(-1);
		}
		return cnfFilename;
	}


	/**
	 * Gets the key to value.
	 *
	 * @param value the value
	 * @return the key to value
	 */
	public  String getKeyToValue(Integer value){
		for( Entry<String, Integer> s:clauseMapping.entrySet()){
			if((s.getValue().intValue())==(value))
			{
				return s.getKey();
			}
		}
		return "wrongvalue";
	}


	/**
	 * Gets the variable mapping.
	 *
	 * @return the variable mapping
	 */
	public  HashMap<String, Integer> getVariableMapping() {
		return variableMapping;
	}
	
	/**
	 * Sets the variable mapping.
	 *
	 * @param variableMapping the variable mapping
	 */
	public  void setVariableMapping(HashMap<String, Integer> variableMapping) {
		this.variableMapping = variableMapping;
	}
	
	/**
	 * Gets the explanations.
	 *
	 * @return the explanations
	 */
	public  HashSet<String> getExplanations() {
		return explanations;
	}
	
	/**
	 * Sets the explanations.
	 *
	 * @param explanations the new explanations
	 */
	public  void setExplanations(HashSet<String> explanations) {
		this.explanations = explanations;
	}

	/**
	 * Gets the filename.
	 *
	 * @return the filename
	 */
	public String getFilename() {
		return filename;
	}

	/**
	 * Sets the filename.
	 *
	 * @param filename the new filename
	 */
	public void setFilename(String filename) {
		this.filename = filename;
	}

	/**
	 * Gets the clause mapping.
	 *
	 * @return the clause mapping
	 */
	public  HashMap<String, Integer> getClauseMapping() {
		return clauseMapping;
	}
	
	/**
	 * Sets the clause mapping.
	 *
	 * @param clauseMapping the clause mapping
	 */
	public  void setClauseMapping(HashMap<String, Integer> clauseMapping) {
		this.clauseMapping = clauseMapping;
	}

	/**
	 * Return model as string.
	 *
	 * @return the string
	 */
	public String returnModelAsString() {
		return model_builder.toString();
	}
}
