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
public class TestEmptyList {
	
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testEmptyList() throws IOException {
		Data listData = DataTools.createEmptyListData(dataService);
		Assert.assertNotNull(listData);
		Assert.assertNotNull(listData.getID());

		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(listData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(listData.getID()));
		
		db.save(new File("/tmp/emptyList.zip"));
	}

	@Test
	public void testEmptyListSaveAndReload() throws IOException {
		DataBundle db1 = new DataBundleImpl();
		Data listData = DataTools.createEmptyListData(db1);
		String id = listData.getID();
		Assert.assertNotNull(listData);
		Assert.assertNotNull(listData.getID());
		db1.save(new File("/tmp/emptyList.zip"));
		
		DataBundle db2 = new DataBundleImpl();
		db2.open(new File("/tmp/emptyList.zip"));
		
		Data newVersion = db2.get(id);
		Assert.assertNotNull(newVersion);
		Assert.assertNotNull(newVersion.getElements());
		Assert.assertTrue(newVersion.getElements().isEmpty());
		
	}		

}
