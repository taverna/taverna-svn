package uk.org.mygrid.logbook.ui.util;

import java.text.ParseException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.TavernaIcons;

import uk.org.mygrid.logbook.util.ProcessRunBean;
import uk.org.mygrid.logbook.util.WorkflowRunBean;
import uk.org.mygrid.provenance.LogBookException;

public class WorkflowRunMemoryImpl extends WorkflowNodeMemoryImpl implements
		WorkflowRun {

	private Logger logger = Logger.getLogger(WorkflowRunMemoryImpl.class);

	private String customTitle;

	private String workflowInitialId;

	private String workflowId;

	private ImageIcon icon;

	private boolean failedWorkflowRun;

	private boolean hasFailedProccess;

	private boolean nestedWorkflowRun;

	// private static Logger logger = Logger
	// .getLogger(WorkflowRunMemoryImpl.class);

	private List<ProcessRun> processRuns;

	private Set<ProcessRunBean> processRunBeans;

	protected WorkflowRunMemoryImpl() {
		// default
	}

	protected WorkflowRunMemoryImpl(String lsid, String workflowInitialId,
			String workflowId, String title, String author, String description,
			String date, Set<ProcessRunBean> processRunBeans)
			throws ParseException {
		super(lsid, title, author, description, date);
		this.workflowId = workflowId;
		this.workflowInitialId = workflowInitialId;
		this.processRunBeans = processRunBeans;
	}

	public WorkflowRunMemoryImpl(String workflowRunLSID,
			WorkflowRunBean workflowRunBean) throws ParseException {
		this(workflowRunLSID, workflowRunBean.getWorkflowInitialId(),
				workflowRunBean.getWorkflowId(), workflowRunBean.getTitle(),
				workflowRunBean.getAuthor(), workflowRunBean.getDescription(),
				workflowRunBean.getDate(), workflowRunBean.getProcessRunBeans());
	}

	public ImageIcon getIcon() {
		if (icon == null) {
			if (isFailedWorkflowRun())
				icon = TavernaIcons.deleteIcon;
			else if (isNestedWorkflowRun())
				this.icon = TavernaIcons.windowExplorer;
			else
				icon = TavernaIcons.runIcon;
		}
		return icon;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see uk.org.mygrid.logbook.ui.util.WorkflowRun#getRunID()
	 */
	public String getLsidSuffix() {
		if (getLsid() == null)
			return "";
		String[] split = getLsid().split(":");
		if (split[3].equals("experimentinstance")
				|| split[3].equals("wfInstance"))
			return split[4];
		return "";
	}

	public String getWorkflowInitialId() throws LogBookException {
		return workflowInitialId;
	}

	public String getWorkflowId() {
		return workflowId;
	}

	public void setWorkflowId(String workflowId) {
		this.workflowId = workflowId;
	}

	public boolean isFailedWorkflowRun() {
		return failedWorkflowRun;
	}

	public boolean isHasFailedProccess() {
		// TODO Auto-generated method stub
		return false;
	}

	public boolean isNestedWorkflowRun() {
		return nestedWorkflowRun;
	}

	public String getCustomTitle() {
		return customTitle;
	}

	public void setCustomTitle(String customTitle) {
		this.customTitle = customTitle;
	}

	public boolean getFailedWorkflowRun() {
		return failedWorkflowRun;
	}

	public void setFailedWorkflowRun(boolean failedWorkflowRun) {
		this.failedWorkflowRun = failedWorkflowRun;
	}

	public boolean getHasFailedProccess() {
		return hasFailedProccess;
	}

	public void setHasFailedProccess(boolean hasFailedProccess) {
		this.hasFailedProccess = hasFailedProccess;
	}

	public boolean getNestedWorkflowRun() {
		return nestedWorkflowRun;
	}

	public void setNestedWorkflowRun(boolean nestedWorkflowRun) {
		this.nestedWorkflowRun = nestedWorkflowRun;
	}

	public void setWorkflowInitialId(String workflowLSID) {
		this.workflowInitialId = workflowLSID;
	}

	public void setIcon(ImageIcon icon) {
		this.icon = icon;
	}

	public List<ProcessRun> getProcessRuns() {
		if (processRuns == null) {
			if (processRunBeans != null) {
				processRuns = new ArrayList<ProcessRun>();
				for (ProcessRunBean processRunBean : processRunBeans) {
					try {
						if (processRunBean != null) {
							if (processRunBean.getProcessIterations() == null)
								processRuns.add(new ProcessRunImpl(
										processRunBean));
							else
								processRuns
										.add(new ProcessRunWithIterationsImpl(
												processRunBean));
						}
					} catch (ParseException e) {
						logger.warn(e);
					}
				}
				ProcessRun[] processRunsArray = new ProcessRun[processRuns
						.size()];
				processRuns.toArray(processRunsArray);
				Arrays.sort(processRunsArray);
				processRuns = Arrays.asList(processRunsArray);
			}
		}
		return processRuns;
	}

	public void setProcessRuns(List<ProcessRun> processRuns) {
		this.processRuns = processRuns;
	}

	public void addProcessRun(ProcessRun processRun) {
		processRuns.add(processRun);
	}

	@Override
	public String toString() {
		if (getCustomTitle() != null && !getCustomTitle().equalsIgnoreCase("")) {
			return getCustomTitle();
		} else {
			return getDisplayDate();
		}
	}

}
