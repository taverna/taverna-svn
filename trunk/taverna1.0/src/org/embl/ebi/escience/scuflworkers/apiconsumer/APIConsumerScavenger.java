/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.apiconsumer;

import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.*;
import org.embl.ebi.escience.scuflworkers.*;
import java.net.URL;
import org.jdom.*;
import org.jdom.input.*;
import java.util.*;

/**
 * Scavenger driven off an XML file created by the APIConsumer
 * tool.
 * @author Tom Oinn
 */
public class APIConsumerScavenger extends Scavenger {

    public APIConsumerScavenger(URL xmlURL) 
	throws ScavengerCreationException {
	super("");
	try {
	    // Load the XML document into a JDOM Document
	    SAXBuilder builder = new SAXBuilder();
	    Document doc = builder.build(xmlURL.openStream());
	    Element root = doc.getRootElement();
	    setUserObject(root.getAttributeValue("name"));
	    // Iterate over the classes...
	    Element classesElement = root.getChild("Classes");
	    List classesList = classesElement.getChildren("Class");
	    for (Iterator i = classesList.iterator(); i.hasNext();) {
		Element classElement = (Element)i.next();
		DefaultMutableTreeNode classNode = 
		    new DefaultMutableTreeNode(classElement.getAttributeValue("name"));
		String className = classElement.getAttributeValue("name");
		// Iterate over methods
		Element methodsElement = classElement.getChild("Methods");
		List methodsList = methodsElement.getChildren("Method");
		for (Iterator j = methodsList.iterator(); j.hasNext();) {
		    Element methodElement = (Element)j.next();
		    
		    String methodName = methodElement.getAttributeValue("name");
		    String methodType = methodElement.getAttributeValue("type");
		    int dimension = Integer.parseInt(methodElement.getAttributeValue("dimension"));
		    String description = methodElement.getChild("Description").getTextTrim();
		    List paramList = methodElement.getChildren("Parameter");
		    String[] pNames = new String[paramList.size()];
		    String[] pTypes = new String[paramList.size()];
		    int[] pDimensions = new int[paramList.size()];
		    int count = 0;
		    for (Iterator k = paramList.iterator(); k.hasNext();) {
			Element parameterElement = (Element)k.next();
			pNames[count] = parameterElement.getAttributeValue("name");
			pTypes[count] = parameterElement.getAttributeValue("type");
			pDimensions[count] = Integer.parseInt(parameterElement.getAttributeValue("dimension"));
			count++;
		    }
		    APIConsumerDefinition ad = new APIConsumerDefinition(className, methodName, pNames, pTypes, pDimensions, methodType, dimension, description, false, false);
		    classNode.add(new DefaultMutableTreeNode(new APIConsumerProcessorFactory(ad)));
		}
		add(classNode);
	    }
	}
	catch (Exception ex) {
	    ScavengerCreationException sce = 
		new ScavengerCreationException(ex.getMessage());
	    sce.initCause(ex);
	    throw sce;
	}
    }
}
