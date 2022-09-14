package diagnosis.algorithms.atms;

import java.util.LinkedList;

import diagnosis.algorithms.compiler.LObject;
import diagnosis.algorithms.compiler.LRule;
import diagnosis.algorithms.compiler.LSentence;

public class Converter4ATMS {

	public static LinkedList<LinkedList<String>> convert(LSentence result) {
		LinkedList<LRule> rules2 = result.rules;
		LinkedList<LRule> rules = new LinkedList<LRule>(rules2);
		LinkedList<LinkedList<String>> newList = new LinkedList<LinkedList<String>>();
		   
		for (LRule rule : rules) {
			LinkedList<String> newRule = new LinkedList<String>();

			// The first element is the head of the rule
						// Generally the parser allows constructs such as A,B->c|d. 
						// But the ATMS only does not deal with disjunctions of effects, thus we
						// are only interested in the first effect.
						newRule.add((rule.head.getFirst()).toString());
			
			// .. followed by the elements of the tail
			for (LObject o : rule.tail) {
				newRule.add(o.toString());
			}

			newList.add(newRule);
		}
		return newList;
	}

}
