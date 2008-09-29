package net.sourceforge.taverna.scuflworkers.io;

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
 * @version $Revision: 1.3 $
 */
public class LocalCommandTest extends TestCase {

    public void testExecute() throws Exception{
        
        LocalCommand cmd = new LocalCommand();
        Map inputMap = new HashMap();
        
        //use dir if windows, others ls
        String command = "dir";
        String os = System.getProperty("os.name");
        if (os!=null && !os.startsWith("Windows")) {
        	command="ls";
        }
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("command",command);
        inAdapter.putStringArray("args",new String[]{"/B"});
         
        Map outputMap = cmd.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertNotNull("The output map was null", outputMap);
        assertTrue("The output map was empty", !outputMap.isEmpty());
        String result = outAdapter.getString("result");        
        assertNotNull("The result was null", result);
    }

}
