package net.sf.taverna.service.datastore.dao.jpa;

import java.util.List;

import javax.persistence.EntityManager;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.dao.ConfigurationDAO;

public class ConfigurationDAOImpl extends GenericDaoImpl<Configuration, String> implements ConfigurationDAO {

	public ConfigurationDAOImpl(EntityManager em) {
		super(Configuration.class,em);
	}
	
	/**
	 * Gets the configuration bean. There should always be one, and this returns the first.
	 * Its created if it doesn't exist but the client is responsible for committing the transaction;
	 * @return Configuration
	 */
	public Configuration getConfig() {
		List<Configuration> all = all();
		if (all.size()==0) {
			return createDefault();
		}
		else {
			return all.get(0);
		}
	}
	
	private Configuration createDefault() {
		Configuration result = new Configuration();
		create(result);
		return result;
	}

}
