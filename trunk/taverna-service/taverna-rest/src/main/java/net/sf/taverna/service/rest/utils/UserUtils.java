package net.sf.taverna.service.rest.utils;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;

import org.apache.log4j.Logger;

public class UserUtils {
	private static Logger logger = Logger.getLogger(UserUtils.class);
	
	public static String resetPassword(String username) {
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		User admin = userDAO.readByUsername(username);
		String password = User.generatePassword();
		boolean createUser = false;
		if (admin == null) {
			admin = new User(username);
			createUser = true;
		}
		admin.setPassword(password);
		if (createUser) {
			userDAO.create(admin);
		}
		DAOFactory.getFactory().commit();
		return password;
	}
	
	public static void makeAdmin(String username) {
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		User admin = userDAO.readByUsername(username);
		boolean createUser = false;
		if (createUser) {
			userDAO.create(admin);
		}
		admin.setAdmin(true);
		DAOFactory.getFactory().commit();
	}
	
}
