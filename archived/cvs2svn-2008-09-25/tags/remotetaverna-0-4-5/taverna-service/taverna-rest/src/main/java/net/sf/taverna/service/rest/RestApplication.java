package net.sf.taverna.service.rest;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.net.URL;
import java.util.logging.Handler;
import java.util.logging.Level;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.rest.resources.AdminCreationResource;
import net.sf.taverna.service.rest.resources.CapabilitiesResource;
import net.sf.taverna.service.rest.resources.ConfigurationResource;
import net.sf.taverna.service.rest.resources.CurrentUserResource;
import net.sf.taverna.service.rest.resources.DataResource;
import net.sf.taverna.service.rest.resources.DatasResource;
import net.sf.taverna.service.rest.resources.DefaultQueueResource;
import net.sf.taverna.service.rest.resources.JobConsoleResource;
import net.sf.taverna.service.rest.resources.JobReportResource;
import net.sf.taverna.service.rest.resources.JobResource;
import net.sf.taverna.service.rest.resources.JobStatusResource;
import net.sf.taverna.service.rest.resources.JobsResource;
import net.sf.taverna.service.rest.resources.QueueResource;
import net.sf.taverna.service.rest.resources.QueuesResource;
import net.sf.taverna.service.rest.resources.UserAddResource;
import net.sf.taverna.service.rest.resources.UserEditResource;
import net.sf.taverna.service.rest.resources.UserRegisterResource;
import net.sf.taverna.service.rest.resources.UserResource;
import net.sf.taverna.service.rest.resources.UsersResource;
import net.sf.taverna.service.rest.resources.WorkerResource;
import net.sf.taverna.service.rest.resources.WorkersResource;
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
import org.restlet.Redirector;
import org.restlet.Restlet;
import org.restlet.Route;
import org.restlet.Router;
import org.restlet.data.Method;
import org.restlet.data.Protocol;
import org.restlet.data.Reference;
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
	
	private static final String WORKER = "/{worker}";

	protected DAOFactory daoFactory = DAOFactory.getFactory();

	private Component component = null;

	public RestApplication(Context context) {
		super(context);
		init();
	}

	public RestApplication() {
		super();
		init();
	}
	
	/**
	 * Do some pre-start initialisation.
	 * 
	 * This includes 
	 * <ul>
	 * 	<li>Creating a default worker, so there is always at least 1</li>
	 * </ul>
	 */
	private void init() {
		initializeRestletLogging();
		createDefaultQueue();
		createDefaultWorker();
	}
	
	private void initializeRestletLogging() {
	    Handler[] handlers = java.util.logging.Logger.getLogger("").getHandlers();
	    for (Handler handler : handlers) {
    		handler.setFormatter(new ReallySimpleFormatter());
	    }
	    java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
            logger.warn("Set logging to WARNING");
	}

	private void createDefaultWorker() {
		if (daoFactory.getWorkerDAO().all().size()==0) {
			logger.info("No workers exist. Creating a default worker");
			WorkerInitialisation.createNew();
		}
	}
	
	private void createDefaultQueue() {
		DAOFactory.getFactory().getQueueDAO().defaultQueue();
		DAOFactory.getFactory().commit();
		DAOFactory.getFactory().close();
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

	public class AdminPresentFilter extends Filter {
		private boolean adminFound=false;
		private Restlet orginalNext;
		
		public AdminPresentFilter(Context context, Restlet next) {
			super(context, next);
			orginalNext = next;
		}

		@Override
		protected void beforeHandle(Request req, Response resp) {
			super.beforeHandle(req, resp);
			if (req.getMethod().equals(Method.GET)) {
				checkForAdmin(req,resp);
			}
		}
		
		private void checkForAdmin(Request req, Response resp) {
			Reference createAdminRef = new Reference(req.getRootRef(), URIFactory.V1 + "/" + URIFactory.getMapping(User.class) + URIFactory.getMappingCreateAdmin()).getTargetRef();
			
			URIFactory uriFactory = URIFactory.getInstance();
			if (req.getResourceRef().equals(createAdminRef)) {
				// always pass through
				setNext(orginalNext);
				return;
			}
			if (!adminFound) { //only check once. Once an admin exists it cannot be demoted.
				if (daoFactory.getUserDAO().admins().isEmpty()) {
					resp.redirectTemporary(createAdminRef);
					setNext(new Restlet());  // to avoid 404
					//setNext(AdminCreationResource.class);
				} else {
					setNext(orginalNext);
					adminFound=true;
				}
			}
		}
	}
	
	public class DaoCloseFilter extends Filter {

		public DaoCloseFilter(Context context, Restlet next) {
			super(context, next);
		}
				
		@Override
		protected void afterHandle(Request req, Response response) {
			closeDao();
		}

		private void closeDao() {
			try {
				daoFactory.rollback();
			} catch (IllegalStateException ex) { 
				// Expected
			} finally {
				daoFactory.close();
			}
		}
	}

	protected void attachHTMLSource(Component component) {
		component.getClients().add(Protocol.FILE);
		URL htmlSource = this.getClass().getResource("/html");
		Directory htmlDir =
			new Directory(component.getContext(), htmlSource.toString());
		// Map /html
		component.getDefaultHost().attach("/" + URIFactory.HTML, htmlDir);
	}

	protected void attachFilters(Component component, Router router) {
		DaoCloseFilter daoCloser =
			new DaoCloseFilter(component.getContext(), router);
		AdminPresentFilter adminFilter=new AdminPresentFilter(component.getContext(),daoCloser);
		// Map /v1
		component.getDefaultHost().attach("/" + URIFactory.V1, adminFilter);
		
		//  Redirector for anything else not matching
		String template = "{oi}" + "/" + URIFactory.V1 + "/";
		Route route = component.getDefaultHost().attach("",
			new Redirector(component.getContext(), template,
				Redirector.MODE_CLIENT_TEMPORARY));
		route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
	}

	public Component createComponent() {
		Component component = new Component();
		
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
		
		route = router.attach("/"+URIFactory.getMapping(User.class)+URIFactory.getMappingRegisterUser(),UserRegisterResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
		
		route =
			router.attach("/" + URIFactory.getMapping(User.class)
				+ URIFactory.getMappingCreateAdmin(),
				AdminCreationResource.class);
		route.getTemplate().setMatchingMode(Template.MODE_STARTS_WITH);
		

		// Everything else goes through our authenticated router
		Router authenticated = new Router(userGuard.getContext());
		
		userGuard.setNext(authenticated);
		
		// /config
		authenticated.attach("/"+URIFactory.getMapping(Configuration.class),ConfigurationResource.class);
		// /jobs/X
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB,
			JobResource.class);
		// /jobs/X/status
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB
			+ URIFactory.getMappingStatus(), JobStatusResource.class);
		// /jobs/X/report
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB
			+ URIFactory.getMappingReport(), JobReportResource.class);
		// /jobs/X/console
		authenticated.attach("/" + URIFactory.getMapping(Job.class) + JOB
			+ URIFactory.getMappingConsole(), JobConsoleResource.class);		
		// /workflows/X
		authenticated.attach("/" + URIFactory.getMapping(Workflow.class)
			+ WORKFLOW, WorkflowResource.class);
		// /data/X
		authenticated.attach("/" + URIFactory.getMapping(DataDoc.class) + DATA,
			DataResource.class);

		// /users;current
		authenticated.attach("/" + URIFactory.getMapping(User.class)
			+ URIFactory.getMappingCurrentUser(), CurrentUserResource.class);

