package net.sf.taverna.service.datastore.dao;

import java.util.List;

import net.sf.taverna.service.datastore.bean.Job;
import net.sf.taverna.service.datastore.bean.Job.Status;

public interface JobDAO extends GenericDao<Job, String> {

	public List<Job> byStatus(Status status);
}
