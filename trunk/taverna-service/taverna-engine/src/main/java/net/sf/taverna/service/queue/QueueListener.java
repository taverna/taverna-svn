package net.sf.taverna.service.queue;

import net.sf.taverna.service.queue.Job.State;

import org.apache.log4j.Logger;



public abstract class QueueListener implements Runnable {

	private static Logger logger = Logger.getLogger(QueueListener.class);
	
	final static int QUEUE_TIMEOUT = 100;

	boolean running;
	TavernaQueue queue;

	public QueueListener(TavernaQueue queue) {
		this.running = true;
		this.queue = queue;
	}

	public void stop() {
		this.running = false;
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
		job.setState(State.RUNNING);
		try {
			logger.debug("Executing job " + job);
			execute(job);			
			logger.debug("Completed job " + job);
			if (! job.isFinished()) {
				logger.warn("Timed out for unfinished " + job + ", in state: " + job.getState());
			}
		} catch (Throwable t) {
			logger.warn("Job " + job + " processing failed", t);
			job.setState(State.FAILED);
			if (t instanceof Error) {
				// Serious stuff we should not catch
				throw (Error) t;
			}
		}
	}
	
	abstract void execute(Job job) throws Exception;	
}
