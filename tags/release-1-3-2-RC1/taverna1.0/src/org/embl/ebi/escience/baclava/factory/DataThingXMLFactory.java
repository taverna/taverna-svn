/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import org.embl.ebi.escience.baclava.Base64;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.NoMetadataFoundException;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.XScufl;

// Utility Imports
import java.util.*;
import java.io.*;

// JDOM Imports
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.Namespace;

import java.lang.Object;
import java.lang.String;
import java.lang.System;



/**
 * Performs the creation of XML elements from DataThing
 * objects and the configuration of existing DataThing
 * objects from an XML representation.
 * @author Tom Oinn
 */
public class DataThingXMLFactory {

    /**
     * The namespace used in the XML representation
     * of a DataThing object
     */
    public static Namespace namespace = 
	Namespace.getNamespace("b",
			       "http://org.embl.ebi.escience/baclava/0.1alpha");
    
    /**
     * Return a Document from a Map of String to DataThing, this
     * is the input and output document format for the workflow
     * enactment engine.
     */
    public static Document getDataDocument(Map dataThings) {
	Element rootElement = new Element("dataThingMap",namespace);
	Document theDocument = new Document(rootElement);
	for (Iterator i = dataThings.keySet().iterator(); i.hasNext(); ) {
	    String key = (String)i.next();
	    DataThing value = (DataThing)dataThings.get(key);
	    Element dataThingElement = new Element("dataThing",namespace);
	    dataThingElement.setAttribute("key",key);
	    dataThingElement.addContent(value.getElement());
	    rootElement.addContent(dataThingElement);
	}
	return theDocument;
    }

    /**
     * Parse a data document and return a Map of DataThing objects,
     * the keys in the map being the string key attributes from the
     * data document
     */
    public static Map parseDataDocument(Document dataDocument) {
	Map result = new HashMap();
	Element rootElement = dataDocument.getRootElement();
	for (Iterator i = rootElement.getChildren("dataThing",namespace).iterator(); i.hasNext(); ) {
	    Element e = (Element)i.next();
	    String key = e.getAttributeValue("key");
	    DataThing d = new DataThing(e.getChild("myGridDataDocument",namespace));
	    result.put(key, d);
	}
	return result;
    }

    /**
     * Build a DataThing from the supplied Element object, the Element 
     * in this case is the 'myGridDataDocument' element.
     */
    public static Object configureDataThing(Element rootElement, DataThing theDataThing) {
	// Configure semantic markup object for the DataThing
	Element semanticMarkupElement = rootElement.getChild("metadata",XScufl.XScuflNS);
	// Get the LSID for the entire datathing
	String lsid = rootElement.getAttributeValue("lsid");
	if (lsid != null) {
	    theDataThing.setLSID(theDataThing, lsid);
	}
	if (semanticMarkupElement != null) {
	    theDataThing.getMetadata().configureFromElement(semanticMarkupElement);
	}
	return objectForElement(rootElement, theDataThing, rootElement);
    }

    /**
     * Handle either a leaf node - dataElement - or a partial 
     * order - partialOrder, and delegate to the appropriate
     * walker method. This searches for the appropriate child
     * of the supplied element and calls the method on that.
     */
    private static Object objectForElement(Element e, DataThing theDataThing, Element rootElement) {
	Element dataElement = e.getChild("dataElement",namespace);
	if (dataElement != null) {
	    return objectForDataElement(dataElement, theDataThing, rootElement);
	}
	Element posetElement = e.getChild("partialOrder",namespace);
	if (posetElement != null) {
	    return objectForCollectionElement(posetElement, theDataThing, rootElement);
	}
	// Fallback case, should never hit this.
	return null;
    }
    
    /**
     * The recursive tree walker to build objects from XML, this call
     * handles leaf nodes.
     */
    private static Object objectForDataElement(Element e, DataThing theDataThing, Element rootElement) {
	// Is there a hint in a metadata block?
	Element metadataElement = e.getChild("metadata",XScufl.XScuflNS);
	String mimeHint = "";
	if (metadataElement != null) {
	    SemanticMarkup m = new SemanticMarkup(new Object());
	    m.configureFromElement(metadataElement);
	    mimeHint = m.getFirstMIMEType().toLowerCase();	    
	}
	if (mimeHint.equals("")) {
	    String documentSyntacticType = rootElement.getAttributeValue("syntactictype");
	    if (documentSyntacticType.equals("") == false) {
		mimeHint = (documentSyntacticType.split("'"))[1].toLowerCase();
	    }
	}
	// Default to assuming string data, which is probably wrong but what
	// else can we do here?
	if (mimeHint.equals("")) {
	    mimeHint = "text/plain";
	}
	// If the major part is anything other than text then we have
	// binary data so it seems reasonable to ask for a byte array
	// back from the convertor.
	String mimeMajorType = mimeHint.split("/")[0];
	String encodedData = e.getChild("dataElementData",namespace).getTextTrim();
	//System.out.println(encodedData);
	byte[] decodedData = Base64.decode(encodedData);
	Object result;
       	if (mimeMajorType.equals("text")) {
	    result = new String(decodedData);
	}
	// Handle java objects
	else if (mimeMajorType.equals("java")) {
	    try {
		ObjectInputStream in = new ObjectInputStream(new ByteArrayInputStream(decodedData));
		result = (Object)in.readObject();
		in.close();		
	    }
	    catch (Exception ex) {
		System.out.println("Failed to deserialize java object type");
		ex.printStackTrace();
		result = new String("Unable to decode "+mimeHint);
	    }
	}
	else {
	    result = decodedData;
	}
	if (metadataElement != null) {
	    SemanticMarkup m = theDataThing.getMetadataForObject(result, true);
	    m.configureFromElement(metadataElement);
	}
	String lsid = e.getAttributeValue("lsid");
	if (lsid != null) {
	    theDataThing.setLSID(result, lsid);
	}
	return result;
    }

