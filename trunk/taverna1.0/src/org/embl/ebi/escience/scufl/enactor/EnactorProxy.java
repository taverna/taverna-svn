/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.*;
import java.util.*;

/**
 * This interface defines the user proxy for a workflow enactment
 * system capable of running workflows based on ScuflModel instances
 * @author Tom Oinn
 */
public interface EnactorProxy {
    
    /**
     * Submit a workflow to the enactor represented by this proxy,
     * the workflow submission is in the form of a ScuflModel
     * instance, and a WorkflowInstance implementation is returned
     * to allow further interaction with the running state.
     */
    public WorkflowInstance submitWorkflow(ScuflModel workflow);

}
