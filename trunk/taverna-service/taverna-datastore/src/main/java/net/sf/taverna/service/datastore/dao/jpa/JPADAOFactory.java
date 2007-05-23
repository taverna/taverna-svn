package net.sf.taverna.service.datastore.dao.jpa;

import java.util.HashMap;
import java.util.Map;

import javax.persistence.EntityManager;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.Util;
import net.sf.taverna.service.datastore.bean.DataDoc;
import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.DataDocDAO;
import net.sf.taverna.service.datastore.dao.JobDAO;
import net.sf.taverna.service.datastore.dao.QueueDAO;
import net.sf.taverna.service.datastore.dao.QueueEntryDAO;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.datastore.dao.WorkerDAO;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;

public class JPADAOFactory extends DAOFactory {

	private static Logger logger = Logger.getLogger(JPADAOFactory.class);
	
	private Map<Thread, EntityManager> managers = new HashMap<Thread, EntityManager>();
	
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
			if (em == null) {
				if (! create) {
					return null;
				}
				em = Util.createEntityManager();
				managers.put(Thread.currentThread(), em);
			}
		}
		if (create) {
			synchronized (em) {
				if (!em.getTransaction().isActive()) {
					em.getTransaction().begin();
				}
			}
		}
		return em;
	}
	
	@Override
	public void commit() {
		EntityManager em = getEntityManager(false);
		if (em == null) {
			logger.info("Commit on not-yet-used entity manager");
			return;
		}
		em.getTransaction().commit();
	}

	@Override
	public void rollback() {
		EntityManager em = getEntityManager(false);
		if (em == null) {
			logger.debug("Rollback on not-yet-used entity manager");
			return;
		}
		em.getTransaction().rollback();
	}

	@Override
	public void close() {
		synchronized (managers) {
			EntityManager em = getEntityManager(false);
			if (em == null) {
				logger.debug("Close on not-yet-used entity manager");
				return;
			}
			synchronized (em) {
				em.close();
			}
			managers.remove(Thread.currentThread());
		}
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
}