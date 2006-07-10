package net.sourceforge.taverna.scuflworkers.jdbc;

import java.io.StringWriter;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import javax.sql.rowset.WebRowSet;

import net.sourceforge.taverna.baclava.DataThingAdapter;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scuflworkers.java.LocalWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

import com.sun.rowset.WebRowSetImpl;

/**
 * This processor executes SQL prepared statements, and returns the results as
 * an array of arrays. It can also, optionally generate an XML representation of
 * the results.
 * 
 * @author mfortner
 * @version $Revision: 1.2 $
 * 
 * @tavinput url The jdbc database URL.
 * @tavinput driver A fully qualified driver classname.
 * @tavinput userid The userid required for database access.
 * @tavinput password The password required for database access.
 * @tavinput sql The SQL statement to be executed.
 * @tavinput params A list of parameters that need to be bound to the query.
 * @tavinput provideXml If set to "true", generate an XML representation of the
 *           results. Defaults to "false".
 * 
 * @tavinput resultList An array of arrays, containing the results.
 * @tavinput xmlresults An XML representation of the results.
 */
public class SQLQueryWorker implements LocalWorker {
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection connection = null;
		ArrayList resultList = new ArrayList();
		try {
			// get and validate parameters
			String driverName = inAdapter.getString("driver");
			if (driverName == null || driverName.equals("")) {
				throw new TaskExecutionException("The 'driver' port cannot be null");
			}
			String url = inAdapter.getString("url");
			if (url == null || url.equals("")) {
				throw new TaskExecutionException("The 'url' port cannot be null");
			}
			String username = inAdapter.getString("userid");
			if (username == null || username.equals("")) {
				throw new TaskExecutionException("The 'userid' port cannot be null");
			}
			String password = inAdapter.getString("password");

			String provideXmlStr = inAdapter.getString("provideXml");
			boolean provideXml = (provideXmlStr != null) ? Boolean.valueOf(provideXmlStr.toUpperCase()).booleanValue()
					: false;

			// get the sql statement parameters
			String[] params = inAdapter.getStringArray("params");
			String sql = inAdapter.getString("sql");
			if (sql == null || sql.equals("")) {
				throw new TaskExecutionException("The 'sql' port cannot be null");
			}

			// Load the JDBC driver
			Class.forName(driverName);

			// Create a connection to the database
			connection = DriverManager.getConnection(url, username, password);
			if (connection == null) {
				throw new TaskExecutionException("The connection was null");
			}

			ps = connection.prepareStatement(sql);

			// bind the parameters to the prepared statement & execute it.
			for (int i = 0; i < params.length; i++) {
				ps.setObject(i + 1, params[i]);
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			// if the provideXml flag is set, convert the results into XML.
			if (provideXml) {
				WebRowSet webrs = new WebRowSetImpl();
				StringWriter sw = new StringWriter();
				webrs.writeXml(rs, sw);
				outAdapter.putString("xmlresults", sw.toString());
			}
			int numCols = rsmd.getColumnCount();

			// put the results into the results list.
			while (rs.next()) {
				ArrayList row = new ArrayList(numCols);
				for (int i = 0; i < numCols; i++) {
					row.add(rs.getString(i + 1));
				}
				resultList.add(row);
			}

		} catch (ClassNotFoundException e) {
			throw new TaskExecutionException(e);
		} catch (SQLException e) {
			throw new TaskExecutionException(e);
		} catch (Exception ex) {
			throw new TaskExecutionException(ex);
		} finally {

			try {
				if (rs != null) {
					rs.close();
				}
				if (ps != null) {
					ps.close();
				}
				if (connection != null) {
					connection.close();
				}
			} catch (SQLException se) {
				throw new TaskExecutionException(se);
			}
		}

		// put results into hashmap
		outputMap.put("resultList", new DataThing(resultList));

		return outputMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { "url", "driver", "userid", "password", "sql", "params", "provideXml" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'", "'text/plain'",
				"l('text/plain')", "'text/plain'" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { "resultList", "xmlresults" };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { "l(l('text/plain'))", "'text/plain'" };
	}
}