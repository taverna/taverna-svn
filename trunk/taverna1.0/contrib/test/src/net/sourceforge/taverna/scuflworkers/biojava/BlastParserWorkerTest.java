package net.sourceforge.taverna.scuflworkers.biojava;

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
public class BlastParserWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        HashMap inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("fileurl","C:/Documents and Settings/Mark/My Documents/projects/taverna/contrib/test/src/etc/blast.txt");
        
        HashMap outputMap = new HashMap();
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        BlastParserWorker worker = new BlastParserWorker();
        outputMap = (HashMap)worker.execute(inputMap);
        
        String results = outAdapter.getString("blastresults");
        assertNotNull("The results were null",results);
        System.out.println(results);
    }

}
