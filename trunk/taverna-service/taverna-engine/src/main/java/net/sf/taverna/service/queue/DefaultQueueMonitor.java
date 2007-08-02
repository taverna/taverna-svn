package net.sf.taverna.service.queue;

import java.util.ArrayList;
import java.util.List;

import net.sf.taverna.service.backend.executor.JobExecutor;
import net.sf.taverna.service.backend.executor.JobExecutorFactory;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;

/**
 * Periodically monitors the default Queue.
 * If there are queued jobs and an available worker then a new job is kicked off
 * 
 * @author Stuart Owen
 */
public class DefaultQueueMonitor extends Thread {
	private JobExecutorFactory jobExecutorFactory = JobExecutorFactory.getInstance();
	
	private static Logger logger = Logger.getLogger(DefaultQueueMonitor.class);

	private final int CHECK_PERIOD = 20; //checks every 20 seconds.
	private boolean terminate = false;

	private URIFactory uriFactory;
	
	public DefaultQueueMonitor(URIFactory uriFactory) {
		super("Queue Monitor Thread");
		this.uriFactory = uriFactory;
	}
	
	public void run() {
		while(!terminate) {
			
			try {
				removeCompletedJobs();
				killCancelledJobs();
				startWaitingJobs();
			}
			finally {
				DAOFactory.getFactory().close();
			}
			
			try {
				if (!terminate) Thread.sleep(CHECK_PERIOD * 1000);
			} catch (InterruptedException e) {
				terminate = true;
				Thread.currentThread().interrupt();
			}
		}
	}
	
	private void removeCompletedJobs() {
		DAOFactory daoFactory = DAOFactory.getFactory();

		Queue queue=daoFactory.getQueueDAO().defaultQueue();
		List<Job> completedJobs = determineCompletedJobs(daoFactory);
		completedJobs.addAll(determineCancelledJobs(daoFactory));
		for (Job job : completedJobs) {
			QueueEntry entry=queue.removeJob(job);
			daoFactory.getQueueEntryDAO().delete(entry);
			daoFactory.getJobDAO().update(job);
		}
		daoFactory.getQueueDAO().update(queue);
		daoFactory.commit();
	}
	
	private List<Job> determineCompletedJobs(DAOFactory daoFactory) {
		return findJobsForStatus(daoFactory, Status.COMPLETE);
	}
	
	private List<Job> determineCancelledJobs(DAOFactory daoFactory) {
		return findJobsForStatus(daoFactory, Status.CANCELLED);
	}

	private void startWaitingJobs() {
		if (logger.isDebugEnabled()) logger.debug("Checking queue for new jobs");
		List<Job> waitingJobs = null;
		DAOFactory daoFactory = DAOFactory.getFactory();
		try {
			waitingJobs = determineWaitingJobs(daoFactory);
			if (waitingJobs.size() > 0) {
				logger.info(waitingJobs.size() + " waiting jobs found");
				List<Worker> availableWorkers =
					determineAvailableWorkers();
				if (availableWorkers.size() > 0) {
					logger.info(availableWorkers.size()
							+ " available workers found.");
					List<Job> assignedJobs = assignJobsToWorkers(daoFactory,availableWorkers,
							waitingJobs);
					daoFactory.commit();
					startJobs(assignedJobs);
				} else {
					logger.info("No workers available");
				}
			}
		} catch (Exception e) {
			logger.error("Error monitoring queue", e);
		}
	}
	
	private void killCancelledJobs() {
		DAOFactory daoFactory = DAOFactory.getFactory();
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
	 * Causes the thread to exit the poll loop.
	 *
	 */
	public void terminate() {
		terminate=true;
	}

	/**
	 * @return any jobs that have their status as QUEUED, in ID order (since that will be the order they were added to the queue).
	 */
	private List<Job> determineWaitingJobs(DAOFactory daoFactory) {
		return findJobsForStatus(daoFactory,Status.QUEUED);
	}

	private List<Job> findJobsForStatus(DAOFactory daoFactory, Status status) {
		List<Job> result = new ArrayList<Job>();
		Queue defaultQueue = daoFactory.getQueueDAO().defaultQueue();
		defaultQueue = daoFactory.getQueueDAO().refresh(defaultQueue);
		for (Job job : defaultQueue.getJobs()) {
			job = daoFactory.getJobDAO().refresh(job);
			if (job.getStatus().equals(status)) {
				result.add(job);
			}
		}
		return result;
	}
	
	/** 
	 * @return any workers that are not busy
	 */
	private List<Worker> determineAvailableWorkers() {
		DAOFactory daoFactory=DAOFactory.getFactory();
		List<Worker> result = new ArrayList<Worker>();
		
		Queue defaultQueue = daoFactory.getQueueDAO().defaultQueue();
		for (Worker worker : defaultQueue.getWorkers()) {
			if (!worker.isBusy()) { 
				result.add(worker);
			}
		}	

		return result;
	}
	
	private List<Job> assignJobsToWorkers(DAOFactory daoFactory,List<Worker> workers, List<Job> jobs) {
		List<Job> result = new ArrayList<Job>();
		int jobIndex=0;
		
		WorkerDAO workerDAO = daoFactory.getWorkerDAO();
		JobDAO jobDAO = daoFactory.getJobDAO();
		for (Worker worker : workers) {
			if (jobIndex >= jobs.size()) {
				break;
			}
			Job job = jobs.get(jobIndex);
			worker.assignJob(job);
			jobDAO.update(job);
			workerDAO.update(worker);
			jobIndex++;
			result.add(job);
		}
		
		return result;
	}	
	
	private void startJobs(List<Job> jobs) {
		for (Job job : jobs) {
			Worker worker = job.getWorker();
			if (worker!=null) {
				logger.info("Starting job execution:"+job.getId());
				JobExecutor executor = jobExecutorFactory.createExecutor(uriFactory);
				executor.executeJob(job, worker);
			}
			else {
				logger.error("Assigned job has no worker [jobid="+job.getId()+"]");
			}
		}
	}
}
