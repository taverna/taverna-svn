package net.sf.taverna.service.rest;

import java.net.URL;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.CapabilitiesResource;
import net.sf.taverna.service.rest.resources.CurrentUserResource;
import net.sf.taverna.service.rest.resources.DataResource;
import net.sf.taverna.service.rest.resources.DatasResource;
import net.sf.taverna.service.rest.resources.JobReportResource;
import net.sf.taverna.service.rest.resources.JobResource;
import net.sf.taverna.service.rest.resources.JobStatusResource;
import net.sf.taverna.service.rest.resources.JobsResource;
import net.sf.taverna.service.rest.resources.UserResource;
import net.sf.taverna.service.rest.resources.UsersResource;
import net.sf.taverna.service.rest.resources.WorkflowResource;
import net.sf.taverna.service.rest.resources.WorkflowsResource;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Directory;
import org.restlet.Filter;
import org.restlet.Guard;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;
import org.restlet.util.Template;

public class RestApplication {

	private static Logger logger = Logger.getLogger(RestApplication.class);

	private static final String WORKFLOW = "/{workflow}";

	private static final String JOB = "/{job}";

	private static final String USER = "/{user}";

	private static final String DATA = "/{data}";

	private URIFactory uriFactory = URIFactory.getInstance();

	DAOFactory daoFactory = DAOFactory.getFactory();

	private Component component;

	public void startServer(int port) {
		stopServer();
		if (component == null) {
			component = createComponent();
		}
		String base = "http://localhost:" + port;
		uriFactory.setRoot(base + "/v1");
		uriFactory.setHTMLRoot(base + "/html");
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

	public class DaoCloseFilter extends Filter {
		public DaoCloseFilter(Context context, Restlet next) {
			super(context, next);
		}

		@Override
		protected void afterHandle(Request req, Response response) {
			super.afterHandle(req, response);
			try {
				daoFactory.rollback();
			} catch (IllegalStateException ex) { // Expected
			} finally {
				daoFactory.close();
			}
		}
	}

	private Component createComponent() {
		Component component = new Component();
		component.getClients().add(Protocol.FILE);

		URL htmlSource = this.getClass().getResource("/html");
		Directory htmlDir =
			new Directory(component.getContext(), htmlSource.toString());
		component.getDefaultHost().attach("/html", htmlDir);

		Router router = new Router(component.getContext());
		DaoCloseFilter daoCloser =
			new DaoCloseFilter(component.getContext(), router);
		component.getDefaultHost().attach("/v1", daoCloser);

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
		// /jobs/X/status
		authenticated.attach(uriFactory.getMapping(Job.class) + JOB +
			uriFactory.getMappingStatus(), JobStatusResource.class);
		// /jobs/X/report
		authenticated.attach(uriFactory.getMapping(Job.class) + JOB +
				uriFactory.getMappingReport(), JobReportResource.class);
		// /workflows/X
		authenticated.attach(uriFactory.getMapping(Workflow.class) + WORKFLOW,
			WorkflowResource.class);
		// /data/X
		authenticated.attach(uriFactory.getMapping(DataDoc.class) + DATA,
			DataResource.class);

		// /users;current
		authenticated.attach(uriFactory.getMapping(User.class)
			+ uriFactory.getMappingCurrentUser(), CurrentUserResource.class);

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
