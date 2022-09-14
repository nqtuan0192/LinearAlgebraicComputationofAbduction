package diagnosis.algorithms.atms;

import java.beans.PropertyChangeListener;
import java.beans.PropertyChangeSupport;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/** The current version of the ATMS is not really efficient. Hence, improving efficiency is on
 * the todo list
 * @author fwotawa
 *
 */

public class ATMSystem {
	
	private PropertyChangeSupport changes = new PropertyChangeSupport( this );
	
	// Instance variables
	public Set<ATMSNode> nodes;
	public ATMSNode nogood;
	public Set<ATMSRule> rules;
	public double timeout;
	public boolean istimeout = false;
	
	// Methods
	
	public ATMSystem() {
		super();
		nodes = new LinkedHashSet<ATMSNode>();
		nogood = ATMSNode.newNogood();
		nogood.atms(this);
		nodes.add(nogood);
		rules = new LinkedHashSet<ATMSRule>();
	}
	
	public void addRule(ATMSRule rule) {
		rules.add(rule);
		rule.propagateLabelChange();
	}
	
	public void addNode(ATMSNode node) {
		node.atms(this);
		nodes.add(node);
	}
	
	public ATMSLabel label(ATMSNode node) {
		return node.label();
	}
	
	public ATMSNode nogood() {
		return nogood;
	}
	
	public ATMSLabel nogoodLabel() {
		return label(nogood());
	}
	
	public boolean isConsistent() {
		return label(nogood()).isEmpty();
	}
	
	public String toString() {
		String str = new String();
		for (ATMSNode node : nodes) {
			str = str + node + node.label() + "\n";
		}
		return str;
		
	}
	
	// Returns all assumptions used in the ATMS
	public Set<ATMSNode> allAssumptions() {
		LinkedHashSet<ATMSNode> result = new LinkedHashSet<ATMSNode>();

		for (ATMSNode node : nodes) {
			if (node.isAssumption()) {
				result.add(node);
			}
		}
		return result;
	}
	
	// Returns all nodes that are not assumptions
	public Set<ATMSNode> allNodesWithoutAssumptions() {
		LinkedHashSet<ATMSNode> result = new LinkedHashSet<ATMSNode>();
		for (ATMSNode node : nodes) {
			if (!node.isAssumption()) {
				if (!node.isNogood()) {
					result.add(node);
				}
			}
		}
		return result;
	}

	// Returns a likelihood value for a node in relationship to another node
	// Note this method can be used to compute the probability of explanations
	// All arguments must not be assumptions!
	public double likelihood(ATMSNode forNode, ATMSNode exNode) {
		double result = 0.0;
		Set<Set<ATMSNode>> deltaSet = (exNode.label()).environment(); 
				// The delta set comprises all environments
				// of the node to be explained, i.e., exNode
		Double obsNum = 0.0;
		
		for (Set<ATMSNode> env : deltaSet) {
			if (forNode.canBeDerived(env)) {
				obsNum++;
			}
		}
		result = obsNum / deltaSet.size();
		return result;
	}
	
	// Returns an array of string comprising the observations with their 
	// corresponding expected entropy
	public TreeSet<PropositionUtilityPair> computeObservations(ATMSNode exNode) {
		TreeSet<PropositionUtilityPair> resultList = new TreeSet<PropositionUtilityPair>(new ComparatorPropositionUtilityPair());
		
		for (ATMSNode forNode : allNodesWithoutAssumptions()) {
			double val = this.likelihood(forNode,exNode);
			double entropy;
			if ((val==0.0)||(val==1.0)) {
				entropy = 0.0;
				System.out.println("r  " + entropy);
			} else {
				entropy = (-1.0)*((val*Math.log(val)/Math.log(2.0)) + ((1.0-val)*(Math.log(1.0-val)/Math.log(2.0))));
				resultList.add(new PropositionUtilityPair(forNode,entropy));
				System.out.println("r  " + entropy);
			}
		}
		System.out.println("resultlist " + resultList.size()+ " -- " +  allNodesWithoutAssumptions().size());
		return resultList;
	}

	public String[] computeObservationList(ATMSNode exNode) {
		TreeSet<PropositionUtilityPair> resultList = computeObservations(exNode);
		String[] result = new String[resultList.size()];
		int i = 0;
		for (PropositionUtilityPair forNode : resultList) {
			result[i] = forNode.node.toString() + " [" + forNode.utility + "]";
			i = i + 1;
		}
		return result;
	}
	
	public HashMap<String,Double> computeObservationMap(ATMSNode exNode){
		TreeSet<PropositionUtilityPair> resultList = computeObservations(exNode);
		HashMap<String,Double> result = new HashMap<String,Double>();
		int i = 0;
		for (PropositionUtilityPair forNode : resultList) {
			result.put(forNode.node.toString(), Double.valueOf(forNode.utility));
			i = i + 1;
		}
		return result;
	}

	public void addPropertyChangeListener(PropertyChangeListener l){
		changes.addPropertyChangeListener(l);
	}
	
	
	public void setIstimeout(boolean istimeout) {
		this.istimeout = istimeout;
		changes.firePropertyChange("timeout", null, this.timeout);
	}

	
}
