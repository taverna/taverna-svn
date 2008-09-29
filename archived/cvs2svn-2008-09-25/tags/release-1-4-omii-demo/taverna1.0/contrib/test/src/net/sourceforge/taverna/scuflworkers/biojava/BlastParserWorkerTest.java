package net.sourceforge.taverna.scuflworkers.biojava;

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
 * @version $Revision: 1.3 $
 */
public class BlastParserWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("fileUrl","C:/Documents and Settings/Mark/My Documents/projects/taverna/contrib/test/src/etc/blast.txt");
        
       
        
        
        BlastParserWorker worker = new BlastParserWorker();
        Map outputMap = (HashMap)worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("blastresults");
        assertNotNull("The results were null",results);
        System.out.println("\n\nresults: \n"+results);
    }

}
