/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.store;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.StringTokenizer;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
//import org.embl.ebi.escience.scufl.ScuflModel;
//import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

/**
 * An implementation of the BaclavaDataService that backs its data with a JDBC
 * based relational database.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class JDBCBaclavaDataService implements BaclavaDataService, LSIDProvider {

	private static final String MISSING_PROPERTY_MESSAGE = "Cannot initialise service: missing property ";

	public static final String TAVERNA_DATASTORE_JDBC_DRIVER = "taverna.datastore.jdbc.driver";

	public static final String TAVERNA_DATASTORE_JDBC_PASSWORD = "taverna.datastore.jdbc.password";

	public static final String TAVERNA_DATASTORE_JDBC_USER = "taverna.datastore.jdbc.user";

	public static final String TAVERNA_DATASTORE_JDBC_URL = "taverna.datastore.jdbc.url";

	private String connectionURL, username, password, defaultAuthority, instanceID;

	private JDBCConnectionPool pool;

	private Object writeLockObject = new Object();

	private int maxConnections = 5;

	static Logger log = Logger.getLogger(JDBCBaclavaDataService.class.getName());

	/**
	 * Create a new JDBC backed BaclavaDataService with properties taken from
	 * the current set of system properties.
	 */
	public JDBCBaclavaDataService() {
		this(System.getProperties());
	}

	/**
	 * Create a new JDBC backed BaclavaDataService with the supplied properties.
	 */
	public JDBCBaclavaDataService(Properties props) {
		// Get the JDBC connection class
		String driver = props.getProperty(TAVERNA_DATASTORE_JDBC_DRIVER);
		if (driver == null) {
			missingPropertyMessage(TAVERNA_DATASTORE_JDBC_DRIVER);
			return;
		}
		try {
			Class.forName(driver).newInstance();
		} catch (Exception ex) {
			log.error("Exception getting database driver", ex);
		}
		// The default authority string to use for the lsid provider
		defaultAuthority = props.getProperty("taverna.lsid.providerauthority");
		if (defaultAuthority == null) {
			defaultAuthority = "net.sf.taverna";
		}
		connectionURL = props.getProperty(TAVERNA_DATASTORE_JDBC_URL);
		if (connectionURL == null) {
			missingPropertyMessage(TAVERNA_DATASTORE_JDBC_URL);
			return;
		}
		username = props.getProperty(TAVERNA_DATASTORE_JDBC_USER);
		if (username == null) {
			missingPropertyMessage(TAVERNA_DATASTORE_JDBC_USER);
			return;
		}
		password = props.getProperty(TAVERNA_DATASTORE_JDBC_PASSWORD);
		String optionalMaxConnections = props.getProperty("taverna.datastore.jdbc.pool.max");
		if (optionalMaxConnections != null) {
			try {
				maxConnections = Integer.parseInt(optionalMaxConnections);
			} catch (NumberFormatException nfe) {
				//
			}
		}
		String object_id = super.toString();
		StringTokenizer st = new StringTokenizer(object_id, "@", false);
		while (st.hasMoreTokens()) {
			instanceID = st.nextToken();
		}
		pool = new JDBCConnectionPool(driver, connectionURL, username, password, maxConnections);
		createTables();
	}

	private void missingPropertyMessage(String missingProperty) {
		System.err.println(MISSING_PROPERTY_MESSAGE + missingProperty);
		log.error(MISSING_PROPERTY_MESSAGE + missingProperty);
	}

	/**
	 * Create the tables required if they're not already there
	 */
	private void createTables() {
		Connection con = null;
		try {
			con = pool.borrowConnection();
			Statement st = con.createStatement();
			st.executeUpdate("CREATE TABLE IF NOT EXISTS metadata (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
					+ "                                     rdfstring TEXT NOT NULL,"
					+ "                                     PRIMARY KEY(id)) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS lsid2metadata (lsid CHAR(200) NOT NULL,"
					+ "                                          id INT UNSIGNED NOT NULL REFERENCES metadata(id)"
					+ "                                          ) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS datathings (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
					+ "                                       thing TEXT NOT NULL,"
					+ "                                       PRIMARY KEY(id)) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS lsid2datathings (lsid CHAR(200) NOT NULL UNIQUE,"
					+ "                    id INT UNSIGNED NOT NULL REFERENCES datathings(id),"
					+ "                    reftype ENUM(\"datathing\",\"collection\",\"leaf\"),"
					+ "                    mimetype CHAR(200),"
					+ "                    PRIMARY KEY(lsid)) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS idcounter (count INT UNSIGNED NOT NULL) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS workflow  (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
					+ "												lsid CHAR(200) NOT NULL, "
					+ "	                                            title CHAR(200) NOT NULL,"
					+ "												author CHAR(200) NOT NULL,"
					+ "												workflow TEXT NOT NULL, PRIMARY KEY(id)) TYPE=InnoDB");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS data (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
					+ "                                       data TEXT NOT NULL,"
					+ "                                       PRIMARY KEY(id)) TYPE = InnoDB;");
			st.executeUpdate("CREATE TABLE IF NOT EXISTS lsid2data (lsid CHAR(200) NOT NULL,"
					+ "                                          id INT UNSIGNED NOT NULL REFERENCES data(id)"
					+ "                                          ) TYPE = InnoDB;");

			pool.returnConnection(con);
		} catch (Exception ex) {
			pool.returnConnection(con);
			log.error("Exception creating tables", ex);
		} finally {
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
			st.executeUpdate("DROP TABLE IF EXISTS workflow");
			st.executeUpdate("DROP TABLE IF EXISTS data");
			st.executeUpdate("DROP TABLE IF EXISTS lsid2data");

			pool.returnConnection(con);
			log.debug("...finished dropping tables");
		} catch (Exception ex) {
			pool.returnConnection(con);
			log.error("Exception dropping tables", ex);
		} finally {
			//
		}
	}

	/**
	 * Implement LSIDProvider
	 */
	public String getID(LSIDProvider.NamespaceEnumeration namespaceObject) {
		String namespace = namespaceObject.toString();
		String prefix = "urn:lsid:" + defaultAuthority + ":" + namespace + ":";
		// Fetch the next value from the counter
		Connection con = pool.borrowConnection();
		try {
			Statement s = con.createStatement();
			// s.executeUpdate("LOCK TABLES idcounter WRITE");
			s.executeUpdate("UPDATE idcounter SET count = count + 1");
			ResultSet rs = s.executeQuery("SELECT count FROM idcounter");
			String suffix = "1";
			if (rs.first()) {
				// Resultset contained a result so return it
				suffix = rs.getString("count");
			} else {
				s.executeUpdate("INSERT INTO idcounter (count) VALUES (1)");
			}
			rs.close();
			// s.executeUpdate("UNLOCK TABLES");
			s.close();
			con.commit();
			pool.returnConnection(con);
			return prefix + suffix + instanceID;
		} catch (SQLException sqle) {
			log.error("SQL Exception", sqle);
			try {
				con.rollback();
				pool.returnConnection(con);
			} catch (SQLException sqle2) {
				pool.returnConnection(con);
			}
			return "NO_IDENTIFIER_ASSIGNED";
		} finally {
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
			synchronized (writeLockObject) {
				PreparedStatement st = con.prepareStatement("INSERT INTO metadata (rdfstring) VALUES (?)");
				st.setString(1, theMetadata);
				st.executeUpdate();
				st.close();
				PreparedStatement st2 = con
						.prepareStatement("INSERT INTO lsid2metadata (lsid, id) VALUES (?,LAST_INSERT_ID())");
				for (Iterator i = lsidList.iterator(); i.hasNext();) {
					st2.setString(1, (String) i.next());
					st2.executeUpdate();
				}
				st2.close();
				con.commit();
				pool.returnConnection(con);
			}
		} catch (SQLException sqle) {
			log.error("SQLException storing metadata");
			try {
				con.rollback();
				pool.returnConnection(con);
			} catch (Exception e) {
				pool.returnConnection(con);
			}
		} finally {
			//
		}

	}

	/**
	 * Finds all the child elements that are dataElements.
	 * 
	 * @param the
	 *            Element to search
	 * @return all the child elements that are dataElements
	 */
	private Element[] findDataElements(Element element) {
		List elements = new ArrayList();
		Iterator dataElements = element.getChildren("dataElement", DataThingXMLFactory.namespace).iterator();
		while (dataElements.hasNext()) {
			elements.add((Element) dataElements.next());
		}
		Iterator collectionElements = element.getChildren("partialOrder", DataThingXMLFactory.namespace).iterator();
		while (collectionElements.hasNext()) {
			Element collectionElement = (Element) collectionElements.next();
			Element itemListElement = collectionElement.getChild("itemList", DataThingXMLFactory.namespace);
			elements.addAll(Arrays.asList(findDataElements(itemListElement)));
		}
		return (Element[]) elements.toArray(new Element[elements.size()]);
	}

	/**
	 * Store the specified data object
	 */
	public void storeDataThing(DataThing theDataThing, boolean silent) throws DuplicateLSIDException {
		// Get the XML version of the DataThing object
		// from the XML factory
		Element element = DataThingXMLFactory.getElement(theDataThing);
		Element[] dataElements = findDataElements(element);
		// Find all the LSIDs that this document contains
		Map lsidMap = theDataThing.getLSIDMap();
		// Obtain a connection
		Connection con = pool.borrowConnection();
		try {
			synchronized (writeLockObject) {
				boolean addedAtLeastOneMapping = false;
				for (int i = 0; i < dataElements.length; i++) {
					Element dataElementData = dataElements[i]
							.getChild("dataElementData", DataThingXMLFactory.namespace);

					// Store the raw data from each dataElement in the 'data'
					// table and
					PreparedStatement st = con.prepareStatement("INSERT INTO data (data) VALUES (?)");
					st.setString(1, dataElementData.getTextTrim());
					st.executeUpdate();
					ResultSet generatedKeys = st.getGeneratedKeys();
					generatedKeys.first();
					int rowStoringData = generatedKeys.getInt(1);
					st.close();

					// Store the lsid->data mapping in the lsid2data table
					st = con.prepareStatement("INSERT INTO lsid2data (lsid, id) VALUES (?,?)");
					st.setString(1, dataElements[i].getAttributeValue("lsid"));
					st.setInt(2, rowStoringData);
					st.executeUpdate();
					st.close();

					dataElements[i].removeChild("dataElementData", DataThingXMLFactory.namespace);
				}
				// Get the string version of the XML element
				Document doc = new Document(element);
				XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
				String xmlRepresentation = xo.outputString(doc);

				PreparedStatement st = con.prepareStatement("INSERT INTO datathings (thing) VALUES (?)");
				st.setString(1, xmlRepresentation);
				st.executeUpdate();
				st.close();
				// System.out.println("Stored string : \n"+xmlRepresentation);
				PreparedStatement st2 = con
						.prepareStatement("INSERT INTO lsid2datathings (lsid, id, reftype, mimetype) VALUES (?,LAST_INSERT_ID(),?,?)");
				for (Iterator i = lsidMap.keySet().iterator(); i.hasNext();) {
					Object o = i.next();
					String type = null;
					String mimetype = null;
					if (o instanceof DataThing) {
						type = "datathing";
					} else if (o instanceof Collection) {
						type = "collection";
					} else {
						type = "leaf";
						mimetype = theDataThing.getMostInterestingMIMETypeForObject(o);
					}
					st2.setString(1, (String) lsidMap.get(o));
					st2.setString(2, type);
					st2.setString(3, mimetype);
					try {
						st2.executeUpdate();
						addedAtLeastOneMapping = true;
					} catch (SQLException sqle) {
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
					// System.out.println("Stored LSID mapping for LSID =
					// "+lsids[i]);
				}
				st2.close();
				if (addedAtLeastOneMapping) {
					con.commit();
					pool.returnConnection(con);
				} else {
					con.rollback();
					pool.returnConnection(con);
				}
			}
		} catch (Exception ex) {
			log.error("Error occurred storing datathing", ex);
			try {
				con.rollback();
				pool.returnConnection(con);
			} catch (SQLException sqle) {
				pool.returnConnection(con);
				log.error("Error rolling back transaction", sqle);
			}
			if (ex instanceof DuplicateLSIDException) {
				throw (DuplicateLSIDException) ex;
			}
		} finally {
			//
		}
	}

	/**
	 * Fetch a DataThing from the given LSID
	 */
	public DataThing fetchDataThing(String LSID) throws NoSuchLSIDException {
		log.debug("Data request for " + LSID);
		Connection con = null;
		try {
			// DB ACCESS START
			// ***************************************************
			con = pool.borrowConnection();
			PreparedStatement p = con
					.prepareStatement("SELECT t.thing FROM datathings t, lsid2datathings l WHERE t.id=l.id AND l.lsid=?");
			p.setString(1, LSID);
			ResultSet rs = p.executeQuery();
			String thingAsXML = null;
			if (rs.first() == true) {
				thingAsXML = rs.getString("thing");
				log.debug("Found data object for LSID " + LSID);
			} else {
				log.debug("No data found for " + LSID);
				pool.returnConnection(con);
				throw new NoSuchLSIDException();
			}

			// Parse the XML and get the DataThing that was
			// originally submitted, although we may have
			// to split this down to actually get the desired
			// LSID value out
			// System.out.println("Found a data thing as XML :
			// \n\n"+thingAsXML);
			SAXBuilder builder = new SAXBuilder(false);
			Document doc = builder.build(new StringReader(thingAsXML));
			// Retrieve the data and for each dataElement and add a
			// dataElementData
			Element element = doc.getRootElement();
			Element[] dataElements = findDataElements(element);
			for (int i = 0; i < dataElements.length; i++) {
				PreparedStatement st1 = con.prepareStatement("SELECT id FROM lsid2data WHERE lsid=?");
				st1.setString(1, dataElements[i].getAttributeValue("lsid"));
				ResultSet result1 = st1.executeQuery();
				result1.first();
				int id = result1.getInt(1);
				st1.close();

				PreparedStatement st2 = con.prepareStatement("SELECT data FROM data WHERE id=?");
				st2.setInt(1, id);
				ResultSet result2 = st2.executeQuery();
				result2.first();
				String data = result2.getString(1);
				st2.close();

				Element dataElementData = new Element("dataElementData", DataThingXMLFactory.namespace);
				dataElementData.setText(data);
				dataElements[i].addContent(dataElementData);
			}

			pool.returnConnection(con);
			// DB ACCESS END
			// ******************************************************
			DataThing theThing = new DataThing(element);
			// System.out.println(theThing);
			// Was the LSID for the dataThing itself?
			// System.out.println("LSID to find is "+LSID);
			Object o = theThing.getDataObjectWithLSID(LSID);
			if (o == null) {
				log.error("LSID " + LSID + " not found in retrieved data object, index broken!");
				throw new NoSuchLSIDException();
			}
			if (o == theThing) {
				log.debug(LSID + " is a root object, returning it");
				return theThing;
			} else {
				log.debug("Extracting " + LSID + " from data object and returning");
				// Have to split the thing down and return the subthing
				return theThing.extractChild(o);
			}
		} catch (SQLException sqle) {
			log.error("SQL Exception caught!", sqle);
			pool.returnConnection(con);
		} catch (JDOMException jde) {
			log.error("JDOM Exception caught!", jde);
			pool.returnConnection(con);
		} catch (IOException ioe) {
			//
		} finally {
			//
		}
		throw new NoSuchLSIDException();
	}

	/**
	 * Does the given LSID exist in a concrete form?
	 */
	public boolean hasData(String suppliedLSID) {
		log.debug("hasData request for LSID " + suppliedLSID);
		String[] parts = suppliedLSID.split(":");
		String namespace = parts[3];
		String LSID = suppliedLSID;
		if (namespace.equals("datathing") == false) {
			// Construct a new LSID with the namespace replaced by 'datathing'
			LSID = parts[0] + ":" + parts[1] + ":" + parts[2] + ":datathing:" + parts[4];
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
			p.setString(1, LSID);
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
				log.debug("hasData, LSID not found therefore false for LSID " + LSID);
				return false;
			}
			// If the namespace was datathing then return true, this is enough
			if (namespace.equals("datathing")) {
				log.debug("hasData returns true as " + LSID + " is a datathing");
				return true;
			} else if (namespace.equals("raw")) {
				if (refType.equals("leaf")) {
					log.debug("hasData returns true, " + LSID + " is a datathing leaf node");
					return true;
				}
			}
			log.debug("hasData returns false, " + LSID + " is a collection");
			return false;
		} catch (Exception ex) {
			log.error("Exception!", ex);
			pool.returnConnection(con);
		} finally {
			//
		}
		return false;
	}

	/**
	 * Get the raw mime type for the datathing form of this LSID or null if the
	 * node references isn't a leaf or doesn't exist.
	 */
	public String getMIMEType(String LSID) {
		Connection con = null;
		try {
			// DB ACCESS START ***********************************************
			con = pool.borrowConnection();
			PreparedStatement p = con
					.prepareStatement("SELECT mimetype FROM lsid2datathings WHERE lsid = ? AND reftype='leaf'");
			p.setString(1, LSID);
			ResultSet rs = p.executeQuery();
			if (rs.first() == false) {
				return null;
			}
			String result = rs.getString("mimetype");
			rs.close();
			p.close();
			pool.returnConnection(con);
			// DB ACCESS END ************************************************
			return result;
		} catch (SQLException sqle) {
			log.error("SQL Exception in getMIMEType!", sqle);
			pool.returnConnection(con);
			return null;
		} finally {
			//
		}
	}

	/**
	 * Get the metadata associated with this LSID
	 */
	public String getMetadata(String LSID) {
		log.debug("Metadata requested for LSID " + LSID);
		Connection con = null;
		try {
			con = pool.borrowConnection();
			PreparedStatement p = con
					.prepareStatement("SELECT m.rdfstring FROM metadata m, lsid2metadata l WHERE l.id = m.id AND l.lsid = ?");
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
			} else {
				// Result contains a string of XML, normalize it.
				return result;
			}
			/**
			 * try { String provenance = new XMLOutputter().outputString(new
			 * SAXBuilder(false).build(new StringReader(result)));
			 * log.debug("Returning provenance : "+provenance); return
			 * provenance; } catch (JDOMException jde) { log.error("JDOM
			 * Exception when normalizing output provenance!\n\n"+result,jde);
			 * return null; } }
			 */
		} catch (SQLException sqle) {
			log.error("SQLException when getting provenance!", sqle);
			pool.returnConnection(con);
			return null;
		} finally {
			// ;
		}
	}

	/**
	 * Does the given LSID have associated metadata? Always returns false at
	 * present.
	 */
	public boolean hasMetadata(String LSID) {
		return (getMetadata(LSID) != null);
	}

	/**
	 * Stores a workflow xml representation to the database, together with its
	 * title, and lsid * Overwrites an existing workflow if it already exists
	 * with a given lsid
	 */
	/**
	public void storeWorkflow(ScuflModel model) {
		Connection con = null;
		PreparedStatement pstmt = null;

		String LSID = model.getDescription().getLSID();
		String title = model.getDescription().getTitle();
		String author = model.getDescription().getAuthor();

		String xml = XScuflView.getXMLText(model);

		try {
			boolean exists = hasWorkflow(LSID);
			con = pool.borrowConnection();
			if (exists) {
				pstmt = con.prepareStatement("UPDATE workflow SET title=?, workflow=?, author=? WHERE lsid=?");
			} else {
				pstmt = con.prepareStatement("INSERT INTO workflow (title,workflow,author,lsid) VALUES (?,?,?,?)");
			}
			pstmt.setString(1, title);
			pstmt.setString(2, xml);
			pstmt.setString(3, author);
			pstmt.setString(4, LSID);

			pstmt.executeUpdate();
			con.commit();
		} catch (SQLException e) {
			log.error("SQLException when storing workflow for LSID=" + LSID, e);
		} finally {
			if (con != null)
				pool.returnConnection(con);
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.error("Error closing prepared statement", e);
				}
			}
		}
	}
	*/

	public boolean hasWorkflow(String LSID) {
		return (fetchWorkflow(LSID) != null);
	}

	public String fetchWorkflow(String LSID) {
		Connection con = null;
		PreparedStatement pstmt = null;
		ResultSet rst = null;
		String result = null;

		try {
			con = pool.borrowConnection();
			pstmt = con.prepareStatement("SELECT workflow FROM workflow WHERE lsid=?");
			pstmt.setString(1, LSID);
			rst = pstmt.executeQuery();
			if (rst.next()) {
				result = rst.getString("workflow");
			}
		} catch (SQLException e) {
			log.error("SQLException when getting workflow for LSID=" + LSID, e);
		} finally {
			if (con != null)
				pool.returnConnection(con);
			if (pstmt != null) {
				try {
					pstmt.close();
				} catch (SQLException e) {
					log.error("Error closing prepared statement", e);
				}
			}
			if (rst != null) {
				try {
					rst.close();
				} catch (SQLException e) {
					log.error("Error closing resultset", e);
				}
			}
		}

		return result;
	}
}

