package net.sf.taverna.service.rest.utils;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;

public class UserUtils {
	//private static Logger logger = Logger.getLogger(UserUtils.class);
	
	public static String resetPassword(String username) {
		String password = User.generatePassword();
		resetPassword(username, password);
		return password;
	}
	
	public static void resetPassword(String username, String password) {
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		User user = userDAO.readByUsername(username);
		
		boolean createUser = false;
		if (user == null) {
			user = new User(username);
			createUser = true;
		}
		user.setPassword(password);
		if (createUser) {
			userDAO.create(user);
		}
		DAOFactory.getFactory().commit();
	}
	
	public static void makeAdmin(String username) {
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		User admin = userDAO.readByUsername(username);
		admin.setAdmin(true);
		DAOFactory.getFactory().commit();
	}
	
}
