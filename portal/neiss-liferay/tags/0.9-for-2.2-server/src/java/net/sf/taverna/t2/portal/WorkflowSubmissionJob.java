/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package net.sf.taverna.t2.portal;

import java.util.Date;

/**
 *
 * @author Alex Nenadic
 */
public class WorkflowSubmissionJob {

    // Workflow job resource UUID received from a T2 Server
    // after submitting a wf for submission
    private String uuid;

    // Workflow submitted for execution as part fo this job
    private Workflow workflow;

    // Name of the file of the workflow submitted for execution
    //private String workflowFileName;

    // URI of the workflow resource on myExperiment, if this is an execution
    // of a workflow from myExperiment
    //private String workflowMyExperimentResource;

    // Description of the job as entered by the user
    private String workflowRunDescription;

    // No point in caching inputs and outputs, they can be large.
    // Load them from the file on disk where they are saved each
    // time user wants to see them.
    // Workflow inputs
    private Object inputs;
    // Workflow outputs
    private Object outputs;

    // Status of the submitted job
    private String status;

    private Date startDate;

    private Date endDate;

    public WorkflowSubmissionJob(String uuid, Workflow workflow, String status, String workflowRunDescription){
        this.uuid = uuid;
        this.workflow = workflow;
        this.status = status;
        this.workflowRunDescription = workflowRunDescription;
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

    /**
     * @return the startDate
     */
    public Date getStartDate() {
        return startDate;
    }

    /**
     * @param startDate the startDate to set
     */
    public void setStartDate(Date startDate) {
        this.startDate = startDate;
    }

    /**
     * @return the endDate
     */
    public Date getEndDate() {
        return endDate;
    }

    /**
     * @param endDate the endDate to set
     */
    public void setEndDate(Date endDate) {
        this.endDate = endDate;
    }

    /**
     * @return the workflowRunDescription
     */
    public String getWorkflowRunDescription() {
        return workflowRunDescription;
    }

    /**
     * @param workflowRunDescription the workflowRunDescription to set
     */
    public void setWorkflowRunDescription(String workflowRunDescription) {
        this.workflowRunDescription = workflowRunDescription;
    }

    /**
     * @return the workflow
     */
    public Workflow getWorkflow() {
        return workflow;
    }

    /**
     * @param workflow the workflow to set
     */
    public void setWorkflow(Workflow workflow) {
        this.workflow = workflow;
    }

}
