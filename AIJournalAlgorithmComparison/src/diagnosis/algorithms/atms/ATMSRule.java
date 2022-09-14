package diagnosis.algorithms.atms;

import java.beans.PropertyChangeSupport;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

public class ATMSRule {

	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	public static int propagateCounter = 0;

	
	// Instance variables
	public Set<ATMSNode> antecedence;
	public ATMSNode consequent;
	
	// Methods
	
	public ATMSRule() {
		super();
		antecedence = new LinkedHashSet<ATMSNode>();
		consequent = null;
	}
	
	public void consequent(ATMSNode node) {
		(node.inConsequent()).add(this);
		consequent = node;
	}
	
	public ATMSNode consequent() {
		return consequent;
	}
	
	public void addAntecedence(ATMSNode node) {
		(node.inAntecedence()).add(this);
		antecedence.add(node);
	}
	
	public Set<ATMSNode> antecedence() {
		return antecedence;
	}
	
	public String toString() {
		String str = new String();
		Iterator<ATMSNode> it = antecedence.iterator();
		if (it.hasNext())
			str = str + (it.next()).toString();
		while (it.hasNext()) {
				str = str + ", " + (it.next()).toString();
		}
		str = str + " -> " + (consequent().toString()) + ".";
		return str;
	}
	
	public void propagateLabelChange() {
		propagateCounter++;
		// Propagate label information through the ATMS
		ATMSLabel newLabel = weave();
		consequent().addToLabel(newLabel); // This method propagates new environments
	}
	
	public ATMSLabel weave() {
		ATMSLabel newLabel = new ATMSLabel();
		newLabel.addEnvironment(new LinkedHashSet<ATMSNode>());
		
		for (ATMSNode node : antecedence()) {
	        if ((node.label()).isEmpty())
	            return new ATMSLabel();
	        else {
	        	newLabel = newLabel.combine(node.label());
	        }
		}
			
		// Remove all nogoods from the label
		ATMSNode nogood = (consequent().atms).nogood();
		Set<Set<ATMSNode>> nogoodEnvs = (nogood.label()).environment();
		for (Set<ATMSNode> env: nogoodEnvs ) {
			newLabel.removeSupersets(env);
		}
		
		// Return the result
		return newLabel;
	}
}
