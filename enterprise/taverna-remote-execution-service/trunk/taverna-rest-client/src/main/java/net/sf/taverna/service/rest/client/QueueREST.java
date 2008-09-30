package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Queue;

import org.restlet.data.Reference;

public class QueueREST extends LinkedREST<Queue> {
	//private static Logger logger = Logger.getLogger(QueueREST.class);

	public QueueREST(RESTContext context, Reference uri) {
		super(context, uri, Queue.class);
	}

	public QueueREST(RESTContext context, Queue document) {
		super(context, document);
	}
	
	public JobsREST getJobs() {
		return new JobsREST(context, getDocument().getJobs());
	}
	
	public QueueREST clone() {
		return new QueueREST(context, getURIReference());
	}
}
