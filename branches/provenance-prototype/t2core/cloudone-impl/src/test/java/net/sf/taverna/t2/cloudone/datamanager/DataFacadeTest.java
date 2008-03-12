package net.sf.taverna.t2.cloudone.datamanager;

import static net.sf.taverna.t2.cloudone.datamanager.BlobStore.STRING_CHARSET;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Literal;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.ErrorDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.refscheme.file.FileReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.http.HttpReferenceScheme;

import org.apache.commons.io.IOUtils;
import org.jdom.Element;
import org.jdom.output.XMLOutputter;
import org.junit.Before;
import org.junit.Test;

public class DataFacadeTest {

	private static final String TEST_NS = "testNS";
	protected AbstractDataManager dManager;
	protected DataFacade facade;

	@Test
	public void registerBigString() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		String str = "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzx"
				+ "cvbnmqwertyuiopasdfghjklzxcvbnmqwertyuioipasdfghjklzxcvbnmq";
		EntityIdentifier entity = facade.register(str);
		InputStream stream = (InputStream) facade.resolve(entity,
				InputStream.class);
		String newString = IOUtils.toString(stream);
		assertEquals(str, newString);
	}

	@Test
	public void registerBoolean() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
		boolean bool = false;
		EntityIdentifier entity = facade.register(bool);
		assertEquals(bool, facade.resolve(entity, Boolean.class));
	}

	@Test(expected = RetrievalException.class)
	public void registerBooleanRetrieveWrongType() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {
		boolean bool = false;
		EntityIdentifier entity = facade.register(bool);
		assertEquals(bool, facade.resolve(entity, Integer.class));
	}

	@Test
	public void registerBytes() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException,
			DereferenceException {
		final byte[] bytes = "A test".getBytes(STRING_CHARSET);
		DataDocumentIdentifier id = (DataDocumentIdentifier) facade
				.register(bytes);
		byte[] retrievedBytes = (byte[]) facade.resolve(id, byte[].class);
		assertTrue("Retrieved bytes didn't match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void registerDeepEmptyList() throws DataManagerException,
			IOException {
		List<List<Object>> deepEmptyList = new ArrayList<List<Object>>();
		EntityIdentifier id = facade.register(deepEmptyList, 2);
		assertEquals(2, id.getDepth());
	}

	@Test
	public void registerDouble() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
		double number = Double.MAX_VALUE;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity, Double.class));
	}

	@Test
	public void registerDoubleInfinity() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {
		double number = Double.POSITIVE_INFINITY;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity, Double.class));
	}

	@Test
	public void registerEmptyList() throws DataManagerException, IOException {
		List<Object> deepEmptyList = new ArrayList<Object>();
		// Note: Does not actually check the parameterised types, but the
		// depth should match
		EntityIdentifier id = facade.register(deepEmptyList, 1);
		assertEquals(1, id.getDepth());
	}

	@Test(expected = EmptyListException.class)
	public void registerEmptyListFails() throws DataManagerException,
			IOException {
		List<Object> emptyList = new ArrayList<Object>();
		facade.register(emptyList);
	}

	@Test
	public void registerFloat() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
		float number = (float) 13.37;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerFloatInfinity() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {
		float number = Float.NEGATIVE_INFINITY;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test
	public void registerInteger() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
		EntityIdentifier entity = facade.register(25);
		assertEquals(25, facade.resolve(entity));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerList() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
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
	public void registerListOfEmptyListFirst() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {
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

	@SuppressWarnings("unchecked")
	@Test
	public void registerListOfHalfEmptyLists() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {

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

	@Test(expected = EmptyListException.class)
	public void registerListOfJustEmptyListsFails()
			throws DataManagerException, IOException {
		List<Object> emptyList1 = new ArrayList<Object>();
		List<Object> emptyList2 = new ArrayList<Object>();
		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(emptyList1);
		bigList.add(emptyList2);
		facade.register(bigList);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerListOfLists() throws RetrievalException,
			NotFoundException, DataManagerException, IOException {
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
	public void registerListOfListsforResolvingAsString()
			throws RetrievalException, NotFoundException, DataManagerException,
			IOException {

		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		HttpReferenceScheme httpRefScheme = new HttpReferenceScheme(new URL(
				"http://google.com"));
		references.add(httpRefScheme);
		File file = null;
		try {
			file = File.createTempFile("test", ".tmp");
		} catch (IOException e) {

		}
		FileReferenceScheme fileRefScheme = new FileReferenceScheme(file);
		references.add(fileRefScheme);
		BlobReferenceSchemeImpl blobRefScheme = new BlobReferenceSchemeImpl(
				TEST_NS, UUID.randomUUID().toString());
		references.add(blobRefScheme);

		DataDocumentIdentifier docId = dManager.registerDocument(references);

		Throwable ex = new IllegalArgumentException("Did not work",
				new NullPointerException("No no"));

		int depth = 0;
		int implicitDepth = 1;

		ErrorDocumentIdentifier errId1 = dManager.registerError(depth++,
				implicitDepth, ex);

		List<Object> list1 = new ArrayList<Object>();
		list1.add(-25);
		list1.add((float) 30.56);
		list1.add(Double.MIN_VALUE);
		list1.add(true);
		list1.add(Long.MAX_VALUE);
		list1.add("hello");
		list1.add(docId);

		List<Object> list2 = new ArrayList<Object>();
		list2.add(25);
		list2.add((float) 32.546);
		list2.add(Double.MAX_VALUE);
		list2.add(false);
		list2.add(Long.MIN_VALUE);
		list2.add("hello there");
		list2.add(errId1);

		List<List<Object>> bigList = new ArrayList<List<Object>>();
		bigList.add(list1);
		bigList.add(list2);
		EntityIdentifier bigListId = facade.register(bigList);

		Element element = facade.resolveToElement(bigListId.getAsURI());
		XMLOutputter output = new XMLOutputter();
		StringWriter writer = new StringWriter();
		output.output(element, writer);
		System.out.println(writer.toString());
	}

	@Test
	public void registerLiteralCorrectDepth() throws DataManagerException,
			IOException {
		facade.register("I've got correct depth", 0);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerLiteralWrongDepthFails() throws DataManagerException,
			IOException {
		// Literals can only have depth 0 and UNKNOWN_DEPTH
		facade.register("I've messed up my depth", 1);
	}

	@Test
	public void registerLong() throws RetrievalException, NotFoundException,
			DataManagerException, IOException {
		long number = Long.MIN_VALUE;
		EntityIdentifier entity = facade.register(number);
		assertEquals(number, facade.resolve(entity));
	}

	@Test(expected = MalformedListException.class)
	public void registerMalformedList() throws DataManagerException,
			IOException {
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
	public void registerManyEmptyLists() throws DataManagerException,
			IOException {
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

	@Test
	public void registerStream() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException,
			DereferenceException {
		final byte[] bytes = "A test".getBytes(STRING_CHARSET);
		ByteArrayInputStream stream = new ByteArrayInputStream(bytes);
		DataDocumentIdentifier id = (DataDocumentIdentifier) facade
				.register(stream);
		stream.close();
		InputStream resolvedStream = (InputStream) facade.resolve(id);
		assertNotSame(stream, resolvedStream);
		byte[] retrievedBytes = IOUtils.toByteArray(resolvedStream);
		assertTrue("Retrieved bytes didn't match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void registerString() throws RetrievalException, NotFoundException,
			DataManagerException, MalformedIdentifierException,
			UnsupportedEncodingException, IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		assertFalse("Test string was too long", str.length() > dManager
				.getMaxIDLength());
		EntityIdentifier entity = facade.register(str);
		assertTrue("String was not registered as a Literal",
				entity instanceof Literal);
		assertEquals(str, facade.resolve(entity, String.class));
		assertTrue("Was not stored as Literal",
				facade.resolve(entity) instanceof String);
	}

	@Test
	public void registerStringGetAsBytes() throws RetrievalException,
			NotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException,
			IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		assertFalse("Test string was too long", str.length() > dManager
				.getMaxIDLength());
		EntityIdentifier entity = facade.register(str);
		assertTrue("String was not registered as a Literal",
				entity instanceof Literal);
		byte[] bytes = (byte[]) facade.resolve(entity, byte[].class);
		assertEquals(str, new String(bytes, STRING_CHARSET));
	}

	@Test
	public void registerStringGetAsStream() throws RetrievalException,
			NotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException,
			IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		assertFalse("Test string was too long", str.length() > dManager
				.getMaxIDLength());
		EntityIdentifier entity = facade.register(str);
		InputStream stream = (InputStream) facade.resolve(entity,
				InputStream.class);
		assertEquals(str, IOUtils.toString(stream, STRING_CHARSET));
	}

	@Test
	public void registerStringAsBytes() throws RetrievalException,
			NotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException,
			IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		byte[] bytes = str.getBytes(STRING_CHARSET);
		EntityIdentifier entity = facade.register(bytes, STRING_CHARSET);
		assertEquals(str, facade.resolve(entity, String.class));
		assertTrue("Was not stored as blob",
				facade.resolve(entity) instanceof InputStream);
	}

	@Test(expected = RetrievalException.class)
	public void registerStringAsBytesWithoutCharsetFails()
			throws RetrievalException, NotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException,
			IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		byte[] bytes = str.getBytes(STRING_CHARSET);
		EntityIdentifier entity = facade.register(bytes);
		facade.resolve(entity, String.class);
	}

	@Test
	public void registerLongString() throws RetrievalException,
			NotFoundException, DataManagerException,
			MalformedIdentifierException, UnsupportedEncodingException,
			IOException {
		String str = "hello with some / weird\n"
				+ " ! character% and �(x) = �";
		String longString = "";
		for (int i = 0; i < 10; i++) {
			longString = longString + "\n" + str;
		}
		assertTrue("Test string was not long enough",
				longString.length() > dManager.getMaxIDLength());
		EntityIdentifier entity = facade.register(longString);
		assertEquals(longString, facade.resolve(entity, String.class));
		assertTrue("Was not stored as blob",
				facade.resolve(entity) instanceof InputStream);
	}

	@Test(expected = MalformedListException.class)
	public void registerTooShortDepthFails() throws DataManagerException,
			IOException {
		List<Object> list = new ArrayList<Object>();
		list.add(25);
		list.add((float) 32.546);
		list.add(Double.MAX_VALUE);
		list.add(false);
		list.add(Long.MIN_VALUE);
		list.add("hello there");
		facade.register(list, 0);
	}

	@Test(expected = UnsupportedObjectTypeException.class)
	public void registerUnsupportedObjectFails() throws DataManagerException,
			IOException {
		facade.register(this);
	}

	@Test(expected = IllegalArgumentException.class)
	public void registerWrongDepthFails() throws DataManagerException,
			IOException {

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

	@SuppressWarnings("unchecked")
	@Test
	public void registerStringArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		String[] strings = new String[] { "my", "string", "array" };
		EntityIdentifier entity = facade.register(strings);

		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(strings[0], resolved.get(0));
		assertEquals(strings[1], resolved.get(1));
		assertEquals(strings[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerObjectArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		Object[] objects = new Object[] { "my", 14, -13.37 };
		EntityIdentifier entity = facade.register(objects);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(objects[0], resolved.get(0));
		assertEquals(objects[1], resolved.get(1));
		assertEquals(objects[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerIntArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		int[] ints = new int[] { 42, 14, -13 };
		EntityIdentifier entity = facade.register(ints);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(ints[0], resolved.get(0));
		assertEquals(ints[1], resolved.get(1));
		assertEquals(ints[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerLongArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		long[] longs = new long[] { 4212387128373438123l, Long.MAX_VALUE, -13l };
		EntityIdentifier entity = facade.register(longs);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(longs[0], resolved.get(0));
		assertEquals(longs[1], resolved.get(1));
		assertEquals(longs[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerFloatArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		float[] floats = new float[] { 2.12f, 2.44f, -13.37f };
		EntityIdentifier entity = facade.register(floats);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(floats[0], resolved.get(0));
		assertEquals(floats[1], resolved.get(1));
		assertEquals(floats[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerDoubleArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		double[] doubles = new double[] { 2.12, 2.44, -13.37 };
		EntityIdentifier entity = facade.register(doubles);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(doubles[0], resolved.get(0));
		assertEquals(doubles[1], resolved.get(1));
		assertEquals(doubles[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerBoolArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		boolean[] bools = new boolean[] { true, false, false };
		EntityIdentifier entity = facade.register(bools);
		assertEquals(1, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());
		assertEquals(bools[0], resolved.get(0));
		assertEquals(bools[1], resolved.get(1));
		assertEquals(bools[2], resolved.get(2));
	}

	@SuppressWarnings("unchecked")
	@Test
	public void registerStringStringArray() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		String[][] arrayOfStringArrays = new String[][] { {}, // empty
				{ "attempt" }, { "to", "create", "an", "array" } };
		EntityIdentifier entity = facade.register(arrayOfStringArrays);

		assertEquals(2, entity.getDepth());
		List resolved = (List) facade.resolve(entity);
		assertEquals(3, resolved.size());

		List firstList = (List) resolved.get(0);
		assertTrue(firstList.isEmpty());

		List secondList = (List) resolved.get(1);
		assertEquals(1, secondList.size());
		assertEquals(arrayOfStringArrays[1][0], secondList.get(0));

		List thirdList = (List) resolved.get(2);
		assertEquals(4, thirdList.size());
		assertEquals(arrayOfStringArrays[2][0], thirdList.get(0));
		assertEquals(arrayOfStringArrays[2][1], thirdList.get(1));
		assertEquals(arrayOfStringArrays[2][2], thirdList.get(2));
		assertEquals(arrayOfStringArrays[2][3], thirdList.get(3));
	}

	@Before
	public void setDataManager() {
		// dManager = new FileDataManager("testNS",
		// new HashSet<LocationalContext>(), new File("/tmp/fish"));
		dManager = new InMemoryDataManager(TEST_NS,
				new HashSet<LocationalContext>());
		facade = new DataFacade(dManager);
	}

}
