package net.sourceforge.taverna.scuflworkers.io;

import java.io.File;
import java.util.HashMap;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public abstract class VelocityFileWriterTest extends TestCase {
    
    VelocityFileWriter vfw = new VelocityFileWriter();
    
    public void execute() throws Exception{
       String homeDir = System.getProperty("user.home");
       File testFile = new File(homeDir, "test.txt");
       File template = new File(homeDir, "template.vm");
       
       HashMap inputMap = new HashMap();
       DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
       
       HashMap outputMap = new HashMap();
       DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
       
       inAdapter.putString("outputFile", testFile.getName());
       inAdapter.putString("template", template.getName());
       
       outputMap = (HashMap)vfw.execute(inputMap);
       
       assertTrue("The test file does not exist",testFile.exists());
       assertTrue("The test file is empty", testFile.length() > 0);
    }

}
