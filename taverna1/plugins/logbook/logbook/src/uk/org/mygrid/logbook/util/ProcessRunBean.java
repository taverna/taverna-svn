/**
 * 
 */
package uk.org.mygrid.logbook.util;

import java.util.Set;

public class ProcessRunBean {

	private String processRunId;

	private String parentId;
	
	private String workflowRunId;
	
	private String workflowId;

	private String date;

	private String process;
	
	private boolean failed;
	
	private String cause;
	
	private boolean subworkflow;

	private Set<ProcessRunBean> processIterations;

	private String processClassName;

	public ProcessRunBean(String processRunId, String parentId, String date,
			String process, String processClassName) {
		this.processRunId = processRunId;
		this.parentId = parentId;
		this.date = date;
		this.process = process;
		this.processClassName = processClassName;
	}

	public final String getDate() {
		return date;
	}

	public final String getProcess() {
		return process;
	}

	public final String getProcessRunId() {
		return processRunId;
	}

	public final String getParentId() {
		return parentId;
	}

	public final void setParentId(String workflowRunId) {
		this.parentId = workflowRunId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public String getProcessClassName() {
		return processClassName;
	}

	public void setProcessClassName(String processClassName) {
		this.processClassName = processClassName;
	}

	public ProcessRunBean setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
		return this;
	}

	public String getWorkflowRunId() {
		return workflowRunId;
	}

	public ProcessRunBean setWorkflowRunId(String workflowRunId) {
		this.workflowRunId = workflowRunId;
		return this;
	}

	public final Set<ProcessRunBean> getProcessIterations() {
		return processIterations;
	}

	public final ProcessRunBean addProcessIteration(
			ProcessRunBean processIteration) {
		processIterations.add(processIteration);
		return this;
	}

	public final ProcessRunBean setProcessIterations(
			Set<ProcessRunBean> processIterations) {
		this.processIterations = processIterations;
		return this;
	}

	public String getCause() {
		return cause;
	}

	public boolean isFailed() {
		return failed;
	}

	public ProcessRunBean setFailed(boolean failed, String cause) {
		this.failed = failed;
		this.cause = cause;
		return this;
	}

	public boolean isSubworkflow() {
		return subworkflow;
	}

	public void setSubworkflow(boolean subworkflow) {
		this.subworkflow = subworkflow;
	}

	@Override
	public String toString() {
		return "[" + processRunId + ", " + process + ", " + parentId + ", "
				+ date + "]";
	}

}