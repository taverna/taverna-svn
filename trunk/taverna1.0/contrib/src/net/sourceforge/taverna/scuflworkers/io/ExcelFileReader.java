package net.sourceforge.taverna.scuflworkers.io;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

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
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class ExcelFileReader implements LocalWorker {

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

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
            POIFSFileSystem fs = new POIFSFileSystem(new FileInputStream(
                    this.filename));
            HSSFWorkbook wb = new HSSFWorkbook(fs);
            HSSFSheet sheet = wb.getSheetAt(sheetNum);

            if (sheet.getLastRowNum() == 0) {
                throw new Exception("The sheet:" + sheetNum
                        + " does not contain any rows.");
            }

            this.lastRow = sheet.getLastRowNum();

            HSSFRow firstRow = sheet.getRow(0);
            this.lastCol = firstRow.getLastCellNum();

            ArrayList currRowList = null;

            HSSFRow currRow = null;
            Iterator cellIt = null;
            HSSFCell currCell = null;
            String currCellVal = null;
            int cellType;

            Iterator it = sheet.rowIterator();

            while (it.hasNext()) {
                currRow = (HSSFRow) it.next();
                currRowList = new ArrayList();
                this.lastCol = currRow.getLastCellNum();
                for (int i = 0; i <= lastCol; i++) {

                    currCell = currRow.getCell((short) i);

                    if (currCell != null) {
                        cellType = currCell.getCellType();
                        
                        switch (cellType) {
                        case HSSFCell.CELL_TYPE_BOOLEAN: {

                            currRowList.add(String.valueOf(currCell
                                    .getBooleanCellValue()));
                            break;
                        }
                        case HSSFCell.CELL_TYPE_STRING: {
                            currRowList.add(currCell
                                    .getStringCellValue());
                            break;
                        }
                        case HSSFCell.CELL_TYPE_NUMERIC: {
                            
                            if (HSSFDateUtil.isCellDateFormatted(currCell)){ 
                                
                                Date date = HSSFDateUtil.getJavaDate(currCell.getNumericCellValue());
                            }else{
                                
                                double tempCellVal = currCell.getNumericCellValue();
                                Double dcurrCellVal = new Double(tempCellVal);
                    			String value = "";
                    			double valFloor = Math.floor(tempCellVal);
                    			if ((tempCellVal - valFloor) > 0) { // is value really a float?
                    				value = tempCellVal + "";
                    				currRowList.add(new Double(tempCellVal).toString());
                    			} else {
                    				value = (int) valFloor + "";
                    				currRowList.add(new Integer(new Double(tempCellVal).intValue()).toString());
                    			}
                    			
                            }
                            break;
                        } 
                        case HSSFCell.CELL_TYPE_FORMULA: 
                        case HSSFCell.CELL_TYPE_ERROR:
                        case HSSFCell.CELL_TYPE_BLANK:
                        default:{
                            currRowList.add("");
                            break;                        
                        }
                       }
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
    private String filename = null;

    private int sheetNum = 0;

    private boolean firstRowContainsColNames = true;

    int lastCol, lastRow;

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[] { "filename", "firstRowContainsColumnNames" };
    }

    /**
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[] { "'text/plain'" };
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