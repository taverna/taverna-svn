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
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class OMIMWorkerTest extends AbstractXmlWorkerTest {

    public void testExecute() throws Exception{
        LocalWorker worker = new OMIMWorker();
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("term","brca2");
        
        Map outputMap = worker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
        
        String results = outAdapter.getString("resultsXml");
        assertNotNull("The results were null", results);
        System.out.println(results);
        
        this.writeFile("omim_test.xml", results);
        Element root = this.parseXml(results);
        this.testXmlNotEmpty(root);


    }

}
