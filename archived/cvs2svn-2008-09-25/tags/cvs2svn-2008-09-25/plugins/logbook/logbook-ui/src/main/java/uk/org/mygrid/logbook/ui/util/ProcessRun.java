package uk.org.mygrid.logbook.ui.util;

import java.util.Date;

import javax.swing.ImageIcon;

public interface ProcessRun extends Comparable<ProcessRun>{

	public abstract String getCause();

	public abstract Date getDate();

	public abstract void setDate(Date date);

	public abstract String getDateString();

	public abstract boolean isFailed();

	public abstract void setFailed(boolean failed);

	public abstract String getLsid();

	public abstract void setLsid(String lsid);

	public abstract ImageIcon getIcon();

	public abstract void setIcon(ImageIcon icon);

	public abstract String getName();

	public abstract void setName(String name);

	public abstract void setCause(String cause);

	public abstract String getWorkflowLSID();

	public abstract void setWorkflowLSID(String workflowLSID);

	/*
	 * FIME: this method does not belong here but current implementation of GUI requires it.
	 */
	public abstract String getWorkflowRunLSID();

	/*
	 * FIME: this method does not belong here but current implementation of GUI requires it.
	 */
	public abstract void setWorkflowRunLSID(String workflowRunLSID);

	public abstract boolean isSubWorkflow();

	public abstract void setSubWorkflow(boolean subWorkflow);
	
	//public abstract WorkflowRun getNestedWorkflowRun();

}