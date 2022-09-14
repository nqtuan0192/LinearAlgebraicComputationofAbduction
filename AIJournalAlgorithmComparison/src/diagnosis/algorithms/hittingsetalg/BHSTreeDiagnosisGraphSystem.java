package diagnosis.algorithms.hittingsetalg;

import java.util.Vector;

public class BHSTreeDiagnosisGraphSystem extends HSDiagnosisGraphSystem{
	private BHSTree rootNode;
	
	public BHSTreeDiagnosisGraphSystem(Vector components, SubSetContainer conflictSets){
		super();
		this.components=components;
		this.conflictSets =conflictSets;
	}
	
	public void computeHST(){
		rootNode = new BHSTree(getConflictSets());
		diagnoses = rootNode.getMinimalHittingSet();
	}
	
	public SubSetTree getInternalTree(){
		return rootNode;
	}
}
