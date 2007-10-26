package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.impl.http.LocationalContextImpl;

import org.apache.commons.io.IOUtils;

public class InMemoryBlobStore implements BlobStore {

	private Map<String, byte[]> blobs = new HashMap<String, byte[]>();

	private String namespace;

	private Set<LocationalContext> locationalContexts;

	public InMemoryBlobStore() {
		// Always a new namespace
		namespace = UUID.randomUUID().toString();
	}

	public boolean hasBlob(BlobReferenceScheme<?> reference) {
		if (!reference.getNamespace().equals(namespace)) {
			return false;
		}
		return blobs.containsKey(reference.getId());
	}

	/**
	 * Retrieve blob as a byte[] array, or <code>null</code> if this store
	 * does not contain the blob.
	 * 
	 * @param reference
	 *            A reference to a blob, previously stored using
	 *            {@link #storeFromBytes(byte[])} or
	 *            {@link #storeFromStream(InputStream)}
	 * @return byte[] array or <code>null</code>
	 * @throws NotFoundException
	 */
	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference)
			throws NotFoundException {
		if (!reference.getNamespace().equals(namespace)) {
			throw new NotFoundException("Unknown namespace "
					+ reference.getNamespace());
		}
		byte[] bytes = blobs.get(reference.getId());
		if (bytes == null) {
			throw new NotFoundException("Can't find " + reference);
		}
		return bytes;
	}

	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference)
			throws NotFoundException {
		byte[] bytes = retrieveAsBytes(reference);
		return new ByteArrayInputStream(bytes);
	}

	public String retrieveAsString(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException,
			IllegalArgumentException {
		String charset;
		try {
			charset = reference.getCharset();
		} catch (DereferenceException e) {
			throw new RetrievalException("Could not retrieve reference "
					+ reference, e);
		}
		if (charset == null) {
			throw new IllegalArgumentException(
					"Reference did not have character set " + reference);
		}
		return retrieveAsString(reference, charset);
	}

	public String retrieveAsString(BlobReferenceScheme<?> reference,
			String charset) throws RetrievalException, NotFoundException,
			IllegalArgumentException {
		try {
			return IOUtils.toString(retrieveAsStream(reference), charset);
		} catch (IOException e) {
			throw new RetrievalException(e);
		}
	}

	public long sizeOfBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		byte[] bytes = retrieveAsBytes(reference);
		return bytes.length;
	}

	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes) {
		return storeFromBytes(bytes, null);
	}

	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes, String charset)
			throws StorageException {
		String id = UUID.randomUUID().toString();
		blobs.put(id, bytes);
		return new BlobReferenceSchemeImpl(namespace, id, charset);
	}

	public BlobReferenceScheme<?> storeFromStream(InputStream stream)
			throws StorageException {
		return storeFromStream(stream, null);
	}

	public BlobReferenceScheme<?> storeFromStream(InputStream stream,
			String charset) throws StorageException {
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(stream);
		} catch (IOException e) {
			throw new StorageException("Could not read from stream", e);
		}
		return storeFromBytes(bytes, charset);
	}

	public BlobReferenceScheme<?> storeFromString(String string)
			throws StorageException {
		String id = UUID.randomUUID().toString();
		try {
			blobs.put(id, string.getBytes(STRING_CHARSET));
		} catch (UnsupportedEncodingException e) {
			throw new StorageException(e);
		}
		return new BlobReferenceSchemeImpl(namespace, id, STRING_CHARSET);
	}

	public Set<LocationalContext> getLocationalContexts() {
		if (locationalContexts == null) {
			// Create our LocationalContext
			Map<String, String> contextMap = new HashMap<String, String>();
			// Our namespace is an UUID
			contextMap.put(LOCATIONAL_CONTEXT_KEY_UUID, namespace);
			LocationalContext locationalContext = new LocationalContextImpl(
					LOCATIONAL_CONTEXT_TYPE, contextMap);
			locationalContexts = Collections.singleton(locationalContext);
		}
		return locationalContexts;

	}

}
