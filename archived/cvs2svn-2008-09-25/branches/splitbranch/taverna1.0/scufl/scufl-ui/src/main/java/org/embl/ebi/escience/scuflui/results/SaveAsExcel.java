/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.results;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileOutputStream;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.prefs.Preferences;

import javax.swing.Icon;
import javax.swing.JComponent;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.apache.poi.hssf.usermodel.HSSFCell;
import org.apache.poi.hssf.usermodel.HSSFCellStyle;
import org.apache.poi.hssf.usermodel.HSSFRow;
import org.apache.poi.hssf.usermodel.HSSFSheet;
import org.apache.poi.hssf.usermodel.HSSFWorkbook;
import org.apache.poi.hssf.util.HSSFColor;
import org.embl.ebi.escience.baclava.BaclavaIterator;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.TavernaIcons;

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
	public ActionListener getListener(Map results, JComponent parent) {
		final Map resultMap = results;
		final JComponent parentComponent = parent;
		return new ActionListener() {
			HSSFWorkbook wb = null;

			HSSFSheet sheet = null;

			HSSFCellStyle[] styles = null;

			HSSFCellStyle headingStyle = null;

			private void setStyles() {
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
								int index = n + 2 * s + 4 * e + 8 * w;
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

			private HSSFCell getCell(int column, int row) {
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

			private void setStyle(int currentCol, int column, int row) {
				if (!hasValue(column, row)) {
					return;
				}
				HSSFCell cell = getCell(column, row);
				int n = 0;
				int s = 0;
				int w = 0;
				int e = 0;
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

			private boolean hasValue(int column, int row) {
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

			private boolean isTextual(Object o) {
				if (o instanceof String) {
					return true;
				} else if (o instanceof Collection) {
					Collection c = (Collection) o;
					if (c.isEmpty()) {
						return false;
					} else {
						return isTextual(c.iterator().next());
					}
				}
				return false;
			}

			public void actionPerformed(ActionEvent e) {
				try {
					wb = new HSSFWorkbook();
					setStyles();
					sheet = wb.createSheet("Workflow results");
					sheet.setDisplayGridlines(false);
					int currentCol = 0;
					for (Iterator i = resultMap.keySet().iterator(); i.hasNext();) {
						String resultName = (String) i.next();
						System.out.println("Output for : " + resultName);
						DataThing resultValue = (DataThing) resultMap.get(resultName);
						// Check whether there's a textual type
						
						boolean textualType = isTextual(resultValue.getDataObject());
						if (textualType) {
							System.out.println("Output is textual");
							getCell(currentCol, 0).setCellValue(resultName);
							getCell(currentCol, 0).setCellStyle(headingStyle);
							int setWidth = 1;
							int setHeight = 1;
							BaclavaIterator bi = resultValue.iterator("''");
							while (bi.hasNext()) {
								DataThing containedThing = (DataThing) bi.next();
								String containedValue = (String) containedThing.getDataObject();
								int columnOffset = 0;
								int rowOffset = 0;
								int[] location = bi.getCurrentLocation();
								if (location.length == 1) {
									rowOffset = location[0];
								} else if (location.length > 1) {
									rowOffset = location[location.length - 2];
									columnOffset = location[location.length - 1];
								}
								if ((columnOffset + 1) > setWidth) {
									setWidth = columnOffset + 1;
								}
								if ((rowOffset + 1) > setHeight) {
									setHeight = rowOffset + 1;
								}
								int baseRow = 1;
								getCell(currentCol + columnOffset, baseRow + rowOffset).setCellValue(containedValue);
								// setStyle(currentCol + columnOffset, baseRow +
								// rowOffset);
							}
							// Set the styles
							for (int x = currentCol; x < currentCol + setWidth; x++) {
								for (int y = 1; y < setHeight + 1; y++) {
									setStyle(currentCol, x, y);
								}
							}
							sheet.setColumnWidth((short) (currentCol + setWidth), (short) 200);
							currentCol += setWidth + 1;
						}

					}

					JFileChooser jfc = new JFileChooser();
					Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
					String curDir = prefs.get("currentDir", System.getProperty("user.home"));
					jfc.resetChoosableFileFilters();
					jfc.setFileFilter(new ExtensionFileFilter(new String[] { "xls" }));
					jfc.setCurrentDirectory(new File(curDir));
					jfc.setFileSelectionMode(JFileChooser.FILES_ONLY);
					int returnVal = jfc.showSaveDialog(parentComponent);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						// Build the string containing the XML
						// document from the datathing map
						File f = jfc.getSelectedFile();
						FileOutputStream fos = new FileOutputStream(f);
						wb.write(fos);
						fos.close();
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(parentComponent, "Problem saving results : \n" + ex.getMessage(),
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

}
