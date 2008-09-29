package net.sf.taverna.service.rest.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.xml.Queue;
import net.sf.taverna.service.xml.Queues;

import org.restlet.data.Reference;

public class QueuesREST extends LinkedREST<Queues> {

	//private static Logger logger = Logger.getLogger(QueuesREST.class);

	public QueuesREST(RESTContext context, Reference uri) {
		super(context, uri, Queues.class);
	}

	public QueuesREST(RESTContext context, Queues document) {
		super(context, document);
	}
	
	public List<QueueREST> getQueues() {
		List<QueueREST> jobs = new ArrayList<QueueREST>();
		for (Queue q : getDocument().getQueueArray()) {
			jobs.add(new QueueREST(context, q));
		}
		return jobs;
	}

	public Iterator<QueueREST> iterator() {
		// FIXME: Should not create all the QueueREST objects before needed
		return getQueues().iterator();
	}

	public QueuesREST clone() {
		return new QueuesREST(context, getURIReference());
	}

}
