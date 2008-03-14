package net.sf.taverna.t2.cloudone.datamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.datamanager.file.FileBlobStoreTest;
import net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryBlobStoreTest;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

/**
 * Abstract test case for testing any {@link BlobStore}. Used by
 * {@link FileBlobStoreTest} and {@link InMemoryBlobStoreTest}.
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public abstract class AbstractBlobStoreTest {

	protected static final String TEST_STRING = "qwertyuiop";
	protected static final String TEST_NS = "testNS";

	protected BlobStore blobStore;

	protected DataManager dManager;

	@SuppressWarnings("unchecked")
	@Test
	public void registerBlobAndDereference() throws DereferenceException,
			IOException, RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) dManager
				.getBlobStore().storeFromBytes(bytes);
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		references.add(ref);
		DataDocumentIdentifier docId = dManager.registerDocument(references);

		// Retrieve it again going through the dManager
		DataDocument retrieved = (DataDocument) dManager.getEntity(docId);
		final Set<ReferenceScheme> retrievedReferences = retrieved
				.getReferenceSchemes();
		assertFalse("Did not contain reference scheme", retrievedReferences
				.isEmpty());
		assertEquals("Too many reference schemes", 1, retrievedReferences
				.size());
		ReferenceScheme retrievedRef = retrievedReferences.iterator().next();


		if (retrievedRef instanceof BlobReferenceSchemeImpl) {
			BlobReferenceSchemeImpl blobRef = (BlobReferenceSchemeImpl) retrievedRef;
			// TODO: Test blobRef
		}
		
		InputStream stream = retrievedRef.dereference(dManager);
		byte[] retrievedBytes = IOUtils.toByteArray(stream);		
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Before
	public abstract void setDataManagerAndBlobStore();

	@Test
	public void storeBytesRetrieveBytes() throws RetrievalException,
			NotFoundException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeBytesRetrieveStream() throws DereferenceException,
			RetrievalException, NotFoundException, IOException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		InputStream stream = blobStore.retrieveAsStream(ref);
		byte[] retrievedBytes = IOUtils.toByteArray(stream);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeInputStreamRetrieveBytes() throws RetrievalException,
			NotFoundException {
		byte[] bytes = makeByteArray();
		InputStream inStream = new ByteArrayInputStream(bytes);
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromStream(inStream);
		assertNotNull(ref);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeInputStreamRetrieveStream() throws IOException,
			RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		InputStream inStream = new ByteArrayInputStream(bytes);
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromStream(inStream);
		assertNotNull(ref);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeStringRetrieveString() throws RetrievalException,
			IllegalArgumentException, NotFoundException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		BlobReferenceScheme<?> ref = blobStore.storeFromString(string);
		assertNotNull(ref);
		String retrievedString = blobStore.retrieveAsString(ref);
		assertEquals(string, retrievedString);
	}

	@Test
	public void storeStringRetrieveStream() throws RetrievalException,
			NotFoundException, IOException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		BlobReferenceScheme<?> ref = blobStore.storeFromString(string);
		assertNotNull(ref);
		InputStream stream = blobStore.retrieveAsStream(ref);
		assertEquals(string, IOUtils.toString(stream, "utf-8"));
	}

	@Test
	public void storeInputStreamRetrieveString() throws RetrievalException,
			IllegalArgumentException, NotFoundException,
			UnsupportedEncodingException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		byte[] bytes = string.getBytes("UTF-16");
		InputStream inStream = new ByteArrayInputStream(bytes);
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromStream(inStream);
		assertNotNull(ref);
		String retrievedString = blobStore.retrieveAsString(ref, "UTF-16");
		assertEquals(string, retrievedString);
	}

	@Test
	public void storeBytesRetrieveString() throws UnsupportedEncodingException,
			RetrievalException, IllegalArgumentException, NotFoundException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		byte[] bytes = string.getBytes("UTF-16");
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		assertNotNull(ref);
		String retrievedString = blobStore.retrieveAsString(ref, "UTF-16");
		assertEquals(string, retrievedString);
		String retrievedString2 = blobStore.retrieveAsString(ref, "iso8859-1");
		assertFalse(string.equals(retrievedString2));
	}

	@Test
	public void storeBytesWithEncodingRetrieveString()
			throws UnsupportedEncodingException, RetrievalException,
			IllegalArgumentException, NotFoundException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		byte[] bytes = string.getBytes("UTF-16");
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes, "UTF-16");
		assertNotNull(ref);
		String retrievedString = blobStore.retrieveAsString(ref);
		assertEquals(string, retrievedString);

		String retrievedString2 = blobStore.retrieveAsString(ref, "UTF-16");
		assertEquals(string, retrievedString2);
	}

	@Test(expected=IllegalArgumentException.class)
	public void storeBytesWithoutEncodingRetrieveStringFails()
			throws UnsupportedEncodingException, RetrievalException,
			IllegalArgumentException, NotFoundException {
		String string = "qwertyuiop¿¿ÄÄÅÅ";
		byte[] bytes = string.getBytes("UTF-16");
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		assertNotNull(ref);
		assertNull(ref.getCharset());
		blobStore.retrieveAsString(ref);
	}

	@Test
	public void hasBlob() {
		BlobReferenceSchemeImpl notExists = new BlobReferenceSchemeImpl(
				TEST_NS, "notExists");
		assertFalse(blobStore.hasBlob(notExists));
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		assertTrue(blobStore.hasBlob(ref));
	}

	@Test
	public void size() throws RetrievalException, NotFoundException {
		new BlobReferenceSchemeImpl(TEST_NS, "notExists");
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore
				.storeFromBytes(bytes);
		assertEquals(10, blobStore.sizeOfBlob(ref));
	}

	private byte[] makeByteArray() {
		byte[] bytes;
		try {
			bytes = TEST_STRING.getBytes("utf8");
		} catch (UnsupportedEncodingException e) {
			throw new RuntimeException("utf8 was unknown encoding", e);
		}
		assertEquals(10, bytes.length);
		assertEquals(113, bytes[0]); // "q"
		assertEquals(112, bytes[9]); // "p"
		return bytes;
	}
}
