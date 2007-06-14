package net.sf.taverna.service.datastore.dao;

import net.sf.taverna.service.datastore.bean.Queue;

public interface QueueDAO extends GenericDao<Queue, String> {

	public Queue defaultQueue();
}
