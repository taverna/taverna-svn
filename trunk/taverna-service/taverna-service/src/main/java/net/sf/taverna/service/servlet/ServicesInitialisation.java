package net.sf.taverna.service.servlet;

import java.util.Enumeration;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import org.apache.derby.drda.NetworkServerControl;
import org.apache.log4j.Logger;
import net.sf.taverna.service.queue.DefaultQueueMonitor;
import net.sf.taverna.service.rest.utils.URIFactory;

public class ServicesInitialisation implements ServletContextListener {

	private static Logger logger = Logger.getLogger(ServicesInitialisation.class); 
	
	private DefaultQueueMonitor queueMonitor;
	
	public void contextDestroyed(ServletContextEvent event) {
		stopQueueMonitor();
		//stopDatabase();
	}

	private void stopQueueMonitor() {
		logger.info("Stopping the Job Queue Monitor");
		if (queueMonitor!=null) {
			queueMonitor.terminate();
		}
	}

	public void contextInitialized(ServletContextEvent event) {
		initialiseUriFactory(event.getServletContext());
		//startDatabase();
		startQueueMonitor();
	}

	private void startQueueMonitor() {
		logger.info("Starting the Job Queue Monitor");
		logger.info("Base URI set to:"+URIFactory.getInstance().getRoot());
		queueMonitor=new DefaultQueueMonitor();
		queueMonitor.setDaemon(true);
		queueMonitor.start();
	}
	
	private void startDatabase() {
		logger.info("About to start database");
		//NetworkServerControl.main(new String[] { "start", "-p", "1337" });
		logger.info("Database started");
	}
	
	private void stopDatabase() {
		//NetworkServerControl.main(new String[] { "stop", "-p", "1337" });
	}
	
	private void initialiseUriFactory(ServletContext context) {
		URIFactory uriFactory = URIFactory.getInstance();
		String base = "http://localhost:9090/taverna-service-1.0.0";
		uriFactory.setRoot(base+"/v1");
		uriFactory.setHTMLRoot(base+"/html");
	}
}
