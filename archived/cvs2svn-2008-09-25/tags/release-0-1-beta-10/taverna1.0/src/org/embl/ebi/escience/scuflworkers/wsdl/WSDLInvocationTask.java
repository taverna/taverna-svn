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
import org.apache.axis.utils.*;

// Utility Imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

import org.embl.ebi.escience.scuflworkers.wsdl.WSDLBasedProcessor;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;
import java.lang.System;



/**
 * The task required to invoke an arbitrary web service.
 * @author Tom Oinn
 */
public class WSDLInvocationTask implements ProcessorTaskWorker {
    
    private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private static Service service = new org.apache.axis.client.Service();
    private Processor proc;

    private Call getCall() {
	synchronized (service) {
	    try {
		return (Call) service.createCall();
	    }
	    catch (Exception ex) {
		throw new RuntimeException(ex);
	    }
	}
    }
    
    public WSDLInvocationTask(Processor p) {
	this.proc = p;
    }
    
    public Map execute(Map inputMap) throws TaskExecutionException {
	try {
	    WSDLBasedProcessor p = (WSDLBasedProcessor)proc;
	    Vector outNames = p.outNames;
	    synchronized (p.call) {
		Call call = (org.apache.axis.client.Call)p.call;
		Object[] args = new Object[p.getInputPorts().length];
		for (int i = 0; i < args.length; i++) {
		    DataThing theData = (DataThing)inputMap.get(p.getInputPorts()[i].getName());
		    Object theDataObject = theData.getDataObject();
		    // Check for the case of List of String and convert to a String[]
		    if (theDataObject instanceof List) {
			if (((List)theDataObject).isEmpty()) {
			    theDataObject = new String[0];
			}
			else {
			    Object firstItem = ((List)theDataObject).get(0);
			    if (firstItem instanceof String) {
				theDataObject = ((List)theDataObject).toArray(new String[0]);
			    }
			}
		    }
		    args[i] = theDataObject;
		    if (args[i] == null) {
			throw new TaskExecutionException("Null argument not allowed, check preceeding processors!");
		    }
		    System.out.println("Data thing for port "+p.getInputPorts()[i].getName());
		    System.out.println(theData.getDataObject());
		}
		call.setTimeout(new Integer(0));
		
		Object[] parsedArgs = new Object[args.length];
		for (int i = 0; i < parsedArgs.length; i++) {
		    if (args[i] instanceof String) {
			try {
			    parsedArgs[i] = new Integer((String)args[i]);
			}
			catch (NumberFormatException nfe) {
			    try {
				parsedArgs[i] = new Float((String)args[i]);
			    }
			    catch (NumberFormatException nfe2) {
				
				if (((String)args[i]).equalsIgnoreCase("false")) {
				    parsedArgs[i] = Boolean.FALSE;
				}
				else if (((String)args[i]).equalsIgnoreCase("true")) {
				    parsedArgs[i] = Boolean.TRUE;
				}
				else {
				    parsedArgs[i] = args[i];
				}
				
				
			    }
			}
		    }
		    else {
			parsedArgs[i] = args[i];
		    }
		}
		
		Object ret = call.invoke(parsedArgs);
		Map outputs = call.getOutputParams();
		HashMap map = new HashMap();
		for (int pos = 0; pos < p.outNames.size(); ++pos) {
		    String name = (String)outNames.get(pos);
		    Object value = outputs.get(name);
		    if ((value == null) && (pos == 0)) {
			if (ret instanceof org.w3c.dom.Element) {
			    // Convert to string of xml
			    ret = XMLUtils.ElementToString((org.w3c.dom.Element)ret);
			}
			map.put(name, DataThingFactory.bake(ret));
		    }
		    else {
			if (value instanceof org.w3c.dom.Element) {
			    // Convert to string of xml
			    value = XMLUtils.ElementToString((org.w3c.dom.Element)value);
			}
			map.put(name, DataThingFactory.bake(value));
		    }
		}
		return map;
	    }
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
