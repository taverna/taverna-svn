package net.sourceforge.taverna.scuflworkers.io;

import java.util.ArrayList;
import java.util.HashMap;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class verifies that basic Excel file parsing is working.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class ExcelFileReaderTest extends TestCase {

    public void testExecute() throws Exception{
    	System.out.println("ExcelFileReaderTest.testExecute() skipped");
    	
    	//FAILS - windows specific path c:/....
//        ExcelFileReader reader = new ExcelFileReader();
//        HashMap inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        inAdapter.putString("filename","C:/Documents and Settings/Mark/My Documents/projects/taverna1.0/contrib/test/src/etc/test.xls");
//        
//        HashMap outputMap =  (HashMap)reader.execute(inputMap);       
//        
//        assertTrue("The outputMap was null", outputMap != null);
//        assertTrue("The outputMap was empty", !outputMap.isEmpty());
//        
//        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
//        ArrayList dataArray = outAdapter.getArrayList("data");
//        assertNotNull("The dataArray was null", dataArray);
//        assertTrue("The dataArray was empty", !dataArray.isEmpty());
//        
//        ArrayList firstRow = (ArrayList)dataArray.get(0);
//        assertTrue("Unable to get String","ABC".equals((String)firstRow.get(0)));
//        assertTrue("Unable to get numeric value","123".equals((String)firstRow.get(1)));
//        
//        // retest the Excel parsing after identifying the column that has a date in it.
//        inAdapter.putString("dateIndexes","2");   
//        outputMap = (HashMap)reader.execute(inputMap);
//        outAdapter = new DataThingAdapter(outputMap);
//        dataArray = outAdapter.getArrayList("data");
//        firstRow = (ArrayList)dataArray.get(0);
//        
//        assertTrue("Unable to get date value","04/01/2005".equals((String)firstRow.get(2)));
        
    }

}
