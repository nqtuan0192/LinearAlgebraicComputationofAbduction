package diagnosis.algorithms.hittingsetalg;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import diagnosis.algorithms.atms.ATMSLabel;

import java.util.Vector;
import java.util.Set;

import support.SortedIntList;


public class HSAlgorithms {
	
	private static boolean debug=false;
	private MinHittingSets minHittingSets;
	private BHSTreeDiagnosisGraphSystem bHSTree;
	private BooleanDiagnosisGraphSystem booleanHS;
	private HSTreeDiagnosisGraphSystem hSTree;
	private BergeAlgorithm berge;
	
	public final static int REITER = 3;
	public final static int BHSTREE = 1;
	public final static int BOOLEAN = 2;
	public final static int HSTREE = 4;
	public final static int BERGE = 5;
	
    
	private Vector<String> components;
	private SubSetContainer conflictSets;
	private ArrayList<SortedIntList> conflictSetsInt;
	private Map<String,Integer> map;
	
	private ArrayList<ArrayList<String>> diagnoses;
	
	public double deltaTime= -1;
	

	
	/*return HS*/
	public void computeHS(int algorithm, Set<Set<String>> conflicts) throws Exception{
		double start;
		if(algorithm == this.REITER) {
			//convert to SortedIntList
			this.conflictSetsInt = convertSetToList(conflicts);
			start =  System.nanoTime();
			computeHSReiter();
			deltaTime = (System.nanoTime()-start)/1000000;
			
		} else if(algorithm == this.BERGE) {
			ArrayList<ArrayList<String>> lists = new ArrayList<>();
			for(Set<String> set: conflicts){
				ArrayList<String> list = new ArrayList<>(set.size());
				list.addAll(set);
				lists.add(list);
			}
			start =  System.nanoTime();
			berge = new BergeAlgorithm();
			diagnoses = berge.computeHS(lists);
			deltaTime = (System.nanoTime()-start)/1000000;
		
		}else { 
			//create COMP,Conflicts
			this.components = new Vector<String>();
			this.conflictSets = createConflictSet(conflicts);
/*			components.addElement("a");
			components.addElement("b");
			components.addElement("c");
			components.addElement("d");
			components.addElement("e");
			components.addElement("f");
			components.addElement("g");
			components.addElement("h");
			components.addElement("i");
			components.addElement("j");*/


			switch(algorithm){
				case BHSTREE:
					//PlutoPlaysWithBHSTreesAndHittingSets bHSTreeT = new PlutoPlaysWithBHSTreesAndHittingSets(components, conflictSets);
					//bHSTreeT.computeHST();
					//System.out.println(" BHSTREE" + bHSTreeT.returnDiagnoses().toString());
					//diagnoses=convertSubSetContainerToArrayListArrayList(bHSTreeT.returnDiagnoses());
					
					bHSTree = new BHSTreeDiagnosisGraphSystem(components, conflictSets);
					start = System.nanoTime();
					bHSTree.computeHST();
					deltaTime = (System.nanoTime()-start)/1000000;
					
					//System.out.println(" BHSTREE" + bHSTree.returnDiagnoses().toString());
					diagnoses = convertSubSetContainerToArrayListArrayList(bHSTree.returnDiagnoses());
					break;
				case BOOLEAN:
					//PlutoPlaysWithBooleanHittingSetAlgorithms booleanHSB = new PlutoPlaysWithBooleanHittingSetAlgorithms(components, conflictSets);
					//booleanHSB.computeHST();
					//System.out.println(" BOOLEAN" + booleanHSB.returnDiagnoses().toString());
					//diagnoses=convertSubSetContainerToArrayListArrayList(booleanHSB.returnDiagnoses());
					
					
					booleanHS = new BooleanDiagnosisGraphSystem(components, conflictSets);
					start = System.nanoTime();
					booleanHS.computeHST();
					deltaTime = (System.nanoTime()-start)/1000000;
				
					
					
					System.out.println(" BOOLEAN" + booleanHS.returnDiagnoses().toString());
					diagnoses = convertSubSetContainerToArrayListArrayList(booleanHS.returnDiagnoses());
					break;
				case HSTREE:
					CollectionGraphSystem hSD = new CollectionGraphSystem(components,conflictSets);
					DiagnosisGraph dg = new DiagnosisGraph(hSD);
					
					booleanHS = new BooleanDiagnosisGraphSystem(components, conflictSets);
					start = System.nanoTime();
					dg.computeHST();
					deltaTime = (System.nanoTime()-start)/1000000;
					diagnoses=  convertVectorVectorToArrayListArrayList(dg.returnDiagnoses());

					break;
						
			}
		}
		
	}

	
	private void computeHSReiter(){	
		 minHittingSets = new MinHittingSets(false, this.conflictSetsInt);
		 minHittingSets.compute(100, 100);
         
		 
		 
		 diagnoses = convertIntToString();
         
         boolean minimal = minHittingSets.checkMinimalityHS();
         if (minimal) System.out.println("MINIMAL!");
         else System.out.println("NOT MINIMAL!");

         boolean hitsAllCS = minHittingSets.hitsAllConflictSets();
         if (hitsAllCS) System.out.println("OK, hits all conflict sets");
         else System.out.println("ERROR: does not hit all conflict sets!");
		
	}
	
 
	
