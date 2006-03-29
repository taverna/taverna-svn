package net.sourceforge.taverna.scuflworkers.biojava;

import java.util.HashMap;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

/**
 * This class tests the EMBLParserWorker by parsing a test file.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class EMBLParserWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("fileUrl","C:/Documents and Settings/Mark/My Documents/projects/taverna/contrib/test/src/etc/AY069118.em");       
       
        EMBLParserWorker worker = new EMBLParserWorker();
        HashMap outputMap = (HashMap)worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
              
        String results = outAdapter.getString("emblFile");
        assertNotNull("The results were null",results);
        

    }

}
