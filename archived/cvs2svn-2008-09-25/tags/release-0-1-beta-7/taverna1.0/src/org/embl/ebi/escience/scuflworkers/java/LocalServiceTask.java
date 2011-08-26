/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

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




/**
 * A task to invoke a LocalServiceProcessor
 * @author Tom Oinn
 */
public class LocalServiceTask extends ProcessorTask {
    
    private static Logger logger = Logger.getLogger(LocalServiceTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    
    public LocalServiceTask(String id,Processor proc,LogLevel l,String userID, String userCtx) {
	super(id,proc,l,userID,userCtx);
    }
    
    protected Map execute(Map inputMap) throws TaskExecutionException {
	LocalServiceProcessor theProcessor = (LocalServiceProcessor)proc;
	return theProcessor.getWorker().execute(inputMap);
    }
    
    public void cleanUpConcreteTask() {
    }
    
    public Element getProvenance() {
	return new Element("local",PROVENANCE_NAMESPACE);
    }

}
