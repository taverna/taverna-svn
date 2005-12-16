package net.sourceforge.taverna.scuflworkers.io;

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
 * @version $Revision: 1.2 $
 */
public class LocalCommandTest extends TestCase {

    public void testExecute() throws Exception{
        
        LocalCommand cmd = new LocalCommand();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("command","dir");
        inAdapter.putStringArray("args",new String[]{"/B"});
         
        Map outputMap = cmd.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertNotNull("The output map was null", outputMap);
        assertTrue("The output map was empty", !outputMap.isEmpty());
        String result = outAdapter.getString("result");
        System.out.println("result: " + result);
        assertNotNull("The result was null", result);
    }

}
