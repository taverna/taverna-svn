/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.File;
import java.io.IOException;

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
public class TestStringValue {
	
	private static final String FRED = "fred";
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testStringValue() throws IOException {
		Data stringData = DataTools.createExplicitTextData(dataService, FRED);
		Assert.assertNotNull(stringData);
		Assert.assertNotNull(stringData.getID());
		Assert.assertEquals(stringData.getExplicitValue(), FRED);
		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(stringData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(stringData.getID()));
		
		Data newVersion = db.get(path);
		String fileContents = (String) newVersion.getExplicitValue();
		Assert.assertEquals(FRED, fileContents);
		db.save(new File("/tmp/stringValue.zip"));
	}

}
