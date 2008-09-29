/**
 * 
 */
package net.sf.taverna.service.backend.executor;

import java.util.Date;

import net.sf.taverna.service.rest.client.JobREST;
import net.sf.taverna.service.rest.client.NotSuccessException;

import org.apache.log4j.Logger;
import org.apache.xmlbeans.GDateBuilder;
import org.apache.xmlbeans.XmlException;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventAdapter;
import org.embl.ebi.escience.scufl.enactor.WorkflowEventListener;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.WorkflowCreationEvent;

/**
 * Thread that updates progress report on given intervals
 * 
 * @author Stian Soiland
 *
 */
public class ProgressUpdaterThread extends Thread {

	private static Logger logger =
		Logger.getLogger(ProgressUpdaterThread.class);

	/**
	 * Minimal time for loop to sleep in milliseconds
	 */
	public final int MIN_SLEEP = 2000;

	private JobREST job;

	private Date sleepUntil;

	public boolean loop = true;

	public final WorkflowEventListener workflowEventListener = new ProgressReportListener();

	public WorkflowInstance wfInstance = null;
	
	public ProgressUpdaterThread(JobREST job) {
		super("Job progress updater");
		this.job = job.clone();
	}

	public class ProgressReportListener extends WorkflowEventAdapter {
		
		@Override
		public void workflowCreated(WorkflowCreationEvent event) {
			if (wfInstance == null) {
				// Only pick it up the first time
				wfInstance  = event.getWorkflowInstance();
			} else {
				logger.warn("Got a second workflowCreated, first wfInstance is "
					+ wfInstance
					+ " and now got "
					+ event.getWorkflowInstance());
			}
		}			
		
	}
	
	public void updateSleepUntil() {
		GDateBuilder gDateBuilder = new GDateBuilder();
		gDateBuilder.setDate(new Date());
		gDateBuilder.addGDuration(job.getUpdateInterval());
		sleepUntil = gDateBuilder.getDate();
	}

	@Override
	public void run() {
		updateSleepUntil();
		while (loop) {
			try {
				// We'll always sleep MIN_SLEEP even if our job submitter wants
				// us to be faster
				sleep(MIN_SLEEP);
			} catch (InterruptedException e) {
				logger.warn("Aborting " + this);
				return;
			}
			if (sleepUntil.after(new Date())) {
				continue;
			}
			if (wfInstance == null) {
				continue;
			}
			try {
				job.setReport(wfInstance.getProgressReportXMLString());
			} catch (NotSuccessException e) {
				logger.warn("Could not set progress report for " + job, e);
				continue;
			} catch (XmlException e) {
				logger.error("Could not serialize progress report for " + job
					+ ", aborting " + this, e);
				return;
			}
			logger.info("Updated progress report for " + job);
			updateSleepUntil();
		}
	}
}