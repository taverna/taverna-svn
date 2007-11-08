package net.sf.taverna.t2.cloudone.datamanager.file;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 * Test {@link FileDataManager} using the tests of
 * {@link AbstractDataManagerTest} in addition to testing that generated IDs are
 * UUIDs.
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class FileDataManagerTest extends AbstractDataManagerTest {

	private static File tmpDir;

	@AfterClass
	public static void deleteTmp() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}

	private FileDataManager fileDataManager;

	FileDataManager fileDataManager2;

	@Test
	public void generateIdDataUUID() {
		String dataId = fileDataManager.generateId(IDType.Data);
		String dataId2 = fileDataManager2.generateId(IDType.Data);
		assertFalse("Not unique identifiers", dataId.equals(dataId2));

		String dataPrefix = "urn:t2data:ddoc://" + TEST_NS + "/";
		assertTrue(dataId.startsWith(dataPrefix));
		UUID uuid = UUID.fromString(dataId.replace(dataPrefix, ""));
		assertEquals(4, uuid.version()); // random
	}

	@Before
	public void makeExtraDataManager() {
		fileDataManager2 = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
	}

	@Test
	public void nextDataIdentifierUUID() {
		DataDocumentIdentifier dataId = fileDataManager.nextDataIdentifier();
		UUID uuid = UUID.fromString(dataId.getName());
		assertEquals(4, uuid.version()); // random
	}


	@Test
	public void nextErrorIdentifierUUID() {
		ErrorDocumentIdentifier errorId = fileDataManager.nextErrorIdentifier(
				2, 3);
		UUID uuid = UUID.fromString(errorId.getName());
		assertEquals(4, uuid.version()); // random
	}

	@Test
	public void nextListIdentifierUUID() {
		EntityListIdentifier listId = fileDataManager.nextListIdentifier(2);
		UUID uuid = UUID.fromString(listId.getName());
		assertEquals(4, uuid.version()); // random
	}

	@Override
	@Before
	public void setDataManager() {
		fileDataManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		dManager = fileDataManager;
	}

}
