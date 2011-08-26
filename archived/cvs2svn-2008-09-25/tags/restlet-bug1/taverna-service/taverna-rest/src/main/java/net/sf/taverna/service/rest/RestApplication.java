package net.sf.taverna.service.rest;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.CapabilitiesResource;
import net.sf.taverna.service.rest.resources.CurrentUserResource;
import net.sf.taverna.service.rest.resources.DataResource;
import net.sf.taverna.service.rest.resources.DatasResource;
import net.sf.taverna.service.rest.resources.JobResource;
import net.sf.taverna.service.rest.resources.JobsResource;
import net.sf.taverna.service.rest.resources.UserResource;
import net.sf.taverna.service.rest.resources.UsersResource;
import net.sf.taverna.service.rest.resources.WorkflowResource;
import net.sf.taverna.service.rest.resources.WorkflowsResource;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Guard;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.util.Template;

public class RestApplication {

	public static final String CURRENT = ";current";

	private static Logger logger = Logger.getLogger(RestApplication.class);

	private static final String WORKFLOW = "/{workflow}";

	private static final String JOB = "/{job}";

	private static final String USER = "/{user}";

	private static final String DATA = "/{data}";

	private static final int DEFAULT_PORT = 8976;

	public static void main(String[] args) {
		int port = DEFAULT_PORT;
		if (args.length > 0) {
			port = Integer.parseInt(args[0]);
		}
		new RestApplication().startServer(port);
	}

	private URIFactory uriFactory = URIFactory.getInstance();

	DAOFactory daoFactory = DAOFactory.getFactory();

	private Component component;

	public void startServer(int port) {
		stopServer();
		if (component == null) {
			component = createComponent();
		}
		uriFactory.setRoot("http://localhost:" + port + "/v1");
		// Create a new Restlet component and add a HTTP server connector to it
		component.getServers().add(Protocol.HTTP, port);

		// Now, let's start the component!
		// Note that the HTTP server connector is also automatically started.
		try {
			component.start();
		} catch (Exception ex) {
			// TODO Auto-generated catch block
			ex.printStackTrace();
		}
	}

	public void stopServer() {
		if (component != null && component.isStarted()) {
			try {
				component.stop();
			} catch (Exception e) {
				logger.warn("Could not stop server " + component, e);
			}
		}
		component = null;
	}

	private Component createComponent() {
		Component component = new Component();
		Router router = new Router(component.getContext());
		component.getDefaultHost().attach("/v1", router);

		Guard userGuard = new UserGuard(router.getContext());


		// Authenticate access to mostly anything
		router.attach(userGuard);
		// /user (exact match) is not authenticated (for registering with POST)
		Route route =
			router.attach(uriFactory.getMapping(User.class),
				UsersResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		
		// Capabilities at / is also unprotected
		route = router.attach("/", CapabilitiesResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);
		
		// Everything else goes through our authenticated router
		Router authenticated = new Router(userGuard.getContext());
		userGuard.setNext(authenticated);
		// /jobs/X
		authenticated.attach(uriFactory.getMapping(Job.class) + JOB,
			JobResource.class);
		// /workflows/X
		authenticated.attach(uriFactory.getMapping(Workflow.class) + WORKFLOW,
			WorkflowResource.class);
		// /data/X
		authenticated.attach(uriFactory.getMapping(DataDoc.class) + DATA,
			DataResource.class);
		
		// /users;current
		authenticated.attach(uriFactory.getMapping(User.class) + CURRENT,
			CurrentUserResource.class);
		// /users/X
		authenticated.attach(uriFactory.getMapping(User.class) + USER,
			UserResource.class);

		// Collections - below user
		
		// /users/X/workflows
		authenticated.attach(uriFactory.getMapping(User.class) + USER
			+ uriFactory.getMapping(Workflow.class), WorkflowsResource.class);
		// /users/X/data
		authenticated.attach(uriFactory.getMapping(User.class) + USER
			+ uriFactory.getMapping(DataDoc.class), DatasResource.class);
		// /users/X/jobs
		authenticated.attach(uriFactory.getMapping(User.class) + USER
			+ uriFactory.getMapping(Job.class), JobsResource.class);

		
		// TODO: Queues and workers
		
		// /queues
		// /queues/X
		// /workers
		// /workers/X

		return component;
	}
}
