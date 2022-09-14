package model.source.converter;

import java.io.*;
import java.util.*;

import org.apache.poi.hssf.usermodel.*;
import org.apache.poi.openxml4j.exceptions.InvalidFormatException;
import org.apache.poi.openxml4j.opc.OPCPackage;
import org.apache.poi.poifs.filesystem.NPOIFSFileSystem;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.*;

import support.Debugger;
import support.ModelFileFilter;
import model.source.data_structures.*;



// TODO: Auto-generated Javadoc
/**
 * The Class ExelReader.
 */
public class ExelReader extends FMEAReader {

	/**
	 * Generates fault modes corresponding to an excel sheet. For each row, 
	 * where all the necessary cells are available, a fault mode is generated,
	 * if it is not yet contained in the ArrayList of fault modes.
	 * @param sheet an excel sheet, which contains FMEA records.
	 */
	protected void createFaultMode(Sheet sheet){
		Iterator<Row> rowIterator = sheet.iterator();
		while(rowIterator.hasNext()) {
			Row row = rowIterator.next();	

			// skip the first row
			if(row.getRowNum() > 0){	
				ModelEntry entry = new ModelEntry();
				if((row.getCell(this.effectRow, Row.RETURN_BLANK_AS_NULL)!=null) &&
						(row.getCell(this.componentRow, Row.RETURN_BLANK_AS_NULL)!=null)&&
						(row.getCell(this.componentRow, Row.RETURN_BLANK_AS_NULL)!=null)){
					String mode = returnCellValue(row,this.faultModeRow);
					String component = returnCellValue(row,this.componentRow);
					String effects = returnCellValue(row,this.effectRow);					
					FaultMode fm = faultModeExists(component,mode,this.faultmodes);
					if(fm!=null){
						Set<String> effectSet = fm.getEffects();
						Set<String> newEffects = parseEffectSet(effects);
						effectSet.addAll(newEffects);
						String logic = fm.getObservations_logic();
						logic += "," + parseEffectString(effects.toLowerCase());
						fm.setObservations_logic(logic);
					}
					else{
						FaultMode newFailureMode = new FaultMode();
						String newMode =replaceChars(mode);
						newFailureMode.setMode(newMode);	
						String newComp = replaceChars(component);
						newFailureMode.setComponent(newComp);
						newFailureMode.setId(createFaultModeId(newComp, newMode));										
						newFailureMode.setEffects(parseEffectSet(effects));		
						newFailureMode.setObservations_logic(replaceChars(effects.toLowerCase()));
						newFailureMode.setOriginal_component(component);
						newFailureMode.setOriginal_mode(mode);
						this.faultmodes.add(newFailureMode);
						//create entry
						entry.faultmodes.add(newFailureMode);
						entry.observation_logic = parseEffectString(effects.toLowerCase());

					}

				}
				entries.add(entry);
			}

		}
	}

	/**
	 * Generates a string representation for a fault mode, i.e. a component-
	 * mode pair.
	 * @param component string denoting the component.
	 * @param mode string denoting the mode.
	 * @return a string representing the fault mode.
	 */
	protected String createFaultModeId(String component, String mode){	
		return "Mode("+component+","+mode+")";
	}


	/**
	 * Parses the column description. If the first row contains a column header
	 * "Component", "Fault Mode" and "Effects" or some derivations of it, the
	 * corresponding member fields are set to the according column integer 
	 * value.
	 *
	 * @param sheet the sheet containing the FMEA records.
	 * @return <code> true </code> if all column headers where found.
	 * 			<code> false </code> otherwise.
	 * @throws NullPointerException the null pointer exception
	 */
	protected boolean parseColumnDescription(Sheet sheet) throws NullPointerException{
		Row descriptionRow = sheet.getRow(0);
		if(descriptionRow == null)
			return false;

		Iterator<Cell> cellIterator = descriptionRow.cellIterator();
		while(cellIterator.hasNext()) {
			Cell cell = cellIterator.next();
			if(cell !=null  && (cell.getCellType() == Cell.CELL_TYPE_STRING)){
				String columnName = cell.getStringCellValue().toLowerCase();
				if(columnName.contains(COMPONENT)){
					this.componentRow=cell.getColumnIndex();
				}
				else if(columnName.contains(EFFECT)){
					this.effectRow = cell.getColumnIndex();
				}
				else if(columnName.contains(FAILUREMODE) || 
						columnName.contains(FAULTMODE) || 
						columnName.contains(FAILUREMODE.replace(" ",""))|| 
						columnName.contains(FAULTMODE.replace(" ",""))){
					this.faultModeRow=cell.getColumnIndex();
				}
			}
		}

		if(this.faultModeRow<0 || this.componentRow<0 || this.effectRow<0)
			return false;
		return true;
	}


