package net.sf.taverna.service.rest.client;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import net.sf.taverna.service.xml.Workflow;
import net.sf.taverna.service.xml.Workflows;

import org.apache.log4j.Logger;

public class WorkflowsREST extends LinkedREST<Workflows> implements
	Iterable<WorkflowREST> {

	private static Logger logger = Logger.getLogger(WorkflowsREST.class);

	public WorkflowsREST(RESTContext context, String uri, Workflows document) {
		super(context, uri, document);
	}

	public WorkflowsREST(RESTContext context, Workflows document) {
		super(context, document);
	}

	public WorkflowsREST(RESTContext context, String uri,
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
		return getWorkflows().iterator();
	}
	
}
