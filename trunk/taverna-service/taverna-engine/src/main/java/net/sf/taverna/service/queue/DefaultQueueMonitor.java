package net.sf.taverna.service.queue;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import net.sf.taverna.service.backend.executor.JobExecutor;
import net.sf.taverna.service.backend.executor.JobExecutorFactory;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
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

	//FIXME: this should be increased after testing, or better still read from config
	private final int CHECK_PERIOD = 5; //checks every 5 seconds.
	private boolean terminate = false;

	private URIFactory uriFactory;
	
	public DefaultQueueMonitor(URIFactory uriFactory) {
		super("Queue Monitor Thread");
		this.uriFactory = uriFactory;
	}
	
	public void run() {
		while(!terminate) {
			killCancelledJobs();
			
			DAOFactory daoFactory = null;
			try {
				daoFactory = DAOFactory.getFactory();

				List<Job> waitingJobs = determineWaitingJobs(daoFactory);
				if (waitingJobs.size() > 0) {
					logger.info(waitingJobs.size() + " waiting jobs found");
					List<Worker> availableWorkers =
						determineAvailableWorkers(daoFactory);
					if (availableWorkers.size() > 0) {
						logger.info(availableWorkers.size()
							+ " available workers found.");
						assignJobsToWorkers(daoFactory, availableWorkers,
							waitingJobs);
					} else {
						logger.info("No workers available");
					}
				}
			} catch (Exception e) {
				logger.error("Error monitoring queue", e);
			} finally {
				if (daoFactory != null) {
					daoFactory.commit();
					daoFactory.close();
				}
			}
			
			kickstartWorkers();
			
			try {
				if (!terminate) Thread.sleep(CHECK_PERIOD * 1000);
			} catch (InterruptedException e) {
				terminate = true;
				Thread.currentThread().interrupt();
			}
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
		List<Job> result = new ArrayList<Job>();
		Queue defaultQueue = daoFactory.getQueueDAO().defaultQueue();
		defaultQueue = daoFactory.getQueueDAO().refresh(defaultQueue);
		for (Job job : defaultQueue.getJobs()) {
			job = daoFactory.getJobDAO().refresh(job);
			if (job.getStatus().equals(Status.QUEUED)) {
				result.add(job);
			}
		}
		return result;
	}
	
	/** 
	 * @return any workers that are not busy
	 */
	private List<Worker> determineAvailableWorkers(DAOFactory daoFactory) {
		List<Worker> result = new ArrayList<Worker>();
		Queue defaultQueue = daoFactory.getQueueDAO().defaultQueue();
		for (Worker worker : defaultQueue.getWorkers()) {
			if (!worker.isBusy()) { 
				result.add(worker);
			}
		}
		return result;
	}
	
	private void assignJobsToWorkers(DAOFactory daoFactory, List<Worker> workers, List<Job> jobs) {
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
		}
	}	
	
	private void kickstartWorkers() {
		DAOFactory daoFactory = DAOFactory.getFactory();
		Set<Worker> workers =  daoFactory.getQueueDAO().defaultQueue().getWorkers();
		for (Worker worker : workers) {
			worker = daoFactory.getWorkerDAO().reread(worker);
			if (!worker.isRunning()) {
				Job job = worker.getNextDequeuedJob();
				if (job != null) {
//					job = daoFactory.getJobDAO().refresh(job);
//					if (! job.getStatus().equals(Status.DEQUEUED)) {
//						logger.warn("dequeued job " + job + " was in status "
//							+ job.getStatus());
//						worker.getJobs().remove(job);
//						continue;
//					}
					logger.info("Starting job execution:"+job.getId());
					JobExecutor executor = jobExecutorFactory.createExecutor(uriFactory);
					daoFactory.commit();
					executor.executeJob(job, worker);
				}
			}
		}
		
		
	}
}
