/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import org.apache.commons.io.FileUtils;
import org.junit.*;

import uk.org.taverna.data.bundle.api.DataBundle;
import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataNature;
import uk.org.taverna.platform.data.api.DataTools;
import uk.org.taverna.platform.data.impl.DataServiceImpl;

/**
 * @author alanrw
 *
 */
public class TestListOfString {
	
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testOneElementList() throws IOException {
		ArrayList<Data> list = new ArrayList<Data>();
		list.add(DataTools.createExplicitTextData(dataService, "fred"));

		Data listData = DataTools.createListData(dataService, list);
		Assert.assertNotNull(listData);
		Assert.assertNotNull(listData.getID());
		Assert.assertEquals(1, listData.getElements().size());

		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(listData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(listData.getID()));
		
		db.save(new File("/tmp/oneElementList.zip"));
	}
	
	@Test
	public void testSeveralElementList() throws IOException {
		ArrayList<Data> list = new ArrayList<Data>();
		list.add(DataTools.createExplicitTextData(dataService, "fred"));
		list.add(DataTools.createExplicitTextData(dataService, "bob"));
		list.add(DataTools.createExplicitTextData(dataService, "jim"));

		Data listData = DataTools.createListData(dataService, list);

		Assert.assertNotNull(listData);
		Assert.assertNotNull(listData.getID());
		Assert.assertEquals(3, listData.getElements().size());

		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(listData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(listData.getID()));
		
		db.save(new File("/tmp/severalElementList.zip"));
	}
	
	@Test
	public void testSeveralElementIncludingNullList() throws IOException {
		ArrayList<Data> list = new ArrayList<Data>();
		list.add(DataTools.createExplicitTextData(dataService, "fred"));
		list.add(DataTools.createNullData(dataService));
		list.add(DataTools.createExplicitTextData(dataService, "jim"));

		Data listData = DataTools.createListData(dataService, list);
		Assert.assertNotNull(listData);
		Assert.assertNotNull(listData.getID());
		Assert.assertEquals(3, listData.getElements().size());

		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(listData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path + "should end with " + listData.getID(), path.endsWith(listData.getID()));
		
		db.save(new File("/tmp/severalElementIncludingNullList.zip"));
	}
}
