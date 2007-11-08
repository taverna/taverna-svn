/**
 * CloudOne is the data layer designed for use by Taverna 2. Although this
 * is its primary purpose it is intended to be a generic and extensible
 * framework defining a lightweight peer to peer datagrid with explicit
 * handling and migration of data by reference.
 * <p>
 * The main entrance point is the
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataManager}, which can store and
 * retrieve {@link net.sf.taverna.t2.cloudone.entity.Entity}s identified
 * using {@link net.sf.taverna.t2.cloudone.identifier.EntityIdentifier}. There
 * are several implementations of DataManager, mainly
 * {@link net.sf.taverna.t2.cloudone.datamanager.memory.InMemoryDataManager}
 * and {@link net.sf.taverna.t2.cloudone.datamanager.file.FileDataManager}.
 * <p>
 * The DataManager also has a higher level interface
 * {@link net.sf.taverna.t2.cloudone.datamanager.DataFacade} that
 * is useful for converting back and forth between Java structures and
 * stored {@link net.sf.taverna.t2.cloudone.entity.Entity}s and blobs in
 * a {@link net.sf.taverna.t2.cloudone.datamanager.BlobStore}.
 */
package net.sf.taverna.t2.cloudone;

