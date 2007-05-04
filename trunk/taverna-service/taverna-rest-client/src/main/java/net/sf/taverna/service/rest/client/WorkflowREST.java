package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Workflow;


public class WorkflowREST extends OwnedREST<Workflow> {

	public WorkflowREST(RESTContext context, Workflow document) {
		super(context, document);
	}
	
	public WorkflowREST(RESTContext context, String uri, Workflow document) {
		super(context, uri, document);
	}
	
	public WorkflowREST(RESTContext context, String uri,
		Class<Workflow> documentClass) {
		super(context, uri, documentClass);
	}
	
	public String getScufl() {
		return getDocument().getScufl().toString();
	}
}
