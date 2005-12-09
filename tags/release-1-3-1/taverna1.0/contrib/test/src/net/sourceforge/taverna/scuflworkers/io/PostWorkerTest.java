package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class verifies that we can post to a URL and get a result back.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class PostWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("url","http://www.ncbi.nlm.nih.gov");
        String[] paramNames = new String[]{};
        String[] paramValues = new String[]{};
        inAdapter.putStringArray("paramnames",paramNames);
        inAdapter.putStringArray("paramvalues",paramValues);
        
        PostWorker worker = new PostWorker();
        
        Map outputMap = worker.execute(inputMap);
        assertNotNull("The output map was null",outputMap);
        assertTrue("The output map was empty",!outputMap.isEmpty());
        
    }

}
