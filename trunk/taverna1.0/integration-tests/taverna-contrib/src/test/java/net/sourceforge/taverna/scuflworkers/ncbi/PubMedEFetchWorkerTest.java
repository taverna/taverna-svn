package net.sourceforge.taverna.scuflworkers.ncbi;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * @author Mark
 */
public abstract class PubMedEFetchWorkerTest extends AbstractXmlWorkerTest {

	@Test
    public void testExecute() throws Exception{
        LocalWorker worker = new PubMedEFetchWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("id","11700088,9997");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("outputText");
        assertNotNull("The results were null", results);        
               
        this.writeFile("test_pubmed_efetch.xml", results);
        Element root = this.parseXml(results);
        this.testXmlNotEmpty(root);


    }

}
