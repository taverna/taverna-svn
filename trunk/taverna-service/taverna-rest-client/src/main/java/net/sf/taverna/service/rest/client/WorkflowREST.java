package net.sf.taverna.service.rest.client;

import org.restlet.data.Reference;

import net.sf.taverna.service.xml.Workflow;


public class WorkflowREST extends OwnedREST<Workflow> {

	public WorkflowREST(RESTContext context, Workflow document) {
		super(context, document);
	}
	
	public WorkflowREST(RESTContext context, Reference uri, Workflow document) {
		super(context, uri, document);
	}
	
	public WorkflowREST(RESTContext context, Reference uri) {
		super(context, uri, Workflow.class);
	}
	
	public String getScufl() {
		return getDocument().getScufl().toString();
	}
}
