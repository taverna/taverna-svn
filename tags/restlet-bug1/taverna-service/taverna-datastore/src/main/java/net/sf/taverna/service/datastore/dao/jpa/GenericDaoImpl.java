package net.sf.taverna.service.datastore.dao.jpa;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.dao.GenericDao;

public abstract class GenericDaoImpl<Bean, PrimaryKey extends Serializable> implements
	GenericDao<Bean, PrimaryKey> {
	
	private static Logger logger = Logger.getLogger(GenericDaoImpl.class);
	
	private Class<Bean> beanType;

	EntityManager em;
	
	public GenericDaoImpl(Class<Bean> type, EntityManager em) {
		beanType = type;
		this.em = em;
	}

	/**
	 * Name of named query used by {@link #all()}. If <code>null</code> is
	 * returned, a generic SELECT query will be generated on the fly.
	 * 
	 * @return Name of named query
	 */
	public String namedQueryAll() {
		return null;
	}
	
	public void create(Bean bean) {
		em.persist(bean);
	}

	public Bean read(PrimaryKey id) {
		return em.find(beanType, id);
	}

	public void update(Bean bean) {
		em.merge(bean);
	}

	public void delete(Bean bean) {
		em.remove(bean);
	}
	
	public void refresh(Bean bean) {
		em.refresh(bean);
	}

	@SuppressWarnings("unchecked")
	public List<Bean> all() {
		Query query;
		if (namedQueryAll() != null) {
			query = em.createNamedQuery(namedQueryAll());
		} else {
			logger.warn("Generating all-query for " + beanType.getSimpleName());
			// Should we warn about this?
			query = em.createQuery("SELECT o FROM " + 
				beanType.getSimpleName() + " o");
		}
		return query.getResultList();
	}

	public Iterator<Bean> iterator() {
		return all().iterator();
	}

}
