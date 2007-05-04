package net.sf.taverna.service.rest.client;

import net.sf.taverna.service.xml.User;

import org.apache.log4j.Logger;

public class UserREST extends LinkedREST<User> {

	private static Logger logger = Logger.getLogger(UserREST.class);

	public UserREST(RESTContext context, String uri) {
		super(context, uri, User.class);
	}

	public UserREST(RESTContext context, String uri, User document) {
		super(context, uri, document);
	}

	public UserREST(RESTContext context, User document) {
		super(context, document);
	}

	public WorkflowsREST getWorkflows() {
		return new WorkflowsREST(context, getDocument().getWorkflows());
	}

}
