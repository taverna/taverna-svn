/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import java.util.HashMap;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;


/**
 * A task to invoke an RDFGeneratorProcessor
 * @author Tom Oinn
 */
public class RDFGeneratorTask implements ProcessorTaskWorker {
        

    public RDFGeneratorTask(Processor p) {
	
    }
    
    /**
     * NOT IMPLEMENTED YET!
     */
    public java.util.Map execute(java.util.Map workflowInputMap, IProcessorTask parentTask) throws TaskExecutionException {
	/**
	   try{
	   //
	   }
	   catch(Exception ex) {
	   logger.error("Error invoking task " +getID() ,ex);
	   throw new TaskExecutionException("Task " + getID() + " failed");
	   }
	*/
	return new HashMap();
    }
       
}
