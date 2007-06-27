package net.sf.taverna.service.rest;

import java.net.URL;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
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
import net.sf.taverna.service.rest.resources.QueueResource;
import net.sf.taverna.service.rest.resources.UserResource;
import net.sf.taverna.service.rest.resources.UsersResource;
import net.sf.taverna.service.rest.resources.WorkflowResource;
import net.sf.taverna.service.rest.resources.WorkflowsResource;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;
import org.restlet.Application;
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

public class RestApplication extends Application {

	private static Logger logger = Logger.getLogger(RestApplication.class);

	private static final String WORKFLOW = "/{workflow}";

	private static final String JOB = "/{job}";

	private static final String USER = "/{user}";

	private static final String DATA = "/{data}";

	private static final String QUEUE = "/{queue}";

	protected DAOFactory daoFactory = DAOFactory.getFactory();

	private Component component = null;

	public RestApplication(Context context) {
		super(context);
	}

	public RestApplication() {
		super();
	}

	@Override
	public Restlet createRoot() {
		return createComponent();
	}

	public void startServer(int port) {
		stopServer();
		if (component == null) {
			component = createComponent();
		}
		component.getServers().add(Protocol.HTTP, port);

		// Now, let's start the component!
		// Note that the HTTP server connector is also automatically started.
		try {
			component.start();
		} catch (Exception ex) {
			logger.error("Error starting the server", ex);
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

	protected void attachHTMLSource(Component component) {
		URL htmlSource = this.getClass().getResource("/html");
		Directory htmlDir =
			new Directory(component.getContext(), htmlSource.toString());
		// Map /html
		component.getDefaultHost().attach("/" + URIFactory.HTML, htmlDir);
	}

	protected void attachFilters(Component component, Router router) {
		DaoCloseFilter daoCloser =
			new DaoCloseFilter(component.getContext(), router);
		// Map /v1
		component.getDefaultHost().attach("/" + URIFactory.V1, daoCloser);
	}

	public Component createComponent() {
		Component component = new Component();
		component.getClients().add(Protocol.FILE);
		attachHTMLSource(component);
		
		
		Router router = new Router(component.getContext());
		attachFilters(component, router);

		Guard userGuard = new UserGuard(router.getContext());

		// Authenticate access to mostly anything
		router.attach(userGuard);
		// /user (exact match) is not authenticated (for registering with POST)
		Route route =
			router.attach("/" + URIFactory.getMapping(User.class),
				UsersResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		// Capabilities at / is also unprotected
		route = router.attach("/", CapabilitiesResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_EQUALS);

		// Everything else goes through our authenticated router
		Router authenticated = new Router(userGuard.getContext());
		userGuard.setNext(authenticated);
		// /jobs/X
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB,
			JobResource.class);
		// /jobs/X/status
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB
			+ URIFactory.getMappingStatus(), JobStatusResource.class);
		// /jobs/X/report
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB
			+ URIFactory.getMappingReport(), JobReportResource.class);
		// /workflows/X
		authenticated.attach("/" + URIFactory.getMapping(Workflow.class)
			+ WORKFLOW, WorkflowResource.class);
		// /data/X
		authenticated.attach("/" + URIFactory.getMapping(DataDoc.class) + DATA,
			DataResource.class);

		// /users;current
		authenticated.attach("/" + URIFactory.getMapping(User.class)
			+ URIFactory.getMappingCurrentUser(), CurrentUserResource.class);

		// /users/X
		authenticated.attach("/" + URIFactory.getMapping(User.class) + USER,
			UserResource.class);

		authenticated.attach("/" + URIFactory.getMapping(Worker.class) + USER,
			UserResource.class);

		// Collections - below user

		// /users/X/workflows
		authenticated.attach("/" + URIFactory.getMapping(User.class) + USER
			+ "/" + URIFactory.getMapping(Workflow.class),
			WorkflowsResource.class);
		// /users/X/data
		authenticated.attach("/" + URIFactory.getMapping(User.class) + USER
			+ "/" + URIFactory.getMapping(DataDoc.class), DatasResource.class);
		// /users/X/jobs
		authenticated.attach("/" + URIFactory.getMapping(User.class) + USER
			+ "/" + URIFactory.getMapping(Job.class), JobsResource.class);

		// TODO: Queues and workers

		// /queues
		// /queues/X
		authenticated.attach(URIFactory.getMapping(Queue.class) + QUEUE,
			QueueResource.class);

		// /workers
		// /workers/X

		return component;
	}
}
