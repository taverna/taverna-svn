package net.sf.taverna.feta.browser;

import net.sf.taverna.feta.browser.resources.OrganisationResource;
import net.sf.taverna.feta.browser.resources.ServiceResource;
import net.sf.taverna.feta.browser.resources.ServicesResource;
import net.sf.taverna.feta.browser.resources.UpdateRegistryResource;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Restlet;
import org.restlet.VirtualHost;
import org.restlet.data.Protocol;

public class FetaApplication extends Application {

	private static Logger logger = Logger.getLogger(FetaApplication.class);

	private Component component;

	private String host;

	private int port;

	public FetaApplication(String host, int port) {
		this.host = host;
		this.port = port;
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

	public Component createComponent() {
		Component component = new Component();
		VirtualHost defaultHost = component.getDefaultHost();
		defaultHost.attach("/services/{id}", ServiceResource.class);
		defaultHost.attach("/services/", ServicesResource.class);
		defaultHost.attach("/organisations/{name}", OrganisationResource.class);
		defaultHost.attach("/registry;update", UpdateRegistryResource.class);
		return component;
	}

}
