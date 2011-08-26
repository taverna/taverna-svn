package net.sf.taverna.service.rest.resources.util;

import net.sf.taverna.service.datastore.dao.DAOFactory;
import net.sf.taverna.service.datastore.dao.UserDAO;

public class UserDetailsValidator {
	public static void validate(String name, String password, String confirm,
		String email) throws UserValidationException {
		if (name == null)
			throw new UserValidationException("You must provide a username");
		if (!isAlphaNumeric(name))
			throw new UserValidationException(
				"The username must contain only alpha numeric characters and no spaces");
		validate(password, confirm, email);
		UserDAO userDAO = DAOFactory.getFactory().getUserDAO();
		if (userDAO.readByUsername(name) != null)
			throw new UserValidationException("The username '" + name
				+ "' has already been taken.");
	}

	public static void validate(String password, String confirm, String email)
		throws UserValidationException {
		if (password == null)
			throw new UserValidationException("You must provide a password");
		if (confirm == null)
			throw new UserValidationException("You must confirm the passowrd");
		if (password.length() < 5)
			throw new UserValidationException(
				"The password must be at least 5 characters long");
		if (!password.equals(confirm))
			throw new UserValidationException(
				"The confirmation password does not match");
		validateEmail(email);
	}

	public static void validateEmail(String email)
		throws UserValidationException {
		if (email == null)
			throw new UserValidationException(
				"You must provide a valid email address");
		if (!email.contains("@"))
			throw new UserValidationException("The email address is invalid");
	}

	private static boolean isAlphaNumeric(String str) {
		for (char c : str.toCharArray()) {
			if (!Character.isLetterOrDigit(c))
				return false;
		}
		return true;
	}
}
