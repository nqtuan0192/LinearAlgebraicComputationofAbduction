package diagnosis.data_structures;

import java.util.HashSet;
import java.util.Set;

public class Diagnosis {
	private String name;
	private Set<Hypothesis> hypotheses;
	private long probability;
	public String getName() {
		return name;
	}
	public void setName(String name) {
		this.name = name;
	}
	public Set<Hypothesis> getHypotheses() {
		return hypotheses;
	}
	public void setFailuremodes(Set<Hypothesis> hypotheses) {
		this.hypotheses = hypotheses;
	}
	
	public Set<String>  getHypothesisAsStrings(){
		Set<String> hypo = new HashSet<String>();
		for(Hypothesis hypothesis: hypotheses){
			hypo.add(hypothesis.toString());
		}
		return hypo;
	}
	
	public void setHypotheses(HashSet<Hypothesis> hypotheses) {
		this.hypotheses = hypotheses;
	}
	public long getProbability() {
		return probability;
	}
	
	
	/*Berechne mithilfe aller Hypothesen*/
	public void computeProbability(HashSet<Hypothesis> allHypotheses){
		
	}
	
	
	
}
