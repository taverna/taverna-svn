package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.peer.LocationalContextImpl;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.DereferenceException;
import net.sf.taverna.t2.cloudone.refscheme.blob.BlobReferenceSchemeImpl;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

/**
 * File-based {@link BlobStore}. This store is very similar to a
 * {@link FileDataManager}, they can even share the directory structure. Each
 * blob is simply stored as a separate file.
 * 
 * <pre>
 * 	namespace1/
 * 		blob/
 * 			65/
 *  			651375b7-8ce1-4d05-95ed-7b4912a50d0c.blob
 * 			4d/
 * 				4d056513-75b7-8ce1-4d05-95ed7b4912a5.blob
 * </pre>
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileBlobStore implements BlobStore {
	/*
	 * Where are the blobs stored
	 */
	private File path;

	private String namespace;
	/*
	 * Where is the blob store valid eg. local, same IP, global etc.
	 */
	private Set<LocationalContext> locationalContexts;

	/**
	 * Construct a FileBlobStore with a given <code>namespace</code> which can
	 * store and retrieve from a directory structure below the given
	 * <code>path</code>.
	 * <p>
	 * The {@link FileBlobStore} can retrieve from other namespaces as long as
	 * the blobs are present in the <code>path</code>, but stored blobs will
	 * be stored under and assigned the given namespace.
	 * 
	 * @param namespace
	 *            Namespace of blobstore
	 * @param path
	 *            Path to repository where to store blobs
	 */
	public FileBlobStore(String namespace, File path) {
		if (!EntityIdentifier.isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace: "
					+ namespace);
		}
		this.namespace = namespace;
		path.mkdirs();
		if (!path.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory " + path);
		}
		this.path = path;

		initContext();
	}

	/**
	 * Read/generate locational context for {@link #path}. This makes it
	 * possible to share {@link FileBlobStore} between different nodes accessing
	 * the same file system.
	 * 
	 */
	private void initContext() {
		File context = new File(path, LOCATIONAL_CONTEXT_TYPE
				+ ".locationalcontext");
		String uuid;
		if (!context.exists()) {
			// Create and store in file
			uuid = UUID.randomUUID().toString();
			try {
				FileUtils.writeStringToFile(context, uuid, STRING_CHARSET);
			} catch (IOException e) {
				throw new IllegalStateException("Can't write context to "
						+ context, e);
			}
		} else {
			// Read it from file
			try {
				uuid = FileUtils.readFileToString(context, STRING_CHARSET);
			} catch (IOException e) {
				throw new IllegalStateException("Can't read context from "
						+ context, e);
			}
		}
		Map<String, String> contextMap = new HashMap<String, String>();
		contextMap.put(LOCATIONAL_CONTEXT_KEY_UUID, uuid);
		LocationalContext locationalContext = new LocationalContextImpl(
				LOCATIONAL_CONTEXT_TYPE, contextMap);
		locationalContexts = Collections.singleton(locationalContext);
	}

	/**
	 * {@inheritDoc}
	 */
	public boolean hasBlob(BlobReferenceScheme<?> reference) {
		return fileByReference(reference).isFile();
	}

	/**
	 * {@inheritDoc}
	 */
	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference)
			throws NotFoundException {
		InputStream stream = retrieveAsStream(reference);
		try {
			return IOUtils.toByteArray(retrieveAsStream(reference));
		} catch (IOException e) {
			throw new RetrievalException("Can't read " + reference, e);
		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference)
			throws NotFoundException {
		File file = fileByReference(reference);
		try {
			return new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new NotFoundException(reference);
		}
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
		InputStream stream = retrieveAsStream(reference);
		try {
			return IOUtils.toString(stream, charset);
		} catch (IOException e) {
			throw new RetrievalException("Could not retrieve " + reference, e);
		}
	}

	/**
	 * {@inheritDoc}
	 */
	public long sizeOfBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		File blobFile = fileByReference(reference);
		if (!blobFile.isFile()) {
			throw new NotFoundException(reference);
		}
		return blobFile.length();
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes)
			throws StorageException {
		return storeFromBytes(bytes, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes, String charset)
			throws StorageException {
		String id = UUID.randomUUID().toString();
		File file = fileById(namespace, id);
		try {
			FileUtils.writeByteArrayToFile(file, bytes);
		} catch (IOException e) {
			throw new StorageException("Could not store to " + file, e);
		}
		return new BlobReferenceSchemeImpl(namespace, id, charset);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream inStream)
			throws StorageException {
		return storeFromStream(inStream, null);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream inStream,
			String charset) throws StorageException {
		String id = UUID.randomUUID().toString();
		File file = fileById(namespace, id);
		OutputStream outStream;
		try {
			outStream = new BufferedOutputStream(new FileOutputStream(file));
		} catch (FileNotFoundException e) {
			throw new StorageException("Could not open for writing: " + file, e);
		}

		try {
			IOUtils.copy(inStream, outStream);
		} catch (IOException e) {
			throw new StorageException(
					"Could not read from stream or write to: " + file, e);
		} finally {
			IOUtils.closeQuietly(outStream);
		}

		return new BlobReferenceSchemeImpl(namespace, id, charset);
	}

	/**
	 * {@inheritDoc}
	 */
	public BlobReferenceScheme<?> storeFromString(String string)
			throws StorageException {
		InputStream stream;
		try {
			stream = IOUtils.toInputStream(string, STRING_CHARSET);
		} catch (IOException e) {
			throw new StorageException("Failed to store from string", e);
		}
		return storeFromStream(stream, STRING_CHARSET);
	}

	/**
	 * {@inheritDoc}
	 */
	private File fileById(String namespace, String id) {
		File nsDir = new File(path, namespace);
		File typeDir = new File(nsDir, "blob");
		File dir = parentDirectory(typeDir, id);
		String fileName = id + ".blob";
		return new File(dir, fileName);
	}

	/**
	 * Generate filename for reference.
	 * 
	 * @param reference
	 *            {@link BlobReferenceScheme}
	 * @return generated {@link File}
	 */
	private File fileByReference(BlobReferenceScheme<?> reference) {
		return fileById(reference.getNamespace(), reference.getId());
	}

	/**
	 * Find (and make) parent directory for a given id within a type directory.
	 * The parent directory is normally given by the first two characters of the
	 * id.
	 * 
	 * @param typeDir
	 *            The parent of the parent directory
	 * @param id
	 *            The identifier
	 * @return The {@link File} for given identifier.
	 */
	private File parentDirectory(File typeDir, String id) {
		String newName = id.substring(0, 2);
		File dirs = new File(typeDir, newName);
		dirs.mkdirs();
		if (!dirs.isDirectory()) {
			throw new IllegalStateException("Invalid directory" + dirs);
		}
		return dirs;
	}

	/**
	 * {@inheritDoc}
	 */
	public Set<LocationalContext> getLocationalContexts() {
		return locationalContexts;
	}

}
