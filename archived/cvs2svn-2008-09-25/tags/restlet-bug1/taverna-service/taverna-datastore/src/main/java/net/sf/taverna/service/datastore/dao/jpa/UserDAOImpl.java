package net.sf.taverna.service.datastore.dao.jpa;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.UserDAO;

public class UserDAOImpl extends GenericDaoImpl<User, String> implements UserDAO {

	public UserDAOImpl(EntityManager em) {
		super(User.class, em);
	}
	
	@Override
	public String namedQueryAll() {
		return User.NAMED_QUERY_ALL;
	}
	
	public User readByUsername(String username) {
		Query query = em.createNamedQuery(User.NAMED_QUERY_USER);
		query.setParameter("username", username);
		try {
			return (User) query.getSingleResult();
		} catch (NoResultException ex) {
			return null;
		}
	}	
}
