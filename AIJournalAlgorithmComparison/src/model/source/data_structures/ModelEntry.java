package model.source.data_structures;

import java.util.ArrayList;
import java.util.Iterator;

/**
 * The Class ModelEntry represents an entry of a fault mode within an FMEA.
 */
public class ModelEntry {
	
	/** The faultmodes. */
	public ArrayList<FaultMode> faultmodes = new ArrayList<FaultMode>();
	
	/** The observation_logic. */
	public String observation_logic;

	/* (non-Javadoc)
	 * @see java.lang.Object#toString()
	 */
	public String toString(){
		StringBuilder sb = new StringBuilder();
		Iterator<FaultMode> faultmode_iterator = faultmodes.iterator();
		while(faultmode_iterator.hasNext()){
			sb.append(faultmode_iterator.next().getId());
			if(faultmode_iterator.hasNext()){
				sb.append(",");
			}
		}
		sb.append("->");
		sb.append(observation_logic);
		sb.append(".\n");
		return sb.toString();
	}

}