	/*Convert ATMS label / oder ATMSLabel.toSet in subsetcontainer + vector elements*/
	private SubSetContainer createConflictSet(Set<Set<String>> conflictSets){
		SubSetContainer conflicts = new SubSetContainer();
		Iterator<Set<String>> iterator = conflictSets.iterator();
		while(iterator.hasNext()){
			Set<String> conflictSet = iterator.next(); 
			SubSet cs = new SubSet();
			Iterator<String> iterator2 = conflictSet.iterator();
			while(iterator2.hasNext()){
				String conflict = iterator2.next();
				cs.add(conflict);
				if(!this.components.contains(conflict)){
					this.components.add(conflict);
				}
			}
			conflicts.add(cs);
		}	
		return conflicts;
	}
	
	
	private ArrayList<ArrayList<String>> convertVectorVectorToArrayListArrayList(Vector<Vector<DiagnosisCompWrapper>> container){
		ArrayList<ArrayList<String>> list = new ArrayList<ArrayList<String>>();
		for(Vector<DiagnosisCompWrapper> vector:container){
			ArrayList<String> sublist = new ArrayList<>();
			for(DiagnosisCompWrapper dcw:vector){
				sublist.add(dcw.toString());
			}
			list.add(sublist);
		}
		
		return list;
		
	}
	
	
	
	/*Map Set<Set> to ArrayList<Sortedintlist>*/
	private ArrayList<SortedIntList> convertSetToList(Set<Set<String>> conflictSets){
		this.map = new HashMap<String,Integer>();
		int counter = 0;
		ArrayList<SortedIntList> conflicts = new ArrayList<SortedIntList>();
		Iterator<Set<String>> iterator = conflictSets.iterator();
		while(iterator.hasNext()){
			Set<String> conflictSet = iterator.next(); 
			SortedIntList cs = new SortedIntList();
			Iterator<String> iterator2 = conflictSet.iterator();
			while(iterator2.hasNext()){
				String conflict = iterator2.next();
				//Integer intConflict = conflict.hashCode();
				//map.put(counter++, conflict);
				if(!map.containsKey(conflict)){
					map.put(conflict,counter);
					cs.add(counter);
					debugPrintln(conflict + " ->" + counter);
					counter++;
				}
				else{
					debugPrintln(conflict + " ----->" + map.get(conflict));
					cs.add(map.get(conflict));				
				}			
			}
			conflicts.add(cs);
		}	
		return conflicts;
	}
	
