package uk.org.mygrid.logbook.ui.util;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflui.TavernaIcons;

public class WorkflowRunImpl extends WorkflowNodeMemoryImpl implements WorkflowRun {

    private String lsid;

    protected String Title;

    protected String customTitle;

    private String Author;

    private String Description;

    private Date Date;

    private String workflowInitialId;
    
    private String workflowId;

    private ImageIcon icon;

    private boolean failedWorkflowRun;

    private boolean hasFailedProccess;

    private boolean nestedWorkflowRun;
    
    private List<ProcessRun> processRuns = new ArrayList<ProcessRun>();

    public WorkflowRunImpl() {
        super();
    }

    public WorkflowRunImpl(String LSID, ImageIcon icon) {
        this.icon = icon;
        this.lsid = LSID;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getIcon()
	 */
    public ImageIcon getIcon() {

        return this.icon;

    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setIcon(javax.swing.ImageIcon)
	 */
    public void setIcon(ImageIcon icon) {

        this.icon = icon;
    }

    public String toString() {

        if (getCustomTitle() != null && !getCustomTitle().equalsIgnoreCase("")) {
            return getCustomTitle();
        } else {
            return getDisplayDate();
        }
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getDisplayDate()
	 */
    public String getDisplayDate() {
		if (getDate() == null)
            return "";
        return WorkflowNodeMemoryImpl.getDisplayDate(getDate());
    }

	@Override
	public int hashCode() {
		return lsid.hashCode();
	}

	public boolean equals(Object o) {
    	if (this == o)
    		return true;
        if (!(o instanceof WorkflowRun)) 
        	return false;
        return lsid.equals(((WorkflowRun)o).getLsid());
    }

	@Override
    public int compareTo(WorkflowNode o) {
		if (this == o)
			return 0;
    	// FIXME: not compatible with equals!
        if (this.Date == null || o.getDate() == null)
            return 0;
        else
            return this.Date.compareTo(o.getDate()) * -1;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getAuthor()
	 */
    public String getAuthor() {
        return Author;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setAuthor(java.lang.String)
	 */
    public void setAuthor(String author) {
        Author = author;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getDate()
	 */
    public Date getDate() {
        return Date;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setDate(java.util.Date)
	 */
    public void setDate(Date date) {
        Date = date;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getDescription()
	 */
    public String getDescription() {
        return Description;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setDescription(java.lang.String)
	 */
    public void setDescription(String description) {
        Description = description;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#isHasFailedProccess()
	 */
    public boolean isHasFailedProccess() {
        return hasFailedProccess;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setHasFailedProccess(boolean)
	 */
    public void setHasFailedProccess(boolean hasFailedProccess) {
        this.hasFailedProccess = hasFailedProccess;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getLSID()
	 */
    public String getLsid() {
        return lsid;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getRunID()
	 */
    public String getLsidSuffix() {
        if (lsid == null)
            return "";
        String[] split = lsid.split(":");
        if (split[3].equals("experimentinstance")||split[3].equals("wfInstance"))
            return split[4];
        return "";
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setLSID(java.lang.String)
	 */
    public void setLsid(String lsid) {
        this.lsid = lsid;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getTitle()
	 */
    public String getTitle() {
        return Title;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setTitle(java.lang.String)
	 */
    public void setTitle(String title) {
        Title = title;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getWorkflowLSID()
	 */
    public String getWorkflowInitialId() {
        return workflowInitialId;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setWorkflowLSID(java.lang.String)
	 */
    public void setWorkflowInitialId(String workflowLSID) {
        this.workflowInitialId = workflowLSID;
    }

    public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	/* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getCustomTitle()
	 */
    public String getCustomTitle() {
        return customTitle;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setCustomTitle(java.lang.String)
	 */
    public void setCustomTitle(String customTitle) {
        this.customTitle = customTitle;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#isNestedWorkflowRun()
	 */
    public boolean isNestedWorkflowRun() {
        return nestedWorkflowRun;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setNestedWorkflowRun(boolean)
	 */
    public void setNestedWorkflowRun(boolean nestedWorkflowRun) {
        this.nestedWorkflowRun = nestedWorkflowRun;
        if (nestedWorkflowRun)
            this.icon = TavernaIcons.windowExplorer;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#isFailedWorkflowRun()
	 */
    public boolean isFailedWorkflowRun() {
        return failedWorkflowRun;
    }

    /* (non-Javadoc)
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#setFailedWorkflowRun(boolean)
	 */
    public void setFailedWorkflowRun(boolean failedWorkflowRun) {
        this.failedWorkflowRun = failedWorkflowRun;
    }
    
    public List<ProcessRun> getProcessRuns() {
		return processRuns;
	}

	public void setProcessRuns(List<ProcessRun> processRuns) {
		this.processRuns = processRuns;
	}

	public void addProcessRun(ProcessRun processRun) {
		processRuns.add(processRun);
	}

}
