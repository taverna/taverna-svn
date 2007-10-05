package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.Workflow;
import net.sf.taverna.service.xml.WorkflowDocument;
import static net.sf.taverna.service.rest.client.RESTContext.xmlOptions;

import org.restlet.data.Reference;


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
		return getDocument().getScufl().xmlText();
	}
	
	public WorkflowREST clone() {
		return new WorkflowREST(context, getURIReference());
	}

}
