/**
 * 
 */
package net.sf.taverna.t2.lineageService.util;

import java.sql.DriverManager;
import java.sql.SQLException;

/**
 * @author paolo
 *
 */
public class DBconnections {

	/**
	 * connection parameters hardcoded for testing
	 * @return a connection to the provenance DB
	 * @throws ClassNotFoundException 
	 * @throws IllegalAccessException 
	 * @throws InstantiationException 
	 * @throws SQLException 
	 */
	public java.sql.Connection openConnection() throws InstantiationException, IllegalAccessException, ClassNotFoundException, SQLException {

		//		mysql on rpc264
		String DB_URL_RPC264 = "jdbc:mysql://rpc264.cs.man.ac.uk/T2Provenance?autoReconnect=true";  // URL of database server
		String DB_URL_LOCAL = "jdbc:mysql://localhost/T2Provenance?autoReconnect=true";  // URL of database server
		String DB_USER = "paolo";                        // database user id
		String DB_PASSWD = "riccardino";                          // database password

		Class.forName("com.mysql.jdbc.Driver").newInstance();

		System.out.println("opening DB connection to "+DB_URL_LOCAL);

		return  DriverManager.getConnection(DB_URL_LOCAL, DB_USER, DB_PASSWD);
	}


	
}
