/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;


import uk.ac.soton.itinnovation.taverna.enactor.entities.*;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.talisman.TalismanProcessor;
import org.embl.ebi.escience.talisman.tservice.TeaTray;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.core.entities.graph.GraphNode;
import uk.ac.soton.itinnovation.mygrid.workflow.enactor.io.Part;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;

// Network Imports
import java.net.URL;




/**
 * A task to invoke an RDFGeneratorProcessor
 * @author Tom Oinn
 */
public class RDFGeneratorTask extends ProcessorTask {
    
    private static Logger logger = Logger.getLogger(RDFGeneratorTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    
    public RDFGeneratorTask(String id,Processor proc,LogLevel l,String userID, String userCtx) {
	super(id,proc,l,userID,userCtx);
    }
    
    /**
     * NOT IMPLEMENTED YET!
     */
    protected java.util.Map execute(java.util.Map workflowInputMap) throws TaskExecutionException {
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
    
    public void cleanUpConcreteTask() {
    }
    
}
