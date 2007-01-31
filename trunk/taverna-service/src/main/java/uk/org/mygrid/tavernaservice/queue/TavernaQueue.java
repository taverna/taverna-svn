package uk.org.mygrid.tavernaservice.queue;

import java.util.Queue;
import java.util.concurrent.LinkedBlockingQueue;


import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;

import uk.org.mygrid.tavernaservice.queue.Job;
import uk.org.mygrid.tavernaservice.queue.Job.State;

public class TavernaQueue {
	
	Queue<Job> queue;
	
	public TavernaQueue() {
		queue = new LinkedBlockingQueue<Job>();
	}
	
	public synchronized int size() {		
		return queue.size();
	}
	
	public Job add(String workflow) throws QueueException {
		ScuflModel model = new ScuflModel();
		try {
			XScuflParser.populate(workflow, model, null);
		} catch (ScuflException ex) {
			throw new QueueException("Could not load workflow", ex);
		}
		Job job = new Job(model);
	
		synchronized(this) {
			if (! queue.offer(job)) {
				throw new QueueException("Can't add to queue");
			}
			job.setState(State.QUEUED);
			this.notifyAll();
			return job;			
		}		
	}	

	public synchronized Job peek() {		
		return queue.peek();		
	}

	public synchronized Job poll() {	
		Job job = queue.poll();
		if (job != null) {
			job.setState(State.DEQUEUED);
			this.notifyAll();
		}
		return job;
	}
			
}
