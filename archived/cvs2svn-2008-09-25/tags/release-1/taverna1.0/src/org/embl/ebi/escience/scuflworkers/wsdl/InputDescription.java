/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import javax.xml.namespace.QName;
import org.w3c.dom.*;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerConfigurationException;

import javax.xml.transform.dom.DOMSource; 
import javax.xml.parsers.*;

import javax.xml.transform.stream.StreamResult; 
import java.io.ByteArrayOutputStream;
import java.io.ByteArrayInputStream;

/**
 * Holds parameters required to go from the input map to
 * a single DOM for document oriented invocation with
 * complex types such as those used in the NCBI services.
 * @author Tom Oinn
 */
public class InputDescription {
    
    QName elementName, elementType;
    boolean isOptional, isArray, simpleContent;

    public InputDescription(QName elementName,
			    QName elementType,
			    boolean isOptional,
			    boolean isArray) {
	this.elementName = elementName;
	this.elementType = elementType;
	this.isOptional = isOptional;
	this.isArray = isArray;
	getTavernaType();
    }

    public String toString() {
	return elementName.getLocalPart()+" "+getTavernaType()+(isOptional?" [optional]":"");
    }

    public String getTavernaType() {
	String s = elementType.getLocalPart();
	Class c = null;
	simpleContent = true;
	if ("string".equals(s)) {
	    c = String.class;
	} else if ("double".equals(s)) {
	    c = Integer.TYPE;
	} else if ("float".equals(s)) {
	    c = Float.TYPE;
	} else if ("int".equals(s)) {
	    c = Integer.TYPE;
	} else if ("boolean".equals(s)) {
	    c = Boolean.TYPE;
	} else {
	    simpleContent = false;
	    c = org.w3c.dom.Element.class;
	}
	String temp = WSDLBasedProcessor.translateJavaType(c);
	if (isArray) {
	    return ("l("+temp+")");
	}
	else {
	    return temp;
	}
    }

    // Get the DOM node for this input given a string object
    public org.w3c.dom.Element getElement(String contents, Document document) {
	try {
	    Element root = (Element)document.createElementNS(elementName.getNamespaceURI(), elementName.getLocalPart());
	    // If simple content then just add the text as a text node, otherwise parse in the new DOM from 
	    // the XML text input and add it as a child
	    if (simpleContent) {
		root.appendChild(document.createTextNode(contents));
	    }
	    else {
		DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		Document doc = builder.parse(new ByteArrayInputStream(contents.getBytes()));
		Element childElement = doc.getDocumentElement();
		document.importNode(childElement, true);
		root.appendChild(childElement);
	    }
	    return root;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    throw new RuntimeException(ex);
	}
    }
    
}
