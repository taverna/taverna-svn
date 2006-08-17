package net.sourceforge.taverna.scuflworkers.jdbc;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class SQLQueryWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        SQLQueryWorker worker = new SQLQueryWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        
        
        Map outputMap = worker.execute(inputMap);
        assertNotNull("The output map was null", outputMap);
        assertTrue("The output map was empty", !outputMap.isEmpty());
        
    }

}
