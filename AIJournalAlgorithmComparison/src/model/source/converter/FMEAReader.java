package model.source.converter;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import model.source.data_structures.*;


// TODO: Auto-generated Javadoc
/**
 * The Class FMEAReader.
 */
public class FMEAReader {
	
	/** The Constant COMPONENT. */
	static final String COMPONENT = "component";
	
	/** The Constant EFFECT. */
	static final String EFFECT = "effect";
	
	/** The Constant FAILUREMODE. */
	static final String FAILUREMODE = "failure mode";
	
	/** The Constant FAULTMODE. */
	static final String FAULTMODE = "fault mode";
	
	/** The Constant seperator_and. */
	protected final static String seperator_and = ",";
	
	/** The Constant seperator_or. */
	protected final static String seperator_or = "|";
	
	/** The disjunctive_effects. */
	public static boolean disjunctive_effects = false;
	
	/** The component row. */
	protected int componentRow=-1;
	
	/** The effect row. */
	protected int effectRow =-1;
	
	/** The fault mode row. */
	protected int faultModeRow =-1;

	/** The faultmodes. */
	ArrayList<FaultMode> faultmodes = new ArrayList<FaultMode>();
	
	/** The entries. */
	public ArrayList<ModelEntry> entries = new ArrayList<>();
	
	/** The file. */
	FileInputStream file;

	/**
	 * Creates a new String, missing the replaceable character.
	 *
	 * @param name string to replace the chars.
	 * @return the string where all special characters have been replaced by
	 * underscore.
	 */
	protected static String replaceChars(String name){
		name = name.trim().replaceAll("[^\\w\\s]+", "").replaceAll(" +", " ").replaceAll(" ", "_");
		return name;
	}


	/**
	 * Prints the faultmodes ArrayList.
	 */
	public void printArrayList(){
		for(FaultMode fm: this.faultmodes){
			System.out.print(fm.getId() + "->");
			for(String e:fm.getEffects()){
				System.out.print(e+",");
			}
			System.out.println();
		}
	}



	/**
	 * Parses the effects from a string where the effects are contained
	 * separated by commas.
	 * @param effects string containing comma separated effects.
	 * @return a set containing the effects as strings.
	 */
	public static Set<String> parseEffectSet(String effects){
		Set<String> effectSet = new HashSet<String>();
		if(effects.contains(seperator_or)){
			disjunctive_effects = true;	
		}
		String regex = "([\\w\\d\\s]+)|([|])|([,])|([/(/)])";
		Matcher m = Pattern.compile(regex).matcher(effects);
		LinkedList<String> list_ori = new LinkedList();
		while (m.find()) {
			list_ori.add(m.group());
		}
		for(int i = 0; i<list_ori.size();i++){
			String list_element = list_ori.get(i).trim();
			if(!list_element.matches("[|]") && !list_element.matches("[,]") && !list_element.matches("^$") && !list_element.matches("[\\s]") &&!list_element.matches("[/(]") && !list_element.matches("[/)]")){

				String tmp = list_element.trim();
				list_element = list_element.replaceAll("[\\s+]", " ").replaceAll(" ", "_");	
				if(list_element.matches("^\\d.*$")){
					list_element = "effect_" +list_element	;
				}
				if(tmp.startsWith("-")){
					list_element = list_element.replaceFirst("_", "");
					list_element = "-" + list_element;
				}
				effectSet.add(list_element);
			}
		}

		return effectSet;

	}

	/**
	 * Parses the effect string.
	 *
	 * @param effects the effects
	 * @return the string
	 */
	public static String parseEffectString(String effects){
		StringBuilder result = new StringBuilder();
		String regex = "([\\w\\d\\s]+)|([|])|([,])|([/(/)])";
		Matcher m = Pattern.compile(regex).matcher(effects);
		LinkedList<String> list_ori = new LinkedList();
		LinkedList<String> list_copy = new LinkedList();
		while (m.find()) {
			list_ori.add(m.group());
		}

		for(int i = 0; i<list_ori.size();i++){
			String list_element = list_ori.get(i).trim();
			if(!list_element.matches("[|]") && !list_element.matches("[,]") && !list_element.matches("^$") && !list_element.matches("[\\s]") &&!list_element.matches("[/(]") && !list_element.matches("[/)]")){

				String tmp = list_element.trim();
				list_element = list_element.replaceAll("[\\s+]", " ").replaceAll(" ", "_");	
				if(list_element.matches("^\\d.*$")){
					list_element = "effect_" +list_element	;
				}
				if(tmp.startsWith("-")){
					list_element = list_element.replaceFirst("_", "");
					list_element = "-" + list_element;
				}
				//list_ori.set(i, list_element);
			}
			if(!list_element.matches("^$")){
				list_copy.add(list_element);
			}


		}

		for(String list_element:list_copy){
			result.append(list_element);//.append(" ");
		}


		return result.toString();

	}


	/**
	 * Return the ArrayList containing the fault modes.
	 * @return ArrayList containing the fault modes.
	 */
	public ArrayList<FaultMode> getFaultmodes() {
		return faultmodes;
	}

	/**
	 * Determines whether a fault mode (i.e. component-mode pair) already 
	 * consists in the ArrayList of fault modes.
	 * @param component string denoting the component.
	 * @param mode string denoting the mode
	 * @param faultmodes the ArrayList containing all fault modes.
	 * @return the FaultMode if it is contained in the ArrayList.
	 * 			<code> null </code> otherwise.
	 */
	protected FaultMode faultModeExists(String component, String mode, 
			ArrayList<FaultMode> faultmodes){
		String compTemp = replaceChars(component);
		String modeTemp = replaceChars(mode);
		for(FaultMode fm : faultmodes){			
			if(fm.getComponent().equals(compTemp) && fm.getMode().
					equals(modeTemp)){
				return fm;
			}
		}
		return null;
	}


}