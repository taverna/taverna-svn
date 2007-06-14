package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.QueueDAO;

public class QueueDAOImpl extends GenericDaoImpl<Queue, String> implements QueueDAO {

	public QueueDAOImpl(EntityManager em) {
		super(Queue.class, em);
	}
	
	/**
	 * 
	 * @return the default queue, which for currently is the first queue. If the queue doesn't exist it is created.
	 */
	public Queue defaultQueue() {
		if (all().size()==0) {
			Queue queue = new Queue();
			create(queue);
			return queue;
		}
		else {
			return all().get(0);
		}
	}
}
