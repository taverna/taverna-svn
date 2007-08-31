package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Collections;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManagerTest;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import static org.junit.Assert.*;

public class FileDataManagerTest extends AbstractDataManagerTest {



	private File tmpDir;

	FileDataManager fileDataManager2;

	private FileDataManager fileDataManager;
	
	@Before
	public void makeTmp() throws IOException {
		tmpDir = File.createTempFile("test", "datamanager");
		tmpDir.delete();
		tmpDir.mkdir();
	}
	
	@After
	public void deleteTmp() throws IOException {
		FileUtils.deleteDirectory(tmpDir);
	}
	
	@Override
	@Before
	public void setDataManager() {
		fileDataManager = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
		dManager = fileDataManager;
	}
	
	@Before
	public void makeExtraDataManager() {		
		fileDataManager2 = new FileDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet(), tmpDir);
	}
	
	@Test
	public void generateIdData() {
		String dataId = fileDataManager.generateId(IDType.Data);
		String dataId2 = fileDataManager2.generateId(IDType.Data);
		assertFalse("Not unique identifiers", dataId.equals(dataId2));

		String dataPrefix = "urn:t2data:ddoc://" + TEST_NS + "/";
		assertTrue(dataId.startsWith(dataPrefix));
		UUID uuid = UUID.fromString(dataId.replace(dataPrefix, ""));
		assertEquals(4, uuid.version()); // random
	}
	
	@Test
	public void generateIdList() {		
		String id = fileDataManager.generateId(IDType.List);
		String prefix = "urn:t2data:list://" + TEST_NS + "/";
		assertTrue(id.startsWith(prefix));
	}
	
	@Test
	public void generateIdError() {				
		String id = fileDataManager.generateId(IDType.Error);
		String prefix = "urn:t2data:error://" + TEST_NS + "/";
		assertTrue(id.startsWith(prefix));
	}
	
	@Test(expected=IllegalArgumentException.class)
	public void generateIdLiteral() {
		fileDataManager.generateId(IDType.Literal);
	}
	
	@Test
	public void nextListIdentifier() {
		EntityListIdentifier listId = fileDataManager.nextListIdentifier(2);
		assertEquals(TEST_NS, listId.getNamespace());
		assertEquals(2, listId.getDepth());
		UUID uuid = UUID.fromString(listId.getName());
		assertEquals(4, uuid.version()); // random
	}
	
	@Test
	public void nextErrorIdentifier() {
		ErrorDocumentIdentifier errorId = fileDataManager.nextErrorIdentifier(2, 3);
		assertEquals(TEST_NS, errorId.getNamespace());
		assertEquals(2, errorId.getDepth());
		assertEquals(3, errorId.getImplicitDepth());
		UUID uuid = UUID.fromString(errorId.getName());
		assertEquals(4, uuid.version()); // random
	}
	
	@Test
	public void nextDataIdentifier() {
		DataDocumentIdentifier dataId = fileDataManager.nextDataIdentifier();
		assertEquals(TEST_NS, dataId.getNamespace());
		UUID uuid = UUID.fromString(dataId.getName());
		assertEquals(4, uuid.version()); // random
	}


	
	
	
}
