package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Worker;
import net.sf.taverna.service.datastore.dao.WorkerDAO;

public class WorkerDAOImpl extends GenericDaoImpl<Worker, String> implements WorkerDAO {

	public WorkerDAOImpl(EntityManager em) {
		super(Worker.class, em);
	}
}
