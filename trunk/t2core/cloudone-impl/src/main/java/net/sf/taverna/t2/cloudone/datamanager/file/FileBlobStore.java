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
import java.util.UUID;

import net.sf.taverna.t2.cloudone.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.DereferenceException;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;

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
 * 			651375b7-8ce1-4d05-95ed-7b4912a50d0c.blob
 * 			4d056513-75b7-8ce1-4d05-95ed7b4912a5.blob
 * </pre>
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public class FileBlobStore implements BlobStore {

	private File path;

	private String namespace;

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

	private File fileById(String namespace, String id) {
		File nsDir = new File(path, namespace);
		File typeDir = new File(nsDir, "blob");
		// typeDir.mkdirs();
		// if (! typeDir.isDirectory()) {
		// throw new IllegalStateException("Invalid directory" + typeDir);
		// }
		String fileName = id + ".blob";
		return new File(parentDirectory(typeDir, id), fileName);
		// return new File(typeDir, fileName);
	}

	private File fileByReference(BlobReferenceScheme<?> reference) {
		return fileById(reference.getNamespace(), reference.getId());
	}

	private File parentDirectory(File typeDir, String id) {
		String newName = id.substring(0, 2);
		File dirs = new File(typeDir, newName);
		dirs.mkdirs();
		if (!dirs.isDirectory()) {
			throw new IllegalStateException("Invalid directory" + dirs);
		}
		return dirs;
	}

}
