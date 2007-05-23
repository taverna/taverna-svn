package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.dao.JobDAO;

public class JobDAOImpl extends GenericDaoImpl<Job, String> implements JobDAO {

	public JobDAOImpl(EntityManager em) {
		super(Job.class, em);
	}
	
	@Override
	public String namedQueryAll() {
		return Job.NAMED_QUERY_ALL;
	}
}
