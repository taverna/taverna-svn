package net.sf.taverna.service.queue;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.JobDAO;

import org.apache.log4j.Logger;



public abstract class QueueListener implements Runnable {

	DAOFactory daoFactory = DAOFactory.getFactory();
	
	JobDAO jobDao = daoFactory.getJobDAO();
	
	private static Logger logger = Logger.getLogger(QueueListener.class);
	
	final static int QUEUE_TIMEOUT = 100;

	boolean running;
	TavernaQueue queue;

	public QueueListener(TavernaQueue queue) {
		running = true;
		this.queue = queue;
	}

	public void stop() {
		running = false;
	}
	
	public void run() {
		while (running) {
			Job job;
			synchronized (queue) {
				try {									
					queue.wait(QUEUE_TIMEOUT);				
				} catch (InterruptedException e) {
					// pass
				}
				job = queue.poll();
			}
			if (job == null) {
				continue;
			}
			process(job);
		}
	}

	void process(Job job) {		
		job.setStatus(Status.RUNNING);
		//jobDao.update(job);
		daoFactory.commit();
		try {
			logger.debug("Executing job " + job);
			execute(job);			
			logger.debug("Completed job " + job);
			if (! job.isFinished()) {
				logger.warn("Timed out for unfinished " + job + ", in state: " + job.getStatus());
			}
		} catch (Throwable t) {
			logger.warn("Job " + job + " processing failed", t);
			job.setStatus(Status.FAILED);
			jobDao.update(job);
			daoFactory.commit(); // again?
			if (t instanceof Error) {
				// Serious stuff we should not catch
				throw (Error) t;
			}
		}
	}
	
	abstract void execute(Job job) throws Exception;	
}