	private ArrayList<ArrayList<String>> convertSubSetContainerToArrayListArrayList(SubSetContainer container){
		ArrayList<ArrayList<String>> diagnoses = new ArrayList<ArrayList<String>>();
		Iterator<SubSet> iterator = container.iterator();
		while(iterator.hasNext()){
			SubSet conflictSet = iterator.next(); 
			ArrayList<String> diagnosis = new ArrayList<String>();
			Iterator<String> iterator2 = conflictSet.iterator();
			while(iterator2.hasNext()){
				String conflict = iterator2.next();
				diagnosis.add(conflict);
			}
			diagnoses.add(diagnosis);
		}	
		return diagnoses;	
	}
	
	
	public int returnDiagnosesSize(){
		if(diagnoses!=null)
			return diagnoses.size();
		else
			return -1;
	}
	
	
	
	/*Map to Array<Sortedintlist>*/
	private ArrayList<ArrayList<String>> convertIntToString(){
        Iterator itHS = minHittingSets.getMinHSAsIntLists().iterator();
        ArrayList<ArrayList<String>> diagnoses = new ArrayList<ArrayList<String>>();
        while(itHS.hasNext()) {
            debugPrintln("-----------------------------------***");
            SortedIntList hs = (SortedIntList)itHS.next();
            ArrayList<String> subset= new ArrayList<String>();
            Iterator itInt = hs.iterator();
            while(itInt.hasNext()) {
                Integer n = (Integer)itInt.next();
                //debugPrint(n.intValue() + " ");
                if(map.containsValue(n)){
                	String key =getKeyByValue(n);
                	if(key!=null){
                		debugPrint(n.intValue() + " " + key );
                		subset.add(key);
                	}
                }
            }
            diagnoses.add(subset);
            debugPrintln("");
        }
		return diagnoses;
	}
	
	

	
	private String getKeyByValue(int value) {
	    for (Entry<String, Integer> entry : map.entrySet()) {
	        if (value ==(entry.getValue().intValue())) {
	            return entry.getKey();
	        }
	    }
	    return null;
	}

    public ArrayList<ArrayList<String>> returnDiagnoses(){
    	if(diagnoses!=null)
    		return this.diagnoses;
    	else{
    		debugPrintln("No diagnoses set available");
    		return null;
    	}
    }
    
    
	public static void setDebugEnabled(boolean enabled){
		debug=enabled;
	}

	protected void debugPrintln(String s){
		if(debug){
			System.out.println(s);
		}
	}
	
	protected void debugPrint(String s){
		if(debug){
			System.out.print(s);
		}
	}

	public void printConflictSets(){
		debugPrintln("--------------- CONFLICT SETS INT ---------------");
		if(this.conflictSetsInt!=null){
			Iterator<SortedIntList> i1 = this.conflictSetsInt.iterator();
			while(i1.hasNext()){
				Iterator<Integer> i2 = (i1.next()).iterator();
				debugPrint("{");
				while(i2.hasNext()){
					debugPrint(i2.next().toString());
					if(i2.hasNext())
						debugPrint(",");
				}
				debugPrint("}");
				debugPrintln("");
			}
		}
		else if(this.conflictSets!=null){
			conflictSets.toString("(",")","[", "]",true,",");
		}
		else
			debugPrintln("No Conflict Set available");
	}
	
	public void printMap(){
		debugPrintln("--------------- MAP INT STRING ---------------");
		if(this.map!=null){
			Set<String> keys = map.keySet();
			Iterator<String> i = keys.iterator();
			while(i.hasNext()){
				String key = i.next();
				debugPrintln(key + " - " + map.get(key));
			}
		}
		else
			debugPrintln("Map unavailable");
	}
	
	public String printDiagnoses(){
		String printDiagnoses ="";
		debugPrintln("--------------- DIAGNOSES ---------------");
		if(this.diagnoses!=null){
			for(int i = 0; i < diagnoses.size(); i++){
				for(int j = 0; j < diagnoses.get(i).size();j++){
					debugPrint( diagnoses.get(i).get(j) + " ");
					printDiagnoses = printDiagnoses + diagnoses.get(i).get(j) + " ";
				}
			 debugPrintln("");
			}
		}
		else{
			debugPrintln("Diagnosis unavailable");
		}
		return printDiagnoses;
	}


