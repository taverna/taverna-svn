/**

 * This file is a component of the Taverna project,

 * and is licensed under the GNU LGPL.

 * Copyright Tom Oinn, EMBL-EBI

 */

package org.embl.ebi.escience.scuflworkers.wsdl;



import javax.wsdl.Binding;

import javax.wsdl.Operation;

import javax.wsdl.Port; // ambiguous with: org.embl.ebi.escience.scufl.Port 

import javax.wsdl.Service; // ambiguous with: javax.xml.rpc.Service 

import javax.wsdl.extensions.soap.SOAPAddress;

import javax.xml.namespace.QName;

import javax.xml.rpc.Call;

import org.apache.axis.wsdl.gen.Parser;

import org.apache.axis.wsdl.symbolTable.*;

import org.embl.ebi.escience.scufl.*;

import java.net.*;

// Utility Imports

import java.util.*;

import org.apache.axis.encoding.ser.ElementSerializerFactory;
import org.apache.axis.encoding.ser.ElementDeserializerFactory;
import org.apache.axis.encoding.ser.ElementDeserializer;

import java.lang.Class;

import java.lang.Exception;

import java.lang.Object;

import java.lang.RuntimeException;

import java.lang.String;

import java.lang.Thread;







/**

 * A processor based on an operation defined within 

 * a WSDL file accessible to the class at construction

 * time.

 * @author Tom Oinn

 */

public class WSDLBasedProcessor extends Processor implements java.io.Serializable {

    

    // Fields used by the configuration operations

    // Output types and names

    Vector outNames = new Vector();

    Vector outTypes = new Vector();

    // Input types and names

    Vector inNames = new Vector();

    Vector inTypes = new Vector();

    // WSDL Parser

    Parser wsdlParser = null;

    // Call object to invoke this processor

    Call call = null;

    