class JDBCConnectionPool extends ObjectPool {

	String driver, dsn, usr, pwd;

	private static Logger log = Logger.getLogger(JDBCConnectionPool.class.getName());

	public Connection borrowConnection() {
		log.debug("Thread " + Thread.currentThread().toString() + " requested connection.");
		Connection con = (Connection) super.checkOut();
		log.debug("Thread " + Thread.currentThread().toString() + " acquired connection.");
		return con;
	}

	public void returnConnection(Connection c) {
		super.checkIn(c);
		log.debug("Thread " + Thread.currentThread().toString() + " returned connection.");
	}

	public JDBCConnectionPool(String driver, String dsn, String usr, String pwd, int maxConnections) {
		try {
			Class.forName(driver).newInstance();
		} catch (Exception e) {
			log.error("Unable to find driver class", e);
		}
		this.dsn = dsn;
		this.usr = usr;
		this.pwd = pwd;
		this.maxObjects = maxConnections;
	}

	Object create() {
		try {
			Connection con = DriverManager.getConnection(dsn, usr, pwd);
			con.setAutoCommit(false);
			return con;
		} catch (SQLException e) {
			log.error("Unable to establish database connection", e);
			return (null);
		}
	}

	void expire(Object o) {
		try {
			((Connection) o).close();
		} catch (SQLException e) {
			log.error("Error closing database connection", e);
		}
	}

