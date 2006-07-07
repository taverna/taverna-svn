package net.sourceforge.taverna.scuflworkers.ncbi;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import org.w3c.dom.Element;

import junit.framework.TestCase;

/**
 * This class
 * 
 * Last edited by $Author: davidwithers $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.1 $
 */
public class NucleotideTinySeqXMLWorkerTest extends AbstractXmlWorkerTest {

    public void testExecute() throws Exception{
        LocalWorker worker = new NucleotideTinySeqXMLWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("id","NM_000059");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("outputText");
        assertNotNull("The results were null", results);
        System.out.println(results);
        
        this.writeFile("test_nuc_tinyseq.xml", results);
        Element root = this.parseXml(results);
        this.testXmlNotEmpty(root);


    }

}
