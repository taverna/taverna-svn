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
     * Submit a workflow to the enactor represented by this proxy.
     * The workflow submission is provided in the form of a ScuflModel
     * instance, and a WorkflowInstance implementation is returned
     * to allow further interaction with the workflow state.  Calling
     * this method compiles the workflow but doesn't start it running.
     * This allows registering listeners on the WorkflowInstance state
     * before it starts running.<br>
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
    public WorkflowInstance compileWorkflow(ScuflModel workflow, Map inputs, UserContext user)
	throws WorkflowSubmissionException;
    
    /**
     * Submit to the enactor represented by this proxy for compilation the
     * workflow submission.
     * The workflow submission is provided in the form of a ScuflModel
     * instance, and a WorkflowInstance implementation is returned
     * to allow further interaction with the workflow state.  Calling
     * this method compiles the workflow but doesn't set its inputs or start 
     * it running.  This allows registering listeners on the WorkflowInstance state
     * before it starts running.
     */
    public WorkflowInstance compileWorkflow(ScuflModel workflow, UserContext user)
	throws WorkflowSubmissionException;
    
}
