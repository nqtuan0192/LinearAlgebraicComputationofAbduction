package diagnosis.algorithms.hittingsetalg;

import java.util.Enumeration;
import java.util.Vector;

public abstract class HSDiagnosisGraphSystem implements DiagnosisGraphSystem{
	protected Vector components;
	protected long TCONFLICTS = 0;
	protected SubSetContainer conflictSets;
	protected SubSetContainer diagnoses;
	
	
	public Vector diagnosisComponents() {
		return getComponents();
	}

	public Vector computeConflicts(Vector hittingset) {
		long tstart = System.currentTimeMillis();
		Enumeration eco = getConflictSets().elements();
		while (eco.hasMoreElements()) {
			Vector co = (Vector)eco.nextElement();
			if (!hasIntersection(co,hittingset)) {
				TCONFLICTS = TCONFLICTS + 
				(System.currentTimeMillis() - tstart);
				return co;
			}
		}
		TCONFLICTS = TCONFLICTS + (System.currentTimeMillis() - tstart);
		return null;
	}

	public boolean hasIntersection(Vector v1, Vector v2) {
		Enumeration e = v1.elements();
		while (e.hasMoreElements()) {
			if (v2.contains(e.nextElement())) {
				return true;
			}
		}
		return false;
	}

	public int maxDiagnosisSize() {
		if (getComponents().size() > 3) {
			return 3;
		} else {
			return getComponents().size();}
	}

	public int maxNumberOfDiagnoses() {
		return 1000;
	}

	
	
	public Vector getComponents() {
		return components;
	}

	public void setComponents(Vector components) {
		this.components = components;
	}

	public SubSetContainer getConflictSets() {
		return conflictSets;
	}

	public void setConflictSets(SubSetContainer conflictSets) {
		this.conflictSets = conflictSets;
	}	
	
	public long getTCONFLICTS() {
		return TCONFLICTS;
	}

	public void setTCONFLICTS(long tCONFLICTS) {
		TCONFLICTS = tCONFLICTS;
	}

	public SubSetContainer getDiagnoses() {
		return diagnoses;
	}


	public SubSetContainer returnDiagnoses(){
		return diagnoses;
	}
	
	public void setDiagnoses(SubSetContainer diagnoses) {
		this.diagnoses = diagnoses;
	}


	
}
