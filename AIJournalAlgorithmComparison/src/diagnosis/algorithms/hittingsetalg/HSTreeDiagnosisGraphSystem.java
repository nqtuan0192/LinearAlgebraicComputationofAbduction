package diagnosis.algorithms.hittingsetalg;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

public class HSTreeDiagnosisGraphSystem extends HSDiagnosisGraphSystem{

	DiagnosisGraph dg; //= new DiagnosisGraph(hSD);
	protected Vector conflictSets;
	
    public HSTreeDiagnosisGraphSystem (Vector elements, Vector conflicts) {
		super();
		components = new Vector();
		Hashtable ht = new Hashtable();
		Object obj;
		Vector co;
		Enumeration e2;
		Vector co2;
		Enumeration e = elements.elements();
		while (e.hasMoreElements()) {
		    obj = e.nextElement();
		    ht.put(obj,new DiagnosisCompWrapper(obj));
		    components.addElement(ht.get(obj));
		}
		conflictSets = new Vector();
		e = conflicts.elements();
		while (e.hasMoreElements()) {
		    co = (Vector)e.nextElement();
		    e2 = co.elements();
		    co2 = new Vector();
		    while(e2.hasMoreElements()) {
			co2.addElement(ht.get(e2.nextElement()));
		    }
		    conflictSets.addElement(co2);
		}
    }
    
	public void computeHST(){
		dg = new DiagnosisGraph(this);
		dg.computeHST();

	}
	
	public SubSetContainer returnDiagnoses(){
		if(dg!=null){
			SubSetContainer sub = new SubSetContainer();
			sub.addAll(dg.returnDiagnoses());
			return sub;
		}
		return null;
	}

	
}