//		 /users;add
		authenticated.attach("/"+URIFactory.getMapping(User.class)+URIFactory.getMappingAddUser(), UserAddResource.class);
		
		// /users/X
		authenticated.attach("/" + URIFactory.getMapping(User.class) + USER,
			UserResource.class);
		
		// /users/X/edit
		authenticated.attach("/" + URIFactory.getMapping(User.class)+USER+URIFactory.getMappingEditUser(),UserEditResource.class);

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

		// /queues
		authenticated.attach("/" + URIFactory.getMapping(Queue.class),
			QueuesResource.class);
		// /queues/X
		authenticated.attach("/" + URIFactory.getMapping(Queue.class) + QUEUE,
			QueueResource.class);
		// /queues;default
		authenticated.attach("/" + URIFactory.getMapping(Queue.class)
			+ URIFactory.getMappingDefaultQueue(), DefaultQueueResource.class);

		// /workers
		authenticated.attach("/"+URIFactory.getMapping(Worker.class),WorkersResource.class);
		authenticated.attach("/"+URIFactory.getMapping(Worker.class) + WORKER,WorkerResource.class);
		// /workers/X

		return component;
	}
}
class ReallySimpleFormatter extends java.util.logging.Formatter {
	@Override
	public String format(java.util.logging.LogRecord record) {
		String msg = record.getMessage() + "\n";
		StringWriter sw = new StringWriter();
		if (record.getThrown() != null) {
			record.getThrown().printStackTrace(new PrintWriter(sw));
		}
		return msg + sw;
	}	
}