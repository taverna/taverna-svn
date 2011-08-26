package net.sourceforge.taverna.scuflworkers.ncbi;

import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.junit.Test;
import org.w3c.dom.Element;

/**
 * 
 * @author Mark
 */
public abstract class ProteinTinySeqXMLWorkerTest extends AbstractXmlWorkerTest {

	@Test
    public void testExecute() throws Exception{
        LocalWorker worker = new ProteinTinySeqXMLWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("id","NP_000050");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("outputText");
        assertNotNull("The results were null", results);        
               
        this.writeFile("test_prot_tinyseq.xml", results);
        Element root = this.parseXml(results);
        this.testXmlNotEmpty(root);

    }

}
