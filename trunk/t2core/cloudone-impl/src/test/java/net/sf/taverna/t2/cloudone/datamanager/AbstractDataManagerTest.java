package net.sf.taverna.t2.cloudone.datamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import net.sf.taverna.t2.cloudone.ReferenceScheme;
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

import org.junit.Before;
import org.junit.Test;

/**
 * Test the Data Manager. Creates Empty Lists, data documents, Lists of Empty
 * Lists and Lists of Data Documents and registers them all. Used by
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public abstract class AbstractDataManagerTest {

	private static final int SMALL_LIST_SIZE = 3;

	protected static final String TEST_NS = "testNS";

	protected AbstractDataManager dManager;

	public AbstractDataManagerTest() {
		super();
	}

	@Test
	public void addDeeperEmptyList() throws StorageException {
		int depth = SMALL_LIST_SIZE;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
	}

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
		// This shouldn't really be empty, but when this is implemented, this
		// test should be updated
		assertTrue("Locational contexts was not empty", dManager
				.getLocationalContexts().isEmpty());
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
		EntityListIdentifier listId = dManager.nextListIdentifier(2);
		assertEquals(TEST_NS, listId.getNamespace());
		assertEquals(2, listId.getDepth());
	}

	@Test
	public void registerDocument() throws NotFoundException, StorageException,
			RetrievalException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		DataDocument doc = (DataDocument) dManager.getEntity(docId);
		assertEquals(docId, doc.getIdentifier());
		assertTrue("Reference schemes no longer empty", doc
				.getReferenceSchemes().isEmpty());
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

	@Before
	public abstract void setDataManager();

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

}