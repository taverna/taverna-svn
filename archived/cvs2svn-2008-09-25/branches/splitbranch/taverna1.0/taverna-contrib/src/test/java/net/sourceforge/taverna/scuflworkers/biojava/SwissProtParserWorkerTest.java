package net.sourceforge.taverna.scuflworkers.biojava;

import java.util.HashMap;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import junit.framework.TestCase;

/**
 * This class tests the SwissProtParserWorker by parsing a SwissProt 
 * test file.
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class SwissProtParserWorkerTest extends TestCase {

    public void testExecute() throws Exception{
    	
    	System.out.println("SwissProtParserWorkerTest.testExecute() skipped");
    	
    	//FAILS - windows specific path
//        HashMap inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        inAdapter.putString("fileUrl","C:/Documents and Settings/Mark/My Documents/projects/taverna/contrib/test/src/etc/AAC4_HUMAN.sp");       
//       
//        SwissProtParserWorker worker = new SwissProtParserWorker();
//        HashMap outputMap = (HashMap)worker.execute(inputMap);
//        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
//              
//        String results = outAdapter.getString("results");
//        assertNotNull("The results were null",results);        
    }

}
