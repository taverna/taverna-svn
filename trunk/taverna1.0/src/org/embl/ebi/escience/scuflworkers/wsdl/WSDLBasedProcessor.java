/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import javax.wsdl.Operation;
import javax.wsdl.Service;
import javax.xml.namespace.QName;
import org.embl.ebi.escience.scufl.*;
import java.net.*;

import java.util.*;
import org.apache.wsif.providers.soap.apacheaxis.WSIFPort_ApacheAxis;
import javax.wsdl.Definition;
import javax.wsdl.Input;
import javax.wsdl.Output;
import javax.wsdl.Part;
import javax.wsdl.PortType;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;

/**
 * A processor based on an operation defined within 
 * a WSDL file accessible to the class at construction
 * time. Much of the wsdl parsing code is based on that
 * found in the dynamic invocation sample in the apache
 * axis project (http://ws.apache.org)
 * @author Tom Oinn
 */

public class WSDLBasedProcessor extends Processor implements java.io.Serializable {

    WSIFPort port = null;
    String operationName = null;
    String inputName = null;
    String outputName = null;
    String wsdlLocation = null;
    String[] inNames, outNames;
    Class[] inTypes, outTypes;
    
    /**
     * Construct a new processor from the given WSDL definition
     * and operation name, delegates to superclass then instantiates
     * ports based on WSDL inspection.
     */
    public WSDLBasedProcessor(ScuflModel model, String procName, String wsdlLocation, String operationName)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, procName);
	try {
	    this.wsdlLocation = wsdlLocation;
	    this.operationName = operationName;
	    
	    // Configure to use axis then read the WSDL
	    WSIFPluggableProviders.overrideDefaultProvider("http://schemas.xmlsoap.org/wsdl/soap/",
							   new WSIFDynamicProvider_ApacheAxis());
	    Definition def = WSIFUtils.readWSDL(null, wsdlLocation);
	    
	    // Select the default service
	    Service service = WSIFUtils.selectService(def, null, null);
	    
	    // Select the default port type
	    PortType portType = WSIFUtils.selectPortType(def, null, null);
	    WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
	    WSIFService dpf = factory.getService(def, service, portType);
	    
	    // Select the default port
	    port = dpf.getPort();
	    
	    // Get the input and output names
	    String inputName = null;
	    String outputName = null;
	    List operationList = portType.getOperations();
	    boolean found = false;
	    Operation op = null;
	    for (Iterator i = operationList.iterator(); i.hasNext();) {
		Operation opTemp = (Operation)i.next();
		String name = opTemp.getName();
		if (!name.equals(operationName)) {
		    continue;
		}
		if (found) {
		    throw new RuntimeException("Operation "+name+" is overloaded, confused now.");
		}
		op = opTemp;
		found = true;
		Input opInput = op.getInput();
		inputName = (opInput.getName() == null) ? null : opInput.getName();
		Output opOutput = op.getOutput();
		outputName = (opOutput.getName() == null) ? null : opOutput.getName();
	    }
	    if (!found) {
		throw new RuntimeException("Unable to locate the named operation");
	    }
	    
	    inNames = new String[0];
	    inTypes = new Class[0];
	    Input opInput = op.getInput();
	    if (opInput != null) {
		List parts = opInput.getMessage().getOrderedParts(null);
		unWrapIfWrappedDocLit(parts, op.getName(), def);
		int count = parts.size();
		inNames = new String[count];
		inTypes = new Class[count];
		retrieveSignature(parts, inNames, inTypes);
		// Build input ports from these definitions
		for (int i = 0; i < count; i++) {
		    InputPort inputPort = new InputPort(this, inNames[i]);
		    inputPort.setSyntacticType(translateJavaType(inTypes[i]));
		    addPort(inputPort);
		}
	    } 
	    
	    outNames = new String[0];
	    outTypes = new Class[0];
	    Output opOutput = op.getOutput();
	    if (opOutput != null) {
		List parts = opOutput.getMessage().getOrderedParts(null);
		unWrapIfWrappedDocLit(parts, op.getName()+"Response", def);
		int count = parts.size();
		outNames = new String[count];
		outTypes = new Class[count];
		retrieveSignature(parts, outNames, outTypes);
		// Build output ports from definitions
		for (int i = 0; i < count; i++) {
		    OutputPort outputPort = new OutputPort(this, outNames[i]);
		    outputPort.setSyntacticType(translateJavaType(outTypes[i]));
		    addPort(outputPort);
		}
	    }
	    
	}
	
	catch (DuplicatePortNameException dpne) {
	    ProcessorCreationException pce = new ProcessorCreationException("Duplicate port names!");
	    pce.initCause(dpne);
	    throw pce;
	}
	catch (PortCreationException portce) {
	    ProcessorCreationException pce = new ProcessorCreationException("Port creation failure!");
	    pce.initCause(portce);
	    throw pce;
	}
	catch (Exception ex) {
	    ProcessorCreationException pce = new ProcessorCreationException("Unable to load wsdl at " +
									    wsdlLocation);
	    pce.initCause(ex);
	    pce.printStackTrace();
	    throw pce;
	}
    }


    /**
     * Build a single use WSIFOperation object. This should only be used
     * for a single invocation of the target service!
     */
    WSIFOperation getWSIFOperation() throws WSIFException {
	return port.createOperation(operationName, inputName, outputName);
    }
    

    /**
     * Get the properties for this processor for display purposes
     */
    public Properties getProperties() {
	Properties props = new Properties();
	props.put("wsdlLocation",getWSDLLocation());
	props.put("operation",getOperationName());
	return props;
    } 

    
    /**
     * Get the WSDL location for this processor
     */
    public String getWSDLLocation() {
	return this.wsdlLocation;
    }


    /**
     * Get the target endpoint for this processor
     */
    public String getResourceHost() {
	if (port instanceof WSIFPort_ApacheAxis) {
	    URL endpoint = ((WSIFPort_ApacheAxis)port).getEndPoint();
	    return endpoint.getHost();
	}
	else {
	    return "Unknown";
	}
    }
    
    String getTargetEndpoint() {
	if (port instanceof WSIFPort_ApacheAxis) {
	    return ((WSIFPort_ApacheAxis)port).getEndPoint().toString();
	}
	else {
	    return "Unknown";
	}
    }

    /**
     * Get the operation name for this processor
     */
    public String getOperationName() {
	return this.operationName;
    }

    private static void retrieveSignature(List parts, String[] names, Class[] types) {
        // get parts in correct order
        for (int i = 0; i < names.length; ++i) {
            Part part = (Part) parts.get(i);
            names[i] = part.getName();
            QName partType = part.getTypeName();
            if (partType == null) {
		partType = part.getElementName();
            }
            if (partType == null) {
                throw new RuntimeException("part " + names[i] + " must have type name declared");
            }
            // only limited number of types is supported
            // cheerfully ignoring schema namespace ...
            String s = partType.getLocalPart();
            if ("string".equals(s)) {
                types[i] = String.class;
	    } else if ("arrayof_xsd_string".equalsIgnoreCase(s) ||
		       "arrayofstring".equalsIgnoreCase(s)) {
		types[i] = String[].class;
            } else if ("double".equals(s)) {
                types[i] = Integer.TYPE;
            } else if ("float".equals(s)) {
                types[i] = Float.TYPE;
            } else if ("int".equals(s)) {
                types[i] = Integer.TYPE;
            } else if ("boolean".equals(s)) {
                types[i] = Boolean.TYPE;
            } else {
                throw new RuntimeException("part type " + partType + " not supported in this sample");
            }
        }
    }
    
    /**
     * Unwraps the top level part if this a wrapped DocLit message.
     */
    private static void unWrapIfWrappedDocLit(List parts, String operationName, Definition def) throws WSIFException {
	Part p = WSIFUtils.getWrappedDocLiteralPart(parts, operationName);
	if (p != null) {
	    List unWrappedParts = WSIFUtils.unWrapPart(p, def);
	    parts.remove(p);
	    parts.addAll(unWrappedParts);
	}
    }  
    
    /** 
     * Translate a java type into a taverna type string
     */
    private static String translateJavaType(Class type) {
	if (type.equals(String[].class)) {
	    return ("l('text/plain')");
	}
	else {
	    return ("'text/plain'");
	}
    }
        
}

