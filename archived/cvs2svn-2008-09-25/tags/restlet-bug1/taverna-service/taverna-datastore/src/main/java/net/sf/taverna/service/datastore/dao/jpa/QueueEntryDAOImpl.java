package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.QueueEntry;
import net.sf.taverna.service.datastore.dao.QueueEntryDAO;

public class QueueEntryDAOImpl extends GenericDaoImpl<QueueEntry, String> implements QueueEntryDAO {

	public QueueEntryDAOImpl(EntityManager em) {
		super(QueueEntry.class, em);
	}
}
