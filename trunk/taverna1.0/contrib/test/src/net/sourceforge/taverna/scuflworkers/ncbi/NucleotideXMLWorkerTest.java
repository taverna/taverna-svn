package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import junit.framework.TestCase;

/**
 * This class tests the NucleotideXMLWorker.
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class NucleotideXMLWorkerTest extends TestCase {

    public void testExecute() throws Exception{
        LocalWorker worker = new NucleotideXMLWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("term","NM_000059");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("resultsXml");
        assertNotNull("The results were null", results);
        System.out.println(results);

    }

}
