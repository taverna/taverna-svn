/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

import org.embl.ebi.escience.scufl.ScuflModel;

// Utility Imports
import java.util.Map;

import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.WorkflowSubmissionException;
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
     * to allow further interaction with the running state.<br>
     * The inputs parameter is used to supply any known inputs at the
     * time of workflow submission. Some enactor implementations may
     * only allow input specification at this stage, but others may
     * allow subsequent inputs to be added through the setInputs method
     * in the returned WorkflowInstance.
     * @param workflow The workflow model to enact
     * @param inputs A Map of DataThing objects with keys corresponding
     * to the named input parameters of the workflow.
     * @exception WorkflowSubmissionException thrown if the workflow
     * submission fails for some reason. This exception is only used
     * to wrap the real exception using standard exception chaining
     * mechanisms.
     */
    public WorkflowInstance submitWorkflow(ScuflModel workflow, Map inputs)
	throws WorkflowSubmissionException;

}
