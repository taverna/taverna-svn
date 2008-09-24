/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.beans.IntrospectionException;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.iterator.BaclavaIterator;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.actions.SaveWorkflowAction;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.spi.ResultMapSaveSPI;

/**
 * Uses the Apache POI to save the map of data things to an Excel format
 * document.
 * 
 * @author Tom Oinn
 */
public class SaveAsExcel implements ResultMapSaveSPI {

	/**
	 * Return the standard looking save to disk icon
	 */
	public Icon getIcon() {
		return TavernaIcons.saveIcon;
	}

	/**
	 * Get the description for this plugin
	 */
	public String getDescription() {
		return ("Saves textual (non image) data to Excel format.");
	}

	/**
	 * Get the short name
	 */
	public String getName() {
		return "Excel";
	}

	/**
	 * Show a standard save dialog and dump the entire result set to the
	 * specified XML file
	 */
	@SuppressWarnings("unchecked")
	public ActionListener getListener(Map results, JComponent parent) {
		// Assumes Map<String, DataThing> (which it should be)		
		return new SaveExcelAction(results, parent);
	}
}

class SaveExcelAction implements ActionListener {
	
	private static Logger logger = Logger.getLogger(SaveExcelAction.class);
	
	Map<String, DataThing> map;
	JComponent component;
	HSSFWorkbook wb = null;
	HSSFSheet sheet = null;
	HSSFCellStyle[] styles = null;
	HSSFCellStyle headingStyle = null;
	
	SaveExcelAction(Map<String, DataThing> map, JComponent component) {
		this.map = map;
		this.component = component;
	}

