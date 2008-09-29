package uk.org.mygrid.logbook.ui.util;

import java.util.Arrays;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;

public class WorkflowImpl extends WorkflowRunImpl implements Workflow {

	WorkflowRun[] workflowRuns;

	public WorkflowImpl() {
		super();
	}

	public WorkflowImpl(String LSID, ImageIcon i) {
		super(LSID, i);
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.Workflow#getWorkflowRuns()
	 */
	public WorkflowRun[] getWorkflowRuns() {
		return workflowRuns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.Workflow#setWorkflowRuns(java.lang.Object[])
	 */
	public void setWorkflowRuns(WorkflowRun[] workflowRuns) {
		Arrays.sort(workflowRuns);
		this.workflowRuns = workflowRuns;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.Workflow#getLatestRun()
	 */
	public WorkflowRun getLatestRun() {

		if (this.workflowRuns != null && this.workflowRuns.length > 0)
			return (WorkflowRun) workflowRuns[0];

		return new WorkflowRunImpl();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.Workflow#getIcon()
	 */
	public ImageIcon getIcon() {

		return TavernaIcons.windowDiagram;

	}

	@Override
    public int compareTo(WorkflowNode o) {
		if (this == o)
			return 0;
    	// FIXME: not compatible with equals!
        if (this.getDate() == null || o.getDate() == null)
            return 0;
        else
            return this.getDate().compareTo(o.getDate()) * -1;
    }

	public String toString() {

		if (this.customTitle != null && !this.customTitle.equalsIgnoreCase("")) {
			return this.customTitle;
		} else {
			return this.Title;
		}
	}

}
