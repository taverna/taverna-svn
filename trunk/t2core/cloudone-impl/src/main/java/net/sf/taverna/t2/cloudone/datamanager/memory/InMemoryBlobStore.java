package net.sf.taverna.t2.cloudone.datamanager.memory;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.BlobReferenceScheme;

import org.apache.commons.io.IOUtils;

public class InMemoryBlobStore implements BlobStore{
	
	private Map<String, byte[]> blobs = new HashMap<String, byte[]>();
	
	private String namespace;
	
	public InMemoryBlobStore() {
		// Always a new namespace
		namespace = UUID.randomUUID().toString();
	}
	
	public boolean hasBlob(BlobReferenceScheme<?> reference) {
		if (! reference.getNamespace().equals(namespace)) {
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
	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference) throws NotFoundException {
		if (! reference.getNamespace().equals(namespace)) {
			throw new NotFoundException("Unknown namespace " + reference.getNamespace());
		}
		byte[] bytes = blobs.get(reference.getId());
		if (bytes == null) {
			throw new NotFoundException("Can't find " + reference);
		}
		return bytes;
	}
	
	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference) throws NotFoundException  {
		byte[] bytes = retrieveAsBytes(reference);
		return new ByteArrayInputStream(bytes);
	}
	
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes) {
		String id = UUID.randomUUID().toString();
		blobs.put(id, bytes);
		return new BlobReferenceSchemeImpl(namespace, id);
	}
	
	public BlobReferenceScheme<?> storeFromStream(InputStream stream) throws StorageException {
		byte[] bytes;
		try {
			bytes = IOUtils.toByteArray(stream);
		} catch (IOException e) {
			throw new StorageException("Could not read from stream", e);
		}
		return storeFromBytes(bytes);
	}

}
