package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.User;

import org.apache.log4j.Logger;
import org.restlet.data.Reference;

public class UserREST extends LinkedREST<User> {

	private static Logger logger = Logger.getLogger(UserREST.class);


	public UserREST(RESTContext context, Reference uri) {
		super(context, uri, User.class);
	}

	public UserREST(RESTContext context, Reference uri, User document) {
		super(context, uri, document);
	}

	public UserREST(RESTContext context, User document) {
		super(context, document);
	}

	public WorkflowsREST getWorkflows() {
		return new WorkflowsREST(context, getDocument().getWorkflows());
	}
	
	public String getUsername() {
		return getDocument().getUsername();
	}

	public JobsREST getJobs() {
		return new JobsREST(context, getDocument().getJobs());
	}

}
