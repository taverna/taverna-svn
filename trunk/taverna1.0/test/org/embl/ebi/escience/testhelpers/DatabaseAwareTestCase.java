package org.embl.ebi.escience.testhelpers;

import java.sql.Connection;
import java.sql.DriverManager;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.StupidLSIDProvider;
import org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService;


public abstract class DatabaseAwareTestCase extends PropertiesAwareTestCase 
{

	private JDBCBaclavaDataService dataService=null;
	
	protected void setUp() throws Exception 
	{		
		super.setUp();		
		dataService=new JDBCBaclavaDataService();
		dataService.reinit();		
	}
	
	protected void tearDown() throws Exception 
	{
        dataService = null;
	}
	
	protected Connection getConnection() throws Exception
	{
		Connection result=null;
		
		String url = System.getProperties().getProperty("taverna.datastore.jdbc.url");
		String username = System.getProperties().getProperty("taverna.datastore.jdbc.user");
		String password = System.getProperties().getProperty("taverna.datastore.jdbc.password");		
		
		return DriverManager.getConnection(url,username,password);		
	}	
	
	protected JDBCBaclavaDataService getDataService()
	{
		return dataService;
	}
}
