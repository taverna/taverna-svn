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
import org.apache.log4j.*;

/**
 * An implementation of the BaclavaDataService that backs its
 * data with a JDBC based relational database.
 * @author Tom Oinn
 */
public class JDBCBaclavaDataService implements BaclavaDataService, LSIDProvider {
    
    private String connectionURL, username, password, defaultAuthority, instanceID;
    private JDBCConnectionPool pool;
    
    private Object connectionSetLockObject = new Object();
    private Object writeLockObject = new Object();

    private int maxConnections = 5;
    
    static Logger log = Logger.getLogger(JDBCBaclavaDataService.class.getName());

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
	// The default authority string to use for the lsid provider
	defaultAuthority = props.getProperty("taverna.lsid.providerauthority");
	if (defaultAuthority == null) {
	    defaultAuthority = "net.sf.taverna";
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
	String object_id = super.toString();
	StringTokenizer st = new StringTokenizer(object_id,"@",false);
	while (st.hasMoreTokens()) {
	    instanceID = st.nextToken();
	}
	pool = new JDBCConnectionPool(props.getProperty("taverna.datastore.jdbc.driver"),
				      connectionURL,
				      username,
				      password,
				      maxConnections);
	createTables();
    }
    
    
    /**
     * Create the tables required if they're not already there
     */
    private void createTables() {
	Connection con = null;
	try {
	    con = pool.borrowConnection();
	    Statement st = con.createStatement();
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"+
			     "                                     rdfstring TEXT NOT NULL,"+
			     "                                     PRIMARY KEY(id)) TYPE = InnoDB;");
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS lsid2metadata (lsid CHAR(200) NOT NULL,"+
			     "                                          id INT UNSIGNED NOT NULL REFERENCES metadata(id)"+
			     "                                          ) TYPE = InnoDB;");
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS datathings (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"+
			     "                                       thing TEXT NOT NULL,"+
			     "                                       PRIMARY KEY(id)) TYPE = InnoDB;");
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS lsid2datathings (lsid CHAR(200) NOT NULL UNIQUE,"+
			     "                    id INT UNSIGNED NOT NULL REFERENCES datathings(id),"+
			     "                    reftype ENUM(\"datathing\",\"collection\",\"leaf\"),"+
			     "                    mimetype CHAR(200),"+
			     "                    PRIMARY KEY(lsid)) TYPE = InnoDB;");
	    st.executeUpdate("CREATE TABLE IF NOT EXISTS idcounter (count INT UNSIGNED NOT NULL) TYPE = InnoDB;");
	    
	    pool.returnConnection(con);
	}	
	catch (Exception ex) {
	    pool.returnConnection(con);
	    ex.printStackTrace();
	}
	finally {
	    //
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
	    log.debug("Dropping tables...");
	    con = pool.borrowConnection();

	    Statement st = con.createStatement();
	    st.executeUpdate("DROP TABLE IF EXISTS datathings");
	    st.executeUpdate("DROP TABLE IF EXISTS lsid2datathings");
	    st.executeUpdate("DROP TABLE IF EXISTS metadata");
	    st.executeUpdate("DROP TABLE IF EXISTS lsid2metadata");
	    st.executeUpdate("DROP TABLE IF EXISTS idcounter");
	
	    pool.returnConnection(con);
	    log.debug("...finished dropping tables");
	}
	catch (Exception ex) {
	    pool.returnConnection(con);
	    ex.printStackTrace();
	}
	finally {
	    //
	}
    }


    /**
     * Implement LSIDProvider
     */
    public String getID(String namespace) {
	String prefix = "urn:lsid:"+defaultAuthority+":"+namespace+":";
	// Fetch the next value from the counter
	Connection con = pool.borrowConnection();
	try {
	    Statement s = con.createStatement();
	    //s.executeUpdate("LOCK TABLES idcounter WRITE");
	    s.executeUpdate("UPDATE idcounter SET count = count + 1");
	    ResultSet rs = s.executeQuery("SELECT count FROM idcounter");
	    String suffix = "1";
	    if (rs.first()) {
		// Resultset contained a result so return it
		suffix = rs.getString("count");
	    }
	    else {
		s.executeUpdate("INSERT INTO idcounter (count) VALUES (1)");
	    }
	    rs.close();
	    //s.executeUpdate("UNLOCK TABLES");
	    s.close();
	    con.commit();
	    pool.returnConnection(con);
	    return prefix+suffix+instanceID;
	}
	catch (SQLException sqle) {
	    sqle.printStackTrace();
	    try {
		con.rollback();
		pool.returnConnection(con);
	    }
	    catch (SQLException sqle2) {
		pool.returnConnection(con);
	    }
	    return "NO_IDENTIFIER_ASSIGNED";
	}
	finally {
	    //
	}
    }


    /**
     * Store some metadata
     */
    public void storeMetadata(String theMetadata) {
	// First decompose the metadata string to find
	// any LSIDs
	String[] split = theMetadata.split("\"");
	List lsidList = new ArrayList();
	for (int i = 0; i < split.length; i++) {
	    if (split[i].startsWith("urn:lsid:")) {
		lsidList.add(split[i]);
	    }
	}
	if (lsidList.isEmpty()) {
	    // No LSID references found within the metadata
	    // so it's not particularly interesting to us
	    return;
	}
	Connection con = pool.borrowConnection();
	try {
	    synchronized(writeLockObject) {
		PreparedStatement st = con.prepareStatement("INSERT INTO metadata (rdfstring) VALUES (?)");
		st.setString(1,theMetadata);
		st.executeUpdate();
		st.close();
		PreparedStatement st2 = con.prepareStatement("INSERT INTO lsid2metadata (lsid, id) VALUES (?,LAST_INSERT_ID())");
		for (Iterator i = lsidList.iterator(); i.hasNext();) {
		    st2.setString(1,(String)i.next());
		    st2.executeUpdate();
		}
		st2.close();
		con.commit();
		pool.returnConnection(con);
	    }
	}
	catch(SQLException sqle) {
	    sqle.printStackTrace();
	    try {
		con.rollback();
		pool.returnConnection(con);
	    }
	    catch (Exception e) {
		pool.returnConnection(con);
	    }
	}
	finally {
	    //
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
	Map lsidMap = theDataThing.getLSIDMap();
	// Obtain a connection
	Connection con = pool.borrowConnection();
	try {
	    synchronized(writeLockObject) {
		boolean addedAtLeastOneMapping = false;
		PreparedStatement st = con.prepareStatement("INSERT INTO datathings (thing) VALUES (?)");
		st.setString(1,xmlRepresentation);
		st.executeUpdate();	    
		st.close();
		//System.out.println("Stored string : \n"+xmlRepresentation);
		PreparedStatement st2 = con.prepareStatement("INSERT INTO lsid2datathings (lsid, id, reftype, mimetype) VALUES (?,LAST_INSERT_ID(),?,?)");
		for (Iterator i = lsidMap.keySet().iterator(); i.hasNext();) {
		    Object o = i.next();
		    String type = null;
		    String mimetype = null;
		    if (o instanceof DataThing) {
			type = "datathing";
		    }
		    else if (o instanceof Collection) {
			type = "collection";
		    }
		    else {
			type = "leaf";
			mimetype = theDataThing.getMostInterestingMIMETypeForObject(o);
		    }
		    st2.setString(1,(String)lsidMap.get(o));
		    st2.setString(2,type);
		    st2.setString(3,mimetype);
		    try {
			st2.executeUpdate();
			addedAtLeastOneMapping = true;
		    }
		    catch (SQLException sqle) {
			// Can cause an exception if the LSID is already
			// asigned to a concrete object, in this case
			// because the constraint is violated we can either
			// do nothing (silent == true) or throw an exception
			if (!silent) {
			    pool.returnConnection(con);
			    DuplicateLSIDException dle = new DuplicateLSIDException();
			    dle.initCause(sqle);
			    throw dle;
			}
		    }
		    //System.out.println("Stored LSID mapping for LSID = "+lsids[i]);
		}
		st2.close();
		if (addedAtLeastOneMapping) {
		    con.commit();
		    pool.returnConnection(con);
		}
		else {
		    con.rollback();
		    pool.returnConnection(con);
		}
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    try {
		con.rollback();
		pool.returnConnection(con);
	    }
	    catch (SQLException sqle) {
		pool.returnConnection(con);
		sqle.printStackTrace();
	    }
	    if (ex instanceof DuplicateLSIDException) {
		throw (DuplicateLSIDException)ex;
	    }
	}
	finally {
	    //
	}	
    }

    
    /**
     * Fetch a DataThing from the given LSID
     */
    public DataThing fetchDataThing(String LSID)
	throws NoSuchLSIDException {
	log.debug("Data request for "+LSID);
	Connection con = null;
	try {
	    // DB ACCESS START ***************************************************
	    con = pool.borrowConnection();
	    PreparedStatement p = con.prepareStatement("SELECT t.thing FROM datathings t, lsid2datathings l WHERE t.id=l.id AND l.lsid=?");
	    p.setString(1, LSID);
	    ResultSet rs = p.executeQuery();
	    String thingAsXML = null;
	    if (rs.first() ==true) {
		thingAsXML = rs.getString("thing");
		log.debug("Found data object for LSID "+LSID);
	    }
	    else {
		log.debug("No data found for "+LSID);
		pool.returnConnection(con);
		throw new NoSuchLSIDException();
	    }
	    pool.returnConnection(con);
	    // DB ACCESS END ******************************************************

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
		log.error("LSID "+LSID+" not found in retrieved data object, index broken!");
		throw new NoSuchLSIDException();
	    }
	    if (o == theThing) {
		log.debug(LSID+" is a root object, returning it");
		return theThing;
	    }
	    else {
		log.debug("Extracting "+LSID+" from data object and returning");
		// Have to split the thing down and return the subthing
		return theThing.extractChild(o);
	    }
	}
	catch (SQLException sqle) {
	    log.error("SQL Exception caught!",sqle);
	    pool.returnConnection(con);
	    sqle.printStackTrace();
	}
	catch (JDOMException jde) {
	    log.error("JDOM Exception caught!",jde);
	    pool.returnConnection(con);
	    jde.printStackTrace();
	}
	finally {
	    //
	}
	throw new NoSuchLSIDException();
    }

    
    /**
     * Does the given LSID exist in a concrete form?
     */
    public boolean hasData(String suppliedLSID) {
	log.debug("hasData request for LSID "+suppliedLSID);
	String[] parts = suppliedLSID.split(":");
	String namespace = parts[3];
	String LSID = suppliedLSID;
	if (namespace.equals("datathing") == false) {
	    // Construct a new LSID with the namespace replaced by 'datathing'
	    LSID = parts[0]+":"+parts[1]+":"+parts[2]+":datathing:"+parts[4];
	    if (parts.length == 6) {
		// has version
		LSID = LSID + ":" + parts[5];
	    }
	}
    	Connection con = null;
	try {
	    // DB ACCESS START **************************************
	    con = pool.borrowConnection();
	    PreparedStatement p = con.prepareStatement("SELECT id, reftype FROM lsid2datathings WHERE lsid=?");
	    p.setString(1,LSID);
	    ResultSet rs = p.executeQuery();
	    boolean hasReference = rs.first();
	    String refType = null;
	    if (hasReference) {
		refType = rs.getString("reftype");
	    }
	    rs.close();
	    p.close();
	    pool.returnConnection(con);
	    // DB ACCESS END ****************************************
	    if (hasReference == false) {
		log.debug("hasData, LSID not found therefore false for LSID "+LSID);
		return false;
	    }
	    // If the namespace was datathing then return true, this is enough
	    if (namespace.equals("datathing")) {
		log.debug("hasData returns true as "+LSID+" is a datathing");
		return true;
	    }
	    else if (namespace.equals("raw")) {
		if (refType.equals("leaf")) {
		    log.debug("hasData returns true, "+LSID+" is a datathing leaf node");
		    return true;
		}
	    }
	    log.debug("hasData returns false, "+LSID+" is a collection");
	    return false;
	}
	catch (Exception ex) {
	    log.error("Exception!",ex);
	    pool.returnConnection(con);
	}
	finally {
	    //
	}
	return false;
    }

    
    /**
     * Get the raw mime type for the datathing form of this LSID
     * or null if the node references isn't a leaf or doesn't
     * exist.
     */
    public String getMIMEType(String LSID) {
	Connection con = null;
	try {
	    // DB ACCESS START ***********************************************
	    con = pool.borrowConnection();
	    PreparedStatement p = con.prepareStatement("SELECT mimetype FROM lsid2datathings WHERE lsid = ? AND reftype='leaf'");
	    p.setString(1, LSID);
	    ResultSet rs = p.executeQuery();
	    if (rs.first()==false) {
		return null;
	    }
	    String result = rs.getString("mimetype");
	    rs.close();
	    p.close();
	    pool.returnConnection(con);
	    // DB ACCESS END ************************************************
	    return result;
	}
	catch (SQLException sqle) {
	    log.error("SQL Exception in getMIMEType!",sqle);
	    pool.returnConnection(con);
	    sqle.printStackTrace();
	    return null;
	}
	finally {
	    //
	}
    }
    

    /**
     * Get the metadata associated with this LSID
     */
    public String getMetadata(String LSID) {
	log.debug("Metadata requested for LSID "+LSID);
	Connection con = null;
	try {
	    con = pool.borrowConnection();
	    PreparedStatement p = con.prepareStatement("SELECT m.rdfstring FROM metadata m, lsid2metadata l WHERE l.id = m.id AND l.lsid = ?");
	    p.setString(1, LSID);
	    ResultSet rs = p.executeQuery();
	    StringBuffer sb = new StringBuffer();
	    while (rs.next()) {
		sb.append(rs.getString("rdfstring"));
	    }
	    String result = sb.toString();
	    pool.returnConnection(con);
	    if (result.equals("")) {
		return null;
	    }
	    else {
		// Result contains a string of XML, normalize it.
		return result;
	    }
	    /**
	       try {
	       String provenance = new XMLOutputter().outputString(new SAXBuilder(false).build(new StringReader(result)));
	       log.debug("Returning provenance : "+provenance);
	       return provenance;
	       }
	       catch (JDOMException jde) {
	       log.error("JDOM Exception when normalizing output provenance!\n\n"+result,jde);
	       return null;
	       }
	       }
	    */
	}
	catch (SQLException sqle) {
	    log.error("SQLException when getting provenance!",sqle);
	    pool.returnConnection(con);
	    sqle.printStackTrace();
	    return null;
	}
	finally {
	    //;
	}
    }


    /**
     * Does the given LSID have associated metadata?
     * Always returns false at present.
     */
    public boolean hasMetadata(String LSID) {
	return (getMetadata(LSID)!=null);
    }
}

