package diagnosis.algorithms.hittingsetalg;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Vector;

/**
 * This class implements a DiagnosisGraphSystem.
 *
 * @version 1.0, Date 15.03.2000
 * @author Franz Wotawa
 * @see DiagnosisGraphSystem
 * @see DiagnosisGraph
 */
public class CollectionGraphSystem extends Object 
implements DiagnosisGraphSystem {

    public static char[][][] TESTCASES = {
	{{'a'},{'b'},{'c'},{'d'}},
	{{'a','b'},{'c','d'}},
	{{'a','b','c','d'}},
	{{'a','b'},{'a','c'},{'a','d'}},
	{{'a','b'},{'b','c'},{'c','d'}},
	{{'a','b'},{'a'},{'c','d'}},
	{{'a'},{'a','b'},{'c','d'}},
	{{'a','b','c'}, {'c','d','a'}, {'a'}},
	{{'a','b','c'}, {'c','d','a'}},
	{{'a', 'b'}, {'a', 'b', 'd'}, {'c', 'b', 'a'}},
        {{'a','b'}, {'c','b'},{'c','d'},{'e','d'}},
    {{'a','c'},{'a','b','d'}}
    };

    protected Vector components;
    protected Vector conflictSets;

    CollectionGraphSystem (Vector elements, Vector conflicts) {
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

    public static void main(String args[]) {
	System.out.println("Test started");

	CollectionGraphSystem gsys;
	DiagnosisGraph dg;
	Vector el = new Vector();
	Vector conf;
	Vector tmp;

	el.addElement(new Character('a'));
	el.addElement(new Character('b'));
	el.addElement(new Character('c'));
	el.addElement(new Character('d'));
        el.addElement(new Character('e'));

	for (int k=0; k<TESTCASES.length; k++) {

	    System.out.println("TEST" + (new Integer(k)).toString());

	    conf = new Vector();
	    System.out.print("[ ");
	    for(int i=0; i<TESTCASES[k].length; i++) {
		tmp = new Vector();
		System.out.print("[");
		for (int j=0; j<TESTCASES[k][i].length; j++) {
		    Character ch = new Character(TESTCASES[k][i][j]);
		    System.out.print(ch.toString() + " ");
		    tmp.addElement(ch);
		}
		System.out.print("] ");
		conf.addElement(tmp);
	    }
	    System.out.println("]");

	    gsys = new CollectionGraphSystem(el,conf);
	    dg = new DiagnosisGraph(gsys);
	    dg.computeHST();

	    System.out.println("Diagnoses = ");
	    System.out.println((dg.returnDiagnoses()).toString());
	    System.out.println("Tree = ");
	    System.out.println(dg.toString());
	}

	System.out.println("Test finished");
    }

    public Vector diagnosisComponents() {
	return components;
    }

    public Vector computeConflicts(Vector hittingset) {
	Enumeration eco = conflictSets.elements();
	while (eco.hasMoreElements()) {
	    Vector co = (Vector)eco.nextElement();
	    if (!hasIntersection(co,hittingset)) {
		return co;
	    }
	}
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
	return components.size();
    }

    public int maxNumberOfDiagnoses() {
	return 1000;
    }
}