	boolean validate(Object o) {
		try {
			return (!((Connection) o).isClosed());
		} catch (SQLException e) {
			log.error("Error database connection status", e);
			return (false);
		}
	}

}

abstract class ObjectPool {

	private long expirationTime;

	private Hashtable locked, unlocked;

	int maxObjects = 10;

	abstract Object create();

	abstract boolean validate(Object o);

	abstract void expire(Object o);

	ObjectPool() {
		expirationTime = 30000; // 30 seconds
		locked = new Hashtable();
		unlocked = new Hashtable();
	}

	synchronized Object checkOut() {
		try {
			return realCheckOut();
		} catch (Exception e) {
			while (true) {
				try {
					Thread.sleep(2000);
					return realCheckOut();
				} catch (Exception e2) {
					//
				}
			}
		}
	}

	synchronized Object realCheckOut() throws Exception {
		long now = System.currentTimeMillis();
		Object o;
		if (unlocked.size() > 0) {
			Enumeration e = unlocked.keys();
			while (e.hasMoreElements()) {
				o = e.nextElement();
				if ((now - ((Long) unlocked.get(o)).longValue()) > expirationTime) {
					// object has expired
					unlocked.remove(o);
					expire(o);
					o = null;
				} else {
					if (validate(o)) {
						unlocked.remove(o);
						locked.put(o, new Long(now));
						return (o);
					} else {
						// object failed validation
						unlocked.remove(o);
						expire(o);
						o = null;
					}
				}
			}
		}
		int currentObjects = locked.size();
		if (currentObjects < maxObjects || maxObjects == 0) {
			// no objects available, create a new one
			o = create();
			if (o != null)
				locked.put(o, new Long(now));
			return (o);
		} else {
			throw new Exception("Pool too big, refusing to grow");
		}
	}

	synchronized void checkIn(Object o) {
		if (o == null)
			return;
		locked.remove(o);
		unlocked.put(o, new Long(System.currentTimeMillis()));
	}

}
