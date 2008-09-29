package net.sourceforge.taverna.scuflworkers.xml;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.scuflworkers.ncbi.AbstractXmlWorkerTest;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinGBSeqWorker;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Test;

/**
 * 
 * @author Mark
 */
public class XQueryWorkerTest extends AbstractXmlWorkerTest {
      
	@Test
    public void testExecute() throws Exception{
    	// fetch a protein sequence needed for testing.
        LocalWorker protworker = new ProteinGBSeqWorker();
        
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("id","NP_000050");
        
        Map outputMap = protworker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);       
        String results = outAdapter.getString("outputText");              
        
        // create an XQueryWorker and run a query
        LocalWorker worker = new XQueryWorker();
        Map xqInputMap = new HashMap();
        DataThingAdapter xqInAdapter = new DataThingAdapter(xqInputMap);
        
        xqInAdapter.putString("script","//GBReference");
        xqInAdapter.putString("inputdocText",results);
        
        Map xqOutput = worker.execute(xqInputMap);
        DataThingAdapter xqOutAdapter = new DataThingAdapter(xqOutput);
        
        String xqResults = outAdapter.getString("outputText");
        assertNotNull("The results were null", xqResults);        
        
        this.writeFile("test_xquery.xml", xqResults);
        
    }

}
