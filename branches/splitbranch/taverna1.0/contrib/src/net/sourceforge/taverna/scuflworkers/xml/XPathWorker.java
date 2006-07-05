/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 */
package net.sourceforge.taverna.scuflworkers.xml;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.dom4j.Document;
import org.dom4j.Node;
import org.dom4j.io.SAXReader;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This worker applies an arbitrary XPath expression to an XML document, and
 * returns a nodelist containing the nodes that match the XPath expression.
 * 
 * @author mfortner
 * @version $Revision: 1.3 $
 * 
 * @tavinput xpath			The XPath expression used to extract data from the document.
 * @tavinput xml-text		The XML text to be processed.
 * @tavoutput nodelist		A String array containing the nodes that match the XPath expression.
 */
public class XPathWorker implements LocalWorker {
    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
     */
    public Map execute(Map inputMap) throws TaskExecutionException {
        Map outputMap = new HashMap();
        DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
        String xpathStr = inAdapter.getString("xpath");
        String xmlText = inAdapter.getString("xml-text");

        Document doc;
        // Get the matching elements
        try {
            //doc = parseXml(xmlText, false);
            //XPath xpathSelector = DocumentHelper.createXPath(xpathStr);
            
            //List nodelist = xpathSelector.selectNodes(doc);
        	
        	SAXReader reader = new SAXReader();
        	
            Document document = reader.read(xmlText);
            List nodelist = document.selectNodes(xpathStr);
            
            // Process the elements in the nodelist
            ArrayList outputList = new ArrayList();
           
            String val= null;
            System.out.println("===============");
            for (Iterator iter = nodelist.iterator(); iter.hasNext();) {
                Node element = (Node) iter.next();
              
                if (val != null && !val.equals("")){
                	outputList.add(element.asXML());
                	System.out.println("value: "+element.asXML());
                }
                
            }
            String[] nodeArray = new String[outputList.size()];
            outputList.toArray(nodeArray);
            
            outputMap.put("nodelist", DataThingFactory.bake(nodeArray));
            
        }catch (Throwable th){
        	throw new TaskExecutionException(th);
        }

        return outputMap;
    }


    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
     */
    public String[] inputNames() {
        return new String[] { "xpath", "xml-text" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
     */
    public String[] inputTypes() {
        return new String[] { "'text/plain'", "'text/xml'" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
     */
    public String[] outputNames() {
        return new String[] { "nodelist" };
    }

    /*
     * (non-Javadoc)
     * 
     * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
     */
    public String[] outputTypes() {
        return new String[] { "l('text/plain')" };
    }
}