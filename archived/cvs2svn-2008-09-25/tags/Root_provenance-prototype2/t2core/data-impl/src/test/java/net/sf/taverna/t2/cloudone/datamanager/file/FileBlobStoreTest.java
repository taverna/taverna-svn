package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;

import net.sf.taverna.t2.cloudone.datamanager.AbstractBlobStoreTest;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.BeforeClass;

/**
 * Test {@link FileBlobStore} using the tests of {@link AbstractBlobStoreTest}.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class FileBlobStoreTest extends AbstractBlobStoreTest {

	private static File tmpDir;

	@AfterClass
	public static void deleteTmp() throws IOException {
//		System.out.println("FileBlobStore dir " + tmpDir);
		try {
			FileUtils.deleteDirectory(tmpDir);
		} catch (IOException ex) {
			// OK
		}
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}


	@Override
	public void setDataManagerAndBlobStore() {
		dManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		blobStore = dManager.getBlobStore();
	}

}
