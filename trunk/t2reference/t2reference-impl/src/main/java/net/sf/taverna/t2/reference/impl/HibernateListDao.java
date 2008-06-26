package net.sf.taverna.t2.reference.impl;

import net.sf.taverna.t2.reference.DaoException;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ListDao;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;
import net.sf.taverna.t2.reference.annotations.GetIdentifiedOperation;
import net.sf.taverna.t2.reference.annotations.PutIdentifiedOperation;

import org.springframework.orm.hibernate3.support.HibernateDaoSupport;

/**
 * An implementation of ListDao based on Spring's HibernateDaoSupport. To use
 * this in spring inject a property 'sessionFactory' with either a
 * {@link org.springframework.orm.hibernate3.LocalSessionFactoryBean LocalSessionFactoryBean}
 * or the equivalent class from the T2Platform module to add SPI based
 * implementation discovery and mapping. To use outside of Spring ensure you
 * call the setSessionFactory(..) method before using this (but really, use it
 * from Spring, so much easier).
 * 
 * @author Tom Oinn
 * 
 */
public class HibernateListDao extends HibernateDaoSupport implements ListDao {

	/**
	 * Fetch a t2reference list by id
	 * 
	 * @param ref
	 *            the T2Reference to fetch
	 * @return a retrieved identified list of T2 references
	 * @throws DaoException
	 *             if the supplied reference is of the wrong type or if
	 *             something goes wrong fetching the data or connecting to the
	 *             database
	 */
	@GetIdentifiedOperation
	public IdentifiedList<T2Reference> get(T2Reference ref) throws DaoException {
		if (ref == null) {
			throw new DaoException(
					"Supplied reference is null, can't retrieve.");
		} else if (ref.getReferenceType()
				.equals(T2ReferenceType.IdentifiedList) == false) {
			throw new DaoException(
					"This dao can only retrieve reference of type T2Reference.IdentifiedList");
		}
		if (ref instanceof T2ReferenceImpl) {
			try {
				return (T2ReferenceListImpl) getHibernateTemplate().get(
						T2ReferenceListImpl.class,
						((T2ReferenceImpl) ref).getCompactForm());
			} catch (Exception ex) {
				throw new DaoException(ex);
			}
		} else {
			throw new DaoException(
					"Reference must be an instance of T2ReferenceImpl");
		}
	}

	@PutIdentifiedOperation
	public void store(IdentifiedList<T2Reference> theList) throws DaoException {
		if (theList.getId() == null) {
			throw new DaoException("Supplied list set has a null ID, allocate "
					+ "an ID before calling the store method in the dao.");
		} else if (theList.getId().getReferenceType().equals(
				T2ReferenceType.IdentifiedList) == false) {
			throw new DaoException("Strangely the list ID doesn't have type "
					+ "T2ReferenceType.IdentifiedList, something has probably "
					+ "gone badly wrong somewhere earlier!");
		}
		if (theList instanceof T2ReferenceListImpl) {
			try {
				getHibernateTemplate().save(theList);
			} catch (Exception ex) {
				throw new DaoException(ex);
			}
		} else {
			throw new DaoException(
					"Supplied identifier list not an instance of T2ReferenceList");
		}
	}
}
