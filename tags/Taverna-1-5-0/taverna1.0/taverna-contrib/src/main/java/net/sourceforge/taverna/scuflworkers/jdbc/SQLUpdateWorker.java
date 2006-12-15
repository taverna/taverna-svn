package net.sourceforge.taverna.scuflworkers.jdbc;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * This processor is used to execute SQL update/insert statements.
 * 
 * @author Mark
 * @version $Revision: 1.2 $
 * 
 * @tavinput url The jdbc database URL.
 * @tavinput driver A fully qualified driver classname.
 * @tavinput userid The userid required for database access.
 * @tavinput password The password required for database access.
 * @tavinput sql The SQL statement to be executed.
 * @tavinput params A list of parameters that need to be bound to the query.
 * 
 * @tavinput resultList Returns "update successful".
 */
public class SQLUpdateWorker extends SQLQueryWorker implements LocalWorker {

	/**
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);

		// validate parameters
		String driverName = inAdapter.getString("driver");
		if (driverName == null || driverName.equals("")) {
			throw new TaskExecutionException("The driver port cannot be null");
		}
		String url = inAdapter.getString("url");
		if (url == null || url.equals("")) {
			throw new TaskExecutionException("The url port cannot be null");
		}
		String username = inAdapter.getString("username");
		if (username == null || username.equals("")) {
			throw new TaskExecutionException("The username port cannot be null");
		}
		String password = inAdapter.getString("password");

		String[] params = inAdapter.getStringArray("params");
		String sql = inAdapter.getString("sql");

		Connection connection = null;
		ArrayList resultList = new ArrayList();
		ResultSet rs = null;
		PreparedStatement ps = null;

		try {
			// Load the JDBC driver
			Class.forName(driverName);

			// Create a connection to the database
			connection = DriverManager.getConnection(url, username, password);

			ps = connection.prepareStatement(sql);

			// bind the parameters to the prepared statement
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			ps.executeUpdate();

		} catch (ClassNotFoundException e) {
			throw new TaskExecutionException(e);
		} catch (SQLException e) {
			throw new TaskExecutionException(e);
		} finally {
			try {
				rs.close();
				ps.close();
				connection.close();
			} catch (SQLException se) {
				throw new TaskExecutionException(se);
			}
		}

		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);
		// put results into hashmap
		outAdapter.putString("resultList", "update successful");

		return outputMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "url", "driver", "userid", "password", "sql", "params" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"l('text/plain')" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "resultList" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "'text/plain'" };
	}
}
