/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

import org.apache.axis.utils.*;

// Utility Imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;
import java.util.Iterator;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;

import org.apache.wsif.WSIFException;
import org.apache.wsif.WSIFMessage;
import org.apache.wsif.WSIFOperation;
import org.apache.wsif.WSIFPort;
import org.apache.wsif.WSIFService;
import org.apache.wsif.WSIFServiceFactory;
import org.apache.wsif.providers.soap.apacheaxis.WSIFDynamicProvider_ApacheAxis;
import org.apache.wsif.util.WSIFPluggableProviders;
import org.apache.wsif.util.WSIFUtils;

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
 * The task required to invoke an arbitrary web service.
 * @author Tom Oinn
 */
public class WSDLInvocationTask implements ProcessorTaskWorker {
    
    private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    //private static Service service = new org.apache.axis.client.Service();
    private WSDLBasedProcessor processor;
    
    public WSDLInvocationTask(Processor p) {
	this.processor = (WSDLBasedProcessor)p;
    }
    
    public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
	try {
	    // Obtain an instance of the WSIFOperation from the parent processor
	    WSIFOperation operation = processor.getWSIFOperation();
	    WSIFMessage input = operation.createInputMessage();
	    WSIFMessage output = operation.createOutputMessage();
	    WSIFMessage fault = operation.createFaultMessage();
	    // Iterate over the inputs...
	    for (int i = 0; i < processor.inNames.length; i++) {
		Object value = null;
		Class c = processor.inTypes[i];
		String argName = processor.inNames[i];
		DataThing inputObject = (DataThing)inputMap.get(argName);
		if (processor.documentPartQName != null) {
		    DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();
		    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
		    Document document = builder.newDocument();
		    System.out.println("Attempting to create DOM, top level node has namespace "+processor.documentPartQName.getNamespaceURI());
		    
		    Element root = (Element)document.createElementNS(processor.documentPartQName.getNamespaceURI(),
								     processor.documentPartQName.getLocalPart());
		    // Hack to introduce the namespace back as an attribute. There has to be a nicer
		    // way of doing it than this but it would seem not.
		    Attr attr = document.createAttribute("xmlns");
		    attr.setValue(processor.documentPartQName.getNamespaceURI());
		    root.setAttributeNode(attr);
		    for (Iterator j = processor.inputDescriptions.iterator(); j.hasNext();) {
			InputDescription idesc = (InputDescription)j.next();
			String inputName = idesc.elementName.getLocalPart();
			DataThing inputThing = (DataThing)inputMap.get(inputName);
			if (inputThing != null) {
			    if (inputThing.getDataObject() instanceof String) {
				String contents = (String)inputThing.getDataObject();
				root.appendChild(idesc.getElement(contents, document));
			    }
			    else if (inputThing.getDataObject() instanceof List) {
				List l = (List)inputThing.getDataObject();
				for (Iterator k = l.iterator(); k.hasNext();) {
				    String item = (String)k.next();
				    root.appendChild(idesc.getElement(item, document));
				}
			    }			    
			}
			else {
			    if (idesc.isOptional == false) {
				throw new TaskExecutionException("Parameter '"+inputName+"' is mandatory for this service");
			    }
			}
		    }
		    document.appendChild(root);
		    value = root;

		}
		else {
		    if (inputObject == null) {
			throw new TaskExecutionException("Input to web service '"+argName+"' was defined but not provided.");
		    }
		    // Check whether the input port has been flagged as text/xml and create a DOM Node if so
		    if (c.equals(org.w3c.dom.Element.class)) {
			try {
			    System.out.println("Trying to create dom...");
			    // create a new Document
			    DocumentBuilder builder = DocumentBuilderFactory.newInstance().newDocumentBuilder();
			    String dataObject = (String)inputObject.getDataObject();
			    Document doc = builder.parse(new ByteArrayInputStream(dataObject.getBytes()));
			    value = doc.getDocumentElement();
			}
			catch (Exception ex) {
			    throw new TaskExecutionException("Operation requires an XML complex type but\n invalid XML was supplied : "+ex.getMessage());
			}
			/**
			   TransformerFactory tFactory = TransformerFactory.newInstance();
			   Transformer transformer = tFactory.newTransformer();
			   DOMSource source = new DOMSource(doc.getDocumentElement());
			   ByteArrayOutputStream baos = new ByteArrayOutputStream();
			   transformer.transform(source, new StreamResult(baos));
			   System.out.println(baos.toString());
			*/
		    }
		    else {
			// If the datathing contains a string and the service wants something else...
			if (inputObject.getDataObject() instanceof String) {
			    String argString = (String)inputObject.getDataObject();
			    if (c.equals(Double.TYPE)) {
				value = new Double(argString);
			    }
			    else if (c.equals(Float.TYPE)) {
				value = new Float(argString);
			    }
			    else if (c.equals(Integer.TYPE)) {
				value = new Integer(argString);
			    }
			    else if (c.equals(Boolean.TYPE)) {
				value = new Boolean(argString);
			    }
			}
		    }
		    if (value == null) {
			value = inputObject.getDataObject();
		    }
		}
		input.setObjectPart(processor.inNames[i], value);
	    }
	    
	    boolean respOK = operation.executeRequestResponseOperation(input, output, fault);
	    
	    if (respOK) {
		// Operation succeeded - extract output parts
		Map resultMap = new HashMap();
		for (int i = 0; i < processor.outNames.length; i++) {
		    String outputName = processor.outNames[i];
		    Object resultObject = output.getObjectPart(outputName);
		    if (resultObject instanceof Node) {
			// If the output is an instance of Node then 
			// convert it to a text/xml form.
			Node node = (Node)resultObject;
			TransformerFactory tFactory = TransformerFactory.newInstance();
			Transformer transformer = tFactory.newTransformer();
			DOMSource source = new DOMSource(node.getOwnerDocument());
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			transformer.transform(source, new StreamResult(baos));
			resultObject = baos.toString();
		    }
		    resultMap.put(outputName, new DataThing(resultObject));
		}
		return resultMap;
	    }
	    else {
		// Operation failed - extract failure parts
		StringBuffer errorMessage = new StringBuffer();
		errorMessage.append("Error from WSIF based invocation :\n");
		for (Iterator i = fault.getPartNames(); i.hasNext();) {
		    String name = (String)i.next();
		    errorMessage.append(fault.getObjectPart(name).toString());
		    if (i.hasNext()) {
			errorMessage.append("\n");
		    }
		}
		throw new TaskExecutionException(errorMessage.toString());
	    }
	}
	catch (Exception ex) {
	    if (ex instanceof TaskExecutionException) {
		throw (TaskExecutionException)ex;
	    }
	    ex.printStackTrace();
	    TaskExecutionException te = new TaskExecutionException("Error occured during invocation "+
								   ex.getMessage());
	    te.initCause(ex);
	    throw te;
	}
    }
  
}
