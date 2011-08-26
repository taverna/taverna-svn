/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.talisman.tservice.TeaTray;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;
import java.lang.Exception;
import java.lang.String;



/**
 * A task to invoke a TalismanProcessor
 * @author Tom Oinn
 */
public class TalismanTask implements ProcessorTaskWorker {
    private static Logger logger = Logger.getLogger(TalismanTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private Processor proc;
        
    public TalismanTask(Processor p) {
	this.proc = p;
    }
    
    public Map execute(java.util.Map workflowInputMap) throws TaskExecutionException {
	try{
	   
	    TalismanProcessor theProcessor = (TalismanProcessor)proc;
	    
	    // Get a map of the inputs, for each entry in the map the key is the
	    // name of the port that the input came in on, the value is the value
	    // of the input (obviously)
	    // Map inputMap = new HashMap();
	    // Parents are all PortTasks (I think?)
	    /*
	      GraphNode[] inputs = getParents();
	      for (int i = 0; i < inputs.length; i++) {
	      if(inputs[i] instanceof PortTask) {
	      PortTask pt = (PortTask)inputs[i];
	      Part p = pt.getData();
	      Element e = (Element)p.getValue();
	      inputMap.put(p.getName(),e.getFirstChild().getNodeValue());
	      }
	      
	      }
	    */
	    Map inputMap = new HashMap();
	    for (Iterator i = workflowInputMap.keySet().iterator(); i.hasNext(); ) {
			String portName = (String)(i.next());
			DataThing theDataThing = (DataThing)workflowInputMap.get(portName);
			inputMap.put(portName, theDataThing.getDataObject());
	    }
	    
	    // Get the parameters for this invocation
	    Map talismanInputMap = theProcessor.getInputMappings();
	    Map talismanOutputMap = theProcessor.getOutputMappings();
	    String definitionURL = theProcessor.getTalismanDefinitionURL();
	    String triggerName = theProcessor.getTriggerName();

	    // Create a new Talisman session etc.
	    // Should use a singleton one of these here! Can't at the moment
	    // because there's no way of cleaning up sessions, if we don't use
	    // the singleton they'll be collected like any other object.
	    TeaTray teaTray = new TeaTray();
	    URL url = new URL(definitionURL);
	    String sessionID = teaTray.createSession(url);
	    
	    // Iterate over the talismanInputMap....
	    for (Iterator i = talismanInputMap.keySet().iterator(); i.hasNext(); ) {
		// portName is the name of one of the ports on this processor
		String portName = (String)i.next();
		// talismanName is the specifier for a field in the talisman session
		String talismanName = (String)talismanInputMap.get(portName);
		// portValue is the string value of the port, and it had better be
		// a string or there will be much bitching!
		String portValue = (String)inputMap.get(portName.toLowerCase());
		// Set the value in the talisman session
		logger.debug("Setting value : "+talismanName+" to "+portValue);
		teaTray.setStringValue(sessionID, talismanName, portValue);
	    }
	    
	    // Invoke the trigger
	    teaTray.invokeTrigger(sessionID, triggerName);
	    
	    Map outMap = new HashMap();
	    // Iterate over the talismanOutputMap....
	    for (Iterator i = talismanOutputMap.keySet().iterator(); i.hasNext(); ) {
		// portName is the name of the port the value should be sent to
		String portName = (String)i.next();
		// talismanName is the name of the field the value should be read from
		String talismanName = (String)talismanOutputMap.get(portName);
		// talismanValue is the contents of the field defined in talismanName
		String talismanValue = teaTray.getStringValue(sessionID, talismanName);
		logger.debug("Creating output - portName = "+portName+", fieldName = "+talismanName);
		
		outMap.put(portName, new DataThing(talismanValue));
	    }
	    
	    // Done? I think so anyway.
	    
	    // Success
	    return outMap;
	}
	catch(Exception ex) {
	    logger.error("Error invoking talisman" ,ex);
	    throw new TaskExecutionException("Task failed due to problem invoking talisman");
	}	
    }
    
}
