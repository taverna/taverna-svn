package net.sf.taverna.t2.cloudone.impl;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.DataDocument;
import net.sf.taverna.t2.cloudone.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.Entity;
import net.sf.taverna.t2.cloudone.EntityList;
import net.sf.taverna.t2.cloudone.EntityListIdentifier;
import net.sf.taverna.t2.cloudone.EntityNotFoundException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;

import org.junit.Test;

/**
 * Tests the In Memory implementation of the Data Manager Creates Empty Lists
 * and registers them Creates Data Documents and registers them Create Lists of
 * Empty Lists and Data Documents and registers them
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class InMemoryDataManagerTest {

	private static final int SMALL_LIST_SIZE = 3;
	InMemoryDataManager dManager = new InMemoryDataManager("dataNS",
			new HashSet<LocationalContext>());

	@Test(expected = IllegalArgumentException.class)
	public void addZeroEmptyList() {
		dManager.registerEmptyList(0);
	}

	@Test
	public void addEmptyList() {
		int depth = 1;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
	}

	@Test
	public void addDeeperEmptyList() {
		int depth = SMALL_LIST_SIZE;
		EntityListIdentifier list = dManager.registerEmptyList(depth);
		assertEquals(depth, list.getDepth());
	}

	@Test
	public void emptyListNamespace() {
		EntityListIdentifier list = dManager.registerEmptyList(1);
		assertEquals(dManager.getCurrentNamespace(), list.getNamespace());
	}

	@Test
	public void getEmptyList() throws EntityNotFoundException {
		EntityListIdentifier listId = dManager.registerEmptyList(1);
		Entity<EntityListIdentifier> entity = dManager.getEntity(listId);
		assertEquals("Didn't return same entity", entity, dManager
				.getEntity(listId));
		assertTrue("Didn't return EntityList", entity instanceof EntityList);
		EntityList entityList = (EntityList) entity;
		assertTrue(entityList.isEmpty());
		assertEquals(listId, entity.getIdentifier());
	}

	@Test
	public void addTwoEmptyLists() {
		int depth = 1;
		EntityListIdentifier list0 = dManager.registerEmptyList(depth);
		EntityListIdentifier list1 = dManager.registerEmptyList(depth);
		assertFalse(list0.equals(list1));
		// InMemoryDataManager has a naive counter, list0, list1, etc.
		assertEquals("urn:t2data:list://dataNS/list1/1", list1.toString());
	}

	@Test(expected = EntityNotFoundException.class)
	public void getNonExistingList() throws EntityNotFoundException,
			MalformedIdentifierException {
		EntityListIdentifier unknownId = new EntityListIdentifier(
				"urn:t2data:list://dataNS/list5311/1");
		dManager.getEntity(unknownId);
	}

	@Test
	public void registerListOfLists() {
		EntityListIdentifier[] ids = createEmptyLists();
		EntityListIdentifier listOfLists = dManager.registerList(ids);
		assertEquals(2, listOfLists.getDepth());
	}

	@Test(expected = ArrayIndexOutOfBoundsException.class)
	public void getListOfLists() throws EntityNotFoundException {
		EntityListIdentifier[] ids = createEmptyLists();
		EntityListIdentifier listOfListsId = dManager.registerList(ids);
		Entity<EntityListIdentifier> entity = dManager.getEntity(listOfListsId);
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

	@Test
	public void getListOfDuplicateLists() throws EntityNotFoundException {
		EntityListIdentifier[] ids = createDuplicateEmptyLists();
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[0], ids[i]);
		}
		EntityListIdentifier listOfListsId = dManager.registerList(ids);
		Entity<EntityListIdentifier> entity = dManager.getEntity(listOfListsId);
		EntityList entityList = (EntityList) entity;
		assertEquals(SMALL_LIST_SIZE, entityList.size());
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			assertEquals(ids[0], entityList.get(i));
		}
	}

	@Test
	public void registerDocument() throws EntityNotFoundException {
		// not sure what a reference scheme is so empty one will have to do
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		DataDocument doc = dManager.registerDocument(references);
		assertTrue("Reference schemes no longer empty", doc
				.getReferenceSchemes().isEmpty());
		assertEquals(doc, dManager.getEntity(doc.getIdentifier()));
	}

	@Test
	public void addDocumentToList() throws EntityNotFoundException {
		DataDocumentIdentifier[] ids = new DataDocumentIdentifier[SMALL_LIST_SIZE];
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
			DataDocument doc = dManager.registerDocument(references);
			ids[i] = doc.getIdentifier();
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

	private EntityListIdentifier[] createEmptyLists() {
		EntityListIdentifier[] ids = new EntityListIdentifier[SMALL_LIST_SIZE];
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			ids[i] = dManager.registerEmptyList(1);
		}
		return ids;
	}

	private EntityListIdentifier[] createDuplicateEmptyLists() {
		EntityListIdentifier[] ids = new EntityListIdentifier[SMALL_LIST_SIZE];
		EntityListIdentifier list = dManager.registerEmptyList(1);
		for (int i = 0; i < SMALL_LIST_SIZE; i++) {
			ids[i] = list;
		}
		return ids;
	}

}
