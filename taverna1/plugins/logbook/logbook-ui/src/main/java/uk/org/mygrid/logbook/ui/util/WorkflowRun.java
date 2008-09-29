package uk.org.mygrid.logbook.ui.util;

import java.util.Date;
import java.util.List;

import uk.org.mygrid.provenance.LogBookException;

public interface WorkflowRun extends WorkflowNode {
	
	public abstract Date getDate();

	public abstract void setLsid(String lsid);

	public abstract String getTitle();

	public abstract void setTitle(String title);

	public abstract String getWorkflowInitialId() throws LogBookException;

	public abstract void setWorkflowInitialId(String workflowInitialId);

	public abstract String getCustomTitle();

	public abstract void setCustomTitle(String customTitle);

	public abstract boolean isNestedWorkflowRun();

	public abstract void setNestedWorkflowRun(boolean nestedWorkflowRun);

	public abstract boolean isFailedWorkflowRun();

	public abstract void setFailedWorkflowRun(boolean failedWorkflowRun);

	public abstract boolean isHasFailedProccess();
	
	public abstract void setHasFailedProccess(boolean hasFailedProccess);
	
	public List<ProcessRun> getProcessRuns();

	public void setProcessRuns(List<ProcessRun> processRuns);

	public void addProcessRun(ProcessRun processRun);

	public String getWorkflowId();
	
	public void setWorkflowId(String workflowId);

	public abstract void setDate(Date date);
	
}