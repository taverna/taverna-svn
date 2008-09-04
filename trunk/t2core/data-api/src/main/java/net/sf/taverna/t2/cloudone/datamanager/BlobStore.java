/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.cloudone.datamanager;

import java.io.InputStream;
import java.util.Set;

import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.refscheme.BlobReferenceScheme;
import net.sf.taverna.t2.cloudone.refscheme.ReferenceScheme;

/**
 * <p>
 * A store for BLOBs, ie. binary data.
 * </p>
 * <p>
 * A BlobStore complements the {@link DataManager}, and notably provided by
 * {@link DataManager#getBlobStore()}, for storing raw data. The data is stored
 * in a blob store, and given a {@link BlobReferenceScheme} that can later by
 * registered as a {@link net.sf.taverna.t2.cloudone.entity.DataDocument} using
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
 * <p>
 * To store strings, either use {@link #storeFromString(String)} or provide the
 * character set using {@link #storeFromBytes(byte[], String)} or
 * {@link #storeFromStream(InputStream, String)}. {@link BlobReferenceScheme}s
 * stored with a reference scheme can be retrieved as Strings using
 * {@link #retrieveAsString(BlobReferenceScheme)}, while
 * {@link #retrieveAsString(BlobReferenceScheme, String)} can be used if the
 * character set is known externally.
 * </p>
 * 
 * @author Ian Dunlop
 * @author Stian Soiland
 * 
 */
public interface BlobStore {

	
	/**
	 * The key for {@link LocationalContext#getValue(String...)} to uniquely
	 * identify a BlobStore.
	 */
	public static final String LOCATIONAL_CONTEXT_KEY_UUID = "uuid";

	/**
	 * The {@link LocationalContext#getContextType()} for
	 * {@link LocationalContext}s returned by {@link #getLocationalContexts()}.
	 */
	public static final String LOCATIONAL_CONTEXT_TYPE = "BlobStore";

	/**
	 * Character set used when storing String with
	 * {@link #storeFromString(String)}.
	 */
	public final String STRING_CHARSET = "utf-8";

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
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
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
	 * Retrieve a blob as a String. The reference must have a non-null
	 * {@link ReferenceScheme#getCharset()}.
	 * 
	 * @see #retrieveAsString(BlobReferenceScheme, String)
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
	 * @return String decoded from blob
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 * @throws NotFoundException
	 *             If the blob didn't exist in BlobStore
	 * @throws IllegalArgumentException
	 *             If the reference didn't have a
	 *             {@link ReferenceScheme#getCharset()}
	 */
	public String retrieveAsString(BlobReferenceScheme<?> reference)
			throws RetrievalException, NotFoundException,
			IllegalArgumentException;

	/**
	 * Retrieve a blob as a String, decoding using specified character set.
	 * <p>
	 * Note that the provided character set will be used even if the reference
	 * already had a (possibly different) non-null
	 * {@link ReferenceScheme#getCharset()}. Use
	 * {@link #retrieveAsString(BlobReferenceScheme)} to decode using the
	 * reference's character set.
	 * 
	 * @see #retrieveAsString(BlobReferenceScheme)
	 * @param reference
	 *            {@link BlobReferenceScheme} referring to the blob
	 * @param charset
	 *            Character set to use for decoding blob
	 * @return String decoded from blob using specified charset
	 * @throws RetrievalException
	 *             If the {@link BlobStore}'s index could not be read
	 * @throws NotFoundException
	 *             If the blob didn't exist in BlobStore
	 */
	public String retrieveAsString(BlobReferenceScheme<?> reference,
			String charset) throws RetrievalException, NotFoundException,
			IllegalArgumentException;

	/**
	 * Store byte[] array as a blob. The returned {@link BlobReferenceScheme}
	 * can be used to retrieve the blob using
	 * {@link #retrieveAsBytes(BlobReferenceScheme)} or
	 * {@link #retrieveAsStream(BlobReferenceScheme)}
	 * 
	 * @param bytes
	 *            The bytes to store
	 * @param charset
	 *            The character set of the bytes
	 * @return A {@link BlobReferenceScheme} referencing the stored blob
	 * @throws StorageException
	 *             If the blob could not be stored, for instance if running out
	 *             of disk space
	 */
	public BlobReferenceScheme<?> storeFromBytes(byte[] bytes, String charset)
			throws StorageException;

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

	/**
	 * Store a blob read from an {@link InputStream}. The returned
	 * {@link BlobReferenceScheme} can be used to retrieve the blob using
	 * {@link #retrieveAsBytes(BlobReferenceScheme)} or
	 * {@link #retrieveAsStream(BlobReferenceScheme)}. The stream is not closed
	 * after storing.
	 * 
	 * @param stream
	 *            The stream from where to read the data
	 * @param charset
	 *            The character set of the input stream
	 * @return A {@link BlobReferenceScheme} referencing the stored blob
	 * @throws StorageException
	 *             If the blob could not be stored, for instance if running out
	 *             of disk space, or if the input <code>stream</code> could
	 *             not be read.
	 */
	public BlobReferenceScheme<?> storeFromStream(InputStream stream,
			String charset) throws StorageException;

	/**
	 * Store a blob from a String, encoded using character set UTF-8 ({@link #STRING_CHARSET}).
	 * The returned {@link BlobReferenceScheme} can be used to retrieve the blob
	 * using {@link #retrieveAsBytes(BlobReferenceScheme)} or
	 * {@link #retrieveAsStream(BlobReferenceScheme)} and its
	 * {@link ReferenceScheme#getCharset()} will be UTF-8.
	 * 
	 * @param string
	 *            The string to store
	 * @return A {@link BlobReferenceScheme} referencing the stored blob
	 * @throws StorageException
	 *             If the blob could not be stored, for instance if running out
	 *             of disk space, or if the input <code>stream</code> could
	 *             not be read.
	 */
	public BlobReferenceScheme<?> storeFromString(String string)
			throws StorageException;

	/**
	 * Get the set of BlobStore related {@link LocationalContext}s, normally
	 * with a {@link LocationalContext#getContextType()} of
	 * {@value #LOCATIONAL_CONTEXT_TYPE}. Used by
	 * {@link ReferenceScheme#validInContext(Set, DataPeer)}. Several
	 * {@link BlobStore} instances might share {@link LocationalContext}s if
	 * they can directly access each others blobs, for instance using a shared
	 * directory or database.
	 * 
	 * @return A {@link Set} of {@link LocationalContext} describing the blob
	 *         store
	 */
	public Set<LocationalContext> getLocationalContexts();

}
