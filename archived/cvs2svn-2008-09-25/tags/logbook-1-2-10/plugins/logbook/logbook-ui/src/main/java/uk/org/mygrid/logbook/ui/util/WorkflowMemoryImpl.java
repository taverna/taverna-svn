package uk.org.mygrid.logbook.ui.util;

import java.util.List;

import javax.swing.ImageIcon;

public class WorkflowMemoryImpl extends WorkflowNodeMemoryImpl implements
		Workflow {

	private String lsid;

	private List<WorkflowRun> workflowRuns;

	private WorkflowRun[] workflowRunsArray;

	public WorkflowMemoryImpl(List<WorkflowRun> workflowRuns) {
		this.workflowRuns = workflowRuns;
		WorkflowRun firstWorkflowRun = workflowRuns.get(0);
		this.lsid = firstWorkflowRun.getWorkflowId();
	}

	public WorkflowRun getLatestRun() {
		return workflowRuns.get(workflowRuns.size() - 1);
	}

	public WorkflowRun[] getWorkflowRuns() {
		if (workflowRunsArray == null)
			workflowRunsArray = workflowRuns
					.toArray(new WorkflowRun[workflowRuns.size()]);
		return workflowRunsArray;
	}

	public String getAuthor() {
		return getLatestRun().getAuthor();
	}

	public String getDescription() {
		return getLatestRun().getDescription();
	}

	public String getDisplayDate() {
		return getLatestRun().getDisplayDate();
	}

	public ImageIcon getIcon() {
		// TODO Auto-generated method stub
		return null;
	}

	public void setIcon(ImageIcon icon) {
		// TODO Auto-generated method stub

	}

	public String getLsid() {
		return lsid;
	}

	@Override
	public String getTitle() {
		return getLatestRun().getTitle();
	}

	@Override
	public String toString() {
		if (getCustomTitle() != null && !getCustomTitle().equalsIgnoreCase("")) {
			return getCustomTitle();
		} else {
			return getDisplayDate();
		}
	}

	/**
	 * Operation not supported because this implementation uses the constructor
	 * to initialise a list of workflow runs instead.
	 */
	public void setWorkflowRuns(WorkflowRun[] runs) {
		throw new UnsupportedOperationException();
	}

}
