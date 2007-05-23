package net.sf.taverna.service.datastore.dao;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.jpa.JPADAOFactory;

public abstract class DAOFactory {

	/**
	 * Annotate a method of the {@link DAOFactory} that provides access to a DAO
	 * implementation, such as {@link DAOFactory#getJobDAO()} that returns a
	 * {@link Job}
	 * 
	 * @author Stian Soiland
	 */
	@Retention(RetentionPolicy.RUNTIME)
	@Target(ElementType.METHOD)
	public @interface DAO {
		Class<?> value();
	}

	private static DAOFactory factory;

	private static final Class<? extends DAOFactory> defaultFactory =
		JPADAOFactory.class;

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
	public synchronized static DAOFactory createDefaultFactory()
		throws RuntimeException {
		try {
			return defaultFactory.newInstance();
		} catch (InstantiationException e) {
			throw new RuntimeException("Could not instanciate DAOFactory "
				+ defaultFactory, e);
		} catch (IllegalAccessException e) {
			throw new RuntimeException("Could not instanciate DAOFactory "
				+ defaultFactory, e);
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

	@DAO(Job.class)
	public abstract JobDAO getJobDAO();

	@DAO(Workflow.class)
	public abstract WorkflowDAO getWorkflowDAO();

	@DAO(DataDoc.class)
	public abstract DataDocDAO getDataDocDAO();

	@DAO(User.class)
	public abstract UserDAO getUserDAO();

	@DAO(Worker.class)
	public abstract WorkerDAO getWorkerDAO();

	@DAO(Queue.class)
	public abstract QueueDAO getQueueDAO();

	@DAO(QueueEntry.class)
	public abstract QueueEntryDAO getQueueEntryDAO();

}
