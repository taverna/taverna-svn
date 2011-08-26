/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;



/**
 * Echo the input list to the output list, does no
 * actual processing at all. This class is intended
 * to be used in conjunction with nested workflows
 * in order to split the iteration out from the previous
 * stage in the flow.
 * @author Tom Oinn
 */
public class EchoList implements LocalWorker {
    
    public String[] inputNames() {
	return new String[]{"inputlist"};
    }
    public String[] inputTypes() {
	return new String[]{"l('')"};
    }
    public String[] outputNames() {
	return new String[]{"outputlist"};
    }
    public String[] outputTypes() {
	return new String[]{"l('')"};
    }
    
    /**
     * Copy each entry in the input list into the output list iff
     * it matches the supplied regular expression.
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	Map outputMap = new HashMap();
	outputMap.put("outputlist",inputs.get("inputlist"));
	return outputMap;
    }

}
