package uk.org.mygrid.logbook.util;

import java.util.Set;

public final class WorkflowRunBean {

	private String workflowRunId;

	private String workflowId;

	private String workflowInitialId;

	private String title;

	private String author;

	private String description;

	private String date;

	private Set<ProcessRunBean> processRunBeans;

	public String getAuthor() {
		return author;
	}

	public WorkflowRunBean setAuthor(String author) {
		this.author = author;
		return this;
	}

	public String getDate() {
		return date;
	}

	public WorkflowRunBean setDate(String date) {
		this.date = date;
		return this;
	}

	public String getDescription() {
		return description;
	}

	public WorkflowRunBean setDescription(String description) {
		this.description = description;
		return this;
	}

	public String getTitle() {
		return title;
	}

	public WorkflowRunBean setTitle(String title) {
		this.title = title;
		return this;
	}

	public String getWorkflowInitialId() {
		return workflowInitialId;
	}

	public WorkflowRunBean setWorkflowInitialId(String workflow) {
		this.workflowInitialId = workflow;
		return this;
	}

	public String getWorkflowRunId() {
		return workflowRunId;
	}

	public WorkflowRunBean setWorkflowRunId(String workflowRunId) {
		this.workflowRunId = workflowRunId;
		return this;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public final Set<ProcessRunBean> getProcessRunBeans() {
		return processRunBeans;
	}

	public WorkflowRunBean setProcessRunBeans(
			Set<ProcessRunBean> processRuns) {
		if (processRuns != null) {
			for (ProcessRunBean bean : processRuns) {
				bean.setWorkflowRunId(workflowRunId).setWorkflowId(workflowId);
			}
		}
		this.processRunBeans = processRuns;
		return this;
	}

}
