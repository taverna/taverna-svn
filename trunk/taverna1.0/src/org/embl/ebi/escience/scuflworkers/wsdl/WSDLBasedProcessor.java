/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import javax.wsdl.*;
import javax.wsdl.extensions.*;
import javax.wsdl.extensions.soap.*;
import javax.xml.namespace.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.Port; // ambiguous with: javax.wsdl.Port 

// Utility Imports
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;




/**
 * A processor based on an operation defined within 
 * a WSDL file accessible to the class at construction
 * time.
 * @author Tom Oinn
 */
public class WSDLBasedProcessor extends Processor implements java.io.Serializable {
    
    private String wsdlLocationString = null;
    private String operationString = null;
    private String portTypeString = null;
    private String requestMessageName = null;
    private String responseMessageName = null;
    private String operationStyle = null;
    private String targetEndpoint = null;

    public String getWSDLLocation() {
	return this.wsdlLocationString;
    }
    public String getOperationName() {
	return this.operationString;
    }
    public String getPortTypeName() {
	return this.portTypeString;
    }
    public String getRequestMessageName() {
	return this.requestMessageName;
    }
    public String getResponseMessageName() {
	return this.responseMessageName;
    }
    public String getOperationStyle() {
	return this.operationStyle;
    }
    public String getTargetEndpoint() {
	return this.targetEndpoint;
    }
    
    /**
     * Construct a new processor from the given WSDL definition
     * and operation name, delegates to superclass then instantiates
     * ports based on WSDL inspection.
     */
    public WSDLBasedProcessor(ScuflModel model, String name, String wsdlLocation, String portTypeName, String operationName, String operationStyle)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	
	// Set members
	this.wsdlLocationString = wsdlLocation;
	this.operationString = operationName;
	this.portTypeString = portTypeName;
	
	// is either 'rpc' or 'document' I think...
	this.operationStyle = operationStyle;

	// Attempt to construct a URL object
	try {
	    URL wsdlLocationURL = new URL(wsdlLocation);
	}
	catch (MalformedURLException mue) {
	    throw new ProcessorCreationException("The supplied URL '"+wsdlLocation+"' for the location of the WSDL file is not valid.");
	}
	// Get a WSDLReader
	Definition theDefinition = null;
	try {
	    WSDLFactory wsdlf = WSDLFactory.newInstance();
	    WSDLReader wsdlr = wsdlf.newWSDLReader();
	    theDefinition = wsdlr.readWSDL(wsdlLocation);
	}
	catch (WSDLException wsdle) {
	    throw new ProcessorCreationException("Unable to load the WSDL definition, underlying reason was "+wsdle.getMessage());
	}

