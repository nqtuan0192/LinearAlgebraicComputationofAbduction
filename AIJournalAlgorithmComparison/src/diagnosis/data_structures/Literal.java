package diagnosis.data_structures;

public class Literal {
	public String name;
	
	public Literal() {
		this.name ="";
	}
	
	public Literal(String name) {
		this.name =name;
	}
	
	public String toString(){
		return this.name;
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
	public boolean equals(Object literal){
		Literal l = (Literal)literal;
		if(l.name.toLowerCase().equals(name.toLowerCase())){
			return true;
		}
		return false;
	}
	
}
