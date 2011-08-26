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
		if (inputObject == null) {
		    throw new TaskExecutionException("Input to web service '"+argName+"' was defined but not provided.");
		}
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
		if (value == null) {
		    value = inputObject.getDataObject();
		}
		input.setObjectPart(processor.inNames[i], value);
	    }
	    
	    operation.executeRequestResponseOperation(input, output, fault);
	    
	    // Debug - print out all available part names
	    for (Iterator i = output.getPartNames(); i.hasNext(); ) {
		System.out.println(i.next());
	    }

	    Map resultMap = new HashMap();
	    for (int i = 0; i < processor.outNames.length; i++) {
		String outputName = processor.outNames[i];
		resultMap.put(outputName, new DataThing(output.getObjectPart(outputName)));
	    }
	    
	    return resultMap;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    TaskExecutionException te = new TaskExecutionException("Error occured during invocation "+
								   ex.getMessage());
	    te.initCause(ex);
	    throw te;
	}
    }
  
}
