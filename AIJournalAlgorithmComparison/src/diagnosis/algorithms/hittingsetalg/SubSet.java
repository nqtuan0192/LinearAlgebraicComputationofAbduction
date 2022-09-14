package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

/**
 * Class describing a collecton of objects. An object can only occur 
 * once in a SubSet.
 * @author fry
 */
public class SubSet extends Vector {
	
/************************************************
 * 			MEMBER VARIABLES
 ************************************************/

	static protected boolean debug = false;
   
/************************************************
 * 				CONSTRUCTORS
 ************************************************/
	//none for now

/************************************************
 * 				METHODS	
 ************************************************/
	
	/*
	 * Return a new SubSet containing all the elements of both this and the other subset. 
	 */
	public SubSet combineWith(SubSet otherSubSet){
		SubSet from =null, to=null, tmp=new SubSet();
		if(size() >= otherSubSet.size()){
			from =this;
			to =otherSubSet;
		}else{
			from = otherSubSet;
			to = this;
		}
		tmp.addAll(to);
		to = tmp;

		int nb = to.size();
		if (nb == 0) nb =1; //later we substract 1, has to result in 0....
		for (int i=0;i<from.size();i++){
			debugPrint("SubSet.combineWith(): Checking " + to.toString() + " for " + from.get(i));
			if(!to.contains(from.get(i), 0, nb-1)){
				to.add(from.get(i));
				debugPrint("SubSet.combineWith(): Not There. Added " + from.get(i) + ". 'to' is now containing:" + to.toString());
			}
		}
		return to; 
	} 
	
	/*
	 * General list method that checks whether certain object is contained in  this SubSet with index
	 * within the range ( <startindex>, <endindex> )
	 */
	public boolean contains(Object o, int startindex, int endindex){
		if(size()==0) return false;
		debugPrint("Indexes: " + startindex + "  " + endindex + "   size: " + size());
		if(startindex > endindex || endindex >= size()) 
			throw new IllegalArgumentException("Illegal Index in SubSet.contains(object,int,int), "
					+ "indexes bad: " + startindex + "/"  + endindex);
		for(int i=startindex; i<=endindex;i++){
			if(get(i).equals(o)) return true;
		}
		return false;
	}
	
	public boolean equals(Vector v){
		return (size() == v.size() && containsAll(v));
	}
	
/************************************************
 * 			OTHER METHODS
 ************************************************/

	/*
	 * Set debug-messages
	 */
	public static void setDebugEnabled(boolean enabled){
		debug = enabled;
	}

	public void debugPrint(String s){
		if(debug) System.out.println(s);
	}

	protected String toString(String open, String close){
	 	StringBuffer s = new StringBuffer(open);
	 	for(int i = 0; i<size()-1; i++){
	 		s.append(get(i));
			s.append(",");
	 	}
	 	if(size() != 0)
			s.append(lastElement());
		s.append(close);
	 	return s.toString();
	 }

/*	 public String toString(){
		 return toString("(", ")");
	 }
*/
}
