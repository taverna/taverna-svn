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
		startQueueMonitor(event.getServletContext());
	}

	private void startQueueMonitor(ServletContext context) {
		logger.info("Starting the Job Queue Monitor");
		String base=context.getInitParameter("baseuri");
		if (!base.endsWith("/")) base+="/";
		URIFactory uriFactory = URIFactory.getInstance(base + "rest/v1/");
		URIFactory.setHTMLpath(base+"html/");
		queueMonitor=new DefaultQueueMonitor(uriFactory);
		queueMonitor.setDaemon(true);
		queueMonitor.start();
	}
	
}
