package net.sf.taverna.service.datastore;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;
import static org.junit.Assert.assertTrue;

import java.util.Date;
import java.util.List;

import net.sf.taverna.service.datastore.bean.User;
import net.sf.taverna.service.datastore.dao.UserDAO;
import net.sf.taverna.service.test.TestDAO;

import org.junit.Test;

public class TestUser extends TestDAO {
	private static String lastUser;
	
	
	@Test
	public void createUser() {
		UserDAO userDao = daoFactory.getUserDAO();
		User user = new User();
		user.setPassword(User.generatePassword());
		userDao.create(user);
		daoFactory.commit();
		lastUser = user.getId();
	}
	
	@Test
	public void getUser() {
		if (lastUser == null) {
			createUser();
		}
		assertNotNull(lastUser);
		UserDAO userDao = daoFactory.getUserDAO();
		User user = userDao.read(lastUser);
		assertFalse("User has modified date in the future",
			user.getLastModified().after(new Date()));
		assertEquals(lastUser, user.getId());
		User otherUser = userDao.readByUsername(user.getUsername());
		assertSame(user, otherUser);
	}
	
	@Test
	public void emptyPassword() {
		if (lastUser == null) {
			createUser();
		}
		UserDAO userDao = daoFactory.getUserDAO();
		User user = userDao.read(lastUser);
		assertFalse(user.checkPassword(""));
		user.setPassword("");
		userDao.update(user);
		daoFactory.commit();
		userDao = daoFactory.getUserDAO();
		userDao.update(user);
		assertTrue(user.checkPassword(""));
		assertFalse(user.checkPassword("somethingelse"));
		user.setPassword("somethingelse");
		assertFalse(user.checkPassword(""));
		assertTrue(user.checkPassword("somethingelse"));
		daoFactory.commit();
	}
	
	@Test
	public void generatePassword() {
		UserDAO userDao = daoFactory.getUserDAO();
		User user = new User();
		String pw = User.generatePassword();
		user.setPassword(pw);
		userDao.create(user);
		daoFactory.commit();
		assertTrue(user.checkPassword(pw));
	}
	
	@Test
	public void getAdmins() {
		UserDAO userDao = daoFactory.getUserDAO();
		List<User> all = userDao.all();

		int origAdmins = userDao.admins().size();
		User u = new User();
		u.setPassword(User.generatePassword());
		u.setAdmin(true);
		userDao.create(u);
		assertEquals("There should be 1 extra admin present", origAdmins + 1,
			userDao.admins().size());

		User u2 = new User();
		u2.setPassword(User.generatePassword());
		u2.setAdmin(true);
		userDao.create(u2);
		assertEquals("There should be 2 extra admins present", origAdmins + 2,
			userDao.admins().size());

		u = userDao.reread(u);
		u2 = userDao.reread(u2);
		userDao.delete(u);
		userDao.delete(u2);
		assertEquals("There should be no extra admins present", origAdmins,
			userDao.admins().size());
	}
	
}
