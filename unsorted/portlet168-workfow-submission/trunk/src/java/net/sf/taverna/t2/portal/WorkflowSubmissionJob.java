/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

/**
 *
 * @author Alex Nenadic
 */
public class WorkflowSubmissionJob {

    // Workflow resource UUID received from a T2 Server
    // after submitting a wf for submission
    private String uuid;

    // Name of the workflow file submitted
    private String workflowFileName;

    // Workflow inputs?

    // Status of the submitted job
    private String status;

    public WorkflowSubmissionJob(String uuid, String workflowFileName, String status){
        this.uuid = uuid;
        this.workflowFileName = workflowFileName;
        this.status = status;
    }

    /**
     * @return the uuid
     */
    public String getUuid() {
        return uuid;
    }

    /**
     * @param uuid the uuid to set
     */
    public void setUuid(String uuid) {
        this.uuid = uuid;
    }

    /**
     * @return the workflowFileName
     */
    public String getWorkflowFileName() {
        return workflowFileName;
    }

    /**
     * @param workflowFileName the workflowFileName to set
     */
    public void setWorkflowFileName(String workflowFileName) {
        this.workflowFileName = workflowFileName;
    }

    /**
     * @return the status
     */
    public String getStatus() {
        return status;
    }

    /**
     * @param status the status to set
     */
    public void setStatus(String status) {
        this.status = status;
    }

}
