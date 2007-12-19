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

import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.datamanager.file.FileBlobStore;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.peer.LocationalContextImpl;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

import org.apache.commons.io.IOUtils;

/**
 * Simple in-memory implementation of {@link BlobStore} storing the blobs as in
 * a HashMap containing byte arrays. Mainly used for testing purposes.
 * 
 * @see FileBlobStore
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class InMemoryBlobStore implements BlobStore {

	private Map<String, byte[]> blobs = new HashMap<String, byte[]>();

	private String namespace;

	private Set<LocationalContext> locationalContexts;

	/**
	 * Construct a in-memory blob store with a new, unique namespace.
	 * 
	 */
	public InMemoryBlobStore() {
		// Always a new namespace
		namespace = UUID.randomUUID().toString();
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasBlob(BlobReferenceScheme<?> reference) {
		if (!reference.getNamespace().equals(namespace)) {
			return false;
		}
		return blobs.containsKey(reference.getId());
	}

	/**
	 * {@inheritDoc}
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

	/**
	 * {@inheritDoc}
	 */
	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference)
			throws NotFoundException {
		byte[] bytes = retrieveAsBytes(reference);
		return new ByteArrayInputStream(bytes);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
	public String retrieveAsString(BlobReferenceScheme<?> reference,
			String charset) throws RetrievalException, NotFoundException,
			IllegalArgumentException {
		try {
			return IOUtils.toString(retrieveAsStream(reference), charset);
		} catch (IOException e) {
			throw new RetrievalException(e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long sizeOfBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		byte[] bytes = retrieveAsBytes(reference);
		return bytes.length;
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes) {
		return storeFromBytes(bytes, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes, String charset)
			throws StorageException {
		String id = UUID.randomUUID().toString();
		blobs.put(id, bytes);
		return new BlobReferenceSchemeImpl(namespace, id, charset);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream stream)
			throws StorageException {
		return storeFromStream(stream, null);
	}

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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

	/**
	 * {@inheritDoc}
	 */
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
