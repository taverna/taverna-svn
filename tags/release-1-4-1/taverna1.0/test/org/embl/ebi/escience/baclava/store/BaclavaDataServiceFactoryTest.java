package org.embl.ebi.escience.baclava.store;


import org.embl.ebi.escience.testhelpers.*;


public class BaclavaDataServiceFactoryTest extends DatabaseAwareTestCase 
{
				
	public void testGetStoreUnknownClass()
	{
		System.setProperty("taverna.datastore.class","TestNonExistentClass");
		BaclavaDataService result=BaclavaDataServiceFactory.getStore();
		assertNull("Returned store should be null for an unknown class",result);
	}
	
	public void testGetStoreJDBC()
	{
		System.setProperty("taverna.datastore.class","org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService");
		BaclavaDataService result=BaclavaDataServiceFactory.getStore();
		assertTrue("Returned class should be JDBCBaclavaDataService",result instanceof org.embl.ebi.escience.baclava.store.BaclavaDataService);
	}
	
	public void testGetStoreRemoteSoapStore()
	{		
		System.setProperty("taverna.datastore.class","org.embl.ebi.escience.baclava.store.RemoteSOAPStore");
		BaclavaDataService result=BaclavaDataServiceFactory.getStore();
		assertTrue("Returned class should be JDBCBaclavaDataService",result instanceof org.embl.ebi.escience.baclava.store.RemoteSOAPStore);
	}
}
