package net.sf.taverna.service.datastore.dao;

import net.sf.taverna.service.datastore.bean.User;

public interface UserDAO extends GenericDao<User, String> {

	public User readByUsername(String userName);
	
}
