package diagnosis.data_structures;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.Iterator;

public class ExploreClause {
	private static String NOT = "~";
	private static String OR = "|";
	public HashSet<String> positive_literals = new HashSet<>();
	public HashSet<String> negative_literals= new HashSet<>();
	
	public ExploreClause(HashSet<String> positive, HashSet<String> negative){
		this.positive_literals = positive;
		this.negative_literals = negative;
	}
	
	public ExploreClause() {
		// TODO Auto-generated constructor stub
	}

	public HashSet<String> negativeLiteralsWithUnaryOp(){
		HashSet<String> negative_literals_with_op = new HashSet<>();
		for(String literal:negative_literals){
			negative_literals_with_op.add(NOT+literal);
		}
		return negative_literals_with_op;
	}
	
	public void addPositiveLiteral(String literal){
		positive_literals.add(literal);
	}
	
	public void addNegativeLiteral(String literal){
		if(literal.startsWith(NOT)){
			literal = literal.replace(NOT, "");
		}
		negative_literals.add(literal);
	}
	
	public void addNegativeLiterals(HashSet<String> neg_literals){
		for(String literal:neg_literals){
				if(literal.startsWith(NOT)){
					literal = literal.replace(NOT, "");
				}
				negative_literals.add(literal);
		}
	}
	
	public void addPositiveLiterals(HashSet<String> pos_literals){
		positive_literals.addAll(pos_literals);
	}
		
	
	
	public String getNegativeLiteralsAsStrings(){
		StringBuilder result = new StringBuilder();
		Iterator<String> iterator_negative = negative_literals.iterator();
		while(iterator_negative.hasNext()){
			result.append(NOT+iterator_negative.next());
			if(iterator_negative.hasNext()){
				result.append(OR);
			}
		}
		return result.toString();
	}
	
	public String getPositiveLiteralsAsStrings(){
		StringBuilder result = new StringBuilder();
		Iterator<String> iterator_positive = positive_literals.iterator();
		while(iterator_positive.hasNext()){
			result.append(iterator_positive.next());
			if(iterator_positive.hasNext()){
				result.append(OR);
			}
		}
		return result.toString();
	}
	
	public String toString(){
		if(positive_literals.size()>0 || negative_literals.size()>0){
			StringBuilder result = new StringBuilder("(");
			result.append(getPositiveLiteralsAsStrings());
			if(negative_literals.size()>0){
				if(positive_literals.size()>0){
					result.append(OR);
				}
				result.append(getNegativeLiteralsAsStrings());
			}
			result.append(")");
			return result.toString();
		}

		return "";
	}
	
	 @Override
	    public int hashCode() {
	        final int prime = 31;
	        int result = 1;
	        result = prime * result + positive_literals.hashCode() + negative_literals.hashCode();  
	        return result;
	    }
	 
	    //Compare only account numbers
	    @Override
	    public boolean equals(Object obj) {
	        if (this == obj)
	            return true;
	        if (obj == null)
	            return false;
	        if (getClass() != obj.getClass())
	            return false;
	        ExploreClause other = (ExploreClause) obj;
	        if (!other.positive_literals.containsAll(this.positive_literals)){
	        	return false;
	        }
	        if (!other.negative_literals.containsAll(this.negative_literals)){
	        	return false;
	        }
	        return true;
	    }
	    
	    
	
}
