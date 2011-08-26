package net.sourceforge.taverna.scuflworkers.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFDateUtil;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.poifs.filesystem.POIFSFileSystem;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This class reads an Excel spreadsheet and creates an ArrayList of ArrayLists
 * containing string data. Note that Formula's are not currently evaluated and
 * thus are returned as empty strings.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class ExcelFileReader implements LocalWorker {
	
	private static Logger logger = Logger.getLogger(ExcelFileReader.class);

	String dateIndexes = null;

	List dateIndexArray = null;

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		dateIndexes = inAdapter.getString("dateIndexes");
		if (dateIndexes != null) {
			dateIndexArray = Arrays.asList(dateIndexes.split(","));
		}

		HashMap outputMap = new HashMap();

		this.filename = inAdapter.getString("filename");

		DataThing data = new DataThing(init());
		outputMap.put("data", data);

		return outputMap;
	}

	/**
	 * This method initialises the datasource using the Excel workbook.
	 * 
	 * @param dataObject
	 *            This parameter is assumed to be a string containing the fully
	 *            qualified filename for the Excel file.
	 */
	public ArrayList init() throws TaskExecutionException {
		ArrayList dataArray = new ArrayList();
		try {
			POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(this.filename));
			HSSFWorkbook wb = new HSSFWorkbook(fs);
			HSSFSheet sheet = wb.getSheetAt(sheetNum);

			// determine the number of rows
			this.lastRow = sheet.getLastRowNum();
			if (this.lastRow == 0) {
				throw new Exception("The sheet:" + sheetNum + " does not contain any rows.");
			}

			// determine the number of columns based on the first row of the
			// sheet
			HSSFRow firstRow = sheet.getRow(0);
			this.lastCol = firstRow.getLastCellNum();

			ArrayList currRowList = null;
			HSSFRow currRow = null;
			HSSFCell currCell = null;

			Iterator it = sheet.rowIterator();

			while (it.hasNext()) {
				currRow = (HSSFRow) it.next();

				currRowList = new ArrayList();
				this.lastCol = currRow.getLastCellNum();
				String currVal = null;
				for (int i = 0; i < lastCol; i++) {
					currCell = currRow.getCell((short) i);
					if (currCell != null) {
						logger.info(currRow.getRowNum() + ":" + currCell.getCellNum() + " ");
						currVal = getCellValue(currCell);
						logger.info(currVal);
						currRowList.add(currVal);
					} else {
						currRowList.add("");
					}

				}

				dataArray.add(currRowList);
			}
		} catch (NumberFormatException ex) {
			throw new TaskExecutionException(ex);
		} catch (FileNotFoundException e) {
			throw new TaskExecutionException(e);
		} catch (IOException e) {
			throw new TaskExecutionException(e);
		} catch (Exception e) {
			throw new TaskExecutionException(e);
		}

		return dataArray;
	}

	/**
	 * This method extracts the value from the cell.
	 * 
	 * @param cell
	 * @return
	 */
	private String getCellValue(HSSFCell cell) {
		String value = null;
		if (cell != null) {
			int cellType = cell.getCellType();

			switch (cellType) {
			case HSSFCell.CELL_TYPE_BOOLEAN: {
				value = String.valueOf(cell.getBooleanCellValue());
				break;
			}
			case HSSFCell.CELL_TYPE_STRING: {
				value = cell.getStringCellValue();
				break;
			}
			case HSSFCell.CELL_TYPE_NUMERIC: {
				double tempCellVal = cell.getNumericCellValue();
				if (dateIndexes != null) {
					int index = cell.getCellNum();
					String strIndex = String.valueOf(index);

					if (dateIndexArray.contains(strIndex)) {
						Date date = HSSFDateUtil.getJavaDate(tempCellVal);
						SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
						value = formatter.format(date);
						return value;
					}

				}
				if (HSSFDateUtil.isCellDateFormatted(cell)) {
					Date date = HSSFDateUtil.getJavaDate(tempCellVal);
					SimpleDateFormat formatter = new SimpleDateFormat("MM/dd/yyyy");
					value = formatter.format(date);
				} else {

					double valFloor = Math.floor(tempCellVal);
					if ((tempCellVal - valFloor) > 0) { // is value really a
						// float?
						value = new Double(tempCellVal).toString();
					} else {
						value = (int) valFloor + "";
						value = new Integer(new Double(tempCellVal).intValue()).toString();

					}

				}
				break;
			}
			case HSSFCell.CELL_TYPE_FORMULA:
			case HSSFCell.CELL_TYPE_ERROR:
			case HSSFCell.CELL_TYPE_BLANK:
			default: {
				value = "";
				break;
			}
			}
		} else {
			value = "";
		}
		return value;
	}

	private String filename = null;

	private int sheetNum = 0;

	int lastCol, lastRow;

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "filename", "firstRowContainsColumnNames", "dateIndexes" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {

		return new String[] { "data" };
	}

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "l(l('text/plain'))" };
	}

}