package diagnosis.algorithms.atms;

import java.beans.PropertyChangeSupport;
import java.util.LinkedHashSet;
import java.util.Set;

public class ATMSNode {

	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	// Instance variables
	
	public String identifier;
	public ATMSLabel label;
	public Set<ATMSRule> inAntecedence;
	public Set<ATMSRule> inConsequent;
	public boolean isAssumption;
	public ATMSystem atms = null;
	
	private static String NOGOOD = "NOGOOD";
	
	// Methods
	
	public ATMSNode (String name) {
		super();
		identifier = name;
		label = new ATMSLabel();
		inAntecedence = new LinkedHashSet<ATMSRule>();
		inConsequent = new LinkedHashSet<ATMSRule>();
		isAssumption = false;
	}
	
	public static ATMSNode newAssumption  (String name) {
		ATMSNode node = new ATMSNode(name);
		node.isAssumption = true;
		Set<ATMSNode> tmp = new LinkedHashSet<ATMSNode>();
		tmp.add(node);
		(node.label()).addEnvironment(tmp);
		return node;
	}
	
	public static ATMSNode newProposition (String name) {
		return new ATMSNode(name);
	}
	
	public static ATMSNode newNogood () {
		return new ATMSNode(NOGOOD);
	}
	
	public boolean isNogood() {
		return identifier == ATMSNode.NOGOOD;
	}
	
	public String identifier () {
		return identifier;
	}
	
	public ATMSLabel label() {
		return label;
	}
	
	public void atms(ATMSystem atmsLnk) {
		atms = atmsLnk;
	}
	
	public Set<ATMSRule> inAntecedence() {
		return inAntecedence;
	}
	
	public Set<ATMSRule> inConsequent() {
		return inConsequent;
	}
	
	public boolean isAssumption() {
		return isAssumption;
	}
	
	public boolean equals(ATMSNode node) {
		return identifier.equals(node);
	}
	
	public int hashCode() {
		return identifier.hashCode();
	}
	
	public String toString() {
		return identifier.toString();
	}
	
	// This method implements the propagation of new node labels.
	public void addToLabel(ATMSLabel newLabel) {
			
		ATMSLabel sLabel = new ATMSLabel();
		
		for (Set<ATMSNode> e: newLabel.environment()) {
			if (!(label.existSubset(e))) {
				sLabel.addEnvironment(e);
			}
		}

		if (sLabel.isEmpty()) return;
		
		for (Set<ATMSNode> e : label.environment()) {
			if (!(sLabel.existSubset(e))) 
				sLabel.addEnvironment(e);
		}

		label = sLabel;

		// Start propagation if I'm not a nogood. Otherwise, remove all new environments...
		
		if (isNogood()) {
			for (ATMSNode node : atms.nodes) {
				
				if (!node.isNogood()) {
					ATMSLabelElement el = label.first;
					while (el != null) {
						(node.label()).removeSupersets(el.elements);
						el = el.next;
					}
				}
			}
		} else {
			for (ATMSRule rule : inAntecedence) { 
				rule.propagateLabelChange();
			}
		}
	}
	
	// Other methods for label handling
	
	// Returns true if the node can be derived from envSet, and false otherwise.
	public boolean canBeDerived(Set<ATMSNode> envSet) {
		return label.existSubset(envSet);
	}
}
