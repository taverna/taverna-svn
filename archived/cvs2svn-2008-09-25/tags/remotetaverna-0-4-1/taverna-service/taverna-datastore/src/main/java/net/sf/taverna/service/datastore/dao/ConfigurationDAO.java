package net.sf.taverna.service.datastore.dao;

import net.sf.taverna.service.datastore.bean.Configuration;

public interface ConfigurationDAO extends GenericDao<Configuration, String> {

	/**
	 * Gets the configuration bean. There should always be one, and this returns the first.
	 * Its created if it doesn't exist but the client is responsible for committing the transaction;
	 * @return Configuration
	 */
	public Configuration getConfig();
	
}
