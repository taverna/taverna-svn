/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;

// Utility Imports
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.WSDLBasedProcessorFactory;
import java.lang.String;



/**
 * A Scavenger that knows how to inspect a given
 * wsdl document for all available port types and
 * operations within them.
 * @author Tom Oinn
 */
public class WSDLBasedScavenger extends Scavenger {

    /**
     * Create a new WSDLBased scavenger, the single parameter
     * should be resolvable to a location from which the 
     * wsdl document can be fetched.
     */
    public WSDLBasedScavenger(String wsdlLocation)
	throws ScavengerCreationException {
	super("WSDL @ "+wsdlLocation);
	// Load the wsdl document
	try {
	    URL wsdlLocationURL = new URL(wsdlLocation);
	}
	catch (MalformedURLException mue) {
	    throw new ScavengerCreationException("Unable to parse the supplied URL '"+wsdlLocation+"', error was "+mue.getMessage());
	}
	// Get a WSDLReader
	Definition theDefinition = null;
	try {
	    WSDLFactory wsdlf = WSDLFactory.newInstance();
	    WSDLReader wsdlr = wsdlf.newWSDLReader();
	    theDefinition = wsdlr.readWSDL(wsdlLocation);
	}
	catch (WSDLException wsdle) {
	    throw new ScavengerCreationException("Unable to load the WSDL definition, underlying reason was "+wsdle.getMessage());
	}
	// Iterate over port types
	Map portTypeMap = theDefinition.getPortTypes();
	for (Iterator i = portTypeMap.values().iterator(); i.hasNext(); ) {
	    PortType thePortType = (PortType)i.next();
	    String portTypeName = thePortType.getQName().getLocalPart();
	    DefaultMutableTreeNode portTypeNode = new DefaultMutableTreeNode("porttype: "+portTypeName);
	    add(portTypeNode);
	    // Iterate over all the operation names
	    List operationList = thePortType.getOperations();
	    for (Iterator j = operationList.iterator(); j.hasNext(); ) {
		Operation op = (Operation)j.next();
		String operationName = op.getName();
		WSDLBasedProcessorFactory wpf = new WSDLBasedProcessorFactory(wsdlLocation, portTypeName, operationName);
		DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(wpf);
		portTypeNode.add(operationNode);
	    }
	}
    }
}
	