    private String wsdlLocation = null;

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
	String fullEndpoint = call.getTargetEndpointAddress();
	try {
	    URL endpointURL = new URL(fullEndpoint);
	    return endpointURL.getHost();
	}
	catch (MalformedURLException mue) {
	    return "Unknown!";
	}
    }
    

    private String operationName = null;

    /**

     * Get the operation name for this processor

     */

    public String getOperationName() {

	return this.operationName;

    }



    /**

     * Construct a new processor from the given WSDL definition

     * and operation name, delegates to superclass then instantiates

     * ports based on WSDL inspection.

     */

    public WSDLBasedProcessor(ScuflModel model, String name, String wsdlLocation, String operationName)

	throws ProcessorCreationException,

	       DuplicateProcessorNameException {

	super(model, name);

	this.wsdlLocation = wsdlLocation;

	this.operationName = operationName;

	// Load the WSDL document

	try {

	    wsdlParser = new Parser();

	    int retryCount = 3;

	    int retryDelay = 1000;

	    boolean loaded = false;

	    while (retryCount > 0 && !loaded) {

		try {

		    wsdlParser.run(wsdlLocation);

		    loaded = true;

		}

		catch (Exception ex2) {

		    retryCount--;

		    if (retryCount > 0) {

			Thread.sleep(retryDelay);

		    }

		    else {

			throw ex2;

		    }

		}

	    }

	}

	catch (Exception ex) {

	    ProcessorCreationException pce = new ProcessorCreationException("Unable to load wsdl at "+wsdlLocation);

	    pce.initCause(ex);

	    pce.printStackTrace();

	    throw pce;

	}



	// Set up input and output vectors and create the Call object

	configureFor(operationName);

	

	// Build ports from the input and output vectors

	try {

	    for (int i = 0; i < outNames.size(); i++) {

		String outName = (String)outNames.get(i);

		OutputPort outputPort = new OutputPort(this, outName);
		// Check whether there's a base type (which we can handle) or not...
		if (((Parameter)outTypes.get(i)).getType().isBaseType() ||
		    ((Parameter)outTypes.get(i)).getType().getQName().getLocalPart().startsWith("ArrayOf")) {
		    outputPort.setSyntacticType(xsdTypeToInternalType(((Parameter)outTypes.get(i)).getType().getQName().getLocalPart()));
		}
		else {
		    // Register the serializer for the element
		    ((org.apache.axis.client.Call)call).registerTypeMapping(org.w3c.dom.Element.class, 
									    ((Parameter)outTypes.get(i)).getType().getQName(),
									    new ElementSerializerFactory(),
									    new ElementDeserializerFactory());
		    outputPort.setSyntacticType("'text/xml'");
		}
		addPort(outputPort);

	    }

	    for (int i = 0; i < inNames.size(); i++) {

		String inName = (String)inNames.get(i);

		InputPort inputPort = new InputPort(this, inName);

		inputPort.setSyntacticType(xsdTypeToInternalType(((Parameter)inTypes.get(i)).getType().getQName().getLocalPart()));

		addPort(inputPort);

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

    }

    

    /**

     * Load information from the WSDL parser to populate the input and 

     * output vectors and create the Call object

     */

    private void configureFor(String operationName) 

	throws ProcessorCreationException {

	try {

	    String portName = null;

	    Service service = ((ServiceEntry)getSymTabEntry(null, ServiceEntry.class)).getService();

	    Port port = selectPort(service.getPorts(), portName);

	    portName = port.getName();

	    Binding binding = port.getBinding();

	    org.apache.axis.client.Service dpf = new org.apache.axis.client.Service(wsdlParser, service.getQName());

	    this.call = dpf.createCall(QName.valueOf(portName), QName.valueOf(operationName));
	    ((org.apache.axis.client.Call)this.call).setProperty(ElementDeserializer.DESERIALIZE_CURRENT_ELEMENT, Boolean.TRUE);
	    

	    SymbolTable symbolTable = wsdlParser.getSymbolTable();

	    BindingEntry bEntry = symbolTable.getBindingEntry(binding.getQName());

	    

	    // Iterate over the binding entry searching for the operation

	    // specified in the argument to the invokeMethod call.

	    Operation operation = null;

	    Parameters parameters = null;

	    for (Iterator i = bEntry.getParameters().keySet().iterator(); i.hasNext(); ) {

		Operation o = (Operation) i.next();

		if (o.getName().equals(operationName)) {

		    operation = o;

		    parameters = (Parameters) bEntry.getParameters().get(o);

		    // Populate the input and output descriptions

		    for (int j = 0; j < parameters.list.size(); j++) {

			Parameter p = (Parameter) parameters.list.get(j);

			if (p.getMode() == 1) { 	  // IN

			    inNames.add(p.getQName().getLocalPart());

			    inTypes.add(p);

			} else if (p.getMode() == 2) {	  // OUT

			    outNames.add(p.getQName().getLocalPart());

			    outTypes.add(p);

			} else if (p.getMode() == 3) {	  // INOUT

			    inNames.add(p.getQName().getLocalPart());

			    inTypes.add(p);

			    outNames.add(p.getQName().getLocalPart());

			    outTypes.add(p);

			}

		    }

		    // Set output type

		    if (parameters.returnParam != null) {

			// Get the QName for the return Type

			QName returnType = org.apache.axis.wsdl.toJava.Utils.getXSIType(parameters.returnParam);

			QName returnQName = parameters.returnParam.getQName();

			outNames.add(returnQName.getLocalPart());

			outTypes.add((Parameter)parameters.returnParam);

		    }

		    // Break out of the loop	

		    break;

		}

	    }

	    if ((operation == null) || (parameters == null)) {

		throw new RuntimeException(operationName + " was not found.");

	    }

	    

	}

	catch (Exception e) {

	    e.printStackTrace();

	    ProcessorCreationException pce = new ProcessorCreationException("Problem initialising from wsdl at "+wsdlLocation);

	    pce.initCause(e);

	    throw pce;

	}

    }



    /**

     * Convert an XSD type into a Baclava type string

     */

    public String xsdTypeToInternalType(String xsdType) {

	// System.out.println("Type conversion requested for "+xsdType);

	// Can cope with String types and nested strings

	if (xsdType.startsWith("ArrayOf_")) {

	    return ("l("+xsdTypeToInternalType(xsdType.replaceFirst("ArrayOf_",""))+")");

	}

	else if (xsdType.startsWith("ArrayOf")) {

	    return ("l("+xsdTypeToInternalType(xsdType.replaceFirst("ArrayOf",""))+")");

	}

	else if (xsdType.equalsIgnoreCase("base64")) {

	    return "'application/octet-stream'";

	}

	else {

	    return "'text/plain'";

	    /**

	       if (xsdType.equalsIgnoreCase("string")) {

	       return "'text/plain'";

	       }

	       else {

	       return "'text/x-xsd-unknown-type-"+xsdType+"'";

	       }

	    */

	}

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

     * Method getSymTabEntry

     * Search the symbol table for an entry with the specified

     * class and optionally QName. If the QName is not specified

     * then examine all entries.

     */

    public SymTabEntry getSymTabEntry(QName qname, Class cls) {

	HashMap map = wsdlParser.getSymbolTable().getHashMap();

	Iterator iterator = map.entrySet().iterator();



	while (iterator.hasNext()) {

	    Map.Entry entry = (Map.Entry) iterator.next();

	    QName key = (QName) entry.getKey();

	    Vector v = (Vector) entry.getValue();



	    if ((qname == null) || qname.equals(qname)) {

		for (int i = 0; i < v.size(); ++i) {

		    SymTabEntry symTabEntry = (SymTabEntry) v.elementAt(i);



		    if (cls.isInstance(symTabEntry)) {

			return symTabEntry;

		    }

		}

	    }

	}

	return null;

    }



    /**

     * Given a map of ports, iterate across it until either

     * a port matching the portName parameter is found or until

     * any port is found with a SOAPAddress extensibility element.

     * Return null if no such port can be located.

     */

    public Port selectPort(Map ports, String portName) throws Exception {

	Iterator valueIterator = ports.keySet().iterator();

	while (valueIterator.hasNext()) {

	    String name = (String) valueIterator.next();

	    if ((portName == null) || (portName.length() == 0)) {

		Port port = (Port) ports.get(name);

		List list = port.getExtensibilityElements();

		for (int i = 0; (list != null) && (i < list.size()); i++) {

		    Object obj = list.get(i);

		    if (obj instanceof SOAPAddress) {

			return port;

		    }

		}

	    } else if ((name != null) && name.equals(portName)) {

		return (Port) ports.get(name);

	    }

	}

	return null;

    }



}

