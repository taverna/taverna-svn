package net.sf.taverna.t2.cloudone.datamanager;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DataManager;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.ReferenceScheme;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.identifier.DataDocumentIdentifier;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;

import org.apache.commons.io.IOUtils;
import org.junit.Before;
import org.junit.Test;

public abstract class AbstractBlobStoreTest {

	protected static final String TEST_STRING = "qwertyuiop";
	protected static final String TEST_NS = "testNS";

	protected BlobStore blobStore;
	
	protected DataManager dManager;

	@Before
	public abstract void setDataManagerAndBlobStore();
	
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

	@Test
	public void storeBytesRetrieveBytes() throws RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore.storeFromBytes(bytes);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeInputStreamRetrieveBytes() throws  RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		InputStream inStream = new ByteArrayInputStream(bytes);
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore.storeFromStream(inStream);
		assertNotNull(ref);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);		
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeBytesRetrieveStream() throws 
			DereferenceException, RetrievalException, NotFoundException, IOException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore.storeFromBytes(bytes);
		InputStream stream = blobStore.retrieveAsStream(ref);
		byte[] retrievedBytes = IOUtils.toByteArray(stream);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void storeInputStreamRetrieveStream() throws IOException, RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		InputStream inStream = new ByteArrayInputStream(bytes);
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) blobStore.storeFromStream(inStream);
		assertNotNull(ref);
		byte[] retrievedBytes = blobStore.retrieveAsBytes(ref);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}

	@Test
	public void registerBlobAndDereference() throws DereferenceException, IOException, RetrievalException, NotFoundException {
		byte[] bytes = makeByteArray();
		BlobReferenceSchemeImpl ref = (BlobReferenceSchemeImpl) dManager.getBlobStore().storeFromBytes(bytes);
		Set<ReferenceScheme> references = new HashSet<ReferenceScheme>();
		references.add(ref);
		DataDocumentIdentifier docId = dManager.registerDocument(references);
		
		// Retrieve it again going through the dManager
		DataDocument retrieved = (DataDocument) dManager.getEntity(docId);
		final Set<ReferenceScheme> retrievedReferences = retrieved.getReferenceSchemes();
		assertFalse("Did not contain reference scheme", retrievedReferences.isEmpty());
		assertEquals("Too many reference schemes", 1, retrievedReferences.size());
		ReferenceScheme retrievedRef = retrievedReferences.iterator().next();
		
		InputStream stream = retrievedRef.dereference(dManager);
		byte[] retrievedBytes = IOUtils.toByteArray(stream);
		assertTrue("Retrieved byte array did not match", Arrays.equals(bytes,
				retrievedBytes));
	}
}
