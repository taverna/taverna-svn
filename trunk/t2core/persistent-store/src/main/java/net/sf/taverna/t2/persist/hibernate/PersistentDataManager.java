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
package net.sf.taverna.t2.persist.hibernate;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import javax.persistence.EntityManager;
import javax.persistence.EntityManagerFactory;
import javax.persistence.EntityTransaction;
import javax.persistence.Persistence;
import javax.persistence.PersistenceContext;

import net.sf.taverna.t2.cloudone.bean.DataDocumentBean;
import net.sf.taverna.t2.cloudone.bean.EntityListBean;
import net.sf.taverna.t2.cloudone.bean.ErrorDocumentBean;
import net.sf.taverna.t2.cloudone.bean.ReferenceBean;
import net.sf.taverna.t2.cloudone.datamanager.AbstractDataManager;
import net.sf.taverna.t2.cloudone.datamanager.BlobStore;
import net.sf.taverna.t2.cloudone.datamanager.RetrievalException;
import net.sf.taverna.t2.cloudone.datamanager.StorageException;
import net.sf.taverna.t2.cloudone.entity.Entity;
import net.sf.taverna.t2.cloudone.entity.EntityList;
import net.sf.taverna.t2.cloudone.entity.ErrorDocument;
import net.sf.taverna.t2.cloudone.entity.impl.DataDocumentImpl;
import net.sf.taverna.t2.cloudone.identifier.EntityIdentifier;
import net.sf.taverna.t2.cloudone.identifier.IDType;
import net.sf.taverna.t2.cloudone.identifier.MalformedIdentifierException;
import net.sf.taverna.t2.cloudone.peer.LocationalContext;

import org.apache.log4j.Logger;

/**
 * A Hibernate based data manager for storing entities in a MySQL database.
 * Hibernate properties are read in from persistence.xml in META-INF. The
 * database connection properties are assigned programatically or from the
 * constructor. Should be used with a {@link PersistentBlobStore}
 * 
 * @author Ian Dunlop
 * 
 */
public class PersistentDataManager extends AbstractDataManager {
	private PersistentBlobStore blobStore;
	public static final String PERSISTENCE_UNIT = "hibernateDataManager";
	@PersistenceContext()
	public static EntityManagerFactory factory;
	private EntityManagerFactory entityManagerFactory;
	private EntityManager entityManager;
//	private String password;
//	private String databaseURL;
//	private String user;
	private boolean managerCreated = false;
	private static final int MAX_ID_LENGTH = 80;

	/**
	 * Creates the data manager without a hibernate entity manager (this is
	 * created when an entity is first stored (or retrieved))
	 * 
	 * @param namespace
	 * @param contexts
	 */
	public PersistentDataManager(String namespace,
			Set<LocationalContext> contexts) {
		super(namespace, contexts);
		// createEntityManagerFactory(PERSISTENCE_UNIT);
		// entityManager = factory.createEntityManager();

		// TODO Auto-generated constructor stub
	}

	/**
	 * Creates the data manager without a hibernate entity manager (this is
	 * created when an entity is first stored (or retrieved)). Additionally has
	 * the parameters used for database connection
	 * 
	 * @param namespace
	 * @param contexts
	 * @param password
	 * @param databaseURL
	 * @param user
	 */
//	public PersistentDataManager(String namespace,
//			Set<LocationalContext> contexts, String password,
//			String databaseURL, String user) {
//		super(namespace, contexts);
//		this.password = password;
//		this.databaseURL = databaseURL;
//		this.user = user;
//	}

	private void createEntityManager() throws Exception {
//		if (password == null || databaseURL == null || user == null) {
//			throw new Exception(
//					"Please ensure password, address & user have been set before proceeding");
//		} else {
		if (entityManagerFactory == null)
		{
			try {
//				Map<String, String> configOverrides = new HashMap<String, String>();
//				configOverrides.put("hibernate.connection.url", databaseURL);
//				configOverrides.put("hibernate.connection.username", user);
//				configOverrides.put("hibernate.connection.password", password);
				entityManagerFactory = Persistence.createEntityManagerFactory(
						PERSISTENCE_UNIT);
				entityManager = entityManagerFactory.createEntityManager();
			} catch (Exception e) {
				throw new Exception("Failed to create entity manager for data storage " + e);
			}
		}
//		}
	}

	@SuppressWarnings("unused")
	private static Logger logger = Logger
			.getLogger(PersistentDataManager.class);

	@Override
	protected String generateId(IDType type) {
		if (type.equals(IDType.Literal)) {
			throw new IllegalArgumentException("Can't generate IDs for Literal");
		}
		return "urn:t2data:" + type.uripart + "://" + getCurrentNamespace()
				+ "/" + UUID.randomUUID();
	}

