package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.AbstractBlobStoreTest;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;
import org.junit.Ignore;
import org.junit.Test;

public class FileBlobStoreTest extends AbstractBlobStoreTest {

	private static File tmpDir;


	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}
	
	@AfterClass
	public static void deleteTmp() throws IOException {
//		System.out.println("FileBlobStore dir " + tmpDir);
		FileUtils.deleteDirectory(tmpDir);
	}

	
	@Override
	public void setDataManagerAndBlobStore() {
		dManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		blobStore = dManager.getBlobStore();
	}
	
}
