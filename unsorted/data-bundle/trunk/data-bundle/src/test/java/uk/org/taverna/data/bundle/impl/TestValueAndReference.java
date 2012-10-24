/**
 * 
 */
package uk.org.taverna.data.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import uk.org.taverna.data.bundle.api.DataBundle;
import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataNature;
import uk.org.taverna.platform.data.api.DataReference;
import uk.org.taverna.platform.data.api.DataTools;
import uk.org.taverna.platform.data.impl.DataServiceImpl;

/**
 * @author alanrw
 *
 */
public class TestValueAndReference {
	
	private static final String WEB_ADDRESS = "http://www.google.com";
	private static final String FRED = "fred";
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testStringValue() throws IOException, URISyntaxException {
		Data referenceData = DataTools.createSingleReferenceTextData(dataService, WEB_ADDRESS);
		referenceData.setExplicitValue(FRED);
		Assert.assertNotNull(referenceData);
		Assert.assertNotNull(referenceData.getID());
		Assert.assertEquals(referenceData.getExplicitValue(), FRED);
		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(referenceData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(referenceData.getID()));
		db.save(new File("/tmp/valueAndReference.zip"));
		
		Data newVersion = db.get(path);
		
		String fileContents = (String) newVersion.getExplicitValue();
		Assert.assertEquals(FRED, fileContents);
		
		Set<DataReference> references = newVersion.getReferences();
		boolean found = false;
		for (DataReference dr : references) {
			if (dr.getURI().toASCIIString().equals(WEB_ADDRESS)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);

		db.save(new File("/tmp/valueAndReference.zip"));
	}


}
