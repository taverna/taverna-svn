package net.sf.taverna.platform.spring.jdbc;

import java.sql.Driver;
import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * Acts as the bridge to the anonymous temporary classloader used to register
 * the real JDBC drivers. The ProxyDriverManager uses this to mediate access to
 * that anonymous class world and proxy it with a DriverProxy
 * 
 * @author Tom Oinn
 */
public class ProxyHelper {

	/**
	 * Used to check whether we can 'see' the specified class.
	 * 
	 * @throws SecurityException
	 *             generally thrown if class loaded by a different class loader
	 *             from us
	 */
	public static ClassLoader getClassLoader(Class<?> theClass) {
		return theClass.getClassLoader();
	}

	/**
	 * Calls the DriverManager.getDrivers method from the temporary classloader
	 * environment.
	 * 
	 * @return an enumeration of JDBC drivers visible to the temporary
	 *         classloader
	 */
	public static java.util.Enumeration<Driver> getDrivers() {
		return DriverManager.getDrivers();
	}

	/**
	 * Calls DriverManager.deregisterDriver on the DriverManager from the
	 * temporary classloader environment, used along with deregistration of the
	 * DriverProxy
	 */
	public static void deregisterDriver(Driver driver) throws SQLException {
		DriverManager.deregisterDriver(driver);
	}
}
