/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.Map;


/**
 * Implementors of this interface provide concrete implementations
 * of the processor they reference.
 * @author Tom Oinn
 */
public interface ProcessorTaskWorker {
    
    /**
     * Given a map of name->DataThing value, invoke
     * the underlying task and return a map of
     * result name -> DataThing value.
     * @exception TaskExecutionException thrown if an error
     * occurs during task invocation
     */
    public Map execute(Map inputMap) throws TaskExecutionException;
    
}
