package net.sf.taverna.service.datastore.dao;

import net.sf.taverna.service.datastore.dao.jpa.JPADAOFactory;

public abstract class DAOFactory {
	
	private static DAOFactory factory;
	
	private static final Class<? extends DAOFactory> defaultFactory = JPADAOFactory.class;
	
	/**
	 * Get the global factory, created if needed by calling
	 * {@link #createDefaultFactory()}. The global factory can be overriden
	 * with {@link #setFactory(DAOFactory)}.
	 * 
	 * @return The global {@link DAOFactory} implementation
	 * @throws RuntimeException
	 *             If the DAOFactory could not be instanciated.
	 */
	public synchronized static DAOFactory getFactory() throws RuntimeException {
		if (factory == null) {
			factory = createDefaultFactory();
		}
		return factory;
	}
	
	
	/**
	 * Get a fresh instance of the default factory. This is used internally at
	 * first call to {@link #getFactory()}, and can be used for test purposes
	 * to get a separate factory.
	 * 
	 * @return A fresh instance of the default {@link DAOFactory}
	 *         implementation, {@value #defaultFactory}
	 */
	public synchronized static DAOFactory createDefaultFactory() throws RuntimeException {
		try {
			return defaultFactory.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Could not instanciate DAOFactory " + defaultFactory, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not instanciate DAOFactory " + defaultFactory, e);				
		}
	}
	
	/**
	 * Set the factory to be returned by {@link #getFactory()}. Normally this
	 * function is not needed, and {@link #createDefaultFactory()} will be
	 * called on first {@link #getFactory()}.
	 * 
	 * @param factory
	 */
	public static void setFactory(DAOFactory factory) {
		DAOFactory.factory = factory;
	}
	
	public abstract void commit();
	
	public abstract void rollback();
	
	public abstract void close();
	
	public abstract JobDAO getJobDAO();
	
	public abstract WorkflowDAO getWorkflowDAO();
	
	public abstract DataDocDAO getDataDocDAO();

	public abstract UserDAO getUserDAO();
	
	public abstract WorkerDAO getWorkerDAO();
	
	public abstract QueueDAO getQueueDAO();

	public abstract QueueEntryDAO getQueueEntryDAO() ;
	
}
