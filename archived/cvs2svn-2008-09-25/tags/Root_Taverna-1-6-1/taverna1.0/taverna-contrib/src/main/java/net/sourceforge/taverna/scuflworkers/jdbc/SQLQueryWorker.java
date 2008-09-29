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
import java.util.List;
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
 * @version $Revision: 1.4 $
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
	// Ports
	private static final String XMLRESULTS = "xmlresults";
	private static final String RESULT_LIST = "resultList";
	private static final String PROVIDE_XML = "provideXml";
	private static final String PARAMS = "params";
	private static final String SQL = "sql";
	private static final String PASSWORD = "password";
	private static final String USERID = "userid";
	private static final String DRIVER = "driver";
	private static final String URL = "url";

	// Mime types
	private static final String TEXT_PLAIN = "'text/plain'";
	private static final String L_TEXT_PLAIN = "l('text/plain')";
	private static final String L_L_TEXT_PLAIN = "l(l('text/plain'))";

	
	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map<String, DataThing> execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap<String, DataThing> outputMap = new HashMap<String, DataThing>();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);

		ResultSet rs = null;
		PreparedStatement ps = null;
		Connection connection = null;
		List<List<String>> resultList = new ArrayList<List<String>>();
		try {
			// get and validate parameters
			String driverName = inAdapter.getString(DRIVER);
			if (driverName == null || driverName.equals("")) {
				throw new TaskExecutionException("The '" + DRIVER + "' port cannot be empty");
			}
			String url = inAdapter.getString(URL);
			if (url == null || url.equals("")) {
				throw new TaskExecutionException("The '" + URL + "' port cannot be empty");
			}
			String username = inAdapter.getString(USERID);
			if (username == null || username.equals("")) {
				throw new TaskExecutionException("The '" + USERID + "' port cannot be empty");
			}
			String password = inAdapter.getString(PASSWORD);

			String provideXmlStr = inAdapter.getString(PROVIDE_XML);
			boolean provideXml = (provideXmlStr != null) ? Boolean.valueOf(provideXmlStr.toUpperCase()).booleanValue()
					: false;

			// get the sql statement parameters
			String[] params = inAdapter.getStringArray(PARAMS);
			
			String sql = inAdapter.getString(SQL);
			if (sql == null || sql.equals("")) {
				throw new TaskExecutionException("The '" + SQL + "' port cannot be empty");
			}

			// Load the JDBC driver
			Class.forName(driverName);

			// Create a connection to the database
			connection = DriverManager.getConnection(url, username, password);
			if (connection == null) {
				throw new TaskExecutionException("The connection was null");
			}

			ps = connection.prepareStatement(sql);

			// if they exist, bind the parameters to the prepared statement & execute it.
			if (params!=null) {
				for (int i = 0; i < params.length; i++) {
					ps.setObject(i + 1, params[i]);
				}
			}
			rs = ps.executeQuery();
			ResultSetMetaData rsmd = rs.getMetaData();

			// if the provideXml flag is set, convert the results into XML.
			if (provideXml) {
				WebRowSet webrs = new WebRowSetImpl();
				StringWriter sw = new StringWriter();
				webrs.writeXml(rs, sw);
				outAdapter.putString(XMLRESULTS, sw.toString());
			}
			int numCols = rsmd.getColumnCount();

			// put the results into the results list.
			while (rs.next()) {
				List<String> row = new ArrayList<String>(numCols);
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
		outputMap.put(RESULT_LIST, new DataThing(resultList));

		return outputMap;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[] { URL, DRIVER, USERID, PASSWORD, SQL, PARAMS, PROVIDE_XML };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[] { TEXT_PLAIN, TEXT_PLAIN, TEXT_PLAIN, TEXT_PLAIN, TEXT_PLAIN,
				L_TEXT_PLAIN, TEXT_PLAIN };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[] { RESULT_LIST, XMLRESULTS };
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[] { L_L_TEXT_PLAIN, TEXT_PLAIN };
	}
}