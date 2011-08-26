package uk.org.mygrid.logbook.ui.util;

import java.util.Date;


public interface Workflow extends WorkflowNode {

	public abstract WorkflowRun[] getWorkflowRuns();

	public abstract WorkflowRun getLatestRun();
	
	public abstract String getTitle();

	public abstract void setLsid(String latestWorkflowLSID);

	public abstract void setAuthor(String author);

	public abstract void setDate(Date object);

	public abstract void setDescription(String description);

	public abstract void setTitle(String title);

	public abstract void setWorkflowRuns(WorkflowRun[] runs);

}