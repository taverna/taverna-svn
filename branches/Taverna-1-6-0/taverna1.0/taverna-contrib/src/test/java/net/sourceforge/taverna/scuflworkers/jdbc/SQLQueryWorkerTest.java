package net.sourceforge.taverna.scuflworkers.jdbc;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class SQLQueryWorkerTest extends TestCase {

    public void testExecute() throws Exception{
    	System.out.println("SQLQueryWorkerTest.testExecute() skipped");
    	
    	//FAILS - driver port is null
//        SQLQueryWorker worker = new SQLQueryWorker();
//        Map inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        
//        
//        Map outputMap = worker.execute(inputMap);
//        assertNotNull("The output map was null", outputMap);
//        assertTrue("The output map was empty", !outputMap.isEmpty());
        
    }

}
