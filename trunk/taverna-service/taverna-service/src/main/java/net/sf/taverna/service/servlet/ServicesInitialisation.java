package net.sf.taverna.service.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.taverna.service.queue.DefaultQueueMonitor;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;

public class ServicesInitialisation implements ServletContextListener {

	private static Logger logger = Logger.getLogger(ServicesInitialisation.class); 
	
	private DefaultQueueMonitor queueMonitor;
	
	public void contextDestroyed(ServletContextEvent event) {
		stopQueueMonitor();
	}

	private void stopQueueMonitor() {
		logger.info("Stopping the Job Queue Monitor");
		if (queueMonitor!=null) {
			queueMonitor.terminate();
		}
	}

	public void contextInitialized(ServletContextEvent event) {
		initialiseUriFactory(event.getServletContext());
		startQueueMonitor();
	}

	private void startQueueMonitor() {
		logger.info("Starting the Job Queue Monitor");
		logger.info("Base URI set to:"+URIFactory.getInstance().getRoot());
		queueMonitor=new DefaultQueueMonitor();
		queueMonitor.setDaemon(true);
		queueMonitor.start();
	}
	
	private void initialiseUriFactory(ServletContext context) {
		URIFactory uriFactory = URIFactory.getInstance();
		String base = context.getInitParameter("baseuri");
		logger.info("Using baseURI of "+base);
		uriFactory.setRoot(base+"/v1");
		uriFactory.setHTMLRoot(base+"/html");
	}
}
