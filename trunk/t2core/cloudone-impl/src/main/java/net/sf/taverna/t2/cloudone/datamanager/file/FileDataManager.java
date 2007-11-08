package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.bean.Beanable;
import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.DataManager;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;
import net.sf.taverna.t2.cloudone.util.BeanSerialiser;

import org.jdom.JDOMException;

/**
 * File based {@link DataManager}. Entities are stored in a directory
 * structure:
 *
 * <pre>
 * 	namespace1/
 * 		ddoc/
 *      	65/
 * 				651375b7-8ce1-4d05-95ed-7b4912a50d0c.xml
 * 				6571b8eb-0a5a-49d9-a7cc-d5f7f64f29b1.xml
 * 			97/
 * 				973ab8eb-0a5a-49d9-a7cc-d5f7f64f29b1.xml
 * 		error/
 * 			9e/
 * 				9e190835-ea2e-45ae-a28d-20ac781e2ede.xml
 * 			6f/
 * 				6f02c8ac-04a3-4d45-8a35-45a59cd2da83.xml
 * 		list/
 * 			25/
 * 				2549b0a5-d70a-4630-9345-ca33b045b4cd.xml
 * 			52/
 * 				523d00b6-0294-455e-8638-1c0a3962e7cd.xml
 * 	namespace2/
 * 		ddoc/
 * 			49/
 * 				49d725d5-0b8f-4572-a804-160a9df690a4.xml
 * 		blob/
 * 			0b/
 * 				0b8f4572-49d7-25d5-160a-a8049df690a4.blob
 * </pre>
 *
 * <p>
 * The {@link UUID} is set in {@link EntityIdentifier#getName()}, and the
 * entities are separated into different directories depending on their type.
 * </p>
 * <p>
 * Entities themselves, or more correctly, their beans as returned by
 * {@link Beanable#getAsBean()}, are serialised by {@link BeanSerialiser}.
 * On deserialisation the entities are reconstructed from their bean.
 * </p>
 *
 * @author Ian Dunlop
 * @author Stian Soiland
 *
 */
public class FileDataManager extends AbstractDataManager {

	private static final int MAX_ID_LENGTH = 80; // for debug reasons

	private File path;

	private FileBlobStore blobStore;

	/**
	 * Construct a FileDataManager for a given namespace.
	 *
	 * @param namespace
	 *            The namespace of assigned identifiers
	 * @param contexts
	 *            Contexts this {@link DataManager} understands. (Currently
	 *            ignored)
	 * @param path
	 *            The root of the repository where the FileDataManager can store
	 *            its data
	 */
	public FileDataManager(String namespace, Set<LocationalContext> contexts,
			File path) {
		super(namespace, contexts);

		if (path == null) {
			throw new NullPointerException("Path can't be null");
		}
		this.path = path;
	}

	/**
	 * Get a {@link BlobStore} with the same file-based store as this
	 * FileDataManager.
	 */
	public FileBlobStore getBlobStore() {
		if (blobStore == null) {
			blobStore = new FileBlobStore(getCurrentNamespace(), path);
		}
		return blobStore;
	}

	public int getMaxIDLength() {
		return MAX_ID_LENGTH;
	}

	private File asPath(EntityIdentifier id) {
		String ns = id.getNamespace();
		String type = id.getType().uripart;
		String name = id.getName() + ".xml";
		if (!EntityIdentifier.isValidName(name)) {
			// TODO: Should escape weird names instead of failing
			// (typically entities coming over p2p)
			throw new MalformedIdentifierException("Unsupported identifier "
					+ id);
		}

		// /path/ns/type/na/name
		File entityPath = new File(parentDirectory(ns, type, name), name);
		return entityPath;
	}

	/**
	 * Find directory for a given namespace.
	 *
	 * @param namespace
	 *            Namespace which directory to find
	 * @return Directory for namespace
	 */
	private File namespaceDir(String namespace) {
		return new File(path, namespace);
	}

	/**
	 * Find the subdirectory for the start of the identifier name. For instance
	 * eg with name "651375b7-8ce1-4d05-95ed-7b4912a50d0c" would find "a3/"
	 *
	 * @param namespace
	 *            Namespace containing identifier
	 * @param type
	 *            Type of identifier
	 * @param name
	 *            Name of identifier (normally an UUID)
	 * @return Directory for identifier
	 */
	private File parentDirectory(String namespace, String type, String name) {
		String parentName = name.substring(0, 2);
		return new File(typeDir(namespace, type), parentName);
	}

	/**
	 * Find directory for a given type within a given namespace.
	 *
	 * @param ns
	 * @param type
	 * @return
	 */
	private File typeDir(String ns, String type) {
		return new File(namespaceDir(ns), type);
	}

	@Override
	protected String generateId(IDType type) {
		if (type.equals(IDType.Literal)) {
			throw new IllegalArgumentException("Can't generate IDs for Literal");
		}
		return "urn:t2data:" + type.uripart + "://" + getCurrentNamespace()
				+ "/" + UUID.randomUUID();
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(ID id)
			throws RetrievalException {
		File entityPath = asPath(id);
		if (!entityPath.isFile()) {
			return null;
		}
		Object bean;
		try {
			bean = BeanSerialiser.fromXMLFile(entityPath, getClass().getClassLoader());
		} catch (JDOMException e) {
			e.printStackTrace();
			return null;
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}

		if (id.getType().equals(IDType.Data)) {
			DataDocument entity = new DataDocumentImpl();
			if (bean instanceof DataDocumentBean) {
				entity.setFromBean((DataDocumentBean) bean);
				return (Entity<ID, ?>) entity;
			} else {
				throw new RetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else if (id.getType().equals(IDType.List)) {
			EntityList entity = new EntityList();
			if (bean instanceof EntityListBean) {
				entity.setFromBean((EntityListBean) bean);
				return (Entity<ID, ?>) entity;
			} else {
				throw new RetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else if (id.getType().equals(IDType.Error)) {
			ErrorDocument entity = new ErrorDocument();
			if (bean instanceof ErrorDocumentBean) {
				entity.setFromBean((ErrorDocumentBean) bean);
				return (Entity<ID, ?>) entity;
			} else {
				throw new RetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else {
			throw new IllegalArgumentException("Data type not recognised for "
					+ entityPath);
		}
	}

	@Override
	protected <Bean> void storeEntity(Entity<?, Bean> entity)
			throws StorageException {
		File entityPath = asPath(entity.getIdentifier());
		if (entityPath.exists()) {
			// Should not happen with our generateId(), but could happen in
			// a future p2p environment for external entities
			throw new IllegalStateException("Already exists: "
					+ entity.getIdentifier());
		}
		Bean bean = entity.getAsBean();
		entityPath.getParentFile().mkdirs();
		// TODO: Could serialise in a more portable and less space-hungry
		// format
		try {
			BeanSerialiser.toXMLFile(bean, entityPath);
		} catch (IOException e) {
			throw new StorageException(
					"Could not store entity to" + entityPath, e);
		}
	}

}
