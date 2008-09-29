package net.sourceforge.taverna.scuflworkers.ncbi;

import java.net.MalformedURLException;
import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.w3c.dom.Element;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.io.TransmitterException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import junit.framework.TestCase;

/**
 * This class
 * 
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 */
public class PubMedEFetchWorkerTest extends AbstractXmlWorkerTest {

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
