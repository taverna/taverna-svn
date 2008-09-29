package org.embl.ebi.escience.baclava.store;

import java.io.ByteArrayInputStream;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.List;
import java.util.Properties;
import java.util.ResourceBundle;
import junit.framework.TestCase;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.StupidLSIDProvider;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.embl.ebi.escience.testhelpers.DatabaseAwareTestCase;
import org.embl.ebi.escience.testhelpers.WorkflowFactory;

public class JDBCBaclavaDataServiceTest extends DatabaseAwareTestCase {
    	

	
	protected void setUp() throws Exception 
	{		
		super.setUp();
		DataThing.SYSTEM_DEFAULT_LSID_PROVIDER = new StupidLSIDProvider();
	}

	/*
	 * Test method for 'JDBCBaclavaDataService.storeDataThing(DataThing)'
     * and 'JDBCBaclavaDataService.fetchDataThing(String)'
	 */
	public final void testStoreAndFetchDataThing() throws Exception {
		JDBCBaclavaDataService dataService=getDataService();
		
        String simpleData = "test string";
        DataThing dataThing = new DataThing(simpleData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "1");
        dataService.storeDataThing(dataThing, true);
        DataThing fetchedThing = dataService.fetchDataThing("1");
        Object fetchedData = fetchedThing.getDataObject();
        assertEquals(simpleData, fetchedData);
        
        List collectionData = Arrays.asList(new String[] {"one", "two", "three", "four"});
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "2");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("2");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
        
        collectionData = new ArrayList();
        collectionData.add(Arrays.asList(new String[] {"five", "six"}));
        collectionData.add(Arrays.asList(new String[] {"seven"}));
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "3");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("3");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
        
        collectionData = new ArrayList();
        collectionData.add(Arrays.asList(new String[] {"eight", "nine"}));
        collectionData.add("ten");
        dataThing = new DataThing(collectionData);
        dataThing.fillLSIDValues();
        dataThing.setLSID(dataThing, "4");
        dataService.storeDataThing(dataThing, true);
        fetchedThing = dataService.fetchDataThing("4");
        fetchedData = fetchedThing.getDataObject();
        assertEquals(collectionData, fetchedData);
	}
	
	public void testStoreWorkflow() throws Exception
	{
		JDBCBaclavaDataService store = getDataService();
		ScuflModel model = WorkflowFactory.getSimpleWorkflowModel();
		store.storeWorkflow(model);
		
		Connection con=getConnection();
		ResultSet rst = null;
		PreparedStatement pstmt = null;
		
		try
		{
			pstmt = con.prepareStatement("SELECT * FROM workflow WHERE lsid=?");
			pstmt.setString(1,model.getDescription().getLSID());
			rst = pstmt.executeQuery();
			if (rst.next())
			{
				assertEquals("Title should be 'simple workflow'","simple workflow",rst.getString("title"));
				assertEquals("Author should be 'tester'","tester",rst.getString("author"));
				assertEquals("ID should be 1",1,rst.getInt("id"));
				assertEquals("XML is invalid",new XScuflView(model).getXMLText(),rst.getString("workflow"));
				
				
				if (rst.next())
				{
					fail("There should only be one record for this given lsid");
				}
			}
			else
			{
				fail("No records returned from database");
			}
			
		}
		catch(Exception e)
		{
			throw e;
		}
		finally
		{
			if (rst!=null) rst.close();
			if (pstmt!=null) pstmt.close();
			con.close();
		}								
	}
	
	public void testFetchWorkflow() throws Exception
	{
		JDBCBaclavaDataService store = getDataService();
		ScuflModel model = WorkflowFactory.getSimpleWorkflowModel();
		store.storeWorkflow(model);
		String xml=store.fetchWorkflow(model.getDescription().getLSID());
		
		ScuflModel fetchedModel=new ScuflModel();
		ByteArrayInputStream instr = new ByteArrayInputStream(xml.getBytes());
		XScuflParser.populate(instr,fetchedModel,null);
		
		assertEquals("Title is wrong","simple workflow",fetchedModel.getDescription().getTitle());
		assertEquals("Author is wrong","tester",fetchedModel.getDescription().getAuthor());
		assertEquals("LSID is wrong",model.getDescription().getLSID(),fetchedModel.getDescription().getLSID());
		assertEquals("An invalid number of Processors",model.getProcessors().length,fetchedModel.getProcessors().length);		
	}

}