	/**
	 * Retrieves entities from a database based on the entity identifier. Checks
	 * what type of entity you want from the identifier and recreates the bean
	 * and therefore the required entity
	 */
	@Override
	protected <ID extends EntityIdentifier> Entity<ID, ?> retrieveEntity(ID id)
			throws RetrievalException {
		if (!managerCreated) {
			try {
				createEntityManager();
				//managerCreated = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.warn(e);
			}
		}

		if (id.getType().equals(IDType.Data)) {
			DataDocumentBean bean = null;
			try {
				bean = (DataDocumentBean) entityManager.find(
						DataDocumentBean.class, id.toString());
			} catch (Exception e) {
				throw new RetrievalException("Could not retrieve " + id, e);
			}
			if (bean == null) {
				return null;
			}
			// entityManager.close();
			DataDocumentImpl dataDoc = new DataDocumentImpl();
			dataDoc.setFromBean(bean);
			return (Entity<ID, ?>) dataDoc;
		} else if (id.getType().equals(IDType.Error)) {
			ErrorDocumentBean bean = null;
			try {
				bean = (ErrorDocumentBean) entityManager.find(
						ErrorDocumentBean.class, id.toString());
			} catch (Exception e) {
				throw new RetrievalException("Could not retrieve " + id, e);
			}
			if (bean == null) {
				return null;
			}
			// entityManager.close();
			ErrorDocument errorDoc = new ErrorDocument();
			errorDoc.setFromBean(bean);
			return (Entity<ID, ?>) errorDoc;
		} else if (id.getType().equals(IDType.List)) {
			EntityListBean bean = null;
			try {
				bean = (EntityListBean) entityManager.find(
						EntityListBean.class, id.toString());
			} catch (Exception e) {
				throw new RetrievalException("Could not retrieve " + id, e);
			}
			if (bean == null) {
				return null;
			}
			// entityManager.close();
			EntityList list = new EntityList();
			list.setFromBean(bean);
			return (Entity<ID, ?>) list;
		} else {
			throw new MalformedIdentifierException(id
					+ " is not a recognised Entity Identifier");
		}
	}

	/**
	 * Works out the type of entity from the identifier and then stores the
	 * entity as its bean type
	 */
	@Override
	protected <Bean> void storeEntity(Entity<?, Bean> entity)
			throws StorageException {
		if (!managerCreated) {
			try {
				createEntityManager();
				//managerCreated = true;
			} catch (Exception e) {
				// TODO Auto-generated catch block
				logger.warn(e);
			}
		}
		if (entity.getIdentifier().getType().equals(IDType.Data)) {
			DataDocumentBean bean = (DataDocumentBean) entity.getAsBean();
			try {
				EntityTransaction transaction = entityManager.getTransaction();
				transaction.begin();
				for (ReferenceBean ref:bean.getReferences()) {
					entityManager.persist(ref);
				}
				entityManager.persist(bean);
				transaction.commit();
//				entityManager.flush();
			} catch (Exception e) {
				throw new StorageException("Could not store " + entity, e);
			}
			// entityManager.close();
		} else if (entity.getIdentifier().getType().equals(IDType.Error)) {
			ErrorDocumentBean bean = (ErrorDocumentBean) entity.getAsBean();
			try {
				EntityTransaction transaction = entityManager.getTransaction();
				transaction.begin();
				entityManager.persist(bean);
				transaction.commit();
//				entityManager.flush();
			} catch (Exception e) {
				throw new StorageException("Could not store " + entity, e);
			}
			// entityManager.close();
		} else if (entity.getIdentifier().getType().equals(IDType.List)) {
			EntityListBean bean = (EntityListBean) entity.getAsBean();
			try {
				EntityTransaction transaction = entityManager.getTransaction();
				transaction.begin();
				entityManager.persist(bean);
				transaction.commit();
//				entityManager.flush();
			} catch (Exception e) {
				throw new StorageException("Could not store " + entity, e);
			}
			// entityManager.close();
		} else if (entity.getIdentifier().getType().equals(IDType.Literal)) {
			// fail?
		} else {
			// fail
		}
	}

	/**
	 * Creates a new {@link PersistentBlobStore} if necessary and gives it to
	 * you
	 */
	public BlobStore getBlobStore() {
		if (blobStore == null) {
			blobStore = new PersistentBlobStore(getCurrentNamespace());
		}
		return blobStore;
	}

	/**
	 * The biggest size of string before it is stored as a blob
	 */
	public int getMaxIDLength() {
		// TODO Auto-generated method stub
		return MAX_ID_LENGTH;
	}

//	/**
//	 * The password used for the database connection
//	 * 
//	 * @return database connection password
//	 */
//	public String getPassword() {
//		return password;
//	}
//
//	/**
//	 * The password used for the database connection
//	 * 
//	 * @param password
//	 */
//	public void setPassword(String password) {
//		this.password = password;
//	}
//
//	/**
//	 * The location of the database
//	 * 
//	 * @return where the database is
//	 */
//	public String getDatabaseURL() {
//		return databaseURL;
//	}
//
//	/**
//	 * The location of the database eg. localhost
//	 * 
//	 * @param databaseURL
//	 */
//	public void setDatabaseURL(String databaseURL) {
//		this.databaseURL = databaseURL;
//	}
//
//	/**
//	 * The username to get write/read access to the database
//	 * 
//	 * @return who owns the database
//	 */
//	public String getUser() {
//		return user;
//	}
//
//	/**
//	 * The username to get write/read access to the database
//	 * 
//	 * @param user
//	 */
//	public void setUser(String user) {
//		this.user = user;
//	}

}
