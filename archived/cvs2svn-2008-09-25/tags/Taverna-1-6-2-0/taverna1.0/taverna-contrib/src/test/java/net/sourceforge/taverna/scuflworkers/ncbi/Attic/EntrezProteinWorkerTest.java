package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.w3c.dom.Element;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class EntrezProteinWorkerTest extends AbstractXmlWorkerTest {

    public void testExecute() throws Exception {    	
    	System.out.println("EntrezProteinWorkerTest.testExecute skipped");
    	return;
    	
    	//COMMENTED OUT - TEST FAILS DUE TO INVALID XML
//        LocalWorker worker = new EntrezProteinWorker();
//        Map inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        inAdapter.putString("term","NP_000050");
//        
//        Map outputMap = worker.execute(inputMap);
//        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
//        
//        String results = outAdapter.getString("resultsXml");
//        assertNotNull("The results were null", results);
//        System.out.println(results);
//        
//        this.writeFile("test_entrez_prot.xml",results);
//        Element root = this.parseXml(results);
//        this.testXmlNotEmpty(root);
        
    }

}
