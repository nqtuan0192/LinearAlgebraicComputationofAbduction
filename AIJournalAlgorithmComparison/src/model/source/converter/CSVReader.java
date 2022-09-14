package model.source.converter;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.Reader;
import java.util.List;
import java.util.Set;

import model.source.data_structures.*;
import support.Debugger;

import org.apache.commons.csv.CSVFormat;
import org.apache.commons.csv.CSVParser;
import org.apache.commons.csv.CSVRecord;



// TODO: Auto-generated Javadoc
/**
 * The Class CSVReader.
 */
public class CSVReader extends FMEAReader {
	
	private String COMPONENT = "Component";
	private String FAILUREMODE = "Failure Mode";
	private String FAULTMODE = "Fault Mode";
	private String EFFECT = "Effects";

	/**
	 * Reads a given CSV file and generates the fault modes and stores in an
	 * ArrayList.
	 * @param file Excel file containing FMEA records.
	 * @return error message if an error occurred, the empty string otherwise.
	 */
	public String readFile(File file) {
		Reader in;
		StringBuilder error = new StringBuilder("");
		try {
			Debugger.log(1,"# \t Read CSV File " + file.getName() +".\n");		
			in = new FileReader(file);

			Debugger.log(1,"# \t Parsing CSV file: ");
			long time = System.currentTimeMillis();
			CSVParser parser = new CSVParser(in, CSVFormat.RFC4180);
			List<CSVRecord> records = parser.getRecords();
			Debugger.log(1,( System.currentTimeMillis()-time) + " ms.\n");

			/*first row*/
			CSVRecord firstrow = records.get(0);

			for(int index = 0; index < firstrow.size();index++){
				String column_header = firstrow.get(index).toLowerCase();
				if(column_header.contains(COMPONENT)){
					componentRow = index;
				}
				if(column_header.contains(FAILUREMODE) || 
						column_header.contains(FAULTMODE)){
					faultModeRow = index;
				}
				if(column_header.contains(EFFECT)){
					effectRow = index;
				}
			}
			if(componentRow<0 || faultModeRow < 0 || effectRow < 0){
				StringBuilder sb = new StringBuilder("The file "+ 
						file.getName() +" does not contain the following " +
						"column headers: ");

				if(componentRow < 0){
					sb.append(" Component ");
				}
				if(faultModeRow < 0){
					sb.append(" Faultmode ");
				}
				if(effectRow < 0){
					sb.append(" Effects ");
				}

				error.append(sb.toString() + "\n");
				return error.toString();
			}
			else{
				createFaultMode(records);
			}
			return "";

		} catch (FileNotFoundException e1) {
			// TODO Auto-generated catch block
			error.append("File "+ file.getPath() +" not found.\n");
			return error.toString();
		} catch (IOException e) {
			// TODO Auto-generated catch block
			error.append("Error while handling file "+ file.getPath()+ "\n");
			return error.toString();
		}

	}

	/**
	 * Generates fault modes corresponding to a list of CSV records. For each 
	 * row, where all the necessary cells are available, a fault mode is 
	 * generated, if it is not yet contained in the ArrayList of fault modes.
	 *
	 * @param records the records
	 */
	private void createFaultMode(List<CSVRecord> records){
		Debugger.log(1,"# \t Creating fault modes: ");
		long time = System.currentTimeMillis();
		for (int index2 = 1; index2 < records.size(); index2++) {			
			CSVRecord record = records.get(index2);
			if(!(record.get(componentRow).equals("")) && 
					!(record.get(faultModeRow).equals("")) && 
					!(record.get(effectRow).equals(""))){
				ModelEntry entry = new ModelEntry();

				String componentStr = replaceChars(record.get(componentRow));
				String faultmode = replaceChars(record.get(faultModeRow));
				Set<String> effectSet = parseEffectSet(record.get(effectRow));	
				if(faultModeExists(componentStr,faultmode,faultmodes)==null){
					FaultMode fm = new FaultMode();
					fm.setComponent(componentStr);
					fm.setMode(faultmode);
					fm.setObservations_logic(parseEffectString(record.get(effectRow).toLowerCase()));
					fm.setEffects(effectSet);
					fm.setId("Mode("+componentStr+","+faultmode+")");							
					fm.setOriginal_mode(faultmode);
					fm.setOriginal_component(componentStr);
					faultmodes.add(fm);

					entry.faultmodes.add(fm);
					entry.observation_logic=fm.getObservations_logic();
				}
				entries.add(entry);
			}

		}
		Debugger.log(1,System.currentTimeMillis()-time +" ms.\n");

	}

}