class JDBCConnectionPool extends ObjectPool {
    
    String driver, dsn, usr, pwd;
    
    static Logger log = Logger.getLogger(JDBCConnectionPool.class.getName());

    public Connection borrowConnection() {
	log.debug("Thread "+Thread.currentThread().toString()+" requested connection.");
	Connection con = (Connection)super.checkOut();
	log.debug("Thread "+Thread.currentThread().toString()+" acquired connection.");
	return con;
    }
    
    public void returnConnection( Connection c ) {
	super.checkIn( c );
	log.debug("Thread "+Thread.currentThread().toString()+" returned connection.");
    }
    
    public JDBCConnectionPool( String driver, String dsn, 
			       String usr, String pwd, int maxConnections ) {
	try {
	    Class.forName( driver ).newInstance();
	}
	catch( Exception e ) {
	    e.printStackTrace();
	}
	this.dsn = dsn;
	this.usr = usr;
	this.pwd = pwd;
	this.maxObjects = maxConnections;
    }
    
    Object create() {
	try {
	    Connection con = DriverManager.getConnection( dsn, usr, pwd );
	    con.setAutoCommit(false);
	    return con;
	}
	catch( SQLException e ) {
	    e.printStackTrace();
	    return( null );
	}
    }
    
    void expire( Object o ) {
	try {
	    ( ( Connection ) o ).close();
	}
	catch( SQLException e ) {
	    e.printStackTrace();
	}
    }

