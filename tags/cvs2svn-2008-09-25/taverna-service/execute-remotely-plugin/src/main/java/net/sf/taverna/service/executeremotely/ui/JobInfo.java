package net.sf.taverna.service.executeremotely.ui;

import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.text.DateFormat;

import javax.swing.AbstractAction;
import javax.swing.JButton;
import javax.swing.JLabel;
import javax.swing.JPanel;

import net.sf.taverna.service.executeremotely.RemoteWorkflowInstance;
import net.sf.taverna.service.executeremotely.UILogger;
import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;
import net.sf.taverna.service.rest.client.RESTException;
import net.sf.taverna.service.xml.StatusType;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.shared.ModelMap;
import org.restlet.data.Reference;
import org.restlet.data.Status;

public class JobInfo extends JPanel {
	
	private static final long serialVersionUID = -6878731337707257390L;

	private static Logger logger = Logger.getLogger(JobInfo.class);

	private UILogger uiLog;
	
	DateFormat dateFormat = DateFormat.getDateInstance(DateFormat.FULL);

	public final JobREST job;

	private JobsPanel jobsPanel;

	public JobInfo(UILogger uiLogger, JobsPanel jobsPanel, JobREST job) {
		this.uiLog = uiLogger;
		this.jobsPanel = jobsPanel;
		this.job = job;
		init();
	}
	
	public void init() {
		removeAll();
		setLayout(new GridBagLayout());
		setOpaque(false);
		GridBagConstraints c = new GridBagConstraints();
		c.anchor = GridBagConstraints.FIRST_LINE_START;
		c.fill = GridBagConstraints.NONE;
//		c.fill = GridBagConstraints.HORIZONTAL;
		c.gridwidth = 1;

		int row = 0;

		c.gridy = row++;
		c.gridx = 0;
		add(new JLabel("Created:"), c);
		c.gridx = 1;
		add(new JLabel(dateFormat.format(job.getCreated().getTime())), c);

		c.gridy = row++;
		c.gridx = 0;
		add(new JLabel("Last modified:"), c);
		c.gridx = 1;
		add(new JLabel(dateFormat.format(job.getLastModified().getTime())),
			c);

		c.gridy = row++;
		c.gridx = 0;
		c.gridwidth = GridBagConstraints.REMAINDER;
		c.fill = GridBagConstraints.NONE;
		JButton progress = progressButton();
		if (progress != null) {
			add(progress, c);
			c.gridy = row++;
		}
		
		StatusType.Enum status = null;
		try {
			status = job.getStatus();
		} catch (RESTException e) {
			logger.warn("Could not check status of job " + job, e);
		}
		if (status == StatusType.RUNNING) {
			add(cancelAction(), c);
			c.gridy = row++;
			//} else if (status == StatusType.QUEUED) {
			//add(removeFromQueueAction(), c); Not implemented
			// c.gridy = row++;
		} else {
			// FIXME: Should not always allow deletion
			add(deleteAction(), c);
			c.gridy = row++;
		}
		
		c.gridy = row++;
		c.weightx = 0.2;
		add(new JPanel(), c); // filler

	}

	private JButton cancelAction() {
		return new JButton(new AbstractAction("Cancel execution"){
			public void actionPerformed(ActionEvent e) {
				try {
					job.setStatus(StatusType.CANCELLING);
				} catch (NotSuccessException e1) {
					logger.warn("Could not cancel " + job, e1);
					uiLog.log("Could not cancel " + job);
					return;
				}
				uiLog.log("Cancelling " + job);
				jobsPanel.refresh();
			}
		});
	}
	
	private JButton removeFromQueueAction() {
		return new JButton(new AbstractAction("Remove from queue"){
			public void actionPerformed(ActionEvent e) {
				// REST interface does not yet support removing from the queue
				throw new RuntimeException("Not implemented");
			}
		});
	}
	
	private JButton loadWorkflowAction() {
		return new JButton(new AbstractAction("Open workflow"){
			public void actionPerformed(ActionEvent e) {
				Reference url = new Reference(job.getURIReference());
				//url.setUserInfo(arg0)
			}
		});
	}
	

	private JButton deleteAction() {
		return new JButton(new AbstractAction("Delete"){
			public void actionPerformed(ActionEvent e) {
				try {
					job.getOwner().getJobs().delete(job);
					uiLog.log("Deleted " + job);
				} catch (NotSuccessException e1) {
					logger.warn("Could not delete " + job, e1);
					uiLog.log("Could not delete " + job);
				}
				jobsPanel.refresh();
			}
		});
	}

	@SuppressWarnings("serial")
	private JButton progressButton() {
		String report = null;
		try {
			try {
				report = job.getReport();
			} catch (NotSuccessException ex) {
				if (ex.getStatus().equals(Status.CLIENT_ERROR_NOT_FOUND)) {
					logger.debug("Report for " + job + " not found");
				} else {
					throw ex;
				}
			}
		} catch (RESTException ex) {
			logger.warn("Can't get report for " + job);
			uiLog.log(ex);
		}
		if (report == null || report.equals("")) {
			return null;
		}
		String buttonText="View progress";
		try {
			if (job.getStatus().equals(StatusType.COMPLETE)) {
				buttonText="View results";
			}
		} catch (RESTException e1) {
			logger.error("Error reading job status",e1);
		}
		return new JButton(new AbstractAction(buttonText) {
			public void actionPerformed(ActionEvent e) {
				ModelMap modelMap = ModelMap.getInstance();
				if (modelMap.getModels().contains(job.getURI())) {
					// Remove the old one
					modelMap.setModel(job.getURI(), null);
				}
				// Create and add a Workflow instance
				RemoteWorkflowInstance instance =
					new RemoteWorkflowInstance(job, uiLog);
				modelMap.setModel(job.getURI(), instance);
			}
		});
	}
}
