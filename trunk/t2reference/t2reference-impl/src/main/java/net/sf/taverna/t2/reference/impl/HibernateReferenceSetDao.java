package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.ReferenceSet;
import net.sf.taverna.t2.reference.ReferenceSetDao;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.h3.ReferenceSetImpl;
import net.sf.taverna.t2.reference.h3.ReferenceSetT2ReferenceImpl;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * An implementation of ReferenceSetDao based on Spring's HibernateDaoSupport.
 * To use this in spring inject a property 'sessionFactory' with either a
 * {@link org.springframework.orm.hibernate3.LocalSessionFactoryBean LocalSessionFactoryBean}
 * or the equivalent class from the T2Platform module to add SPI based
 * implementation discovery and mapping. To use outside of Spring ensure you
 * call the setSessionFactory(..) method before using this (but really, use it
 * from Spring, so much easier).
 * 
 * @author Tom Oinn
 * 
 */
public class HibernateReferenceSetDao extends HibernateDaoSupport implements
		ReferenceSetDao {

	/**
	 * Store the specified new reference set
	 * 
	 * @param rs
	 *            a reference set, must not already exist in the database.
	 * @throws DaoException
	 *             if the entry already exists in the database, if the supplied
	 *             reference set isn't an instance of ReferenceSetImpl or if
	 *             something else goes wrong connecting to the database
	 */
	public void store(ReferenceSet rs) throws DaoException {
		if (rs instanceof ReferenceSetImpl) {
			try {
				getHibernateTemplate().save(rs);
			} catch (Exception ex) {
				throw new DaoException(ex);
			}
		} else {
			throw new DaoException(
					"Supplied reference set not an instance of ReferenceSetImpl");
		}
	}

	/**
	 * Update a pre-existing entry in the database
	 * 
	 * @param rs
	 *            the reference set to update. This must already exist in the
	 *            database
	 * @throws DaoException
	 */
	public void update(ReferenceSet rs) throws DaoException {
		if (rs instanceof ReferenceSetImpl) {
			try {
				getHibernateTemplate().update(rs);
			} catch (Exception ex) {
				throw new DaoException(ex);
			}
		} else {
			throw new DaoException(
					"Supplied reference set not an instance of ReferenceSetImpl");
		}
	}

	/**
	 * Fetch a reference set by id
	 * 
	 * @param ref
	 *            the ReferenceSetT2ReferenceImpl to fetch
	 * @return a retrieved ReferenceSetImpl
	 * @throws DaoException
	 *             if the supplied reference is of the wrong type or if
	 *             something goes wrong fetching the data or connecting to the
	 *             database
	 */
	public ReferenceSetImpl get(T2Reference ref) throws DaoException {
		if (ref instanceof ReferenceSetT2ReferenceImpl) {
			try {
				return (ReferenceSetImpl) getHibernateTemplate().get(
						ReferenceSetImpl.class,
						(ReferenceSetT2ReferenceImpl) ref);
			} catch (Exception ex) {
				throw new DaoException(ex);
			}
		} else {
			throw new DaoException(
					"Reference must be an instance of ReferenceSetT2ReferenceImpl");
		}
	}
}
