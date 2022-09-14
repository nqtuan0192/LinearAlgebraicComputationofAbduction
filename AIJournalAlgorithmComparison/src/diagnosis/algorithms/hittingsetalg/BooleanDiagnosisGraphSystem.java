package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

public class BooleanDiagnosisGraphSystem extends HSDiagnosisGraphSystem{
	
	public BooleanDiagnosisGraphSystem(Vector components, SubSetContainer conflictSets){
		super();
		this.components=components;
		this.conflictSets =conflictSets;
	}
	
	public void computeHST(){
		//TODO
	    BooleanSubSetContainer cp = (BooleanSubSetContainer)conflictSets.cloneCopy();
	    cp.h_function();
	    diagnoses=cp;
	    System.out.println("***************");
	}


}
