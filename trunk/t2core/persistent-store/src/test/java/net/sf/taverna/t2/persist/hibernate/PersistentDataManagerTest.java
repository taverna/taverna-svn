package net.sf.taverna.t2.persist.hibernate;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.log4j.Logger;
import org.junit.Before;
import org.junit.Test;

public class PersistentDataManagerTest{
	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(PersistentDataManagerTest.class);
	private PersistentDataManager dManager;

	private static final int SMALL_LIST_SIZE = 3;

	protected static final String TEST_NS = "testNS";



	@Test
	public void addDeeperEmptyList() throws StorageException {
		int depth = SMALL_LIST_SIZE;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void addDocumentToList() throws NotFoundException, StorageException,
			RetrievalException {
		DataDocumentIdentifier[] ids = new DataDocumentIdentifier[SMALL_LIST_SIZE];
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
			DataDocumentIdentifier docId = dManager
					.registerDocument(references);
			ids[i] = docId;
			if (i > 0) {
				// always fresh
				assertFalse(ids[i - 1].equals(ids[i]));
			}
		}
		EntityListIdentifier listID = dManager.registerList(ids);
		EntityList entityList = (EntityList) dManager.getEntity(listID);

		assertEquals(SMALL_LIST_SIZE, entityList.size());
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[i], entityList.get(i));
		}
	}

	@Test
	public void addEmptyList() throws StorageException {
		int depth = 1;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
	}

	@Test
	public void addTwoEmptyLists() throws StorageException {
		int depth = 1;
		EntityListIdentifier list0 = dManager.registerEmptyList(depth);
		EntityListIdentifier list1 = dManager.registerEmptyList(depth);
		assertFalse(list0.equals(list1));
	}

	@Test(expected = IllegalArgumentException.class)
	public void addZeroEmptyList() throws StorageException {
		dManager.registerEmptyList(0);
	}

	@Test
	public void currentNamespace() {
		assertEquals(TEST_NS, dManager.getCurrentNamespace());
	}

	@Test
	public void emptyListNamespace() throws StorageException {
		EntityListIdentifier list = dManager.registerEmptyList(1);
		assertEquals(dManager.getCurrentNamespace(), list.getNamespace());
	}

	@Test
	public void generateIdData() {
		String dataId = dManager.generateId(IDType.Data);
		String dataId2 = dManager.generateId(IDType.Data);
		assertFalse("Not unique identifiers", dataId.equals(dataId2));

		String dataPrefix = "urn:t2data:ddoc://" + TEST_NS + "/";
		assertTrue(dataId.startsWith(dataPrefix));
	}

	@Test
	public void generateIdError() {
		String id = dManager.generateId(IDType.Error);
		String prefix = "urn:t2data:error://" + TEST_NS + "/";
		assertTrue(id.startsWith(prefix));
	}

	@Test
	public void generateIdList() {
		String id = dManager.generateId(IDType.List);
		String prefix = "urn:t2data:list://" + TEST_NS + "/";
		assertTrue(id.startsWith(prefix));
	}

	@Test(expected = IllegalArgumentException.class)
	public void generateIdLiteral() {
		dManager.generateId(IDType.Literal);
	}

	@Test
	public void getEmptyList() throws NotFoundException, StorageException,
			RetrievalException {
		EntityListIdentifier listId = dManager.registerEmptyList(1);
		Entity<EntityListIdentifier, ?> entity = dManager.getEntity(listId);
		assertEquals("Didn't return same entity", entity, dManager
				.getEntity(listId));
		assertTrue("Didn't return EntityList", entity instanceof EntityList);
		EntityList entityList = (EntityList) entity;
		assertTrue(entityList.isEmpty());
		assertEquals(listId, entity.getIdentifier());
	}

	@Test
	public void getListOfDuplicateLists() throws NotFoundException,
			StorageException, RetrievalException {
		EntityListIdentifier[] ids = createDuplicateEmptyLists();
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[0], ids[i]);
		}
		EntityListIdentifier listOfListsId = dManager.registerList(ids);
		Entity<EntityListIdentifier, ?> entity = dManager
				.getEntity(listOfListsId);
		EntityList entityList = (EntityList) entity;
		assertEquals(SMALL_LIST_SIZE, entityList.size());
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[0], entityList.get(i));
		}
	}

	@Test(expected = IndexOutOfBoundsException.class)
	public void getListOfLists() throws NotFoundException, StorageException,
			RetrievalException {
		EntityListIdentifier[] ids = createEmptyLists();
		EntityListIdentifier listOfListsId = dManager.registerList(ids);
		Entity<EntityListIdentifier, ?> entity = dManager
				.getEntity(listOfListsId);
		assertTrue("Didn't return EntityList", entity instanceof EntityList);
		EntityList entityList = (EntityList) entity;
		assertEquals(listOfListsId, entityList.getIdentifier());
		assertEquals(SMALL_LIST_SIZE, entityList.size());
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[i], entityList.get(i));
		}
		// Should throw ArrayIndexOutOfBoundsException
		entityList.get(SMALL_LIST_SIZE);
	}

	@Test(expected = NotFoundException.class)
	public void getNonExistingList() throws NotFoundException,
			MalformedIdentifierException, RetrievalException {
		EntityListIdentifier unknownId = new EntityListIdentifier(
				"urn:t2data:list://" + TEST_NS + "/list5311/1");
		dManager.getEntity(unknownId);
	}

	@Test
	public void locationalContexts() {
		Set<LocationalContext> blobContexts = dManager.getBlobStore()
				.getLocationalContexts();
		assertTrue(dManager.getLocationalContexts().containsAll(blobContexts));
		// And nothing else (we had an empty set in our constructor)
		assertEquals(blobContexts.size(), dManager.getLocationalContexts().size());
		assertEquals(1, blobContexts.size());
	}

	@Test
	public void managedNamespaces() {
		List<String> namespaces = dManager.getManagedNamespaces();
		assertTrue("Did not contain current namespace", namespaces
				.contains(dManager.getCurrentNamespace()));
		// If the implementation supports multiple namespaces, update this test
		assertEquals("Unexpectde size of managed namespaces", 1, namespaces
				.size());
	}

	@Test
	public void nextDataIdentifier() {
		DataDocumentIdentifier dataId = dManager.nextDataIdentifier();
		assertEquals(TEST_NS, dataId.getNamespace());
	}

	@Test
	public void nextErrorIdentifier() {
		ErrorDocumentIdentifier errorId = dManager.nextErrorIdentifier(2, 3);
		assertEquals(TEST_NS, errorId.getNamespace());
		assertEquals(2, errorId.getDepth());
		assertEquals(3, errorId.getImplicitDepth());

	}

	@Test
	public void nextListIdentifier() {
		EntityListIdentifier listId = dManager.nextListIdentifier(2, false);
		assertEquals(TEST_NS, listId.getNamespace());
		assertEquals(2, listId.getDepth());
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerDocument() throws NotFoundException, StorageException,
			RetrievalException, MalformedURLException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		try {
			Thread.sleep(10000);
		} catch (InterruptedException e) {
		}
		DataDocument doc = (DataDocument) dManager.getEntity(docId);
		assertEquals(docId, doc.getIdentifier());
		assertTrue("Reference schemes no longer empty", doc
				.getReferenceSchemes().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void registerDocumentWithHttpReference() throws NotFoundException, StorageException,
			RetrievalException, MalformedURLException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		HttpReferenceScheme httpRefScheme = new HttpReferenceScheme(new URL("http://google.com"));
		references.add(httpRefScheme);
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		DataDocument doc = (DataDocument) dManager.getEntity(docId);
		assertEquals(docId, doc.getIdentifier());
		assertTrue("Reference schemes are empty", !doc
				.getReferenceSchemes().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void registerDocumentWithFileReference() throws NotFoundException, StorageException,
			RetrievalException, MalformedURLException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		File file = null;
		try {
			file = File.createTempFile("test", ".tmp");
		} catch (IOException e) {

		}
		FileReferenceScheme fileRefScheme = new FileReferenceScheme(file);
		references.add(fileRefScheme);
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		DataDocument doc = (DataDocument) dManager.getEntity(docId);
		assertEquals(docId, doc.getIdentifier());
		assertTrue("Reference schemes are empty", !doc
				.getReferenceSchemes().isEmpty());
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void registerDocumentWithBlobReference() throws NotFoundException, StorageException,
			RetrievalException, MalformedURLException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		File file = null;
		try {
			file = File.createTempFile("test", ".tmp");
		} catch (IOException e) {

		}
		BlobReferenceSchemeImpl blobRefScheme = new BlobReferenceSchemeImpl(TEST_NS,UUID.randomUUID().toString());
		references.add(blobRefScheme);
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		DataDocument doc = (DataDocument) dManager.getEntity(docId);
		assertEquals(docId, doc.getIdentifier());
		assertTrue("Reference schemes are empty", !doc
				.getReferenceSchemes().isEmpty());
	}

	@Test
	public void registerListOfLists() throws StorageException {
		EntityListIdentifier[] ids = createEmptyLists();
		EntityListIdentifier listOfLists = dManager.registerList(ids);
		assertEquals(2, listOfLists.getDepth());
	}

	@Test
	public void registerLiteral() throws NotFoundException, RetrievalException {
		Literal lit = Literal.buildLiteral(3.14);
		Entity<Literal, ?> ent = dManager.getEntity(lit);
		assertSame("Literal was not its own entity", lit, ent);
	}
	

	@Test
	public void registerError() throws NotFoundException, StorageException,
	RetrievalException {
		Throwable ex = new IllegalArgumentException("Did not work",
				new NullPointerException("No no"));
		
		String msg = "Something failed";
		
		int depth = 0;
		int implicitDepth = 1;
		
		ErrorDocumentIdentifier errId1 = dManager.registerError(depth++,
				implicitDepth, msg);
		assertEquals(0, errId1.getDepth());
		assertEquals(1, errId1.getImplicitDepth());
		
		ErrorDocumentIdentifier errId2 = dManager.registerError(depth++,
				implicitDepth, ex);
		assertEquals(1, errId2.getDepth());
		
		ErrorDocumentIdentifier errId3 = dManager.registerError(depth++,
				implicitDepth, msg, ex);
		assertEquals(2, errId3.getDepth());
		
		ErrorDocument err1 = (ErrorDocument) dManager.getEntity(errId1);
		assertEquals(msg, err1.getMessage());
		assertNull(err1.getCause());
		assertNull(err1.getStackTrace());
		
		ErrorDocument err2 = (ErrorDocument) dManager.getEntity(errId2);
		
		assertNull(err2.getMessage());
		// Exceptions are not serialised
		// assertEquals(ex, err2.getCause());
		assertTrue(err2.getStackTrace().contains("No no"));
		
		ErrorDocument err3 = (ErrorDocument) dManager.getEntity(errId3);
		assertEquals(msg, err3.getMessage());
		// Exceptions are not serialised
		// assertEquals(ex, err3.getCause());
		assertTrue(err3.getStackTrace().contains("No no"));
		
	}

	private EntityListIdentifier[] createDuplicateEmptyLists()
			throws StorageException {
		EntityListIdentifier[] ids = new EntityListIdentifier[SMALL_LIST_SIZE];
		EntityListIdentifier list = dManager.registerEmptyList(1);
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			ids[i] = list;
		}
		return ids;
	}

	private EntityListIdentifier[] createEmptyLists() throws StorageException {
		EntityListIdentifier[] ids = new EntityListIdentifier[SMALL_LIST_SIZE];
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			ids[i] = dManager.registerEmptyList(1);
		}
		return ids;
	}
	@Before
	public void setDataManager() {
		dManager = new PersistentDataManager(TEST_NS, Collections
				.<LocationalContext> emptySet());
	}
}
