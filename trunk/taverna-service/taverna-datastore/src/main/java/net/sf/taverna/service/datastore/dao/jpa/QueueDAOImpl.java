package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.QueueDAO;

public class QueueDAOImpl extends GenericDaoImpl<Queue, String> implements QueueDAO {

	private static Logger logger = Logger.getLogger(QueueDAOImpl.class);
	
	protected static final String DEFAULT = "default";

	public QueueDAOImpl(EntityManager em) {
		super(Queue.class, em);
	}
	
	/**
	 * Retrieve, possibly creating, the default queue. The default queue has a
	 * name "default".
	 * 
	 * @return The default queue
	 */
	public Queue defaultQueue() {
		Queue queue;
		Query query = em.createNamedQuery(Queue.NAMED_QUERY_NAME);
		query.setParameter("name", DEFAULT);
		try {
			queue = (Queue) query.getSingleResult();
		} catch (NoResultException ex) {
			queue = new Queue();
			queue.setName(DEFAULT);
			create(queue);
			logger.info("Created default queue " + queue);
		}
		return queue;
	}
}
