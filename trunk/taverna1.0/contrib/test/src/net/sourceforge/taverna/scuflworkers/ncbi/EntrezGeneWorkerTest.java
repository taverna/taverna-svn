package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import junit.framework.TestCase;
import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

/**
 * This class
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class EntrezGeneWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        LocalWorker worker = new EntrezGeneWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("term","brca2");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("resultsXml");
        assertNotNull("The results were null", results);
        System.out.println(results);

    }

}
