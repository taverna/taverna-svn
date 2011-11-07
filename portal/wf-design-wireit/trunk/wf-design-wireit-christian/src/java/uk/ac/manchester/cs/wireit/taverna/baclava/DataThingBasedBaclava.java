/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package uk.ac.manchester.cs.wireit.taverna.baclava;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import java.util.Map;
import net.sf.taverna.t2.lang.baclava.DataThing;
import net.sf.taverna.t2.lang.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import uk.ac.manchester.cs.wireit.taverna.TavernaException;

/**
 * With thanks to:
 * http://code.google.com/p/taverna/source/browse/portal/neiss-liferay/trunk/src/java/net/sf/taverna/t2/portal/WorkflowResultsPortlet.java
 * @author Christian
 */
public class DataThingBasedBaclava {

    private Map<java.lang.String,DataThing> dataThingMap;
    
    public DataThingBasedBaclava(File file) throws TavernaException{
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream inputStream = new FileInputStream(file);
            Document doc = builder.build(inputStream);
            dataThingMap = DataThingXMLFactory.parseDataDocument(doc);
        } catch (Exception ex) {
            throw new TavernaException ("Exception while reading Baclava uri");
        }
    }
    
    public DataThingBasedBaclava(String uri) throws TavernaException{
        try {
            SAXBuilder builder = new SAXBuilder();
            InputStream inputStream = new URI(uri).toURL().openStream() ;
            Document doc = builder.build(inputStream);
            dataThingMap = DataThingXMLFactory.parseDataDocument(doc);
        } catch (Exception ex) {
            throw new TavernaException ("Exception while reading Baclava uri");
        }
    }

    private void checkData(){
        for (Iterator i = dataThingMap.keySet().iterator(); i.hasNext();) { 
            String portName = (String) i.next();
            DataThing dataThing = dataThingMap.get(portName);
            Object dataObject = dataThing.getDataObject();
            int dataDepth = calculateDataDepth(dataObject);
            String mimeType = dataThing.getMostInterestingMIMETypeForObject(dataObject);
            System.out.println(portName);
            System.out.println(dataDepth);
            System.out.println(mimeType);
            System.out.println(dataThing);
            Object data = dataThing.getDataObject();
            System.out.println(data);
            System.out.println(data.getClass());
        }
    }
    
    /*
     * Calculate depth of a result data item.
     * 
     */
    private static int calculateDataDepth(Object dataObject) {
        if (dataObject instanceof Collection<?>) {
            if (((Collection<?>) dataObject).isEmpty()) {
                return 1;
            } else {
                // Calculate the depth of the first element in collection + 1
                return calculateDataDepth(((Collection<?>) dataObject).iterator().next()) + 1;
            }
        } else {
            return 0;
        }
    }
    
    private void parseDOM(Document doc){
        dataThingMap = DataThingXMLFactory.parseDataDocument(doc);
    }
    
    public boolean hasValue(String key) {
        return dataThingMap.containsKey(key);
    }
    
    public Object getValue(String key) throws TavernaException{
        if (dataThingMap.containsKey(key)){
            DataThing dataThing = dataThingMap.get(key);
            //TODO depth issue.
            return dataThing.getDataObject();
        } else {
            throw new TavernaException ("Baclava does not have key " + key);
        }
    }
    
    public static void main(String[] args) throws TavernaException {
         DataThingBasedBaclava me = new DataThingBasedBaclava("file:d:/taverna/output2.xml");
         me.checkData();
    }

}