    boolean validate( Object o ) {
	try {
	    return( ! ( ( Connection ) o ).isClosed() );
	}
	catch( SQLException e ) {
	    e.printStackTrace();
	    return( false );
	}
    }
    
}


    


abstract class ObjectPool {
    
    private long expirationTime;   
    private Hashtable locked, unlocked;        
    int maxObjects = 10;

    abstract Object create();
    abstract boolean validate( Object o );
    abstract void expire( Object o );

    ObjectPool() {
	expirationTime = 30000; // 30 seconds
	locked = new Hashtable();         
	unlocked = new Hashtable();
    }
    

    synchronized Object checkOut() {
	try {
	    return realCheckOut();
	}
	catch (Exception e) {
	    while (true) {
		try {
		    Thread.sleep(2000);
		    return realCheckOut();
		}
		catch (Exception e2) {
		    //
		}
	    }
	}
    }
    


    synchronized Object realCheckOut() throws Exception {
	long now = System.currentTimeMillis();
	Object o;        
	if( unlocked.size() > 0 ) {
	    Enumeration e = unlocked.keys();  
	    while( e.hasMoreElements() ) {
		o = e.nextElement();           
		if( ( now - ( ( Long ) unlocked.get( o ) ).longValue() ) >
		    expirationTime ) {
		    // object has expired
		    unlocked.remove( o );
		    expire( o );
		    o = null;
		}
		else {
		    if( validate( o ) ) {
			unlocked.remove( o );
			locked.put( o, new Long( now ) );                
			return( o );
		    }
		    else {
			// object failed validation
			unlocked.remove( o );
			expire( o );
			o = null;
		    }
		}
	    }
	}        
	int currentObjects = locked.size();
	if (currentObjects < maxObjects || maxObjects == 0) {
	    // no objects available, create a new one
	    o = create();        
	    locked.put( o, new Long( now ) ); 
	    return( o );
	}
	else {
	    throw new Exception("Pool too big, refusing to grow");
	}
    }
    
    
    synchronized void checkIn( Object o ) {
	locked.remove( o );
	unlocked.put( o, new Long( System.currentTimeMillis() ) );
    }
    
}
