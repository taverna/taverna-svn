/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl;

import javax.wsdl.*;
import javax.wsdl.factory.WSDLFactory;
import javax.wsdl.xml.WSDLReader;
import org.embl.ebi.escience.scufl.Port; // ambiguous with: javax.wsdl.Port 

// Utility Imports
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Network Imports
import java.net.MalformedURLException;
import java.net.URL;

import org.embl.ebi.escience.scufl.DuplicatePortNameException;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.PortCreationException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import java.lang.String;



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

    /**
     * Construct a new processor from the given WSDL definition
     * and operation name, delegates to superclass then instantiates
     * ports based on WSDL inspection.
     */
    public WSDLBasedProcessor(ScuflModel model, String name, String wsdlLocation, String portTypeName, String operationName)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, name);
	
	// Set members
	this.wsdlLocationString = wsdlLocation;
	this.operationString = operationName;
	this.portTypeString = portTypeName;

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
	// Get the list of port types and search for one with the local part of the
	// QName equal to the supplied portTypeName
	Map portTypeMap = theDefinition.getPortTypes();
	PortType portType = null;
	for (Iterator i = portTypeMap.values().iterator(); i.hasNext(); ) {
	    PortType thePortType = (PortType)i.next();
	    if (thePortType.getQName().getLocalPart().equals(portTypeName)) {
		// Found it.
		portType = thePortType;
	    }
	}
	if (portType == null) {
	    throw new ProcessorCreationException("Unable to locate portType '"+portTypeName+"' in '"+wsdlLocation+"'.");
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

	// Do the inputs first...
	Message inputMessage = theOperation.getInput().getMessage();
	requestMessageName = inputMessage.getQName().getLocalPart();
	// Iterate over the message parts, creating appropriate Port implementations
	//System.out.println("Input ports...");
	for (Iterator i = inputMessage.getParts().values().iterator(); i.hasNext(); ) {
	    Part part = (Part)i.next();
	    try {
		Port newInputPort = new InputPort(this, part.getName());
		newInputPort.setSyntacticType(part.getTypeName().getLocalPart());
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
	Message outputMessage = theOperation.getOutput().getMessage();
	responseMessageName = outputMessage.getQName().getLocalPart();
	// Iterate over the output parts, creating appropriate Port implementations
	//System.out.println("Output ports...");
	for (Iterator i = outputMessage.getParts().values().iterator(); i.hasNext(); ) {
	    Part part = (Part)i.next();
	    try {
		Port newOutputPort = new OutputPort(this, part.getName());
		newOutputPort.setSyntacticType(part.getTypeName().getLocalPart());
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


}
