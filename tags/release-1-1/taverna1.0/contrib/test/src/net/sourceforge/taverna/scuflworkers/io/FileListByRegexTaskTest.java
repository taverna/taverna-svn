package net.sourceforge.taverna.scuflworkers.io;

import java.util.HashMap;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import junit.framework.TestCase;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class FileListByRegexTaskTest extends TestCase {
    
    public void testExecute() throws Exception{
        FileListByRegexTask task = new FileListByRegexTask();
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String home = System.getProperty("user.home");
        inAdapter.putString("directory", home);
        inAdapter.putString("regex","brca2*");
        
        
        HashMap outputMap = (HashMap)task.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertFalse("The outputmap was empty",outputMap.isEmpty());
        String[] filelist = outAdapter.getStringArray("filelist");
        
        assertTrue("The filelist was empty ", filelist != null && filelist.length > 0);
        for (int i=0; i < filelist.length; i++){
            System.out.println(filelist[i]);
        }
        
        
    }

}
