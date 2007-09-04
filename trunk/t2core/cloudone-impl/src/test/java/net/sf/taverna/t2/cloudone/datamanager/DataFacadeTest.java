package net.sf.taverna.t2.cloudone.datamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.UnsupportedEncodingException;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;

import org.junit.Before;
import org.junit.Test;

public class DataFacadeTest {

	protected AbstractDataManager dManager;
	protected DataFacade facade;

	@Before
	public void setDataManager() {
		// dManager = new FileDataManager("testNS",
		// new HashSet<LocationalContext>(), new File("/tmp/fish"));
		dManager = new InMemoryDataManager("testNS",
				new HashSet<LocationalContext>());
		facade = new DataFacade(dManager);
	}

	@Test
	public void registerInteger() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		EntityIdentifier entity = facade.register(25);
		assertEquals(25, facade.resolve(entity));
	}

	@Test
	public void registerFloat() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		float number = (float) 13.37;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerFloatInfinity() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		float number = Float.NEGATIVE_INFINITY;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerDouble() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		double number = Double.MAX_VALUE;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerDoubleInfinity() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		double number = Double.POSITIVE_INFINITY;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerBoolean() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		boolean bool = false;
		EntityIdentifier entity = facade.register(bool);
		assertEquals(bool, facade.resolve(entity));
	}

	@Test
	public void registerLong() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		long number = Long.MIN_VALUE;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerString() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException {
		String str = "hello with some / weird\n" + " ! character% and �(x) = �";
		EntityIdentifier entity = facade.register(str);
		assertEquals(str, facade.resolve(entity));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerList() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		List<Object> list = new ArrayList<Object>();
		list.add(-25);
		list.add((float) 30.56);
		list.add(Double.MIN_VALUE);
		list.add(true);
		list.add(Long.MAX_VALUE);
		list.add("hello");
		EntityIdentifier entity = facade.register(list);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(list, resolved);
		assertNotSame(list, resolved);
		assertEquals(Double.MIN_VALUE, resolved.get(2));
		assertEquals("hello", resolved.get(5));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerListOfLists() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		List<Object> list1 = new ArrayList<Object>();
		list1.add(-25);
		list1.add((float) 30.56);
		list1.add(Double.MIN_VALUE);
		list1.add(true);
		list1.add(Long.MAX_VALUE);
		list1.add("hello");

		List<Object> list2 = new ArrayList<Object>();
		list2.add(25);
		list2.add((float) 32.546);
		list2.add(Double.MAX_VALUE);
		list2.add(false);
		list2.add(Long.MIN_VALUE);
		list2.add("hello there");

		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(list1);
		bigList.add(list2);
		EntityIdentifier bigListId = facade.register(bigList);
		assertEquals(2, bigListId.getDepth());
		List<List<Object>> resolved = (List) facade.resolve(bigListId);
		assertEquals(bigList, resolved);
		assertNotSame(bigList, resolved);
		assertEquals(Double.MIN_VALUE, resolved.get(0).get(2));
		assertEquals(Long.MIN_VALUE, resolved.get(1).get(4));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerListOfHalfEmptyLists() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {

		List<Object> list1 = new ArrayList<Object>();
		list1.add(25);
		list1.add((float) 32.546);
		list1.add(Double.MAX_VALUE);
		list1.add(false);
		list1.add(Long.MIN_VALUE);
		list1.add("hello there");

		List<Object> emptyList = new ArrayList<Object>();

		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(list1);
		bigList.add(emptyList);
		EntityIdentifier bigListId = facade.register(bigList);
		assertEquals(2, bigListId.getDepth());
		List<List<Object>> resolved = (List) facade.resolve(bigListId);
		assertEquals(bigList, resolved);
		assertNotSame(bigList, resolved);
		assertTrue(resolved.get(1).isEmpty());
		assertEquals(Long.MIN_VALUE, resolved.get(0).get(4));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerListOfEmptyListFirst() throws EntityRetrievalException,
			EntityNotFoundException, DataManagerException {
		List<Object> list1 = new ArrayList<Object>();
		list1.add(25);
		list1.add((float) 32.546);
		list1.add(Double.MAX_VALUE);
		list1.add(false);
		list1.add(Long.MIN_VALUE);
		list1.add("hello there");

		List<Object> emptyList = new ArrayList<Object>();

		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(emptyList);
		bigList.add(list1);

		EntityIdentifier bigListId = facade.register(bigList);
		assertEquals(2, bigListId.getDepth());
		List<List<Object>> resolved = (List) facade.resolve(bigListId);
		assertEquals(bigList, resolved);
		assertNotSame(bigList, resolved);
		assertTrue(resolved.get(0).isEmpty());
		assertEquals(Long.MIN_VALUE, resolved.get(1).get(4));
	}

	@Test(expected = EmptyListException.class)
	public void registerListOfJustEmptyListsFails() throws DataManagerException {
		List<Object> emptyList1 = new ArrayList<Object>();
		List<Object> emptyList2 = new ArrayList<Object>();
		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(emptyList1);
		bigList.add(emptyList2);
		facade.register(bigList);
	}

	@Test(expected = EmptyListException.class)
	public void registerEmptyListFails() throws DataManagerException {
		List<Object> emptyList = new ArrayList<Object>();
		facade.register(emptyList);
	}

	@Test
	public void registerEmptyList() throws DataManagerException {
		List<Object> deepEmptyList = new ArrayList<Object>();
		// Note: Does not actually check the parameterised types, but the
		// depth should match
		EntityIdentifier id = facade.register(deepEmptyList, 1);
		assertEquals(1, id.getDepth());
	}

	@Test
	public void registerDeepEmptyList() throws DataManagerException {
		List<List<Object>> deepEmptyList = new ArrayList<List<Object>>();
		EntityIdentifier id = facade.register(deepEmptyList, 2);
		assertEquals(2, id.getDepth());
	}

	@Test(expected = MalformedListException.class)
	public void registerMalformedList() throws DataManagerException {
		List<Object> list1 = new ArrayList<Object>();
		list1.add(25);
		list1.add((float) 32.546);
		list1.add(Double.MAX_VALUE);
		list1.add(false);
		list1.add(Long.MIN_VALUE);
		list1.add("hello there");

		List<Object> list2 = new ArrayList<Object>();
		list2.add(25);
		list2.add((float) 32.546);
		list2.add(Double.MAX_VALUE);
		list2.add(false);
		list2.add(Long.MIN_VALUE);
		list2.add("hello there");

		List<List<Object>> deepList1 = new ArrayList<List<Object>>();
		deepList1.add(list1);

		List<List<Object>> deepList2 = new ArrayList<List<Object>>();
		deepList2.add(list2);
		List<List<List<Object>>> deeperList = new ArrayList<List<List<Object>>>();
		deeperList.add(deepList2);

		List<List<?>> malformedList = new ArrayList<List<?>>();
		malformedList.add(deepList1);
		malformedList.add(deeperList);

		facade.register(malformedList, 2);
	}

	@Test
	public void registerManyEmptyLists() throws DataManagerException {
		List<List<Object>> onlyEmptyLists = new ArrayList<List<Object>>();

		List<Object> emptyList1 = new ArrayList<Object>();
		List<Object> emptyList2 = new ArrayList<Object>();
		onlyEmptyLists.add(emptyList1);
		onlyEmptyLists.add(emptyList2);

		List<Object> list2 = new ArrayList<Object>();
		list2.add(25);
		list2.add((float) 32.546);
		list2.add(Double.MAX_VALUE);
		list2.add(false);
		list2.add(Long.MIN_VALUE);
		list2.add("hello there");
		List<List<Object>> deepList2 = new ArrayList<List<Object>>();
		List<Object> emptyList3 = new ArrayList<Object>();
		deepList2.add(list2);
		deepList2.add(emptyList3);

		List<List<List<Object>>> deeperList = new ArrayList<List<List<Object>>>();
		deeperList.add(onlyEmptyLists);
		deeperList.add(deepList2);

		facade.register(deeperList);
	}

	@Test(expected = MalformedListException.class)
	public void registerTooShortDepthFails() throws DataManagerException {
		List<Object> list = new ArrayList<Object>();
		list.add(25);
		list.add((float) 32.546);
		list.add(Double.MAX_VALUE);
		list.add(false);
		list.add(Long.MIN_VALUE);
		list.add("hello there");
		facade.register(list, 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerWrongDepthFails() throws DataManagerException {

		List<Object> list = new ArrayList<Object>();
		list.add(25);
		list.add((float) 32.546);
		list.add(Double.MAX_VALUE);
		list.add(false);
		list.add(Long.MIN_VALUE);
		list.add("hello there");
		List<List<Object>> deepList = new ArrayList<List<Object>>();
		List<Object> emptyList = new ArrayList<Object>();
		deepList.add(emptyList);
		deepList.add(list);
		// deepList is depth 2, should fail with 3
		facade.register(deepList, 3);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerLiteralWrongDepthFails() throws DataManagerException {
		// Literals can only have depth 0 and UNKNOWN_DEPTH
		facade.register("I've messed up my depth", 1);
	}

	@Test
	public void registerLiteralCorrectDepth() throws DataManagerException {
		facade.register("I've got correct depth", 0);
	}
	
	@Test(expected=UnsupportedObjectTypeException.class)
	public void registerUnsupportedObjectFails() throws DataManagerException {
		facade.register(this);
	}

}
