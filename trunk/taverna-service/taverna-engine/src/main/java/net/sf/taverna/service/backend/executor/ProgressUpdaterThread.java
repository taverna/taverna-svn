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
import org.embl.ebi.escience.scufl.tools.WorkflowLauncher;

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
	public final int MIN_SLEEP = 250;

	private JobREST job;

	private WorkflowLauncher launcher;

	private Date sleepUntil;

	public boolean loop = true;

	public ProgressUpdaterThread(WorkflowLauncher launcher, JobREST job) {
		super("Job progress updater");
		this.launcher = launcher;
		this.job = job.clone();
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
				// We'll always sleep MIN_SLEEP even if our job submitter wants us to be
				// faster
				sleep(MIN_SLEEP);
			} catch (InterruptedException e) {
				logger.warn("Aborting " + this);
				return;
			}
			if (sleepUntil.after(new Date())) {
				continue;
			}
			try {
				job.setReport(launcher.getProgressReportXML());
			} catch (NotSuccessException e) {
				logger.warn("Could not set progress report for " + job, e);
				continue;
			} catch (XmlException e) {
				logger.error("Could not serialize progress report for " + job
					+ ", aborting " + this, e);
				return;
			}
			logger.info("Updated progress report for " + job);
			job.invalidate();
			updateSleepUntil();
		}
	}
}