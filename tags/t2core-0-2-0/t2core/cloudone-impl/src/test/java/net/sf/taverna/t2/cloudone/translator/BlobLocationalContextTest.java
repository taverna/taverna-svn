package net.sf.taverna.t2.cloudone.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashSet;

import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.peer.DataPeer;
import net.sf.taverna.t2.cloudone.peer.DataPeerImpl;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

public class BlobLocationalContextTest {

	protected DataManager dManager1;
	protected DataManager dManager2;
	protected DataFacade facade1;
	private FileDataManager dManager3;
	private DataFacade facade2;
	private DataFacade facade3;
	private static final String TEST_NS1 = "testNS1";
	private static final String TEST_NS2 = "testNS2";
	private static final String TEST_NS3 = "testNS3";
	private static File tmpDir;
	private static File tmpDir3;

	@AfterClass
	public static void deleteTmp() {
		try {
			FileUtils.deleteDirectory(tmpDir);
			FileUtils.deleteDirectory(tmpDir3);
		} catch (IOException ioe) {
			// Ignore - this sometimes happens on windows machines
			// according to http://issues.apache.org/jira/browse/IO-17
		}
	}

	@BeforeClass
	public static void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();

		tmpDir3 = File.createTempFile("test", "datamanager");
		tmpDir3.delete();
		tmpDir3.mkdir();

		assertFalse(tmpDir.equals(tmpDir3));
	}

	@Before
	public void setDataManager() {
		dManager1 = new FileDataManager(TEST_NS1,
				new HashSet<LocationalContext>(), tmpDir);
		dManager2 = new FileDataManager(TEST_NS2,
				new HashSet<LocationalContext>(), tmpDir);
		dManager3 = new FileDataManager(TEST_NS3,
				new HashSet<LocationalContext>(), tmpDir3);
		facade1 = new DataFacade(dManager1);
		facade2 = new DataFacade(dManager2);
		facade3 = new DataFacade(dManager3);

	}

	@Test
	public void exportSharedBlobStore() throws RetrievalException,
			IllegalArgumentException, NotFoundException, EmptyListException,
			MalformedListException, UnsupportedObjectTypeException, IOException {
		String string = "qwertyuiop";
		byte[] bytes = string.getBytes();
		DataDocumentIdentifier id = (DataDocumentIdentifier) facade1
				.register(bytes);
		assertNotNull(id);

		DataPeer peer1 = new DataPeerImpl(dManager1);
		DataPeer peer2 = new DataPeerImpl(dManager2);
		DataDocument doc = peer1.exportDataDocument(peer2
				.getLocationalContexts(), id);
		assertEquals("Exported data document didn't match original identifier",
				id, doc.getIdentifier());
		assertEquals(1, doc.getReferenceSchemes().size());
		byte[] retrievedBytes = (byte[]) facade2.resolve(doc.getIdentifier(),
				byte[].class);
		assertTrue("Retrieved bytes didn't match original bytes", Arrays
				.equals(bytes, retrievedBytes));
	}

	@Test(expected = NotFoundException.class)
	public void exportDifferentBlobStore() throws RetrievalException,
			IllegalArgumentException, EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, NotFoundException {
		String string = "qwertyuiop";
		byte[] bytes = string.getBytes();
		DataDocumentIdentifier id = (DataDocumentIdentifier) facade1
				.register(bytes);

		DataPeer peer1 = new DataPeerImpl(dManager1);
		DataPeer peer3 = new DataPeerImpl(dManager3);
		DataDocument doc;
		try {
			doc = peer1.exportDataDocument(peer3.getLocationalContexts(), id);
		} catch (NotFoundException e) {
			fail("Could not export");
			return;
		}
		assertEquals("Exported data document didn't match original identifier",
				id, doc.getIdentifier());
		assertEquals(0, doc.getReferenceSchemes().size());
		// Should throw NotFoundException
		facade3.resolve(doc.getIdentifier(), byte[].class);
	}

}
