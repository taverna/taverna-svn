package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class EnvVariableWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        LocalWorker worker = new EnvVariableWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertFalse("The output map was empty",outputMap.isEmpty());
        assertTrue("The output map does not contain the results",outputMap.containsKey("properties"));
        
        String results = outAdapter.getString("properties");
        assertNotNull("The results were null",results);
        
        System.out.println("results: " + results);
        
        
    }

}
