/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

// JDOM Imports
import org.jdom.Element;

import org.embl.ebi.escience.scuflworkers.stringconstant.StringConstantProcessor;
import java.lang.Exception;
import java.lang.String;



/**
 * A task to invoke a StringConstantProcessor
 * @author Tom Oinn
 */
public class StringConstantTask extends ProcessorTask {
    
    private static Logger logger = Logger.getLogger(StringConstantTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    
    public StringConstantTask(String id,Processor proc,LogLevel l,String userID, String userCtx) {
			super(id,proc,l,userID,userCtx);
    }
    
    protected java.util.Map execute(java.util.Map workflowInputMap) throws TaskExecutionException {
	try{
	   
	    StringConstantProcessor theProcessor = (StringConstantProcessor)proc;
	    // Get the output port, there is always only a single child for this task
	    Map outputMap = new HashMap();
	    outputMap.put("value",new DataThing(theProcessor.getStringValue()));
	    return outputMap;
	}
	catch(Exception ex) {
	    logger.error("Error invoking task " +getID() ,ex);
	    throw new TaskExecutionException("Task " + getID() + " failed");
	}	
    }
    
    public void cleanUpConcreteTask() {
    }

    public Element getProvenance() {
	return new Element("stringconstant",PROVENANCE_NAMESPACE);
    }

}
