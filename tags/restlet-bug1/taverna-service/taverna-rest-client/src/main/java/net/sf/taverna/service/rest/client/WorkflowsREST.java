package net.sf.taverna.service.rest.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.xml.Workflow;
import net.sf.taverna.service.xml.Workflows;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;
import org.restlet.data.Response;

public class WorkflowsREST extends LinkedREST<Workflows> implements
	Iterable<WorkflowREST> {

	private static Logger logger = Logger.getLogger(WorkflowsREST.class);

	public WorkflowsREST(RESTContext context, Reference uri, Workflows document) {
		super(context, uri, document);
	}

	public WorkflowsREST(RESTContext context, Workflows document) {
		super(context, document);
	}

	public WorkflowsREST(RESTContext context, Reference uri,
		Class<Workflows> documentClass) {
		super(context, uri, documentClass);
	}

	public List<WorkflowREST> getWorkflows() {
		List<WorkflowREST> workflows = new ArrayList<WorkflowREST>();
		for (Workflow wf : getDocument().getWorkflowArray()) {
			workflows.add(new WorkflowREST(context, wf));
		}
		return workflows;
	}

	public Iterator<WorkflowREST> iterator() {
		// FIXME: Should not create all the WorkflowREST objects before needed
		return getWorkflows().iterator();
	}

	public WorkflowREST add(String workflow) throws NotSuccessException {
		Response response = context.post(getURIReference(), workflow, RESTContext.scuflType);
		if (response.getRedirectRef() == null) {
			logger.error("Did not get redirect reference for workflow " + workflow);
			return null;
		}
		return new WorkflowREST(context, response.getRedirectRef());
	}
	
}