    public static void main(String[] args) throws Exception {
		Set<Set<String>> conflictsSets = new HashSet<Set<String>>();
		Set<String> s = new HashSet<String>();
		int index = 15;//10;
		char[][][] TESTCASES = {
				{{'a','c'},{'a','b','d'}},
		    	{{'a'},{'b'},{'c'},{'d'}},
		    	{{'a','f'},{'a','b','r'}},
		    	{{'a','b'},{'c','d'}},
		    	{{'a','c'},{'b','c'},{'b','d'},{'a','d'}},
		    	{{'a','b','c','d'}},
		    	{{'a','b'},{'a','c'},{'a','d'}},
		    	{{'a','b'},{'b','c'},{'c','d'}},
		    	{{'a','b'},{'a'},{'c','d'}},
		    	{{'a'},{'a','b'},{'c','d'}},
		    	{{'a','b','c'}, {'c','d','a'}, {'a'}},
		    	{{'a','b','c'}, {'c','d','a'}},
		    	{{'a', 'b'}, {'a', 'b', 'd'}, {'c', 'b', 'a'}},
		            {{'a','b'}, {'c','b'},{'c','d'},{'e','d'}},
		            {{'a'},{'a','b'},{'c'}},
		            {{'a','b'},{'d','f'},{'i','j'},{'b','c'}}, //err HSTREE
		            {{'a','b'},{'d','f'},{'e','g'},{'b','c'}} //kein err HSTREE
		        };
		
		
		for(int i = 0; i < TESTCASES.length;i++){
			for(int j = 0; j < TESTCASES[i].length; j++){
				for(int k = 0; k < TESTCASES[i][j].length; k++){
					System.out.print(" " + Character.toString(TESTCASES[i][j][k])+ " ,");
				}
				System.out.print("          ");
			}
			System.out.println();
		}
	
		

			conflictsSets = new HashSet<Set<String>>();
			for(int j = 0; j < TESTCASES[index].length; j++){
				s = new HashSet<String>();
				for(int k = 0; k < TESTCASES[index][j].length; k++){
					s.add(Character.toString(TESTCASES[index][j][k]));
				}
				conflictsSets.add(s);
			}

		
	

		
		Iterator<Set<String>> i1 = conflictsSets.iterator();
		while(i1.hasNext()){
			Iterator<String> i2 = ((HashSet<String>)(i1.next())).iterator();
			while(i2.hasNext()){
				System.out.print(i2.next() + ", ");
			}
			System.out.println();
		}
	
/*	
		HSAlgorithms hsAlgo3 = new HSAlgorithms();
		hsAlgo3.setDebugEnabled(true);
		hsAlgo3.computeHS(REITER, conflictsSets);
		hsAlgo3.printConflictSets();
		hsAlgo3.printDiagnoses();
		*/
		
		System.out.println("-----------------------------BOOLEAN--------------------------");
		HSAlgorithms hsAlgo4 = new HSAlgorithms();
		hsAlgo4.setDebugEnabled(true);
		hsAlgo4.computeHS(BOOLEAN, conflictsSets);
		hsAlgo4.printConflictSets();
		hsAlgo4.printDiagnoses();
	
		System.out.println("------------------------------HSTREE--------------------------");
		HSAlgorithms hsAlgo = new HSAlgorithms();
		hsAlgo.setDebugEnabled(true);
		hsAlgo.computeHS(HSTREE, conflictsSets);
		hsAlgo.printConflictSets();
		hsAlgo.printDiagnoses();

		System.out.println("----------------------------BHSTREE--------------------------");
		HSAlgorithms hsAlgo2 = new HSAlgorithms();
		hsAlgo2.setDebugEnabled(true);
		hsAlgo2.computeHS(BHSTREE, conflictsSets);
		hsAlgo2.printConflictSets();
		hsAlgo2.printDiagnoses();
	
	
	}

}
