/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import com.ibm.wsdl.extensions.soap.SOAPBindingImpl;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.wsdl.Binding;
import javax.wsdl.Definition;
import javax.wsdl.Operation;
import javax.wsdl.PortType;
import javax.wsdl.WSDLException;
import javax.wsdl.extensions.ExtensibilityElement;
import javax.wsdl.extensions.soap.SOAPBinding;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

// Utility Imports
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;




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
	// Iterate over bindings
	Map bindingMap = theDefinition.getBindings();
	for (Iterator j = bindingMap.values().iterator(); j.hasNext();) {
	    Binding theBinding = (Binding)j.next();
	    List extensibilityElementList = theBinding.getExtensibilityElements();
	    for (Iterator k = extensibilityElementList.iterator(); k.hasNext(); ) {
		ExtensibilityElement ee = (ExtensibilityElement)k.next();
		// Look for a soap binding, which indicates that this binding is interesting.
		if (ee instanceof SOAPBindingImpl) {
		    SOAPBinding sb = (SOAPBinding)ee;
		    // Found the soap binding so add the new scavengers
		    PortType thePortType = theBinding.getPortType();
		    String portTypeName = thePortType.getQName().getLocalPart();
		    String name = "";
		    if (sb.getStyle().equals("document")) {
			name = "<html>porttype: "+portTypeName+" [<font color=\"red\">DOCUMENT</font>]</html>";
		    }
		    else {
			name = "<html>porttype: "+portTypeName+" [<font color=\"green\">"+sb.getStyle().toUpperCase()+"</font>]</html>";
		    }
		    DefaultMutableTreeNode portTypeNode = new DefaultMutableTreeNode(name);
		    add(portTypeNode);
		    // Iterate over all the operation names
		    List operationList = thePortType.getOperations();
		    for (Iterator i = operationList.iterator(); i.hasNext(); ) {
			Operation op = (Operation)i.next();
			String operationName = op.getName();
			WSDLBasedProcessorFactory wpf = new WSDLBasedProcessorFactory(wsdlLocation, operationName, thePortType.getQName());
			DefaultMutableTreeNode operationNode = new DefaultMutableTreeNode(wpf);
			portTypeNode.add(operationNode);
		    }
		}
	    }
	}

    }
}
	
