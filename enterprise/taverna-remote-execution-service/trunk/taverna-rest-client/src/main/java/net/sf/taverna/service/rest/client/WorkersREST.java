package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Workers;

import org.restlet.data.Reference;
import org.restlet.data.ReferenceList;

public class WorkersREST extends AbstractREST<Workers> {

	//private static Logger logger = Logger.getLogger(WorkersREST.class);
	
	public WorkersREST(RESTContext context, Reference uri) {
		super(context, uri, Workers.class);
	}
	
	public WorkersREST(RESTContext context, Reference uri, Workers document) {
		super(context, uri, document);
	}

	public void add(WorkerREST worker) throws NotSuccessException {
		ReferenceList urls = new ReferenceList();
		urls.add(worker.getURIReference());
		context.post(context.getWorkersURI(), urls);
		invalidate();
	}

	public WorkersREST clone() {
		return new WorkersREST(context, getURIReference());
	}
	
	
	
}
