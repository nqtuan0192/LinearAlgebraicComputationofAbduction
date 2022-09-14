package model.source.converter;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.Set;

import model.source.data_structures.*;

/**
 * The Class TextFileReader.
 */
public class TextFileReader extends FMEAReader {

	/** The disjunctive_effects. */
	public boolean disjunctive_effects = false;

	/**
	 * Process thetheory.
	 *
	 * @param theory the theory
	 */
	protected void processTheory(String theory){
		String tokens[] = theory.replace("\n", "").replace("\r", "").split("\\.");
		for(String record:tokens){

			ModelEntry entry = new ModelEntry();
			String tokens2[] = record.split("->");
			String lhs = tokens2[0];
			String rhs = tokens2[1];
			if(!disjunctive_effects && rhs.contains(seperator_or)){
				disjunctive_effects = true;
			}
			String antecedents[] = lhs.split("[,|&](?![^(]*\\))",-1); //matches A,B but not A(B,C)
			for(String antecedent:antecedents){
				FaultMode fm = faultModeExists(antecedent,"",this.faultmodes);
				if(fm!=null){

					Set<String> effectSet = fm.getEffects();
					Set<String> newEffects = parseEffectSet(rhs);
					effectSet.addAll(newEffects);
					String logic = fm.getObservations_logic();
					logic += "," + rhs;
					fm.setObservations_logic(logic);
					entry.faultmodes.add(fm);
					entry.observation_logic=rhs.toLowerCase();
				}
				else{
					FaultMode newFailureMode = new FaultMode();
					String newComp = replaceChars(antecedent);
					newFailureMode.setComponent(newComp);
					String newMode = "";
					newFailureMode.setMode(newMode);	

					//newFailureMode.setId("Mode("+newComp+")");	
					newFailureMode.setId(newComp);	

					newFailureMode.setEffects(parseEffectSet(rhs));		
					newFailureMode.setObservations_logic(rhs.toLowerCase());
					newFailureMode.setOriginal_component(antecedent);
					newFailureMode.setOriginal_mode("");
					this.faultmodes.add(newFailureMode);
					//create entry
					entry.faultmodes.add(newFailureMode);
					entry.observation_logic=rhs.toLowerCase();
				}
			}

			entries.add(entry);
		}
	}



	/**
	 * Reads a text file already containing a logical model.
	 * @param file the text file containing the model.
	 * @return an error message if an error occurs.
	 */
	public String readTextFile(File file){
		StringBuilder error = new StringBuilder();
		System.out.print("# Reading Text File: ");
		long time = System.currentTimeMillis();
		BufferedReader inputStream = null;
		StringBuilder theory = new StringBuilder();
		try {
			inputStream = 
					new BufferedReader(new FileReader(file));
			String str;
			while ((str = inputStream.readLine()) != null) {	
				theory.append(str + "\n");	                
			}
			processTheory(theory.toString());
			System.out.println(( System.currentTimeMillis()-time) + " ms.");
			return "";
		}catch(FileNotFoundException fe){
			error.append("Could not find file " + file);
			return error.toString();

		} catch (Exception e) {    	
			error.append("Could not load file " + file.getName());
			return error.toString();

		} finally {
			if (inputStream != null) {
				try {
					inputStream.close();
				} catch (Exception e) {
					error.append("Could not close file " + file);
					return error.toString();
				}
			}
		}
	}
}
