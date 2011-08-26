package net.sourceforge.taverna.scuflworkers.xml;

import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;
import net.sourceforge.taverna.scuflworkers.ncbi.AbstractXmlWorkerTest;
import net.sourceforge.taverna.scuflworkers.ncbi.ProteinGBSeqWorker;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

/**
 * This class depends on JDK1.5
 * 
 * Last edited by $Author: phidias $
 * 
 * @author Mark
 * @version $Revision: 1.1 $
 */
public class XQueryWorkerTest extends AbstractXmlWorkerTest {
      
    
    
    public void testExecute() throws Exception{
        LocalWorker protworker = new ProteinGBSeqWorker();
        
        Map inputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        inAdapter.putString("id","NP_000050");
        
        Map outputMap = protworker.execute(inputMap);
        DataThingAdapter outAdapter = new DataThingAdapter(outputMap);       
        String results = outAdapter.getString("outputText");      
        System.out.println(results);
        
        LocalWorker worker = new XQueryWorker();
        HashMap xqInputMap = new HashMap();
        DataThingAdapter xqInAdapter = new DataThingAdapter(xqInputMap);
        
        inAdapter.putString("script","//GBReference");
        inAdapter.putString("inputdocText",results);
        
        Map xqOutput = worker.execute(xqInputMap);
        DataThingAdapter xqOutAdapter = new DataThingAdapter(xqOutput);
        
        String xqResults = outAdapter.getString("outputText");
        assertNotNull("The results were null", xqResults);
        System.out.println(xqResults);
        
        this.writeFile("test_xquery.xml", xqResults);
        
    }

}
