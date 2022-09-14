package diagnosis.data_structures;

import model.data_structures.FaultMode;

public class Hypothesis extends Literal{

	public String name;
	private long probability;
	
	public Hypothesis(String name){
		this.name = name;
	}
	
	public Hypothesis(){
		this.name = "";
	}
	
	public String toString(){
		return name;
	}

	public long getProbability() {
		return probability;
	}

	public void setProbability(long probability) {
		this.probability = probability;
	}
	
	public boolean equals(FaultMode fm){
		if(fm.getId().equals(name)){
			return true;
		}
		return false;
	}
	
	/**
	 * Compares whether a given Hypothesis is the same, i.e. has the same name.
	 */
	@Override
	public int hashCode() {
        int hash = 5;
        hash = 89  + hash + (this.name != null ? this.name.hashCode() : 0);
        return hash;
    }

	@Override
	public boolean equals(Object hyp){
		Hypothesis h = (Hypothesis)hyp;
		if(h.name.toLowerCase().equals(name.toLowerCase())){
			return true;
		}
		return false;
	}
	
	
	
	
}
