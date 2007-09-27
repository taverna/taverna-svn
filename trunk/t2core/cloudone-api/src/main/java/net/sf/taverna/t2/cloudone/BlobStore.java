package net.sf.taverna.t2.cloudone;

import java.io.InputStream;

import net.sf.taverna.t2.cloudone.datamanager.NotFoundException;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;

/**
 * <p>
 * A store for BLOBs, ie. binary data.
 * </p>
 * <p>
 * A BlobStore complements the {@link DataManager}, and notably provided by
 * {@link DataManager#getBlobStore()}, for storing raw data. The data is stored
 * in a blob store, and given a {@link BlobReferenceScheme} that can later by
 * registered as a {@link DataDocument} using
 * {@link DataManager#registerDocument(java.util.Set)}.
 * </p>
 * <p>
 * Data can be stored either from a byte[] array using
 * {@link #storeFromBytes(byte[])}, or from an already open {@link InputStream}
 * using {@link #storeFromStream(InputStream)}, such as opened from a file or
 * URL. Similarly, data can be retrieved as an {@link InputStream} using
 * {@link #retrieveAsStream(BlobReferenceScheme)} or a byte[] array using
 * {@link #retrieveAsBytes(BlobReferenceScheme)}.
 * </p>
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public interface BlobStore {

	/**
	 * Check if the BlobStore has a given blob, as referenced from a
	 * {@link BlobReferenceScheme}. If the result is true, the reference can be
	 * used with {@link #retrieveAsBytes(BlobReferenceScheme)} and
	 * {@link #retrieveAsStream(BlobReferenceScheme)}.
	 *
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
	 * @return true if the BlobStore has the blob
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 */
	public boolean hasBlob(BlobReferenceScheme<?> reference)
			throws RetrievalException;

	/**
	 * Get size of blob as number of bytes.
	 *
	 * @param reference {@link BlobReferenceScheme} referring to the blob
	 * @return Size of blob as number of bytes, or <code>-1</code> if size is
	 *         unknown.
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 * @throws NotFoundException
	 *             If the blob didn't exist in BlobStore
	 */
	public long sizeOfBlob(BlobReferenceScheme<?> reference)
	throws RetrievalException, NotFoundException;

	/**
	 * Retrieve a blob as a byte[] array.
	 * <p>
	 * This method should only be called on blobs which size will fit in memory,
	 * otherwise use {@link #retrieveAsStream(BlobReferenceScheme)}. Check size
	 * with {@link #sizeOfBlob(BlobReferenceScheme)}.
	 *
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
	 * @return Initialised and populated byte[] array containing blob
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 * @throws NotFoundException
	 *             If the blob didn't exist in BlobStore
	 */
	public byte[] retrieveAsBytes(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException;
	/**
	 * Retrieve a blob as an {@link InputStream}. Normally used instead of
	 * {@link #retrieveAsBytes(BlobReferenceScheme)}, in particular if the blob
	 * is too large to fit in memory.
	 *
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
	 * @return {@link InputStream} that reads from the blob
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 * @throws NotFoundException
	 *             If the blob didn't exist in BlobStore
	 */
	public InputStream retrieveAsStream(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException;

	/**
	 * Store byte[] array as a blob. The returned {@link BlobReferenceScheme}
	 * can be used to retrieve the blob using
	 * {@link #retrieveAsBytes(BlobReferenceScheme)} or
	 * {@link #retrieveAsStream(BlobReferenceScheme)}
	 *
	 * @param bytes
	 *            The bytes to store
	 * @return A {@link BlobReferenceScheme} referencing the stored blob
	 * @throws StorageException
	 *             If the blob could not be stored, for instance if running out
	 *             of disk space
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes)
			throws StorageException;

	/**
	 * Store a blob read from an {@link InputStream}. The returned
	 * {@link BlobReferenceScheme} can be used to retrieve the blob using
	 * {@link #retrieveAsBytes(BlobReferenceScheme)} or
	 * {@link #retrieveAsStream(BlobReferenceScheme)}. The stream is not closed
	 * after storing.
	 *
	 * @param stream
	 *            The stream from where to read the data
	 * @return A {@link BlobReferenceScheme} referencing the stored blob
	 * @throws StorageException
	 *             If the blob could not be stored, for instance if running out
	 *             of disk space, or if the input <code>stream</code> could
	 *             not be read.
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream stream)
			throws StorageException;

}
