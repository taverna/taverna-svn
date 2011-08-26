package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;

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
public class FileListByExtTaskTest extends TestCase {

    public void testExecute() throws Exception{
        FileListByExtTask task = new FileListByExtTask();
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String home = System.getProperty("user.home");
        inAdapter.putString("directory", home);
        inAdapter.putString("extension","xml");
        
        
        HashMap outputMap = (HashMap)task.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertFalse("The outputmap was empty",outputMap.isEmpty());
        String[] filelist = outAdapter.getStringArray("filelist");
        
        assertTrue("The filelist was empty ", filelist != null || filelist.length > 0);
        for (int i=0; i < filelist.length; i++){
            System.out.println(filelist[i]);
        }
    }

}
