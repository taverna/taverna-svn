/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.store;

import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import java.sql.*;
import java.util.*;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import java.io.*;

/**
 * An implementation of the BaclavaDataService that backs its
 * data with a JDBC based relational database.
 * @author Tom Oinn
 */
public class JDBCBaclavaDataService implements BaclavaDataService {
    
    private String connectionURL, username, password;
    
    private List availableConnections, activeConnections;
    
    private Object connectionSetLockObject = new Object();
    private Object writeLockObject = new Object();

    private int maxConnections = 5;
    

    /**
     * Create a new JDBC backed BaclavaDataService with
     * properties taken from the current set of system
     * properties.
     */
    public JDBCBaclavaDataService() {
	this(System.getProperties());
    }
    
    
    /**
     * Create a new JDBC backed BaclavaDataService with
     * the supplied properties.
     */
    public JDBCBaclavaDataService(Properties props) {
	// Get the JDBC connection class
	try {
	    Class.forName(props.getProperty("taverna.datastore.jdbc.driver")).newInstance();
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	connectionURL = props.getProperty("taverna.datastore.jdbc.url");
	username = props.getProperty("taverna.datastore.jdbc.user");
	password = props.getProperty("taverna.datastore.jdbc.password");
	String optionalMaxConnections = props.getProperty("taverna.datastore.jdbc.pool.max");
	if (optionalMaxConnections != null) {
	    try {
		maxConnections = Integer.parseInt(optionalMaxConnections);
	    }
	    catch (NumberFormatException nfe) {
		//
	    }
	}
	availableConnections = new ArrayList();
	activeConnections = new ArrayList();
	createTables();
    }
    
    
    /**
     * Create the tables required if they're not already there
     */
    private void createTables() {
	Connection con = null;
	try {
	    con = getConnectionObject();
	    Statement st = con.createStatement();
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS datathings (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"+
			     "                         thing TEXT NOT NULL,"+
			     "                         PRIMARY KEY(id));");
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS lsids (lsid CHAR(200) NOT NULL UNIQUE,"+
			     "                    id INT UNSIGNED NOT NULL REFERENCES datathings(id),"+
			     "                    PRIMARY KEY(lsid));");
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	finally {
	    releaseConnection(con);
	}
    }


    /**
     * Drop and recreate the tables
     */
    public void reinit() {
	destroyTables();
	createTables();
    }


    /**
     * Nuke the tables
     */
    private void destroyTables() {
	Connection con = null;
	try {
	    con = getConnectionObject();
	    Statement st = con.createStatement();
	    st.executeUpdate("DROP TABLE IF EXISTS datathings");
	    st.executeUpdate("DROP TABLE IF EXISTS lsids");
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	}
	finally {
	    releaseConnection(con);
	}
    }

    
    /**
     * Store the specified data object
     */
    public void storeDataThing(DataThing theDataThing, boolean silent)
	throws DuplicateLSIDException {	    
	// Get the string version of the DataThing object
	// from the XML factory
	Document doc = new Document(DataThingXMLFactory.getElement(theDataThing));
	XMLOutputter xo = new XMLOutputter();
	xo.setIndent("  ");
	xo.setNewlines(true);
	String xmlRepresentation = xo.outputString(doc);
	// Find all the LSIDs that this document contains
	String[] lsids = theDataThing.getAllLSIDs();
	// Obtain a connection
	Connection con = getConnectionObject();
	try {
	    synchronized(writeLockObject) {
		PreparedStatement st = con.prepareStatement("INSERT INTO datathings (thing) VALUES (?)");
		st.setString(1,xmlRepresentation);
		st.executeUpdate();	    
		st.close();
		//System.out.println("Stored string : \n"+xmlRepresentation);
		PreparedStatement st2 = con.prepareStatement("INSERT INTO lsids (lsid, id) VALUES (?,LAST_INSERT_ID())");
		for (int i = 0; i < lsids.length; i++) {
		    st2.setString(1, lsids[i]);
		    st2.executeUpdate();
		    //System.out.println("Stored LSID mapping for LSID = "+lsids[i]);
		}
		st2.close();
		con.commit();
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    try {
		con.rollback();
	    }
	    catch (SQLException sqle) {
		sqle.printStackTrace();
	    }
	}
	finally {
	    releaseConnection(con);
	}	
    }

    
    /**
     * Fetch a DataThing from the given LSID
     */
    public DataThing fetchDataThing(String LSID)
	throws NoSuchLSIDException {
	Connection con = null;
	try {
	    con = getConnectionObject();
	    PreparedStatement p = con.prepareStatement("SELECT t.thing FROM datathings t, lsids l WHERE t.id=l.id AND l.lsid=?");
	    p.setString(1, LSID);
	    ResultSet rs = p.executeQuery();
	    String thingAsXML = null;
	    if (rs.first() ==true) {
		thingAsXML = rs.getString("thing");
	    }
	    else {
		throw new NoSuchLSIDException();
	    }
	    // Parse the XML and get the DataThing that was
	    // originally submitted, although we may have
	    // to split this down to actually get the desired
	    // LSID value out
	    //System.out.println("Found a data thing as XML : \n\n"+thingAsXML);
	    SAXBuilder builder = new SAXBuilder(false);
	    Document doc = builder.build(new StringReader(thingAsXML));
	    DataThing theThing = new DataThing(doc.getRootElement());
	    //System.out.println(theThing);
	    // Was the LSID for the dataThing itself?
	    //System.out.println("LSID to find is "+LSID);
	    Object o = theThing.getDataObjectWithLSID(LSID);
	    if (o == null) {
		throw new NoSuchLSIDException();
	    }
	    if (o == theThing) {
		return theThing;
	    }
	    else {
		// Have to split the thing down and return the subthing
		return theThing.extractChild(o);
	    }
	}
	catch (SQLException sqle) {
	    sqle.printStackTrace();
	}
	catch (JDOMException jde) {
	    jde.printStackTrace();
	}
	finally {
	    releaseConnection(con);
	}
	throw new NoSuchLSIDException();
    }

    
    /**
     * Does the given LSID exist in a concrete form?
     */
    public boolean hasData(String LSID) {
	Connection con = null;
	try {
	    con = getConnectionObject();
	    PreparedStatement p = con.prepareStatement("SELECT id FROM lsids WHERE lsid=?");
	    p.setString(1,LSID);
	    ResultSet rs = p.executeQuery();
	    return rs.first();
	}
	catch (Exception ex) {
	    //
	}
	finally {
	    releaseConnection(con);
	}
	return false;
    }

    
    /**
     * Does the given LSID have associated metadata?
     * Always returns false at present.
     */
    public boolean hasMetadata(String LSID) {
	return false;
    }


    /**
     * Obtain a connection object for use by methods within
     * this class
     */
    private Connection getConnectionObject() {
	// Is there a free connection?
	try {
	    return testAndGrabConnectionObject();
	}
	catch (Exception ex) {
	    // No free connections, can we make one?
	    if (activeConnections.size()<=maxConnections) {
		return createNewConnectionObject();
	    }
	    else {
		while (true) {
		    try {
			Thread.sleep(1000);
		    }
		    catch (InterruptedException ie) {
			//
		    }
		    try {
			return testAndGrabConnectionObject();
		    }
		    catch (Exception ex2) {
			//
		    }
		}
	    }
	}
    }
    private Connection testAndGrabConnectionObject() throws Exception {
	synchronized(connectionSetLockObject) {
	    if (availableConnections.isEmpty() == false) {
		Connection theConnection = (Connection)availableConnections.remove(0);
		activeConnections.add(theConnection);
		return theConnection;
	    }
	    else {
		throw new Exception();
	    }
	}
    }
    private Connection createNewConnectionObject() {
	synchronized(connectionSetLockObject) {
	    try {
		Connection theConnection =  DriverManager.getConnection(connectionURL,
									username,
									password);
		theConnection.setAutoCommit(false);
		return theConnection;
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
		// Should handle exceptions from jdbc here
		return null;
	    }
	}
    }
    /**
     * Return a connection back to the pool
     */
    private void releaseConnection(Connection con) {
	synchronized(connectionSetLockObject) {
	    if (con!=null && activeConnections.contains(con) && availableConnections.contains(con)==false) {
		activeConnections.remove(con);
		availableConnections.add(con);
	    }
	}
    }

}