	/**
	 * Reads a given Excel file and generates the corresponding workbook
	 * instance. Subsequently, the fault modes are generated and stored in an
	 * ArrayList.
	 * @param file Excel file containing FMEA records.
	 * @return error message if an error occured, the empty string otherwise.
	 */
	public String readFile(File file){
		Debugger.log(1,"# Reading Excel file... \n");
		long time = System.currentTimeMillis();
		FileInputStream fileInputStream;
		try {
			NPOIFSFileSystem fs =null;
			OPCPackage pkg=null;
			fileInputStream = new FileInputStream(file);
			Workbook workbook;
			Debugger.log(1,"#\t Creating workbook: ");
			//Determine xls or xlsx and get the workbook instance for XLS file 
			time = System.currentTimeMillis();
			if(ModelFileFilter.getExtension(file).equals(ModelFileFilter.xlsx)){ 
				//workbook = new XSSFWorkbook(fileInputStream);
				pkg = OPCPackage.open(file);
				workbook = new XSSFWorkbook(pkg);				  
			}
			else{
				fs = new NPOIFSFileSystem(file);
				workbook = new HSSFWorkbook(fs.getRoot(), true);							
			}
			Debugger.log(1,(System.currentTimeMillis() -time) +" ms.\n");

			for(int index = 0; index < workbook.getNumberOfSheets();index++){
				Sheet sheet = workbook.getSheetAt(index);
				if(sheet.getLastRowNum()> 0){
					if(parseColumnDescription(sheet)){
						time = System.currentTimeMillis();
						createFaultMode(sheet);		
						Debugger.log(1, "#\t Creating fault modes: " + 
								(System.currentTimeMillis() -time) +" ms.\n");
					}
					else{
						StringBuilder errorOutput = new StringBuilder("Could " +
								"not find column(s): ");
						if(this.componentRow<0)
							errorOutput.append(" 'Component' ");
						if(this.faultModeRow<0)
							errorOutput.append(" 'Fault Mode' ");
						if(this.effectRow<0)
							errorOutput.append(" 'Effects' ");
						return errorOutput.toString();
					}

				}
			}

			if(fs!=null)
				fs.close();
			if(pkg!=null)
				pkg.close();


			if(faultmodes.size()<1){
				return "There are no component-fault mode pairs";
			}		
		} catch (FileNotFoundException e) {
			return "File " + file.getName() + " not found.";
		} catch (IOException e) {
			return "While handling file " + file.getName() + ".";
		} catch (InvalidFormatException e) {
			return "While handling file " + file.getName() + ".";
		}
		catch (Exception e) {
			return "While handling file " + file.getName() + ".";

		}
		return "";
	}



	/**
	 * Return the String value of a cell specified by a row and column. 
	 * @param row Row instance
	 * @param column integer representation of the column
	 * @return a string representing the cell content. The empty string is 
	 * returned in case the column does not contain a string value.
	 */ 
	protected String returnCellValue(Row row, int column){
		Cell cell = row.getCell(column, Row.RETURN_NULL_AND_BLANK);
		if( cell!=null && (cell.getCellType() == Cell.CELL_TYPE_STRING)){
			String cellvalue = cell.getStringCellValue();
			return cellvalue;
		}
		return "";
	}



}