    /**
     * The recursitve tree walker to build objects from XML, this
     * call handles branch nodes i.e. collections
     */
    private static Object objectForCollectionElement(Element e, DataThing theDataThing, Element rootElement) {
	// For now as we're only looking at sets and lists we can
	// actually ignore the ordering section. For tree and partial
	// order support we're going to need to consider it, but for
	// now we're good.
	String collectionType = e.getAttributeValue("type");
	Element relationListElement = e.getChild("relationList", namespace);
	Element itemListElement = e.getChild("itemList", namespace);
	Collection result = null;
	if (collectionType.equals("list")) {
	    result = new ArrayList();
	}
	else {
	    result = new HashSet();
	}
	
	for (Iterator i = itemListElement.getChildren().iterator(); i.hasNext(); ) {
		Element childElement = (Element)i.next();
		if (childElement.getNamespace().equals(namespace)) {
			if (childElement.getName().equals("dataElement")) {
			    result.add(objectForDataElement(childElement, theDataThing, rootElement));
			} else if (childElement.getName().equals("partialOrder")) {
			    result.add(objectForCollectionElement(childElement, theDataThing, rootElement));				
			}
		}
	}
	String lsid = e.getAttributeValue("lsid");
	if (lsid != null) {
	    theDataThing.setLSID(result, lsid);
	}	
	return result;
    }


    /**
     * Return an XML Jdom Element object for the DataThing supplied
     * as the argument
     */
    public static Element getElement(DataThing theDataThing) {
	Element rootElement = new Element("myGridDataDocument", namespace);
	rootElement.setAttribute("lsid",theDataThing.getLSID(theDataThing));
	rootElement.setAttribute("syntactictype",theDataThing.getSyntacticType());
	rootElement.addContent(theDataThing.getMetadata().getConfigurationElement());
	rootElement.addContent(elementForObject(theDataThing.getDataObject(), theDataThing));
	return rootElement;
    }

    /**
     * The recursive method to convert an arbitrary object
     * into XML
     */
    private static Element elementForObject(Object o, DataThing theDataThing) {
	// Handle collections
	if (o instanceof Collection) {
	    Element poElement = new Element("partialOrder", namespace);
	    poElement.setAttribute("lsid",theDataThing.getLSID(o));
	    Element relationListElement = new Element("relationList", namespace);
	    Element listElement = new Element("itemList", namespace);
	    int currentIndex = 0;
	    poElement.addContent(relationListElement);
	    poElement.addContent(listElement);
	    if (o instanceof List) {
		// Handle lists
		poElement.setAttribute("type","list");
		// Iterate over the list, creating the new items
		if (!((List)o).isEmpty()) {
		    for (Iterator i = ((List)o).iterator(); i.hasNext(); ) {
			Element listItemElement = elementForObject(i.next(), theDataThing);
			listElement.addContent(listItemElement);
			listItemElement.setAttribute("index",""+currentIndex);
			// If the index is non zero create a new relation
			if (currentIndex > 0) {
			    Element relationElement = new Element("relation", namespace);
			    relationElement.setAttribute("parent", ""+(currentIndex-1));
			    relationElement.setAttribute("child", ""+currentIndex);
			    relationListElement.addContent(relationElement);
			}
			currentIndex++;
		    }
		}
	    }
	    else if (o instanceof Set) {
		// Handle sets
		poElement.setAttribute("type","set");
		// Iterate over the set, creating new items
	    }
	    return poElement;
	}
	else {
	    // Handle data elements
	    Element dataElement = new Element("dataElement", namespace);
	    dataElement.setAttribute("lsid",theDataThing.getLSID(o));
	    try {
		SemanticMarkup sm = theDataThing.getMetadataForObject(o, false);
		dataElement.addContent(sm.getConfigurationElement());
	    }
	    catch (NoMetadataFoundException nmfe) {
		// No metadata available for this object
	    }
	    // Add the data itself
	    Element realDataElement = new Element("dataElementData", namespace);
	    byte[] theBytes = new byte[0];
	    if (o instanceof String) {
		theBytes = ((String)o).getBytes();
	    }
	    else if (o instanceof byte[]) {
		theBytes = (byte[])o;
	    }
	    // Handle java object
	    else {
		try {
		    ByteArrayOutputStream bos = new ByteArrayOutputStream();
		    ObjectOutputStream oos = new ObjectOutputStream(bos);
		    oos.writeObject(o);
		    theBytes = bos.toByteArray();
		}
		catch (Exception ex) {
		    System.out.println("Error serializing "+o.getClass().getName());
		    ex.printStackTrace();
		}
	    }
	    // Convert to base64
	    System.out.println("Size of binary data is "+theBytes.length+" bytes.");
	    realDataElement.setText(Base64.encodeBytes(theBytes));
	    dataElement.addContent(realDataElement);
	    return dataElement;
	}
    }

}
