package net.sf.taverna.service.datastore.dao.jpa;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.WeakHashMap;

import javax.persistence.EntityManager;
import javax.persistence.EntityTransaction;

import net.sf.taverna.service.datastore.EntityManagerUtil;
import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.ConfigurationDAO;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.QueueDAO;
import net.sf.taverna.service.datastore.dao.QueueEntryDAO;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;

import org.apache.log4j.Logger;

public class JPADAOFactory extends DAOFactory {

	private static Logger logger = Logger.getLogger(JPADAOFactory.class);
	
	private Map<Thread, EntityManager> managers = new HashMap<Thread, EntityManager>();
	
	// For debugging open transactions (Notice: Weak references)
	private static Map<EntityTransaction, Thread> transactions =
		Collections.synchronizedMap(new WeakHashMap<EntityTransaction, Thread>());
	
	
	/**
	 * Get the entity manager associated with the current thread. A new entity
	 * manager will be created if it does not yet exist, and a transaction will
	 * be started if not already active.
	 * 
	 * @see #getEntityManager(boolean)
	 * @return The thread's entity manager, with an active transaction
	 */
	private EntityManager getEntityManager() {
		return getEntityManager(true);
	}
	
	/**
	 * Get the entity manager associated with the current thread.
	 * <p>
	 * If create is true, a new entity manager will be created if
	 * it does not yet exist. If a transaction has not been started, and
	 * create is true, a new transaction will be started.
	 * <p>
	 * If create is false, <code>null</code> will be returned if the
	 * current thread did not have a entity manager. 
	 * 
	 * @see #getEntityManager()
	 * @param create True if a new entity manager can be created.
	 * @return The thread's entity manager, or null if it does not exist and create is false.
	 */
	private EntityManager getEntityManager(boolean create) {
		EntityManager em;
		synchronized (managers) {
			em = managers.get(Thread.currentThread());
		}
		if (em == null) {
			if (! create) {
				return null;
			}
			em = EntityManagerUtil.createEntityManager();
			synchronized (managers) {
				if (managers.containsKey(Thread.currentThread())) {
					logger.error("Duplicate entity managers for "
						+ Thread.currentThread(), new Exception());
					throw new RuntimeException("Duplicate entity managers for "
						+ Thread.currentThread());
					// Should not really happen, as we are using our currentThread as key
				}
				managers.put(Thread.currentThread(), em);
			}
		}
		
		if (create) {
			if (!em.getTransaction().isActive()) {
				logger.debug("Starting transaction for " + Thread.currentThread());
				em.getTransaction().begin();
				transactions.put(em.getTransaction(), Thread.currentThread());
				logger.debug("Started transaction for " + Thread.currentThread());
			}
		}
		return em;
	}
	
	@Override
	public void commit() {
		EntityManager em = getEntityManager(false);
		if (em == null) {
			logger.info("Commit on not-yet-used entity manager", new Exception());
			return;
		}
		em.getTransaction().commit();
		logger.debug("Committed transaction for " + Thread.currentThread());
	}
	
	@Override
	public boolean hasActiveTransaction() {
		boolean result=false;
		EntityManager em = getEntityManager(false);
		if (em!=null) result=em.getTransaction().isActive();
		return result;
	}

	@Override
	public void rollback() {
		EntityManager em = getEntityManager(false);
		if (em == null) {
			// Not as bad as a commit(), so only debug
			logger.debug("Rollback on not-yet-used entity manager");
			return;
		}		
		em.getTransaction().rollback();
		logger.debug("Rolled back transaction for " + Thread.currentThread());
	}

	@Override
	public void close() {
		EntityManager em = getEntityManager(false);
		if (em == null) {
			logger.debug("Close on not-yet-used entity manager");
			return;
		}
		//transactions.remove(em.getTransaction());
		logger.debug("Closing transaction for " + Thread.currentThread());
		em.clear();
		//defensive code to check for open transactions and to roll them back
		if (em.getTransaction().isActive()) {
			logger.warn("Transaction still open for "+Thread.currentThread()+", rolling back");
			em.getTransaction().rollback();
		}
		em.close();
		synchronized(managers) {
			managers.remove(Thread.currentThread());
		}
		logger.debug("Closed transaction for " + Thread.currentThread());

	}

	@DAO(Job.class)
	@Override
	public JobDAO getJobDAO() {
		return new JobDAOImpl(getEntityManager());
	}

	@DAO(Workflow.class)
	@Override
	public WorkflowDAO getWorkflowDAO() {
		return new WorkflowDAOImpl(getEntityManager());
	}

	@DAO(DataDoc.class)
	@Override
	public DataDocDAO getDataDocDAO() {
		return new DataDocDAOImpl(getEntityManager());
	}

	@DAO(User.class)
	@Override
	public UserDAO getUserDAO() {
		return new UserDAOImpl(getEntityManager());
	}

	@DAO(Queue.class)
	@Override
	public QueueDAO getQueueDAO() {
		return new QueueDAOImpl(getEntityManager());
	}

	@DAO(Worker.class)
	@Override
	public WorkerDAO getWorkerDAO() {
		return new WorkerDAOImpl(getEntityManager());
	}

	@DAO(Queue.class)
	@Override
	public QueueEntryDAO getQueueEntryDAO() {
		return new QueueEntryDAOImpl(getEntityManager());
	}

	@DAO(Configuration.class)
	@Override
	public ConfigurationDAO getConfigurationDAO() {
		return new ConfigurationDAOImpl(getEntityManager());
	}	
}