package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Workflow;
import net.sf.taverna.service.datastore.dao.WorkflowDAO;

public class WorkflowDAOImpl extends GenericDaoImpl<Workflow, String> implements WorkflowDAO {

	public WorkflowDAOImpl(EntityManager em) {
		super(Workflow.class, em);
	}
	
	@Override
	public String namedQueryAll() {
		return Workflow.NAMED_QUERY_ALL;
	}
}
