package diagnosis.algorithms.atms;

public class PropositionUtilityPair {
	ATMSNode node;
	double utility;
	
	public PropositionUtilityPair(ATMSNode n, double u) {
		node = n;
		utility = u;
	}
	
	public boolean equals(PropositionUtilityPair pair) {
		if (node.equals(pair.node)) {
			if (utility == pair.utility) {
				return true;
			}
		}
		return false;
	}
	
	public int compare(PropositionUtilityPair pair) {
		if (equals(pair)) {
			return 0;
		} else {
			if (utility <= pair.utility) {
				return 1;
			} else {
				return -1;	
			}
		}
	}
	
	public String toString() {
		return node.toString() + " [" + utility + "]";
	}
}
