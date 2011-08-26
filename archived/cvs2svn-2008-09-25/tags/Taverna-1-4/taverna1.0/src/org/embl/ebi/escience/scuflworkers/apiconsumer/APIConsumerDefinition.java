/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.jdom.Element;
import java.util.*;
import org.w3c.dom.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource; 
import javax.xml.parsers.*;
import java.io.*;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * Definition for an APIConsumer processor or factory
 * @author Tom Oinn
 */
public class APIConsumerDefinition {

    String className;
    String methodName;
    String[] pNames;
    int[] pDimensions;
    String[] pTypes;
    String tName;
    int tDimension;
    String description;
    boolean isConstructor, isStatic;

    public APIConsumerDefinition(String className,
				 String methodName,
				 String[] pNames,
				 String[] pTypes,
				 int[] pDimensions,
				 String tName,
				 int tDimension,
				 String description,
				 boolean isConstructor,
				 boolean isStatic) {
	this.className = className;
	this.methodName = methodName;
	this.pNames = pNames;
	this.pDimensions = pDimensions;
	this.tName = tName;
	this.tDimension = tDimension;
	this.description = description;
	this.isConstructor = isConstructor;
	this.isStatic = isStatic;
	this.pTypes = pTypes;
    }

    public APIConsumerDefinition(Element defElement) {

	Element classElement = defElement.getChild("class");
	this.className = classElement.getAttributeValue("name");

	Element methodElement = defElement.getChild("method");
	this.methodName = methodElement.getAttributeValue("name");
	this.isStatic = methodElement.getAttributeValue("static").equals("true");
	this.isConstructor = methodElement.getAttributeValue("constructor").equals("true");
	this.tName = methodElement.getAttributeValue("type");
	this.tDimension = Integer.parseInt(methodElement.getAttributeValue("dimension"));

	Element descriptionElement = defElement.getChild("description");
	this.description = descriptionElement.getTextTrim();

	List params = defElement.getChildren("parameter");
	this.pNames = new String[params.size()];
	this.pTypes = new String[params.size()];
	this.pDimensions = new int[params.size()];
	int count = 0;
	for (Iterator i = params.iterator(); i.hasNext(); ) {
	    Element parameterElement = (Element)i.next();
	    pNames[count] = parameterElement.getAttributeValue("name");
	    pTypes[count] = parameterElement.getAttributeValue("type");
	    pDimensions[count] = Integer.parseInt(parameterElement.getAttributeValue("dimension"));
	    count++;
	}
    }

    public Element asXML() {
	Element root = new Element("apiconsumer");
	
	Element classElement = new Element("class");
	classElement.setAttribute("name",className);
	root.addContent(classElement);

	Element method = new Element("method");
	method.setAttribute("name",methodName);
	method.setAttribute("static",isStatic+"");
	method.setAttribute("constructor",isConstructor+"");
	method.setAttribute("type",tName);
	method.setAttribute("dimension",tDimension+"");
	root.addContent(method);

	Element descriptionElement = new Element("description");
	descriptionElement.setText(description);
	root.addContent(descriptionElement);
	
	for (int i = 0; i < pNames.length; i++) {
	    Element parameterElement = new Element("parameter");
	    parameterElement.setAttribute("name",pNames[i]);
	    parameterElement.setAttribute("type",pTypes[i]);
	    parameterElement.setAttribute("dimension",pDimensions[i]+"");
	    root.addContent(parameterElement);
	}

	return root;
    }
    
    public String[] getTavernaTypeStrings() {
	String[] result = new String[pNames.length];
	for (int i = 0; i < pNames.length; i++) {
	    result[i] = getFullTavernaTypeString(pTypes[i], pDimensions[i]);
	}
	return result;
    }

    public String getTavernaOutputTypeString() {
	return getFullTavernaTypeString(tName, tDimension);
    }

    public String getTavernaObjectTypeString() {
	return getFullTavernaTypeString(className, 0);
    }

    /**
     * Convert a classname as described in this bean to something that
     * taverna can use as a port mime type. Ignores dimension, this
     * just returns the 'foo/bar' part of the type.
     */
    static String getTavernaTypeString(String javaType) {
	// Primitive types are all single strings as far as we're concerned...
	if (javaType.split("\\.").length == 1) {
	    return "'text/plain'";
	}
	// Strings should be strings, oddly enough
	if (javaType.equals("java.lang.String")) {
	    return "'text/plain'";
	}
	// Handle XML types
	if (javaType.equals("org.w3c.dom.Document")) {
	    return "'text/xml'";
	}
	// Fallback for types we don't understand, use 'java/full.class.name'
	return "'java/"+javaType+"'";	    
    }

    /**
     * Create a full taverna type string
     */
    static String getFullTavernaTypeString(String javaType, int dimension) {
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < dimension; i++) {
	    sb.append("l(");
	}
	sb.append(getTavernaTypeString(javaType));
	for (int i = 0; i < dimension; i++) {
	    sb.append(")");
	}
	return sb.toString();	
    }

    static Object createInputObject(DataThing thing, String javaType) 
	throws Exception {
	Object dataObject = thing.getDataObject();
	if (dataObject instanceof Collection) {
	    return createInputArray((Collection)dataObject, javaType);
	}
	else {
	    return createSingleItem(dataObject, javaType);
	}  
    }

    static Object createInputArray(Collection collection, String javaType) 
	throws Exception {
	Object[] result = new Object[collection.size()];
	int count = 0;
	for (Iterator i = collection.iterator(); i.hasNext();) {
	    Object item = i.next();
	    if (item instanceof Collection) {
		result[count] = createInputArray((Collection)item, javaType);
	    }
	    else {
		result[count] = createSingleItem(item, javaType);
	    }
	    count++;
	}
	return result;
    }

    static Object createSingleItem(Object item, String javaType) 
	throws Exception {
	if (item instanceof String) {
	    if (javaType.equals("int")) {
		return new Integer((String)item);
	    }
	    if (javaType.equals("long")) {
		return new Long((String)item);
	    }
	    if (javaType.equals("float")) {
		return new Float((String)item);
	    }
	    if (javaType.equals("double")) {
		return new Double((String)item);
	    }
	    if (javaType.equals("boolean")) {
		return new Boolean((String)item);
	    }
	    if (javaType.equals("short")) {
		return new Short((String)item);
	    }
	    if (javaType.equals("java.lang.String")) {
		return item;
	    }
	    if (javaType.equals("org.w3c.dom.Document")) {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(((String)item).getBytes()));
		return doc;
	    }
	}
	// Otherwise just return the item
	return item;
    }
	

}
