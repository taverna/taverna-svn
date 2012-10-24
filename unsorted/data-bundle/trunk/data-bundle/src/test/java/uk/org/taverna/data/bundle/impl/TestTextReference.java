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

import org.apache.commons.io.FileUtils;
import org.junit.*;

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
public class TestTextReference {
	
	private static final String WEB_ADDRESS = "http://www.google.com";
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testTextReferenceNoSnapshot() throws IOException, URISyntaxException {
		Data referenceData = DataTools.createSingleReferenceTextData(dataService, WEB_ADDRESS);
		Assert.assertNotNull(referenceData);
		Assert.assertNotNull(referenceData.getID());
		Assert.assertNull(referenceData.getExplicitValue());
		Assert.assertNotNull(referenceData.getReferences());
		Assert.assertEquals(1, referenceData.getReferences().size());
		boolean found = false;
		for (DataReference dr : referenceData.getReferences()) {
			if (dr.getURI().toASCIIString().equals(WEB_ADDRESS)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(referenceData, false);

		Assert.assertNotNull(path);
		
		db.save(new File("/tmp/textReferenceNoSnapshot.zip"));

		Data newVersion = db.get(path);
		Assert.assertNull(newVersion.getExplicitValue());
		Set<DataReference> references = newVersion.getReferences();
		Assert.assertNotNull(references);
		Assert.assertFalse(references.isEmpty());
		Assert.assertEquals(1, references.size());
		found = false;
		for (DataReference dr : references) {
			if (dr.getURI().toASCIIString().equals(WEB_ADDRESS)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		
	}

	@Test
	public void testTextReferenceWithSnapshot() throws IOException, URISyntaxException {
		Data referenceData = DataTools.createSingleReferenceTextData(dataService, WEB_ADDRESS);
		Assert.assertNotNull(referenceData);
		Assert.assertNotNull(referenceData.getID());
		Assert.assertNull(referenceData.getExplicitValue());
		Assert.assertNotNull(referenceData.getReferences());
		Assert.assertEquals(1, referenceData.getReferences().size());
		boolean found = false;
		for (DataReference dr : referenceData.getReferences()) {
			if (dr.getURI().toASCIIString().equals(WEB_ADDRESS)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(referenceData, true);

		Assert.assertNotNull(path);
		
		Data newVersion = db.get(path);
		Assert.assertNotNull(newVersion.getExplicitValue());
		Set<DataReference> references = newVersion.getReferences();
		Assert.assertNotNull(references);
		Assert.assertFalse(references.isEmpty());
		Assert.assertEquals(1, references.size());
		found = false;
		for (DataReference dr : references) {
			if (dr.getURI().toASCIIString().equals(WEB_ADDRESS)) {
				found = true;
				break;
			}
		}
		Assert.assertTrue(found);
		
		db.save(new File("/tmp/textReferenceWithSnapshot.zip"));
	}

}
