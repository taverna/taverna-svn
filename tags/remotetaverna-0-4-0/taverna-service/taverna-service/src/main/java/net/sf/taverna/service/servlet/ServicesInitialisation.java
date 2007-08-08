package net.sf.taverna.service.servlet;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.queue.DefaultQueueMonitor;
import net.sf.taverna.service.rest.resources.representation.VelocityRepresentation;
import net.sf.taverna.service.rest.utils.URIFactory;

import org.apache.log4j.Logger;

public class ServicesInitialisation implements ServletContextListener {

	private static Logger logger = Logger.getLogger(ServicesInitialisation.class); 
	
	private DefaultQueueMonitor queueMonitor;
	
	private static final String TEMPLATES_PATH="/templates/";
	
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
		createDefaultQueue();
		startQueueMonitor(event.getServletContext());
	}
	
	private void createDefaultQueue() {
		DAOFactory daoFactory = DAOFactory.getFactory();
		try {
			if(daoFactory.getQueueDAO().all().size()==0) {
				daoFactory.getQueueDAO().defaultQueue();
				daoFactory.commit();
			}
		}
		catch(Exception e) {
			logger.error("Error creating default queue",e);
		}
		finally {
			daoFactory.close();
		}
	}

	private void startQueueMonitor(ServletContext context) {
		logger.info("Starting the Job Queue Monitor");
		URIFactory uriFactory = URIFactory.getInstance();
		logger.info("Setting velocity template path to:"+context.getRealPath(TEMPLATES_PATH));
		VelocityRepresentation.setResourcePath(context.getRealPath(TEMPLATES_PATH));
		queueMonitor=new DefaultQueueMonitor(uriFactory);
		queueMonitor.setDaemon(true);
		queueMonitor.start();
	}
	
}
