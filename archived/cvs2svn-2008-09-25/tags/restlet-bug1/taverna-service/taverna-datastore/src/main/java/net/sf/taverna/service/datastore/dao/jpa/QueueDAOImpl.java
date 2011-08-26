package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Queue;
import net.sf.taverna.service.datastore.dao.QueueDAO;

public class QueueDAOImpl extends GenericDaoImpl<Queue, String> implements QueueDAO {

	public QueueDAOImpl(EntityManager em) {
		super(Queue.class, em);
	}
}