	public void actionPerformed(ActionEvent e) {
		try {
			generateSheet();	
			JFileChooser jfc = new JFileChooser();
			Preferences prefs = Preferences.userNodeForPackage(SaveWorkflowAction.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			jfc.resetChoosableFileFilters();
			jfc.setFileFilter(new ExtensionFileFilter(new String[] { "xls" }));
			jfc.setCurrentDirectory(new File(curDir));
			jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
			int returnVal = jfc.showSaveDialog(component);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				// Build the string containing the XML
				// document from the datathing map
				File f = jfc.getSelectedFile();
				saveSheet(f);
			}
		} catch (Exception ex) {
			logger.warn("Could not save to Excel", ex);
			JOptionPane.showMessageDialog(component, "Problem saving results : \n" + ex.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

	void setStyle(int currentCol, int column, int row) {
		if (!hasValue(column, row)) {
			return;
		}
		HSSFCell cell = getCell(column, row);
		int n=0, s=0, w=0, e=0;		
		if (row < 2 || !hasValue(column, row - 1)) {
			n = 1;
		}
		if (column == currentCol || !hasValue(column - 1, row)) {
			w = 1;
		}
		if (!hasValue(column, row + 1)) {
			s = 1;
		}
		if (!hasValue(column + 1, row)) {
			e = 1;
		}
		int index = n + 2 * s + 4 * e + 8 * w;
		cell.setCellStyle(styles[index]);
	}

	void setStyles() {
		headingStyle = wb.createCellStyle();
		headingStyle.setBorderTop(HSSFCellStyle.BORDER_THIN);
		headingStyle.setBorderBottom(HSSFCellStyle.BORDER_THIN);
		headingStyle.setBorderLeft(HSSFCellStyle.BORDER_THIN);
		headingStyle.setBorderRight(HSSFCellStyle.BORDER_THIN);
		headingStyle.setFillBackgroundColor(HSSFColor.LIGHT_YELLOW.index);
		headingStyle.setFillForegroundColor(HSSFColor.LIGHT_YELLOW.index);
		headingStyle.setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
		styles = new HSSFCellStyle[16];
		for (int n = 0; n < 2; n++) {
			for (int s = 0; s < 2; s++) {
				for (int e = 0; e < 2; e++) {
					for (int w = 0; w < 2; w++) {
						int index = n + 2*s + 4*e + 8*w;
						styles[index] = wb.createCellStyle();
						if (n == 1) {
							styles[index].setBorderTop(HSSFCellStyle.BORDER_THIN);
						} else {
							styles[index].setBorderTop(HSSFCellStyle.BORDER_NONE);
						}
						if (s == 1) {
							styles[index].setBorderBottom(HSSFCellStyle.BORDER_THIN);
						} else {
							styles[index].setBorderBottom(HSSFCellStyle.BORDER_NONE);
						}
						if (e == 1) {
							styles[index].setBorderRight(HSSFCellStyle.BORDER_THIN);
						} else {
							styles[index].setBorderRight(HSSFCellStyle.BORDER_NONE);
						}
						if (w == 1) {
							styles[index].setBorderLeft(HSSFCellStyle.BORDER_THIN);
						} else {
							styles[index].setBorderLeft(HSSFCellStyle.BORDER_NONE);
						}
						styles[index].setFillBackgroundColor(HSSFColor.GOLD.index);
						styles[index].setFillForegroundColor(HSSFColor.GOLD.index);
						styles[index].setFillPattern(HSSFCellStyle.SOLID_FOREGROUND);
					}
				}
			}
		}
	}

	/**
	 * Get a cell at the given coordinates, create it if needed.
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	HSSFCell getCell(int column, int row) {
		HSSFRow srow = sheet.getRow((short) row);
		if (srow == null) {
			srow = sheet.createRow((short) row);
		}
		HSSFCell scell = srow.getCell((short) column);
		if (scell == null) {
			scell = srow.createCell((short) column);
		}
		return scell;
	}

	/**
	 * Check if a cell has a value. 
	 * 
	 * @param column
	 * @param row
	 * @return
	 */
	boolean hasValue(int column, int row) {
		HSSFRow srow = sheet.getRow((short) row);
		if (srow == null) {
			return false;
		}
		HSSFCell scell = srow.getCell((short) column);
		if (scell == null) {
			return false;
		}
		return true;
	}

	/**
	 * Check if o is a String or contains elements that satisfy isTextual(o)
	 * <p>
	 * Traverse down the Collection o if possible, and check the tree of collection at the deepest level.
	 * </p>
	 * 
	 * @param o Object to check
	 * @return true if o is a String or is a Collection that contains a string at the deepest level. 
	 * false if o is not a String or Collection, or if it is a collection that contains non-strings.
	 * null if o is a Collection, but it is empty or contains nothing but Collections.    
	 * 
	 */
	Boolean isTextual(Object o) {
		if (o instanceof String) {
			// We dug down and found a string. Hurray!
			return true;
		} 
		if (o instanceof Collection) {			
			for (Object child : (Collection) o) {
				Boolean isTxt = isTextual(child);
				if (isTxt == null) {
					// Unknown, try next one
					continue;
				}
				return isTxt;				
			}
			// We looped through and found just empty collections 
			// (or we are an empty collection), we don't know			
			return null;
		}
		// No, sorry mate.. o was neither a String or Collection
		return false;
	}

	/**
	 * Generate the Excel sheet from the DataThing's in the map.
	 * 
	 * All of the results are shown in the same spreadsheet, but in 
	 * different columns. Flat lists are shown vertically, 2d lists
	 * as a matrix, and deeper lists are flattened to 2d. 
	 * 
	 * @throws IntrospectionException
	 * @throws NoSuchElementException
	 */
	void generateSheet() throws IntrospectionException, NoSuchElementException {
		wb = new HSSFWorkbook();
		setStyles();
		sheet = wb.createSheet("Workflow results");
		sheet.setDisplayGridlines(false);
		int currentCol = 0;
		for (String resultName : map.keySet()) {
			logger.debug("Output for : " + resultName);
			DataThing resultValue = map.get(resultName);
			// Check whether there's a textual type			
			Boolean textualType = isTextual(resultValue.getDataObject());
			if (textualType == null || !textualType) { 
				continue;
			}
			logger.debug("Output is textual");
			getCell(currentCol, 0).setCellValue(resultName);
			getCell(currentCol, 0).setCellStyle(headingStyle);
			int numCols = 1;
			int numRows = 1;
			int currentRow = 0;
			BaclavaIterator rows;
			try {			
				rows = resultValue.iterator("l('')");
			} catch (IntrospectionException ex) {
				// Not a list, single value. We'll fake the iterator
				DataThing fakeValues = new DataThing(Arrays.asList(resultValue.getDataObject()));
				rows = fakeValues.iterator("l('')");
			}
			// If we only have one row, we'll show each value on a new
			// row instead
			boolean isFlat = rows.size() == 1;
			while (rows.hasNext()) {				
				DataThing row = (DataThing) rows.next();		
				// Even increase first time, as we don't want to overwrite our header
				currentRow++;
				BaclavaIterator bi = row.iterator("''");
				while (bi.hasNext()) {
					DataThing containedThing = (DataThing) bi.next();
					String containedValue = (String) containedThing.getDataObject();
					int columnOffset = 0;					
					int[] location = bi.getCurrentLocation();					
					if (!isFlat && location.length > 0) {						
						columnOffset = location[location.length-1];
						numCols = Math.max(numCols, columnOffset + 1);					
					}							
					logger.debug("Storing in cell " + (currentCol+columnOffset) + 
						               " " + currentRow + ": " + containedValue);					
					getCell(currentCol + columnOffset, currentRow).setCellValue(containedValue);
					if (isFlat) {						
						currentRow++;
					}
				}								
			}			
			numRows = Math.max(numRows, currentRow);

			// Set the styles
			for (int x = currentCol; x < currentCol + numCols; x++) {
				for (int y = 1; y < numRows + 1; y++) {
					setStyle(currentCol, x, y);
				}
			}
			sheet.setColumnWidth((short) (currentCol + numCols), (short) 200);
			currentCol += numCols + 1;
		}
	}

	/**
	 * Save the generated worksheet to a file 
	 * 
	 * @param file to save to
	 * @throws FileNotFoundException
	 * @throws IOException
	 */
	void saveSheet(File file) throws FileNotFoundException, IOException {
		FileOutputStream fos = new FileOutputStream(file);
		wb.write(fos);
		fos.close();
	}
}
