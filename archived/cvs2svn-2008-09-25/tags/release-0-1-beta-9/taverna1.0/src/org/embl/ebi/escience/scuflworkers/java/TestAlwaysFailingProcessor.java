/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;



/**
 * A processor that always fails instantly, useful for testing the
 * reliability functions that we're putting in.
 * @author Tom Oinn
 */
public class TestAlwaysFailingProcessor implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"foo","bar"};
    }
    public String[] inputTypes() {
	return new String[]{"'text/plain'","'text/plain'"};
    }
    public String[] outputNames() {
	return new String[]{"urgle"};
    }
    public String[] outputTypes() {
	return new String[]{"'text/plain'"};
    }
    
    /**
     * Just throw an exception!
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	throw new TaskExecutionException("This processor always fails!");
    }


}
