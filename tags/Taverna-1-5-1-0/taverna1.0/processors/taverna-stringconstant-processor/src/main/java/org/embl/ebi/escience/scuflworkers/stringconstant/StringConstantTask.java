/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.stringconstant;

import java.util.HashMap;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;



/**
 * A task to invoke a StringConstantProcessor
 * @author Tom Oinn
 */
public class StringConstantTask implements ProcessorTaskWorker {
        
    
    private Processor proc;

    public StringConstantTask(Processor p) {
	this.proc = p;
    }
    
    public Map execute(java.util.Map workflowInputMap, IProcessorTask parentTask) throws TaskExecutionException {
	try{
	    StringConstantProcessor theProcessor = (StringConstantProcessor)proc;
	    // Get the output port, there is always only a single child for this task
	    Map outputMap = new HashMap();
	    outputMap.put("value",new DataThing(theProcessor.getStringValue()));
	    return outputMap;
	}
	catch(Exception ex) {
	    throw new TaskExecutionException("Couldn't create string constant!");
	}	
    }
    
}
