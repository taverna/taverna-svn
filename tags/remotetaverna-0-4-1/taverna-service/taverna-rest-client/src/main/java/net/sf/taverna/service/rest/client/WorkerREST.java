package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Worker;

import org.restlet.data.Reference;

public class WorkerREST extends LinkedREST<Worker> {

	//private static Logger logger = Logger.getLogger(WorkerREST.class);
	
	public WorkerREST(RESTContext context, Reference uri) {
		super(context, uri, Worker.class);
	}

	public WorkerREST(RESTContext context, Worker document) {
		super(context, document);
	}
	
	public UserREST getUser() {
		return new UserREST(context, getDocument().getUser());
	}

	public JobsREST getJobs() {
		return new JobsREST(context, getDocument().getJobs());
	}
	
	public QueueREST getQueue() {
		return new QueueREST(context, getDocument().getQueue());
	}
	
	public WorkerREST clone() {
		return new WorkerREST(context, getURIReference());
	}
	

}
