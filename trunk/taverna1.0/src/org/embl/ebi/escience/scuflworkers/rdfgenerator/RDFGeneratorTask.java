/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;


/**
 * A task to invoke an RDFGeneratorProcessor
 * @author Tom Oinn
 */
public class RDFGeneratorTask implements ProcessorTaskWorker {
    
    private static Logger logger = Logger.getLogger(RDFGeneratorTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private Processor proc;

    public RDFGeneratorTask(Processor p) {
	this.proc = p;
    }
    
    /**
     * NOT IMPLEMENTED YET!
     */
    public java.util.Map execute(java.util.Map workflowInputMap) throws TaskExecutionException {
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
