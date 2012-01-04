/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package net.sf.taverna.portal.baclava;

import java.io.File;
import java.io.FileInputStream;
import java.io.InputStream;
import java.net.URI;
import java.util.Collection;
import java.util.HashMap;
import java.util.Iterator;
import org.jdom.input.SAXBuilder;
import java.util.Map;
import net.sf.taverna.t2.baclava.DataThing;
import net.sf.taverna.t2.baclava.factory.DataThingXMLFactory;
import org.jdom.Document;
import net.sf.taverna.portal.commandline.TavernaException;
import net.sf.taverna.t2.baclava.factory.DataThingFactory;
import org.jdom.Element;
import org.jdom.Namespace;

/**
 * With thanks to:
 * http://code.google.com/p/taverna/source/browse/portal/neiss-liferay/trunk/src/java/net/sf/taverna/t2/portal/WorkflowResultsPortlet.java
 * @author Christian
 */
public class DataThingBasedBaclava {
    
    // Baclava documents XML namespace
    public static Namespace BACLAVA_NAMESPACE = Namespace.getNamespace("b","http://org.embl.ebi.escience/baclava/0.1alpha");


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
            ex.printStackTrace();
            throw new TavernaException ("Exception while reading Baclava uri ", ex);
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
         DataThingBasedBaclava me = new DataThingBasedBaclava("file:D:/Programs/Tomcat7/webapps/WireIt/Inputs/BaclavaTripleEchoInput.xml");
         me.checkData();
    }

        /*
     * Returns a map of port names to DataThings from a map of port names to a
     * value for that port (which can be a single value or a list of (lists of ...) of values).
     */
    public static Map<String, DataThing> bakeDataThingMap(Map<String, Object> inputsMap){

        Map<String, DataThing> dataThingMap = new HashMap<String, DataThing>();
        
        for (String inputPortName : inputsMap.keySet()) {
            dataThingMap.put(inputPortName, DataThingFactory.bake(inputsMap.get(inputPortName)));
        }
        return dataThingMap;
    }    

        /*
     * Returns a org.jdom.Document from a map of input port names to DataThingS containing
     * the input port's values.
     */
    public static Document getDataDocument(Map<String, DataThing> dataThings) {
	Element rootElement = new Element("dataThingMap", BACLAVA_NAMESPACE);
	Document document = new Document(rootElement);
	for (String key : dataThings.keySet()) {
		DataThing value = (DataThing) dataThings.get(key);
		Element dataThingElement = new Element("dataThing", BACLAVA_NAMESPACE);
		dataThingElement.setAttribute("key", key);
		dataThingElement.addContent(value.getElement());
		rootElement.addContent(dataThingElement);
	}
	return document;
    }
    

}
