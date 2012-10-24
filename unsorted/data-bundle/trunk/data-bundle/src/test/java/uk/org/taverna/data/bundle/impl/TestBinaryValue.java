package uk.org.taverna.data.bundle.impl;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Arrays;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;

import uk.org.taverna.data.bundle.api.DataBundle;
import uk.org.taverna.platform.data.api.Data;
import uk.org.taverna.platform.data.api.DataNature;
import uk.org.taverna.platform.data.api.DataTools;
import uk.org.taverna.platform.data.impl.DataServiceImpl;

public class TestBinaryValue {
	
	private static final String FRED = "fred";
	private DataServiceImpl dataService = new DataServiceImpl();
	
	@Test
	public void testBinaryValue() throws IOException {
		InputStream inputStream = TestBinaryValue.class.getResourceAsStream("/plot.png");
		Assert.assertNotNull(inputStream);
		byte[] bytes = IOUtils.toByteArray(inputStream);
		Data binaryData = DataTools.createExplicitBinaryData(dataService, bytes);
		Assert.assertNotNull(binaryData);
		Assert.assertNotNull(binaryData.getID());
		Assert.assertEquals(binaryData.getExplicitValue(), bytes);
		DataBundle db = new DataBundleImpl();
		
		String path = db.saveData(binaryData, false);

		Assert.assertNotNull(path);
		Assert.assertTrue(path.endsWith(binaryData.getID()));
		
		Data newVersion = db.get(path);
		Assert.assertTrue(newVersion.hasDataNature(DataNature.BINARY_VALUE));
		byte[] fileContents = (byte[]) newVersion.getExplicitValue();
		Assert.assertTrue(Arrays.equals(bytes, fileContents));
		db.save(new File("/tmp/binaryValue.zip"));
	}

	@Test
	public void testBinaryValueSaveAndReload() throws IOException {
		DataBundle db = new DataBundleImpl();
		InputStream inputStream = TestBinaryValue.class.getResourceAsStream("/plot.png");
		Assert.assertNotNull(inputStream);
		byte[] bytes = IOUtils.toByteArray(inputStream);
		Data binaryData = DataTools.createExplicitBinaryData(db, bytes);
		String id = binaryData.getID();
		Assert.assertNotNull(binaryData);
		Assert.assertNotNull(binaryData.getID());
		Assert.assertEquals(binaryData.getExplicitValue(), bytes);
		
		db.save(new File("/tmp/binaryValue.zip"));
		
		DataBundle db2 = new DataBundleImpl();
		db2.open(new File("/tmp/binaryValue.zip"));

		Data newVersion = db2.get(id);
		Assert.assertTrue(newVersion.hasDataNature(DataNature.BINARY_VALUE));
		byte[] fileContents = (byte[]) newVersion.getExplicitValue();
		Assert.assertTrue(Arrays.equals(bytes, fileContents));

	}

}
