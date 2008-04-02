package uk.org.mygrid.logbook.ui.util;

import java.sql.Time;
import java.text.ParseException;
import java.util.Calendar;
import java.util.Date;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

import uk.org.mygrid.logbook.ui.LogBookUIRemoteModel;
import uk.org.mygrid.logbook.util.ProcessRunBean;

public class ProcessRunImpl implements ProcessRun {

	ImageIcon icon;

	String lsid;

	String name;

	Date date;

	String dateString;

	boolean failed;

	boolean subWorkflow;

	String cause;

	String workflowRunLSID;

	String workflowLSID;

	public ProcessRunImpl() {
		// default constructor
	}

	public ProcessRunImpl(ProcessRunBean processRunBean) throws ParseException {
		setLsid(processRunBean.getProcessRunId());
		setName(processRunBean.getProcess());
		setWorkflowRunLSID(processRunBean.getParentId());
		String unparsedDate = processRunBean.getDate();
		setDate(LogBookUIRemoteModel.parseDate(unparsedDate.substring(1,
				unparsedDate.lastIndexOf("\""))));
		setWorkflowLSID(processRunBean.getWorkflowId());
		setSubWorkflow(processRunBean.isSubworkflow());
		if (processRunBean.isFailed()) {
			failed = true;
			cause = processRunBean.getCause();
		}
		String processClassName = processRunBean.getProcessClassName();
		String tagName = ProcessorHelper
				.getTagNameForClassName(processClassName);
		setIcon(ProcessorHelper.getIconForTagName(tagName));
	}

	public int compareTo(ProcessRun other) {
		if (this.date == null || other.getDate() == null)
			return 0;
		else
			return this.date.compareTo(other.getDate());
	}

	public String toString() {
		return this.getName();
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getCause()
	 */
	public String getCause() {

		if (failed)
			return cause;
		else
			return "process did not fail";

	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getDate()
	 */
	public Date getDate() {
		return date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setDate(java.util.Date)
	 */
	public void setDate(Date date) {
		this.date = date;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getDateString()
	 */
	public String getDateString() {
		if (date == null)
			return "";
		Calendar c = Calendar.getInstance();
		c.setTime(date);
		String time = (new Time(date.getTime())).toString();
		dateString = "<html><font color=#999999>"
				+ c.get(Calendar.DAY_OF_MONTH) + "/"
				+ (c.get(Calendar.MONTH) + 1) + "/" + c.get(Calendar.YEAR)
				+ "</font>    " + time;
		return dateString;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#isFailed()
	 */
	public boolean isFailed() {
		return failed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setFailed(boolean)
	 */
	public void setFailed(boolean failed) {
		this.failed = failed;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getLsid()
	 */
	public String getLsid() {
		return lsid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setLsid(java.lang.String)
	 */
	public void setLsid(String lsid) {
		this.lsid = lsid;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getIcon()
	 */
	public ImageIcon getIcon() {
		return icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setIcon(javax.swing.ImageIcon)
	 */
	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getName()
	 */
	public String getName() {
		return name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setName(java.lang.String)
	 */
	public void setName(String name) {
		this.name = name;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setCause(java.lang.String)
	 */
	public void setCause(String cause) {
		this.cause = cause;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getWorkflowLSID()
	 */
	public String getWorkflowLSID() {
		return workflowLSID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setWorkflowLSID(java.lang.String)
	 */
	public void setWorkflowLSID(String workflowLSID) {
		this.workflowLSID = workflowLSID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#getWorkflowRunLSID()
	 */
	public String getWorkflowRunLSID() {
		return workflowRunLSID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setWorkflowRunLSID(java.lang.String)
	 */
	public void setWorkflowRunLSID(String workflowRunLSID) {
		this.workflowRunLSID = workflowRunLSID;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#isSubWorkflow()
	 */
	public boolean isSubWorkflow() {
		return subWorkflow;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.ProcessRun#setSubWorkflow(boolean)
	 */
	public void setSubWorkflow(boolean subWorkflow) {
		this.subWorkflow = subWorkflow;
	}

}
