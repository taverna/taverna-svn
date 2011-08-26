package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Ignore;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Mark
 */
public abstract class PubMedESearchWorkerTest extends AbstractXmlWorkerTest {

	@Ignore("Integration test")
	@Test	
    public void testExecute() throws Exception{
        LocalWorker worker = new PubMedESearchWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("term","metastasis AND gene expression");
        inAdapter.putString("retmax","4");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("outputText");
        assertNotNull("The results were null", results);        
               
        this.writeFile("test_pubmed_esearch.xml", results);
        Element root = this.parseXml(results);
        this.testXmlNotEmpty(root);

    }

}
