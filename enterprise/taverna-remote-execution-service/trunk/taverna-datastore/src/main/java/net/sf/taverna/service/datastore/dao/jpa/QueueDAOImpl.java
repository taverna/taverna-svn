package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.QueueDAO;

import org.apache.log4j.Logger;

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
	public synchronized Queue defaultQueue() {
		Queue queue;
		Query query = em.createNamedQuery(Queue.NAMED_QUERY_NAME);
		query.setParameter("name", DEFAULT);
		try {
			queue = (Queue) query.getResultList().get(0);
		} catch (IndexOutOfBoundsException ex) {
			queue = new Queue();
			queue.setName(DEFAULT);
			create(queue);
			logger.info("Created default queue " + queue);
		}
		return queue;
	}
}
