package net.sf.taverna.service.datastore;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import net.sf.taverna.service.datastore.bean.Configuration;
import net.sf.taverna.service.datastore.dao.ConfigurationDAO;
import net.sf.taverna.service.datastore.dao.DAOFactory;

public class TestConfiguration {
	
	@Before
	public void deleteExisting() {
		ConfigurationDAO configDao = DAOFactory.getFactory().getConfigurationDAO();
		
		for (Configuration conf : configDao.all()) {
			configDao.delete(conf);
		}
	}

	@Test
	public void creation() throws Exception {
		Configuration config = new Configuration();
		config.setAllowRegister(false);
		
		DAOFactory.getFactory().getConfigurationDAO().create(config);
		
		config = DAOFactory.getFactory().getConfigurationDAO().getConfig();
		
		assertNotNull(config);
		assertFalse(config.isAllowRegister());
	}
	
	@Test
	public void defaultCreated() throws Exception {
		ConfigurationDAO configDao = DAOFactory.getFactory().getConfigurationDAO();
		assertEquals("Existing configs should have been deleted",0,configDao.all().size());
		
		Configuration conf=configDao.getConfig();
		
		assertNotNull("A Configuration bean should have been created");
		assertFalse("Register should default to false",conf.isAllowRegister());
		
		conf.setAllowRegister(true);
		configDao.update(conf);
		DAOFactory.getFactory().commit();
	}
}
