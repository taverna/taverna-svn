package net.sf.taverna.service.queue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.interfaces.ParseException;
import net.sf.taverna.service.interfaces.QueueException;

public class TavernaQueue {
	
	//private static Logger logger = Logger.getLogger(TavernaQueue.class);
	
	Queue<Job> queue;
	
	private static DAOFactory daoFactory = DAOFactory.getFactory();
	
	public TavernaQueue() {
		queue = new LinkedBlockingQueue<Job>();
	}
	
	public synchronized int size() {		
		return queue.size();
	}
	
	public Job add(String scufl, String inputDoc) throws QueueException, ParseException {
		Job job = new Job();
		Workflow workflow = new Workflow();
		workflow.setScufl(scufl);
		daoFactory.getWorkflowDAO().create(workflow);
		job.setWorkflow(workflow);
		DataDoc inputs = new DataDoc();
		inputs.setBaclava(inputDoc);
		daoFactory.getDataDocDAO().create(inputs);
		job.setInputs(inputs);	
		daoFactory.getJobDAO().create(job);
		synchronized(this) {
			if (! queue.offer(job)) {
				throw new QueueException("Can't add to queue");
			}
			job.setStatus(Status.QUEUED);
			daoFactory.getJobDAO().update(job);
			notifyAll();
			return job;	
		}
	}	

	public synchronized Job peek() {		
		return queue.peek();		
	}

	public synchronized Job poll() {	
		Job job = queue.poll();
		if (job != null) {
			job.setStatus(Status.DEQUEUED);
			daoFactory.getJobDAO().update(job);
			notifyAll();
		}
		return job;
	}
			
}
