package net.sf.taverna.service.queue;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import javax.management.RuntimeErrorException;

import net.sf.taverna.service.backend.executor.JobExecutor;
import net.sf.taverna.service.backend.executor.JobExecutorFactory;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.QueueDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;

/**
 * Periodically monitors the default Queue. If there are queued jobs and an
 * available worker then a new job is kicked off
 * 
 * @author Stuart Owen
 */
public class DefaultQueueMonitor extends Thread {
	private JobExecutorFactory jobExecutorFactory =
		JobExecutorFactory.getInstance();

	private static Logger logger = Logger.getLogger(DefaultQueueMonitor.class);

	// NOTE: DAOFactory.getFactory() only decides what is the implementation
	// (ie. JPA) and is perfectly thread-safe and can be shared all-over
	private DAOFactory daoFactory = DAOFactory.getFactory();

	private final int CHECK_PERIOD = 20; // checks every 20 seconds.

	private boolean terminate = false;

	private URIFactory uriFactory;

	public DefaultQueueMonitor(URIFactory uriFactory) {
		super("Queue Monitor Thread");
		this.uriFactory = uriFactory;
	}

	public void run() {
		try {
			while (!terminate) {
				mainLoop();
				sleep();
			}
		} finally {
			try {
				daoFactory.rollback();
			} finally {
				daoFactory.close();
			}
		}
	}
	
	/**
	 * Causes the thread to exit the poll loop.
	 */
	public void terminate() {
		terminate = true;
	}

	private void mainLoop() {
		try {
			removeCompletedJobs();
			killCancelledJobs();
			startWaitingJobs();
		} finally {
			daoFactory.rollback();
		}
	}

	private void removeCompletedJobs() {
		JobDAO jobDAO = daoFactory.getJobDAO();
		QueueDAO queueDao = daoFactory.getQueueDAO();
		Queue queue = queueDao.defaultQueue();
		List<Job> completedJobs = jobDAO.byStatus(Status.COMPLETE);
		completedJobs.addAll(jobDAO.byStatus(Status.CANCELLED));
		for (Job job : completedJobs) {
			QueueEntry entry = queue.removeJob(job);
			daoFactory.getQueueEntryDAO().delete(entry);
			jobDAO.update(job);
		}
		queueDao.update(queue);
		daoFactory.commit();
	}
	
	private void startWaitingJobs() {
		try {
			logger.debug("Checking queue for new jobs");
			List<Job> waitingJobs =
				daoFactory.getJobDAO().byStatus(Status.QUEUED);
			if (waitingJobs.isEmpty()) {
				return;
			}
			logger.info(waitingJobs.size() + " waiting jobs found");
			List<Worker> availableWorkers = determineAvailableWorkers();
			if (!availableWorkers.isEmpty()) {
				logger.info("No workers available");
				return;
			}
			logger.info(availableWorkers.size() + " available workers found.");
			List<Job> assignedJobs =
				assignJobsToWorkers(availableWorkers, waitingJobs);
			daoFactory.commit();
			startJobs(assignedJobs);
		} catch (RuntimeErrorException ex) {
			logger.warn("Could not start waiting jobs", ex);
		}
	}

	
	
	private void sleep() {
		try {
			if (!terminate) {
				Thread.sleep(CHECK_PERIOD * 1000);
			}
		} catch (InterruptedException e) {
			terminate = true;
			Thread.currentThread().interrupt();
		}
	}

	private void killCancelledJobs() {
		JobExecutor executor = jobExecutorFactory.createExecutor(uriFactory);
		for (Job j : daoFactory.getJobDAO().byStatus(Status.CANCELLING)) {
			j = daoFactory.getJobDAO().reread(j);
			logger.info("Attempting to kill " + j);
			if (executor.killJob(j)) {
				j = daoFactory.getJobDAO().refresh(j);
				synchronized (j) {
					if (j.getStatus().equals(Status.CANCELLING)) {
						j.setStatus(Status.CANCELLED);
						j.getWorker().unassignJobs();
					}
				}
				daoFactory.commit();
			}
		}
	}

	/**
	 * @return any workers that are not busy
	 */
	private List<Worker> determineAvailableWorkers() {
		List<Worker> result = new ArrayList<Worker>();
		Queue defaultQueue = daoFactory.getQueueDAO().defaultQueue();
		for (Worker worker : defaultQueue.getWorkers()) {
			worker = daoFactory.getWorkerDAO().refresh(worker);
			if (!worker.isBusy()) {
				result.add(worker);
			}
		}
		return result;
	}

	/**
	 * Assign jobs to workers.
	 * 
	 * @param workers
	 *            available workers as determined by
	 *            {@link #determineAvailableWorkers()}
	 * @param jobs
	 *            Jobs to be assigned as determined by
	 *            {@link #checkWaitingJobs()}
	 * @return Subset of jobs that have been assigned and are to be started by
	 *         {@link #startJobs(List)}
	 */
	private List<Job> assignJobsToWorkers(List<Worker> workers, List<Job> jobs) {
		List<Job> assigned = new ArrayList<Job>();

		WorkerDAO workerDAO = daoFactory.getWorkerDAO();
		JobDAO jobDAO = daoFactory.getJobDAO();
		Iterator<Job> jobIterator = jobs.iterator();
		for (Worker worker : workers) {
			if (! jobIterator.hasNext()) {
				break;
			}
			Job job = jobIterator.next();
			worker.assignJob(job);
			jobDAO.update(job);
			workerDAO.update(worker);
			assigned.add(job);
		}
		
		return assigned;
	}

	private void startJobs(List<Job> jobs) {
		for (Job job : jobs) {
			job = daoFactory.getJobDAO().reread(job);
			Worker worker = job.getWorker();
			if (worker == null) {
				logger.error("Job has no assigned worker: " + job);
				continue;
			}
			logger.info("Starting job execution:" + job);
			JobExecutor executor =
				jobExecutorFactory.createExecutor(uriFactory);
			executor.executeJob(job, worker);
		}
	}
}
