package net.sf.taverna.t2.cloudone.translator;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.HashSet;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.DataFacade;
import net.sf.taverna.t2.cloudone.datamanager.EmptyListException;
import net.sf.taverna.t2.cloudone.datamanager.MalformedListException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.UnsupportedObjectTypeException;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.impl.url.URLReferenceScheme;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public class RefSchemeTranslatorTest {

	private static final String LONG_STRING = "qwertyuiopasdfghjklzxcvbnmqwertyuiopasdfghjklzx"
					+ "cvbnmqwertyuiopasdfghjklzxcvbnmqwertyuioipasdfghjklzxcvbnmq";
	private static final String ASCII = "ascii";
	private static final String TEST_DATA = "This is the test data.\n";
	private static final String TEST_NS = "testNS";
	protected AbstractDataManager dManager;
	protected DataFacade facade;
	protected ReferenceSchemeTranslator translator;

	@Before
	public void setDataManager() {
		dManager = new InMemoryDataManager(TEST_NS,
				new HashSet<LocationalContext>());
		facade = new DataFacade(dManager);
		translator = new ReferenceSchemeTranslatorImpl(dManager);
	}

	@Test
	public void testFindBlobScheme() throws RetrievalException, NotFoundException {
		BlobReferenceScheme blobRef = new BlobReferenceSchemeImpl(TEST_NS, UUID
				.randomUUID().toString());
		// Register a new data document with fake scheme
		DataDocumentIdentifier id = (DataDocumentIdentifier) dManager
				.registerDocument(blobRef);
		assertEquals("Didn't match original BlobReferenceScheme", blobRef,
				findBlobScheme(id));
	}

	@Test
	public void testMakeString() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataDocumentIdentifier id = makeString();
		DataDocument ddoc = (DataDocument) dManager.getEntity(id);
		assertFalse("No reference schemes", ddoc.getReferenceSchemes()
				.isEmpty());
		for (ReferenceScheme ref : ddoc.getReferenceSchemes()) {
			assertTrue("Was not a blob reference scheme: " + ref,
					ref instanceof BlobReferenceScheme<?>);
		}

	}

	@SuppressWarnings("unchecked")
	@Test
	public void translateBlobToBlob() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataDocumentIdentifier id = makeString();
		BlobReferenceScheme<?> originalRef = findBlobScheme(id);

		ReferenceScheme refScheme = translator.translate(id,
				BlobReferenceScheme.class);
		assertTrue("Translated scheme was not a BlobReferenceScheme",
				refScheme instanceof BlobReferenceScheme<?>);
		assertEquals("Didn't get original BlobReferenceScheme",
				findBlobScheme(id), refScheme);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void translateBlobToBlobInOrder() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataDocumentIdentifier id = makeString();
		// We prefer URLReferenceScheme (which id don't have), but it should not
		// convert as long as we say we accept BlobReferenceScheme
		ReferenceScheme refScheme = translator.translate(id,
				URLReferenceScheme.class, BlobReferenceScheme.class);
		assertTrue("Translated scheme was not a BlobReferenceScheme",
				refScheme instanceof BlobReferenceScheme<?>);
		assertEquals("Didn't get original BlobReferenceScheme",
				findBlobScheme(id), refScheme);
	}

	@SuppressWarnings("unchecked")
	@Test
	public void translateURLToBlob() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException, DereferenceException {
		File tmpFile = File.createTempFile("test", ".txt");
		tmpFile.deleteOnExit();
		FileUtils.writeStringToFile(tmpFile, TEST_DATA, ASCII);
		URLReferenceScheme urlRef = new URLReferenceScheme(tmpFile.toURI().toURL());
		
		DataDocumentIdentifier id = dManager.registerDocument(urlRef);
		ReferenceScheme blobRef = translator.translate(id,
				BlobReferenceScheme.class);
		assertTrue("Translated scheme was not a BlobReferenceScheme",
				blobRef instanceof BlobReferenceScheme);
		assertEquals(TEST_DATA, 
				IOUtils.toString(blobRef.dereference(dManager), ASCII));
	}
	
	@SuppressWarnings("unchecked")
	@Test
	public void translateBlobToURL() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException, DereferenceException {
		DataDocumentIdentifier id = makeString();
		// We prefer URLReferenceScheme (which id don't have), but it should not
		// convert as long as we say we accept BlobReferenceScheme
		ReferenceScheme refScheme = translator.translate(id,
				URLReferenceScheme.class);
		assertTrue("Translated scheme was not a URLReferenceScheme",
				refScheme instanceof URLReferenceScheme);
		assertEquals(LONG_STRING, 
				IOUtils.toString(refScheme.dereference(dManager), ASCII));
	}

	@Test
	public void translatePreferBlob() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException,
			IOException, RetrievalException, NotFoundException {
		DataDocumentIdentifier id = makeTwoFakeRefs();
		ReferenceScheme refScheme = translator.translate(id,
				BlobReferenceScheme.class, URLReferenceScheme.class);
		assertTrue("Translated scheme was not a BlobReferenceScheme",
				refScheme instanceof BlobReferenceScheme<?>);
		assertEquals("Didn't get original BlobReferenceScheme",
				findBlobScheme(id), refScheme);
	}

	private BlobReferenceScheme<?> findBlobScheme(DataDocumentIdentifier id)
			throws RetrievalException, NotFoundException {
		DataDocument ddoc = (DataDocument) dManager.getEntity(id);
		for (ReferenceScheme ref : ddoc.getReferenceSchemes()) {
			if (ref instanceof BlobReferenceScheme<?>) {
				return (BlobReferenceScheme<?>) ref;
			}
		}
		throw new IllegalArgumentException(
				"DataDocument didn't contain a BlobReferenceScheme: " + id);
	}

	private DataDocumentIdentifier makeString() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException, IOException {
		EntityIdentifier entity = facade.register(LONG_STRING);
		return (DataDocumentIdentifier) entity;
	}

	/**
	 * Register a {@link DataDocument} with two (fake) {@link ReferenceScheme}s,
	 * for testing with {@link #translateBlobToBlobPreferBlob()}
	 * 
	 */
	private DataDocumentIdentifier makeTwoFakeRefs() throws EmptyListException,
			MalformedListException, UnsupportedObjectTypeException, IOException {
		BlobReferenceScheme blobRef = new BlobReferenceSchemeImpl(TEST_NS, UUID
				.randomUUID().toString());
		URLReferenceScheme urlRef = new URLReferenceScheme(new URL(
				"http://taverna.sf.net/"));
		// Register a new data document with both reference schemes
		return (DataDocumentIdentifier) dManager.registerDocument(blobRef,
				urlRef);
	}

}
