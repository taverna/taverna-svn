package net.sf.taverna.feta.browser;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.logging.Handler;
import java.util.logging.Level;

import net.sf.taverna.feta.browser.resources.IndexResource;
import net.sf.taverna.feta.browser.resources.MethodResource;
import net.sf.taverna.feta.browser.resources.MethodsResource;
import net.sf.taverna.feta.browser.resources.NamespaceResource;
import net.sf.taverna.feta.browser.resources.NamespacesResource;
import net.sf.taverna.feta.browser.resources.OrganisationResource;
import net.sf.taverna.feta.browser.resources.OrganisationsResource;
import net.sf.taverna.feta.browser.resources.RegistryUpdateResource;
import net.sf.taverna.feta.browser.resources.ResourceResource;
import net.sf.taverna.feta.browser.resources.ResourcesResource;
import net.sf.taverna.feta.browser.resources.ServiceResource;
import net.sf.taverna.feta.browser.resources.ServicesResource;
import net.sf.taverna.feta.browser.resources.TaskResource;
import net.sf.taverna.feta.browser.resources.TasksResource;
import net.sf.taverna.feta.browser.resources.TypeResource;
import net.sf.taverna.feta.browser.resources.TypesResource;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Filter;
import org.restlet.Restlet;
import org.restlet.Router;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;
import org.restlet.data.Request;
import org.restlet.data.Response;

public class FetaApplication extends Application {

	private static Logger logger = Logger.getLogger(FetaApplication.class);

	private Component component;

	private int port = 0;

	public FetaApplication(Context context) {
		super(context);
		init();
	}

	public FetaApplication(int port) {
		this.port = port;
		init();
	}

	public Component createComponent() {
		Component component = new Component();
		Router router = createRouter(component.getContext());
		Restlet wrapper = createWrapper(router);
		VirtualHost defaultHost = component.getDefaultHost();
		defaultHost.attach(wrapper);
		// defaultHost.attach("/", filter);
		return component;
	}

	protected Restlet createWrapper(Router router) {
		// Add any Filters here
		return router;
	}

	protected Router createRouter(Context context) {
		Router router = new Router(context);
		router.attach("/services/{id}", ServiceResource.class);
		router.attach("/services/", ServicesResource.class);
		router.attach("/organisations/{name}", OrganisationResource.class);
		router.attach("/organisations/", OrganisationsResource.class);
		router.attach("/tasks/{name}", TaskResource.class);
		router.attach("/tasks/", TasksResource.class);
		router.attach("/resources/{name}", ResourceResource.class);
		router.attach("/resources/", ResourcesResource.class);
		router.attach("/methods/{name}", MethodResource.class);
		router.attach("/methods/", MethodsResource.class);
		router.attach("/types/{name}", TypeResource.class);
		router.attach("/types/", TypesResource.class);
		router.attach("/namespaces/{name}", NamespaceResource.class);
		router.attach("/namespaces/", NamespacesResource.class);
		router.attach("/registry;update", RegistryUpdateResource.class);
		router.attach("/", IndexResource.class);
		return router;
	}

	@Override
	public Restlet createRoot() {
		return createComponent();
	}

	public void startServer() {
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

	protected void initializeRestletLogging() {
		Handler[] handlers = java.util.logging.Logger.getLogger("")
				.getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new ReallySimpleFormatter());
		}
		java.util.logging.Logger.getLogger("").setLevel(Level.WARNING);
		logger.warn("Set logging to WARNING");
	}

	protected void init() {
		initializeRestletLogging();
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
