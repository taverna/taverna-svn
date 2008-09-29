package net.sf.taverna.service.test;

import net.sf.taverna.service.datastore.EntityManagerUtil;
import net.sf.taverna.service.datastore.dao.DAOFactory;

import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;

public abstract class TestDAO extends TestCommon {

	public static DAOFactory daoFactory = DAOFactory.getFactory();
	
	public DAOFactory altFactory;
	
	/**
	 * Create an alternative DAOFactory that can be used to get freshly loaded
	 * beans as if in a separate thread.
	 *
	 */
	@Before
	public void getAltFactory() {
		altFactory = DAOFactory.createDefaultFactory();
		altFactory.commit();
	}
	
	@After
	public void closeAltFactory() {
		if (altFactory == null) {
			return;
		}
		altFactory.rollback();
		altFactory.close();
		altFactory = null;
	}
	
	@After
	public void rollback() {
		try {
			daoFactory.rollback();
		} catch (IllegalStateException ex) {
			// OK
		}
	}

	@AfterClass
	public static void disconnectDAO() {
		daoFactory.close();
	}
		
	@AfterClass
	public static void disconnect() {        
		EntityManagerUtil.close();
	}
	
}
