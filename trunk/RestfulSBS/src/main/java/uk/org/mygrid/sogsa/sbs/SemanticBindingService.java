package uk.org.mygrid.sogsa.sbs;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Handler;
import java.util.logging.Level;

import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.resource.Resource;

public class SemanticBindingService extends Application {

	private Component component = null;

	private Map<String, Binding> bindingList;

	public SemanticBindingService() {
		init();
		bindingList = new ConcurrentHashMap<String, Binding>();
	}

	private void init() {
		// read all the binding keys from the database and intialise the list
		initializeRestletLogging();
	}

	@Override
	public Restlet createRoot() {
		return createComponent();
	}

	/**
	 * Creates the RESTFUL {@link Component} which handles all the routes,
	 * logging etc
	 * 
	 * @return
	 */
	private Component createComponent() {
		Component component = new Component();
		attachRoutes(component);
		return component;
	}

	/**
	 * Associate URLs with the {@link Resource}s which will deal with any
	 * RESTFUL calls to them
	 * 
	 * @param component
	 */
	private void attachRoutes(Component component) {

		// /sbs create a binding using the SemanticBindings class
		component.getDefaultHost().attach("/sbs", SemanticBindings.class);
		// /sbs/UUID get a binding using the SemanticBinding class
		// class
		component.getDefaultHost().attach("/sbs/{binding}/",
				SemanticBinding.class);
		// /sbs/UUID/add update the RDF in a binding
		// component.getDefaultHost().attach("/sbs/{binding}/add",
		// RDFUpdate.class);

	}

	private void initializeRestletLogging() {

		Handler[] handlers = java.util.logging.Logger.getLogger("")
				.getHandlers();
		for (Handler handler : handlers) {
			handler.setFormatter(new ReallySimpleFormatter());
		}
		java.util.logging.Logger.getLogger("org.mortbay.log").setLevel(
				Level.WARNING);

	}

	public Map<String, Binding> getBindingList() {
		return bindingList;
	}
}
