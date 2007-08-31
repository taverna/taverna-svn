package net.sf.taverna.t2.cloudone.datamanager.file;

import java.io.File;
import java.io.IOException;
import java.util.Set;
import java.util.UUID;

import net.sf.taverna.t2.cloudone.EntityRetrievalException;
import net.sf.taverna.t2.cloudone.EntityStorageException;
import net.sf.taverna.t2.cloudone.LocationalContext;
import net.sf.taverna.t2.cloudone.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.entity.DataDocument;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.util.EntitySerialiser;

import org.jdom.JDOMException;

public class FileDataManager extends AbstractDataManager {

	private File path;

	public FileDataManager(String namespace, Set<LocationalContext> contexts,
			File path) {
		super(namespace, contexts);
		if (path == null) {
			throw new NullPointerException("Path can't be null");
		}
		this.path = path;
	}

	protected String generateId(IDType type) {
		if (type.equals(IDType.Literal)) {
			throw new IllegalArgumentException("Can't generate IDs for Literal");
		}
		return "urn:t2data:" + type.uripart + "://" + getCurrentNamespace()
				+ "/" + UUID.randomUUID();
	}

	@Override
	protected <Bean> void storeEntity(Entity<?, Bean> entity)
			throws EntityStorageException {
		File entityPath = asPath(entity.getIdentifier());
		if (entityPath.exists()) {
			// Should not happen with our generateId(), but could happen in
			// a future p2p environment for external entities
			throw new IllegalStateException("Already exists: "
					+ entity.getIdentifier());
		}
		Bean bean = entity.getAsBean();
		entityPath.getParentFile().mkdirs();
		try {
			EntitySerialiser.toXMLFile(bean, entityPath);
		} catch (JDOMException e) {

		} catch (IOException e) {
			throw new EntityStorageException("Could not store entity to"
					+ entityPath, e);
		}
	}

	@SuppressWarnings("unchecked")
	@Override
	protected <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(ID id)
			throws EntityRetrievalException {
		File entityPath = asPath(id);
		if (!entityPath.isFile()) {
			return null;
		}
		Object bean;
		try {
			bean = EntitySerialiser.fromXMLFile(entityPath);
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
				throw new EntityRetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else if (id.getType().equals(IDType.List)) {
			EntityList entity = new EntityList();
			if (bean instanceof EntityListBean) {
				entity.setFromBean((EntityListBean) bean);
				return (Entity<ID, ?>) entity;
			} else {
				throw new EntityRetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else if (id.getType().equals(IDType.Error)) {
			ErrorDocument entity = new ErrorDocument();
			if (bean instanceof ErrorDocumentBean) {
				entity.setFromBean((ErrorDocumentBean) bean);
				return (Entity<ID, ?>) entity;
			} else {
				throw new EntityRetrievalException(
						"Data integrity failure, data type changed for "
								+ entityPath);
			}
		} else {
			throw new IllegalArgumentException("Data type not recognised for "
					+ entityPath);
		}
	}

	private File asPath(EntityIdentifier id) {
		String ns = id.getNamespace();
		String type = id.getType().uripart;
		String name = id.getName() + ".xml";
		if (! EntityIdentifier.isValidName(ns)
				|| ! EntityIdentifier.isValidName(name)) {
			throw new MalformedIdentifierException("Invalid identifier " + id);
		}

		// /path/ns/type/name
		File entityPath = new File(new File(new File(path, ns), type), name);
		return entityPath;
	}

}
