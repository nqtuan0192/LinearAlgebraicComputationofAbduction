/**
 * LogicParser: Implementation of a parser for logic programs
 *
 * @version 0.2, DATE: 30.12.1998
 * @author Franz Wotawa
 *
 * This class provides an implementation of a parser
 * for logic programs. It understands rules of the form
 * 
 * a1, .. ,an -> an+1. 
 * -> a.
 * an+1 :- an, .. ,a1.
 * a.
 *
 * V0.1: Implementing the basic functionality (29.12.1998)
 * V0.2: Adding LObject support (30.12.1998)
 */

package diagnosis.algorithms.compiler;

import java.util.LinkedList;

public class LogicParser extends GenericParser {

	// Instance creation and initialization

	public LogicParser() {
		scanner = defaultScanner();
		source = "";
		actualToken = null;
		result = null;
	}

	public LogicParser(String str) {
		scanner = defaultScanner();
		source = str;
		actualToken = null;
		result = null;
	}

	// Parsing

	public Object defaultResult() {
		return new LSentence();
	}

	// Private parsing

	public void parse() throws ParserErrorException {

		if (actualToken.isEOI()) {
			// do nothing
		} else {
			parseSentence();
			parse();
		}
	}

	public void parseSentence() throws ParserErrorException {
		LinkedList<LObject> tail = new LinkedList<LObject>();
		LinkedList<LObject>  head= new LinkedList<LObject>();
		
		if (actualToken.isDelimiter() && actualToken.equalValue("->")) {
			LPredicate predicate;
			predicate = parsePredicate();
			if (actualToken.isDelimiter() && actualToken.equalValue(",")) {
				head.add(predicate);
				head = parseConsequenceRest(head);
				nextToken();
				if (actualToken.isDelimiter()
						&& actualToken.equalValue(".")) {
					((LSentence) result).addRule(new LRule(tail, head));
					nextToken();
				} else {
					errorDetected("'.' expected");
				}
			}
			/*nextToken();
			head = parsePredicate();
			if (actualToken.isDelimiter() && actualToken.equalValue(".")) {
				((LSentence) result).addRule(new LRule(tail, head));
				nextToken();
			} else {
				errorDetected("'.' expected");
			}*/
		} else {
			LPredicate pred;
			pred = parsePredicate();
			if (actualToken.isDelimiter() && actualToken.equalValue(",")) {
				tail.add(pred);
				tail = parseAntecedenceRest(tail);
				if (actualToken.isDelimiter() && actualToken.equalValue("->")) {
					nextToken();
					LPredicate pred2;
					pred2= parsePredicate();
					head.add(pred2);
					head = parseConsequenceRest(head);
					if (actualToken.isDelimiter()
							&& actualToken.equalValue(".")) {
						((LSentence) result).addRule(new LRule(tail, head));
						nextToken();
					} else {
						errorDetected("'.' expected");
					}
				}
			
			} else if (actualToken.isDelimiter() && actualToken.equalValue(".")) {
				head.add(pred);
				head = parseConsequenceRest(head);
				((LSentence) result).addRule(new LRule(tail,head));
				nextToken();
			} else if (actualToken.isDelimiter()
					&& actualToken.equalValue("->")) {
				nextToken();
				tail.add(pred);
				head.add(parsePredicate());
				head = parseConsequenceRest(head);
				if (actualToken.isDelimiter() && actualToken.equalValue(".")) {
					((LSentence) result).addRule(new LRule(tail, head));
					nextToken();
				} else {
					errorDetected("'.' expected");
				}
			} else {
				errorDetected("',', ':-', '->', or '.' expected");
			}
		}
	}

	public LPredicate parsePredicate() throws ParserErrorException {
		LPredicate pred = new LPredicate();
		if (actualToken.isIdentifier() || actualToken.isString()) {
			pred.identifier = actualToken.value();
			nextToken();
			if (actualToken.isDelimiter() && actualToken.equalValue("(")) {
				nextToken();
				pred.arguments = parseArguments(new LinkedList<LObject>());
				if (actualToken.isDelimiter() && actualToken.equalValue(")")) {
					nextToken();
				} else {
					errorDetected("')' expected");
				}
			}
		} else {
			errorDetected("Identifier or string expected");
		}
		return pred;
	}

	public LinkedList<LObject> parseArguments(LinkedList<LObject> v) throws ParserErrorException {
		if (actualToken.isDelimiter() && actualToken.equalValue(")")) {
			return v;
		}
		while (true) {
			v.add(parseFunction());
			if (actualToken.isDelimiter() && actualToken.equalValue(",")) {
				nextToken();
			} else {
				return v;
			}
		}
	}

	public LObject parseFunction() throws ParserErrorException {
		LObject obj = null;

		if (actualToken.isIdentifier() || actualToken.isString()
				|| actualToken.isCharacter()) {
			String val;
			if (actualToken.isIdentifier()) {
				val = actualToken.value();
			} else if (actualToken.isString()) {
				val = "\"" + actualToken.value() + "\"";
			} else {
				val = "'" + actualToken.value() + "'";
			}
			nextToken();
			if (actualToken.isDelimiter() && actualToken.equalValue("(")) {
				nextToken();
				obj = new LFunction(val, parseArguments(new LinkedList<LObject>()));
				if (actualToken.isDelimiter() && actualToken.equalValue(")")) {
					nextToken();
				} else {
					errorDetected("')' expected");
				}
			} else {
				if (Character.isUpperCase(val.charAt(0))) {
					obj = new LVariable(val);
				} else {
					obj = new LConstant(val);
				}
			}
		} else if (actualToken.isFloat() || actualToken.isInteger()
				|| actualToken.isCharacter()) {
			String val;
			if (actualToken.isCharacter()) {
				val = "'" + actualToken.value() + "'";
			} else {
				val = actualToken.value();
			}
			obj = new LConstant(val);
			nextToken();
		} else {
			errorDetected("Identifier, string, float, integer, or character expected");
		}
		return obj;
	}

	public LinkedList<LObject> parseAntecedence(LinkedList<LObject> v) throws ParserErrorException {
		LPredicate pred;
		pred = parsePredicate();
		v.add(pred);
		if (actualToken.isDelimiter() && actualToken.equalValue(",")) {
			v = parseAntecedenceRest(v);
		}
		return v;
	}

	public LinkedList<LObject> parseAntecedenceRest(LinkedList<LObject> v) throws ParserErrorException {
		while (true) {
			LPredicate pred;
			if (actualToken.isDelimiter() && actualToken.equalValue(",")) {
				nextToken();
				pred = parsePredicate();
				v.add(pred);
			} else {
				return v;
			}
		}
	}
	
	public LinkedList<LObject> parseConsequenceRest(LinkedList<LObject> v) throws ParserErrorException {
		while (true) {
			LPredicate pred;
			if (actualToken.isDelimiter() && actualToken.equalValue("|")) {
				nextToken();
				pred = parsePredicate();
				v.add(pred);
			} else {
				return v;
			}
		}
	}

}
