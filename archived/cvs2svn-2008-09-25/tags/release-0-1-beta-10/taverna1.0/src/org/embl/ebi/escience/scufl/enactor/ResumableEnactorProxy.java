/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.enactor;

/**
 * Extends the EnactorProxy interface with the addition of
 * methods to allow the location of and connection to previously
 * submitted workflow instances. This is intended to allow,
 * for example, a long running workflow submitted using the
 * workbench to be examined some weeks later for results. This
 * interface really only applies to remote enactors, an in process
 * one is pretty much by definition not going to support this
 * functionality.
 * @author Tom Oinn
 */
public interface ResumableEnactorProxy extends EnactorProxy {

    /**
     * Get a list of all the known workflow instances, subject
     * to authentication and security constraints, that the
     * enactor represented by this proxy is aware of. It is
     * expected that the creation of the proxy implementation
     * will involve specification of user authorities or identities
     * and that these identities will be used to only present
     * the user of this API with those workflow instance keys
     * that they have permission to connect to.
     * @return String[] containing the identifiers of all
     * visible workflow instances within this enactor
     */
    public String[] getWorkflowInstanceIDList();

    /**
     * Get a WorkflowInstance implementation corresponding to
     * the running workflow with the supplied identifier.
     * The identifier scheme is up to the implementation, we
     * would suggest use of LSID but this is not mandated by
     * this specification.
     * @return WorkflowInstance implementation for the supplied
     * workflow identifier
     * @exception UnknownWorkflowInstanceException if the proxy does
     * not know about the supplied identifier, or if the
     * calling user doesn not have the permissions required
     * to connect to it.
     */
    public WorkflowInstance connectToWorkflow(String workflowInstanceID)
	throws UnknownWorkflowInstanceException;
    
}
