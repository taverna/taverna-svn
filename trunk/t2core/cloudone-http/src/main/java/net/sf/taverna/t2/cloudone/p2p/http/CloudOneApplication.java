package net.sf.taverna.t2.cloudone.p2p.http;

import java.io.File;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Collections;
import java.util.logging.Handler;
import java.util.logging.Level;

import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;

import org.apache.log4j.Logger;
import org.restlet.Application;
import org.restlet.Component;
import org.restlet.Context;
import org.restlet.Restlet;
import org.restlet.data.Protocol;

public class CloudOneApplication extends Application {

	private static Logger logger = Logger.getLogger(CloudOneApplication.class);

	private Component component = null;

	FileDataManager dataManager;

	private String host;

	private int port;

	private String namespace;

	public CloudOneApplication(Context context) {
		super(context);
		init();
	}

	public CloudOneApplication(String host, int port) {
		super();
		this.host = host;
		this.port = port;
		init();

	}

	/**
	 * Do some pre-start initialisation.
	 * 
	 * This includes
	 * <ul>
	 * <li>Creating a default worker, so there is always at least 1</li>
	 * </ul>
	 */
	private void init() {
		initializeRestletLogging();
		createDatamanger();
	}

	private void createDatamanger() {
		namespace = "http2p_" + host + "_" + port;
		File userHome = new File(System.getProperty("user.home"));
		File dataManagerRoot = new File(userHome, ".cloudone");
		dataManager = new FileDataManager(namespace, Collections
				.<LocationalContext> emptySet(), dataManagerRoot);
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
		// Expose data manager
		component.getContext().getAttributes().put("dataManager", dataManager);
		EntityListIdentifier emptyList = dataManager.registerEmptyList(2);
		logger.debug("Registered " + emptyList);
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

	protected void attachDataManager(Component component) {
		component.getDefaultHost().attach("/", DataManagerResource.class);

	}

	public Component createComponent() {
		Component component = new Component();
		attachDataManager(component);
		return component;
	}

	public DataManager getDataManager() {
		return dataManager;
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