/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import org.jdom.*;
import java.util.*;

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
	

}
