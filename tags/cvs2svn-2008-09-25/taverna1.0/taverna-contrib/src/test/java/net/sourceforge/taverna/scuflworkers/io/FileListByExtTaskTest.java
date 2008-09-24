package net.sourceforge.taverna.scuflworkers.io;

import java.io.File;
import java.util.HashMap;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * Test {@link FileListByExtTask}
 * 
 * @author Mark
 * @author Stian Soiland-Reyes
 */
public class FileListByExtTaskTest extends TestCase {

    public void testExecute() throws Exception{
        FileListByExtTask task = new FileListByExtTask();
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        File tempDir = File.createTempFile(getClass().getSimpleName(), "test");
        tempDir.delete();
        assertTrue("Could not create temporary directory" + tempDir, tempDir.mkdir());
        inAdapter.putString("directory", tempDir.getAbsolutePath());
        inAdapter.putString("extension","xml");
        
        File xmlFile = new File(tempDir, "fish.xml");
		assertTrue("Could not create file " + xmlFile, xmlFile.createNewFile());
        
        HashMap outputMap = (HashMap)task.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        assertFalse("The outputmap was empty",outputMap.isEmpty());
        String[] filelist = outAdapter.getStringArray("filelist");
        
        assertTrue("The filelist was null or unexpected length ", filelist != null && filelist.length == 1);        
        assertEquals("Did not list " + xmlFile, xmlFile.getAbsolutePath(), filelist[0]);
    }

}