	PortType portType = null;
	// Iterate over the Service objects in the definition, then over Port objects within
	// services until we find the particular Port containing the binding.
	boolean found = false;
	for (Iterator i = theDefinition.getServices().values().iterator(); i.hasNext() && !found; ) {
	    Service s = (Service)i.next();
	    // Iterate over Port objects within service
	    for (Iterator j = s.getPorts().values().iterator(); j.hasNext() && !found; ) {
		javax.wsdl.Port p = (javax.wsdl.Port)j.next();
		Binding b = p.getBinding();
		PortType pt = b.getPortType();
		// Does this have the correct port type name?
		if (pt.getQName().getLocalPart().equals(portTypeName)) {
		    portType = pt;
		    // Iterate over the extensibility elements for the port
		    for (Iterator k = p.getExtensibilityElements().iterator(); k.hasNext(); ) {
			ExtensibilityElement ee = (ExtensibilityElement)k.next();
			if (ee instanceof SOAPAddress) {
			    targetEndpoint = ((SOAPAddress)ee).getLocationURI();
			}
		    }
		    found = true;
		}
	    }
	}
	if (portType == null) {
	    throw new ProcessorCreationException("Unable to locate portType '"+
						 portTypeName+"' in '"+wsdlLocation+"'.");
	}
	if (targetEndpoint == null) {
	    throw new ProcessorCreationException("Unable to locate the target endpoint for '"+
						 portTypeName+"' in '"+wsdlLocation+"'.");
	}
	// Get the list of operations and search for one with local QName part
	// equal to the supplied operationName
	List operationList = portType.getOperations();
	Operation theOperation = null;
	for (Iterator i = operationList.iterator(); i.hasNext(); ) {
	    Operation op = (Operation)i.next();
	    if (op.getName().equals(operationName)) {
		theOperation = op;
	    }
	}
	if (theOperation == null) {
	    throw new ProcessorCreationException("Unable to locate operation with name '"+operationName+"' in portType '"+portTypeName+"'.");
	}
	// Get the input and output objects, use these to create input and 
	// output ports from the message parts.
	Message inputMessage = theOperation.getInput().getMessage();
	Message outputMessage = theOperation.getOutput().getMessage();
	if (operationStyle.equals("rpc")) {
	    // Do the inputs first...
	    QName inputMessageQName = inputMessage.getQName();
	    // Iterate over the message parts, creating appropriate Port implementations
	    //System.out.println("Input ports...");
	    for (Iterator i = inputMessage.getParts().values().iterator(); i.hasNext(); ) {
		Part part = (Part)i.next();
		try {
		    System.out.println(part.getName());
		    Port newInputPort = new InputPort(this, part.getName());
		    newInputPort.setSyntacticType(xsdTypeToInternalType(part.getTypeName().getLocalPart()));
		    this.addPort(newInputPort);
		}
		catch (DuplicatePortNameException dpne) {
		    throw new ProcessorCreationException("Attempted to create a duplicate input port '"+part.getName()+"'.");
		}
		catch (PortCreationException pce) {
		    throw new ProcessorCreationException("Problem creating input port : "+pce.getMessage());
		}
	    }
	    // Then do the outputs
	    // Iterate over the output parts, creating appropriate Port implementations
	    //System.out.println("Output ports...");
	    for (Iterator i = outputMessage.getParts().values().iterator(); i.hasNext(); ) {
		Part part = (Part)i.next();
		try {
		    Port newOutputPort = new OutputPort(this, part.getName());
		    newOutputPort.setSyntacticType(xsdTypeToInternalType(part.getTypeName().getLocalPart()));
		    this.addPort(newOutputPort);
		}
		catch (DuplicatePortNameException dpne) {
		    throw new ProcessorCreationException("Attempted to create a duplicate output port '"+part.getName()+"'.");
		}
		catch (PortCreationException pce) {
		    throw new ProcessorCreationException("Problem creating output port : "+pce.getMessage());
		}
	    }
	}
	else {
	    try {
		if (inputMessage.getParts().isEmpty() == false) {
		    Port newInputPort = new InputPort(this, "InputDocument");
		    newInputPort.setSyntacticType("string");
		    this.addPort(newInputPort);
		}
		if (outputMessage.getParts().isEmpty() == false) {
		    Port newOutputPort = new OutputPort(this, "OutputDocument");
		    newOutputPort.setSyntacticType("string");
		    this.addPort(newOutputPort);
		}
	    }
	    catch (Exception e) {
		e.printStackTrace();
	    }
	}
    
    }

    public String xsdTypeToInternalType(String xsdType) {
	// Can cope with String types and nested strings
	if (xsdType.startsWith("ArrayOf_")) {
	    return ("l("+xsdTypeToInternalType(xsdType.replaceFirst("ArrayOf_",""))+")");
	}
	else {
	    if (xsdType.equalsIgnoreCase("xsd_string") || 
		xsdType.equalsIgnoreCase("xsd:string") || 
		xsdType.equalsIgnoreCase("string")) {
		return "'text/plain'";
	    }
	}
	// Fall through
	return xsdType;
    }

    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	props.put("WSDL Location",getWSDLLocation());
	props.put("Port Type",getPortTypeName());
	props.put("Operation",getOperationName());
	props.put("Target Endpoint",getTargetEndpoint());
	return props;
    }
}
