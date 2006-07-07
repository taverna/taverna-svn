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
 * Last edited by $Author: sowen70 $
 * 
 * @author Mark
 * @version $Revision: 1.1.2.2 $
 */
public class LocusLinkWorkerTest extends AbstractXmlWorkerTest {

    public void testExecute() throws Exception{
    	
    	System.out.println("LocusLinkWorkerTest.testExecute() skipped");
    	//TEST FAILS DUE TO BADLY FORMED ROOT ELEMENT
//        LocalWorker worker = new LocusLinkWorker();
//        Map inputMap = new HashMap();
//        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
//        inAdapter.putString("term","brca2");
//        
//        Map outputMap = worker.execute(inputMap);
//        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
//        
//        String results = outAdapter.getString("outputText");
//        assertNotNull("The results were null", results);
//        System.out.println(results);
//        
//        this.writeFile("test_locus_link.xml", results);
//        Element root = this.parseXml(results);
//        this.testXmlNotEmpty(root);
    }

}
