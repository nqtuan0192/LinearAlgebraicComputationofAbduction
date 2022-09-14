package diagnosis.algorithms.atms;

import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.Map;
import java.util.Set;

public class ATMSTextInterface {

	public ATMSystem atms;
	public Map<String,ATMSNode> nodeDict;
	
	public static String FALSE = "FALSE";
	
	public ATMSTextInterface () {
		atms = new ATMSystem();
		nodeDict = new LinkedHashMap<String,ATMSNode>();
		nodeDict.put(ATMSTextInterface.FALSE, atms.nogood());
	}
	
	static public ATMSTextInterface create(LinkedList<LinkedList<String>> list) {
		String cons;
		Set<String> tail;
		
		ATMSTextInterface newAtms = new ATMSTextInterface();
		for (LinkedList<String> el:  list) {
			cons = el.get(0);
			tail = new LinkedHashSet<String>(el.subList(1,el.size()));
			newAtms.addRule(tail, cons);
		}
		return newAtms;
	}
	
	public void addRule(Set<String> ant, String cons) {
		ATMSRule rule = new ATMSRule();
		ATMSNode node;
		
		for (String str : ant) {
			if (str.equalsIgnoreCase("FALSE")) {
				node = nodeDict.get(ATMSTextInterface.FALSE);
			} else if (nodeDict.containsKey(str)) {
				node = nodeDict.get(str);
			} else {
				if (Character.isUpperCase(str.charAt(0))) {
					node = ATMSNode.newAssumption(str);
				} else {
					node = ATMSNode.newProposition(str);
				}
				nodeDict.put(str, node);
				atms.addNode(node);
			}
			rule.addAntecedence(node);
		}
		
		if (cons.equalsIgnoreCase("FALSE")) {
			node = nodeDict.get(ATMSTextInterface.FALSE);
		} else if (nodeDict.containsKey(cons)) {
			node = nodeDict.get(cons);		
		} else {
			if (Character.isUpperCase(cons.charAt(0))) {
				node = ATMSNode.newAssumption(cons);
			} else {
				node = ATMSNode.newProposition(cons);
			}
			nodeDict.put(cons, node);
			atms.addNode(node);
		}
		rule.consequent(node);
		atms.addRule(rule);
	}
	
	public String statusAsString() {
		String str = new String();
		for (ATMSNode node :  atms.nodes) {
			str = str + node + ": " + node.label() + "\n";
		}
		return str;
	}
}
