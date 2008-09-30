package net.sf.taverna.service.datastore.dao.jpa;

import java.util.ArrayList;
import java.util.List;

import javax.persistence.EntityManager;
import javax.persistence.NoResultException;
import javax.persistence.Query;

import org.apache.log4j.Logger;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.UserDAO;

public class UserDAOImpl extends GenericDaoImpl<User, String> implements UserDAO {
	
	private static Logger logger = Logger.getLogger(UserDAOImpl.class);

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
	
	public List<User> admins() {
		List<User> result = new ArrayList<User>();
		Query query = em.createNamedQuery(User.NAMED_QUERY_ADMINS);
		try {
			for (Object u : query.getResultList()) {
				result.add((User)u);
			}
			
		}
		catch(NoResultException ex) {
			logger.warn("No result found whilst looking for all admins",ex);
		}
		return result;
	}
}