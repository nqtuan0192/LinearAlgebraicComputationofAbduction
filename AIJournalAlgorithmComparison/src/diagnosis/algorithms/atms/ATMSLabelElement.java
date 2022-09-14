package diagnosis.algorithms.atms;

import java.util.Set;

public class ATMSLabelElement {

		ATMSLabelElement prev;
		ATMSLabelElement next;
		Set<ATMSNode> elements;
		
		public ATMSLabelElement() {
			prev = null;
			next = null;
			elements = null;
		}
		
		
}
