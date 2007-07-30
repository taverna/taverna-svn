package net.sf.taverna.service.datastore.dao.jpa;

import java.io.Serializable;
import java.util.Iterator;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.Query;

import net.sf.taverna.service.datastore.bean.AbstractBean;
import net.sf.taverna.service.datastore.dao.GenericDao;

import org.apache.log4j.Logger;

public abstract class GenericDaoImpl<Bean extends AbstractBean<PrimaryKey>, PrimaryKey extends Serializable>
	implements GenericDao<Bean, PrimaryKey> {

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

	public Bean refresh(Bean bean) {
		Bean newBean = em.merge(bean);
		// refresh is then not needed
		//em.refresh(newBean);
		return newBean;
	}

	@SuppressWarnings("unchecked")
	public List<Bean> all() {
		Query query;
		if (namedQueryAll() != null) {
			query = em.createNamedQuery(namedQueryAll());
		} else {
			if (logger.isDebugEnabled())
				logger.debug("Generating all-query for "
					+ beanType.getSimpleName());
			query =
				em.createQuery("SELECT o FROM " + beanType.getSimpleName()
					+ " o");
		}
		return query.getResultList();
	}

	public Iterator<Bean> iterator() {
		return all().iterator();
	}

}
