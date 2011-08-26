package net.sf.taverna.service.datastore.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;
import net.sf.taverna.service.datastore.dao.JobDAO;

public class JobDAOImpl extends GenericDaoImpl<Job, String> implements JobDAO {

	public JobDAOImpl(EntityManager em) {
		super(Job.class, em);
	}
	
	@Override
	public String namedQueryAll() {
		return Job.NAMED_QUERY_ALL;
	}

	@SuppressWarnings("unchecked")
	public List<Job> byStatus(Status status) {
		Query query = em.createNamedQuery(Job.NAMED_QUERY_STATUS);
		query.setParameter("status", status);
		return query.getResultList();
	}
}
