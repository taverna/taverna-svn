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

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.IOUtils;

import net.sf.taverna.t2.cloudone.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.impl.BlobReferenceSchemeImpl;
import net.sf.taverna.t2.cloudone.BlobReferenceScheme;


public class FileBlobStore implements BlobStore {

	private File path;

	private String namespace;

	public FileBlobStore(String namespace, File path) {
		if(! EntityIdentifier.isValidName(namespace)) {
			throw new MalformedIdentifierException("Invalid namespace: " + namespace);
		}
		this.namespace = namespace;
		path.mkdirs();
		if (! path.isDirectory()) {
			throw new IllegalArgumentException("Invalid directory " + path);
		}
		this.path = path;
	}

	public boolean hasBlob(BlobReferenceScheme<?> reference) {
		return fileByReference(reference).isFile();
	}

	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference) throws NotFoundException {
		InputStream stream = retrieveAsStream(reference);
		try {
			return IOUtils.toByteArray(retrieveAsStream(reference));
		} catch (IOException e) {
			throw new RetrievalException("Can't read " + reference, e);

		} finally {
			IOUtils.closeQuietly(stream);
		}
	}

	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference) throws NotFoundException {
		File file = fileByReference(reference);
		try {
			return new BufferedInputStream(new FileInputStream(file));
		} catch (FileNotFoundException e) {
			throw new NotFoundException("Can't find  " + reference);
		}
	}

	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes) throws StorageException {
		String id = UUID.randomUUID().toString();
		File file = fileById(namespace, id);
		try {
			FileUtils.writeByteArrayToFile(file, bytes);
		} catch (IOException e) {
			throw new StorageException("Could not store to " + file, e);
		}
		return new BlobReferenceSchemeImpl(namespace, id);
	}

	public BlobReferenceScheme<?> storeFromStream(InputStream inStream)
			throws StorageException {
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
			throw new StorageException("Could not read from stream or write to: " + file, e);
		} finally {
			IOUtils.closeQuietly(outStream);
		}

		return new BlobReferenceSchemeImpl(namespace, id);
	}

	private File fileById(String namespace, String id) {
		File nsDir = new File(path, namespace);
		File typeDir = new File(nsDir, "blob");
		//typeDir.mkdirs();
//		if (! typeDir.isDirectory()) {
//			throw new IllegalStateException("Invalid directory" + typeDir);
//		}
		String fileName = id + ".blob";
		return new File(parentDirectory(typeDir, id), fileName);
		//return new File(typeDir, fileName);
	}

	private File fileByReference(BlobReferenceScheme<?> reference) {
		return fileById(reference.getNamespace(), reference.getId());
	}

	private File parentDirectory(File typeDir, String id) {
		String newName = id.substring(0, 2);
		File dirs = new File (typeDir, newName);
		dirs.mkdirs();
		if (! dirs.isDirectory()) {
			throw new IllegalStateException("Invalid directory" + dirs);
		}
		return dirs;
	}

	public long sizeOfBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException {
		File blobFile = fileByReference(reference);
		if (!blobFile.isFile()) {
			throw new NotFoundException("Can't find  " + reference);
		}
		return blobFile.length();
	}


}
