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
import org.apache.wsif.providers.ProviderUtils;
import org.apache.wsif.schema.ComplexType;
import org.apache.wsif.schema.ElementType;
import org.apache.wsif.schema.Parser;
import org.apache.wsif.schema.*;


/**
 * A processor based on an operation defined within 
 * a WSDL file accessible to the class at construction
 * time. Much of the wsdl parsing code is based on that
 * found in the dynamic invocation sample in the apache
 * axis project (http://ws.apache.org)
 * @author Tom Oinn
 */

public class WSDLBasedProcessor extends Processor implements java.io.Serializable {

    public int getMaximumWorkers() {
	return 10;
    }

    WSIFPort port = null;
    WSIFService dpf = null;
    String operationName = null;
    String inputName = null;
    String outputName = null;
    String wsdlLocation = null;
    String[] inNames, outNames;
    Class[] inTypes, outTypes;
    boolean isWrappedDocLit = false;
    // If this is defined then we're using the input part munging code
    QName documentPartQName = null;
    List inputDescriptions = new ArrayList();
    
    public WSDLBasedProcessor(ScuflModel model, String procName, String wsdlLocation, String operationName)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	this(model, procName, wsdlLocation, operationName, null);
    }

    /**
     * Construct a new processor from the given WSDL definition
     * and operation name, delegates to superclass then instantiates
     * ports based on WSDL inspection.
     */
    public WSDLBasedProcessor(ScuflModel model, String procName, String wsdlLocation, String operationName, QName portTypeName)
	throws ProcessorCreationException,
	       DuplicateProcessorNameException {
	super(model, procName);
	this.wsdlLocation = wsdlLocation;
	this.operationName = operationName;
	if (this.isOffline()) {
	    return;
	}
	
	
	// Configure to use axis then read the WSDL
	WSIFPluggableProviders.overrideDefaultProvider("http://schemas.xmlsoap.org/wsdl/soap/",
						       new WSIFDynamicProvider_ApacheAxis());
	Definition def = null;
	try {
	    def = WSIFUtils.readWSDL(null, wsdlLocation);
	}
	catch (Exception ex) {
	    ProcessorCreationException pce = new ProcessorCreationException(procName+": Unable to load wsdl at " +
									    wsdlLocation);
	    pce.initCause(ex);
	    pce.printStackTrace();
	    throw pce;
	}

	try {
	    // Select the default service
	    Service service = WSIFUtils.selectService(def, null, null);
	    
	    // Select the default port type
	    PortType portType = WSIFUtils.selectPortType(def, 
							 portTypeName == null ? null : portTypeName.getNamespaceURI(), 
							 portTypeName == null ? null : portTypeName.getLocalPart());
	    WSIFServiceFactory factory = WSIFServiceFactory.newInstance();
	    dpf = factory.getService(def, service, portType);
	    
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
		    throw new ProcessorCreationException(procName+": Operation "+name+" is overloaded in WSDL at "+wsdlLocation);
		}
		op = opTemp;
		found = true;
		Input opInput = op.getInput();
		inputName = (opInput.getName() == null) ? null : opInput.getName();
		Output opOutput = op.getOutput();
		outputName = (opOutput.getName() == null) ? null : opOutput.getName();
	    }
	    if (!found) {
		throw new ProcessorCreationException(procName+": Unable to locate operation "+operationName+" in WSDL at "+wsdlLocation);
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

		// Check for single length list with complex types and not wrapped doc lit (!)
		if (parts.size() == 1 && !isWrappedDocLit) {
		    Part documentPart = (Part)parts.get(0);
		    if (documentPart.getElementName() != null) {
			
			System.out.println("Top level part has complex type "+documentPart.getElementName().toString());
			documentPartQName = documentPart.getElementName();			

			List schemaTypes = new ArrayList();
			Parser.getAllSchemaTypes(def, schemaTypes, null);
			// Get the QName of the part we're expanding
			QName partQN = documentPart.getElementName();
			ElementType et = getElementType(schemaTypes, partQN);
			List children = et.getChildren();
			ComplexType ct = null;
			if (children == null || children.size() < 1) {
			    ct = getComplexType(schemaTypes, et.getElementType());
			}
			else {
			    ct = (ComplexType)children.get(0);
			}
			SequenceElement[] se = ct.getSequenceElements();
			for (int i = 0; i < se.length; i++) {
			    // Fully qualified element name of the subpart
			    QName elementName = se[i].getTypeName();
			    // Type definition
			    QName elementType = PermissionHack.getXMLAttribute(se[i], new QName("type"));
			    
			    QName temp = PermissionHack.getXMLAttribute(se[i], new QName("minOccurs"));
			    boolean isOptional = false;
			    if (temp != null) {
				if (temp.getLocalPart().equals("0")) {
				    isOptional = true;
				}
			    }
			    temp = PermissionHack.getXMLAttribute(se[i], new QName("maxOccurs"));
			    boolean isArray = true;
			    if (temp != null) {
				if (temp.getLocalPart().equals("1")) {
				    isArray = false;
				}
			    }
			    inputDescriptions.add(new InputDescription(elementName, elementType, isOptional, isArray));
			    
			    //System.out.println(new InputDescription(elementName, elementType, isOptional, isArray));
			    
			    
			    //System.out.println(type);
			    //System.out.println(PermissionHack.getXMLAttribute(se[i], new QName("type")));
			    //System.out.println(PermissionHack.getXMLAttribute(se[i], new QName("minOccurs")));
			    //System.out.println(PermissionHack.getXMLAttribute(se[i], new QName("maxOccurs")));
			}
			
		    }
		}
		
		if (documentPartQName == null) {
		    for (int i = 0; i < count; i++) {
			InputPort inputPort = new InputPort(this, inNames[i]);
			inputPort.setSyntacticType(translateJavaType(inTypes[i]));
			addPort(inputPort);
		    }
		}
		else {
		    for (Iterator i = inputDescriptions.iterator(); i.hasNext();) {
			InputDescription idesc = (InputDescription)i.next();
			InputPort inputPort = new InputPort(this, idesc.elementName.getLocalPart());
			inputPort.setSyntacticType(idesc.getTavernaType());
			addPort(inputPort);
		    }
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
	catch (ProcessorCreationException pce) {
	    throw pce;
	}
	catch (Exception ex) {
	    ProcessorCreationException pce = new ProcessorCreationException(procName+": "+ex.getMessage());
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

    private void retrieveSignature(List parts, String[] names, Class[] types) {
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
		// Hmmm, interesting. This is therefore a type that we haven't seen before
		// and need to fudge things by registering a new serializer and deserializer
		// with the WSIFService instance.
                // throw new RuntimeException("part type " + partType + " not supported in this sample");
		types[i] = org.w3c.dom.Element.class;
		// Map to the Element type
		try {
		    dpf.mapType(partType, org.w3c.dom.Element.class);
		    dpf.mapType(new QName(partType.getNamespaceURI(), getOperationName()+"Response"),
				org.w3c.dom.Element.class);
		}
		catch (WSIFException wsife) {
		    wsife.printStackTrace();
		}
	    }
        }
    }
    
    /**
     * Unwraps the top level part if this a wrapped DocLit message.
     */
    private void unWrapIfWrappedDocLit(List parts, String operationName, Definition def) throws WSIFException {
	Part p = ProviderUtils.getWrapperPart(parts, operationName);
	if (p != null) {
	    //dpf.mapType(p.getTypeName(), org.w3c.dom.Element.class);
	    List unWrappedParts = ProviderUtils.unWrapPart(p, def);
	    parts.remove(p);
	    parts.addAll(unWrappedParts);
	    isWrappedDocLit = true;
	}
	
    }  
    
    /** 
     * Translate a java type into a taverna type string
     */
    static String translateJavaType(Class type) {
	if (type.equals(String[].class)) {
	    return "l('text/plain')";
	}
	else if (type.equals(org.w3c.dom.Element.class)) {
	    return "'text/xml'";
	}
	else {
	    return "'text/plain'";
	}
    }
        
    protected static ElementType getElementType(List l, QName qn) {
    	ElementType et = null;
	for (int i=0; i<l.size() && et==null; i++ ){
	    Object o = l.get(i);
	    if ( o instanceof ElementType ) {
                QName etQN = ((ElementType)o).getTypeName();
		if ( qn.equals(etQN) ){
		    et = (ElementType)o;
		}
	    }
	}
	return et;
    }
    
    protected static ComplexType getComplexType(List l, QName type) {
       	ComplexType ct = null;
    	if (type != null && l != null) {
	    String name = type.getLocalPart();
	    for (int i=0; i<l.size() && ct==null; i++ ) {
		Object o = l.get(i);
		if (o instanceof ComplexType) {
		    if (name.equals(  ((ComplexType)o).getTypeName().getLocalPart() )){
			ct = (ComplexType)o;
		    }
		}
	    }
    	}
	return ct;
    }
    
}

