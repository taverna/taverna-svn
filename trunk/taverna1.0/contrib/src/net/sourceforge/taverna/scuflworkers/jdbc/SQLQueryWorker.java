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
 * This class executes prepared statements.
 * @author mfortner
 */
public class SQLQueryWorker implements LocalWorker {
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#execute(java.util.Map)
	 */
	public Map execute(Map inputMap) throws TaskExecutionException {
		DataThingAdapter inAdapter = new DataThingAdapter(inputMap);
		HashMap outputMap = new HashMap();
		DataThingAdapter outAdapter = new DataThingAdapter(outputMap);		
		
		// get and validate parameters
		String driverName = inAdapter.getString("driver");
		if (driverName == null || driverName.equals("")){
			throw new TaskExecutionException("The driver port cannot be null");
		}
		String url = inAdapter.getString("url");
		if (url == null || url.equals("")){
			throw new TaskExecutionException("The url port cannot be null");
		}
		String username = inAdapter.getString("username");
		if (username == null || username.equals("")){
			throw new TaskExecutionException("The username port cannot be null");
		}
		String password = inAdapter.getString("password");
		
		String provideXmlStr = inAdapter.getString("provideXml");
		boolean provideXml = (provideXmlStr != null)?Boolean.getBoolean(provideXmlStr):false;
		
		// get the sql statement parameters
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
	        for (int i=0; i < params.length; i++){
	        	ps.setObject(i+1, params[i]);
	        }
	        rs = ps.executeQuery();
	        ResultSetMetaData rsmd = rs.getMetaData();
	        
	        if (provideXml){
	            WebRowSet webrs = new WebRowSetImpl();
	            StringWriter sw = new StringWriter();
	            webrs.writeXml(rs, sw);
	            outAdapter.putString("xmlresults",sw.toString());
	        }
	        int numCols = rsmd.getColumnCount();
	        
	        
	        while(rs.next()){
	        	ArrayList row = new ArrayList(numCols);
	        	for (int i=0; i < numCols; i++){
	        		row.add(rs.getArray(i));
	        	}
	        	resultList.add(row);
	        }
	        
	    } catch (ClassNotFoundException e) {
	        throw new TaskExecutionException(e);
	    } catch (SQLException e) {
	        throw new TaskExecutionException(e);
	    } finally{
	    	try {
	    		rs.close();
	    		ps.close();
	    		connection.close();
	    	}catch (SQLException se){
	    		throw new TaskExecutionException(se);
	    	}
	    }

		//put results into hashmap
		outputMap.put("resultList",new DataThing(resultList));
		
		return outputMap;
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputNames()
	 */
	public String[] inputNames() {
		return new String[]{"url","driver","userid","password","sql","params","provideXml"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#inputTypes()
	 */
	public String[] inputTypes() {
		return new String[]{"'text/plain'","'text/plain'","'text/plain'","'text/plain'","'text/plain'","l('text/plain')","'text/plain'"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputNames()
	 */
	public String[] outputNames() {
		return new String[]{"resultList","xmlresults"};
	}
	/* (non-Javadoc)
	 * @see org.embl.ebi.escience.scuflworkers.java.LocalWorker#outputTypes()
	 */
	public String[] outputTypes() {
		return new String[]{"l(l('text/plain')","'text/plain'"};
	}
}
