package diagnosis.evaluation;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashSet;

public class EvaluationSetUpArtificialEx {

	private String model;
	public HashSet<String> observations;
	private ArrayList<String> rules;
	public int hyp_size=0;
	public int eff_size=0;
	public int rule_size=0;

	public void processAritificalExample(String filename,String directory){
		model = new String();
		observations=new HashSet<>();
		rules = new ArrayList<>();

		parseArtificialExample(filename, directory);
		String lastRule = rules.get(rules.size()-1);
		String[] tokens = (lastRule.split("->"))[0].split(",");
		for(String token:tokens){
			token = token.trim();
			observations.add(token);
		}
		rules.remove(rules.size()-1);

		for(String rule:rules){
			model = model + rule + "\n";
		}
		rule_size = rules.size();
		determineNumberOfHypAndEffects();
	}
	
	private void determineNumberOfHypAndEffects(){
		HashSet<String> effects=new HashSet<>();
		HashSet<String> hyps = new HashSet<>();
		for(String rule:rules){
			String[] tokens = rule.split("->");
			String[] lhs = tokens[0].split(",");
			String rhs = tokens[1].replace(".", "");
			effects.add(rhs);
			for(String lhs_element:lhs){
				if(Character.isUpperCase(lhs_element.charAt(0))){
					hyps.add(lhs_element);
				}
				else{
					effects.add(lhs_element);
				}
			}
		}
		
		hyp_size = hyps.size();
		eff_size = effects.size();
				
	}

	private void parseArtificialExample(String filename, String directory){
		File inputFile = new File(filename);
		BufferedReader br = null;
		try {
			br = new BufferedReader(new FileReader(directory+inputFile));
			String currentLine;
			while ((currentLine = br.readLine()) != null) {
				rules.add(currentLine);
			}
		} catch (FileNotFoundException e) {
			System.out.println("File not found");
			e.printStackTrace();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}



	public String getModel() {
		return model;
	}

	public HashSet<String> getObservations() {
		return observations;
	}




}
