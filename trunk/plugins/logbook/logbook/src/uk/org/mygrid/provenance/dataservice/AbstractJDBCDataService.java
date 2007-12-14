/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: AbstractJDBCDataService.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 27-Apr-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.io.IOException;
import java.io.StringReader;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingXMLFactory;
import org.embl.ebi.escience.baclava.store.DuplicateLSIDException;
import org.embl.ebi.escience.baclava.store.NoSuchLSIDException;
import org.embl.ebi.escience.scufl.ScuflException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.view.XScuflView;
import org.jdom.Document;
import org.jdom.Element;
import org.jdom.JDOMException;
import org.jdom.input.SAXBuilder;
import org.jdom.output.Format;
import org.jdom.output.XMLOutputter;

import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * An abstract implementation of the DataService that backs its data with a JDBC
 * based relational database. Code based on
 * {@link org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService}.
 * 
 * @author Daniele Turi
 * @version $Id: AbstractJDBCDataService.java,v 1.1 2006/05/05 14:39:07 turid
 *          Exp $
 */
public abstract class AbstractJDBCDataService implements DataService {

    protected String connectionURL, username, password;

    protected JDBCConnectionPool pool;

    protected Object writeLockObject = new Object();

    protected int maxConnections = 5;

    private Properties configuration;

    static Logger log = Logger.getLogger(AbstractJDBCDataService.class
            .getName());

    public Properties getConfiguration() {
        return configuration;
    }

    public void setConfiguration(Properties configuration) {
        this.configuration = configuration;
    }

    /**
     * Create a new JDBC backed DataService with the supplied properties.
     * 
     * @throws DataServiceCreationException
     */
    public AbstractJDBCDataService(Properties configuration)
            throws DataServiceException {
        this.configuration = configuration;
        String driver = getDriver();
        try {
            if (driver == null) {
                ProvenanceConfigurator
                        .missingPropertyMessage(ProvenanceConfigurator.MYGRID_DATASERVICE_JDBC_DRIVER);
            }
            Class.forName(driver).newInstance();
            connectionURL = getConnectionURL();
            username = getUser();
            password = getPassword();
            String optionalMaxConnections = configuration
                    .getProperty("taverna.datastore.jdbc.pool.max");
            if (optionalMaxConnections != null) {
                try {
                    maxConnections = Integer.parseInt(optionalMaxConnections);
                } catch (NumberFormatException nfe) {
                    log.warn(nfe.getMessage(), nfe);
                }
            }
            pool = new JDBCConnectionPool(driver, connectionURL, username,
                    password, maxConnections);
        } catch (Exception ex) {
            throw new DataServiceCreationException(ex);
        }

        if (!tablesExist())
            createTables();
    }

    /**
     * @return true is tables already exist
     * @throws DataServiceException
     */
    public abstract boolean tablesExist() throws DataServiceException;

    /**
     * The name of the driver in the properties.
     * 
     * @return a String
     * @throws DataServiceException
     */
    public abstract String getDriver() throws DataServiceException;

    /**
     * The value of the connection URL in the properties.
     * 
     * @return a String
     * @throws DataServiceException
     */
    public abstract String getConnectionURL() throws DataServiceException;

    /**
     * The name of the user in the properties.
     * 
     * @return a String
     * @throws DataServiceException
     */
    public abstract String getUser() throws DataServiceException;

    /**
     * The value of the password in the properties.
     * 
     * @return a String
     * @throws DataServiceException
     */
    public abstract String getPassword() throws DataServiceException;

    /**
     * Creates the necessary tables in the database.
     */
    public abstract void createTables() throws DataServiceException;

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#reinit()
     */
    public void clear() throws DataServiceException {
        if (tablesExist())
            destroyTables();
        createTables();
    }

    /**
     * Drops the tables in the database.
     */
    public abstract void destroyTables() throws DataServiceException;

    /**
     * Finds all the child elements that are dataElements.
     * 
     * @param the
     *            Element to search
     * @return all the child elements that are dataElements
     */
    protected Element[] findDataElements(Element element) {
        List elements = new ArrayList();
        Iterator dataElements = element.getChildren("dataElement",
                DataThingXMLFactory.namespace).iterator();
        while (dataElements.hasNext()) {
            elements.add((Element) dataElements.next());
        }
        Iterator collectionElements = element.getChildren("partialOrder",
                DataThingXMLFactory.namespace).iterator();
        while (collectionElements.hasNext()) {
            Element collectionElement = (Element) collectionElements.next();
            Element itemListElement = collectionElement.getChild("itemList",
                    DataThingXMLFactory.namespace);
            elements.addAll(Arrays.asList(findDataElements(itemListElement)));
        }
        return (Element[]) elements.toArray(new Element[elements.size()]);
    }

    /**
     * Store a workflow into the database.
     * 
     * @param model
     */
    public void storeWorkflow(String lsid, ScuflModel model)
            throws DataServiceException {
        XScuflView view = new XScuflView(model);
        Document doc = view.getDocument();
        model.removeListener(view);
        XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
        String xmlRepresentation = xo.outputString(doc);
        Connection con = pool.borrowConnection();
        try {
            synchronized (writeLockObject) {
                PreparedStatement st = con
                        .prepareStatement("INSERT INTO workflow (lsid, workflow) VALUES (?,?)");
                st.setString(1, lsid);
                st.setString(2, xmlRepresentation);
                st.executeUpdate();
                st.close();
                con.commit();
            }
        } catch (SQLException ex) {
            try {
                con.rollback();
            } catch (SQLException sqlex) {
                log.warn("Could not rollback");
            }
            throw new DataServiceException(ex);
        } finally {
            pool.returnConnection(con);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.provenance.dataservice.DataService#populateWorkflowModel(java.lang.String,
     *      org.embl.ebi.escience.scufl.ScuflModel)
     */
    public void populateWorkflowModel(String lsid, ScuflModel model)
            throws DataServiceException, ScuflException {
        String workflowXML= fetchUnparsedWorkflow(lsid);
        model.clear();
        XScuflParser.populate(workflowXML, model, null);
    }

    public String fetchUnparsedWorkflow(String lsid)
            throws DataServiceException {
        Connection con = pool.borrowConnection();
        String workflowXML;
        try {
            PreparedStatement st = con.prepareStatement(
                    "SELECT workflow FROM workflow WHERE lsid=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
            st.setString(1, lsid);
            ResultSet rs = st.executeQuery();
            if (rs.first()) {
                workflowXML = rs.getString("workflow");
                log.debug("Found workflow " + lsid);
            } else {
                String exceptionMesssage = "Could not find workflow " + lsid;
                log.debug(exceptionMesssage);
                throw new DataServiceException(exceptionMesssage);
            }
            rs.close();
            st.close();
        } catch (SQLException ex) {
            log.error("Could not load workflow", ex);
            throw new DataServiceException(ex);
        } finally {
            pool.returnConnection(con);
        }

        return workflowXML;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#storeDataThing(org.embl.ebi.escience.baclava.DataThing,
     *      boolean)
     */
    public void storeDataThing(DataThing theDataThing, boolean silent)
            throws DuplicateLSIDException, DataServiceException {
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
                    Element dataElementData = dataElements[i].getChild(
                            "dataElementData", DataThingXMLFactory.namespace);
                    String data = dataElementData.getTextTrim();

                    int rowStoringData = insertData(con, data);

                    PreparedStatement st = con
                            .prepareStatement("INSERT INTO lsid2data (lsid, id) VALUES (?,?)");
                    st.setString(1, dataElements[i].getAttributeValue("lsid"));
                    st.setInt(2, rowStoringData);
                    st.executeUpdate();
                    st.close();

                    dataElements[i].removeChild("dataElementData",
                            DataThingXMLFactory.namespace);
                }
                // Get the string version of the XML element
                Document doc = new Document(element);
                XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
                String xmlRepresentation = xo.outputString(doc);

                PreparedStatement st = con
                        .prepareStatement("INSERT INTO datathings (thing) VALUES (?)");
                st.setString(1, xmlRepresentation);
                st.executeUpdate();
                st.close();
                // System.out.println("Stored string : \n"+xmlRepresentation);
                PreparedStatement st2 = con
                        .prepareStatement("INSERT INTO lsid2datathings (lsid, id, reftype, mimetype) VALUES (?,"
                                + identity() + ",?,?)");
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
                        mimetype = theDataThing
                                .getMostInterestingMIMETypeForObject(o);
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
            ex.printStackTrace();
            try {
                con.rollback();
                pool.returnConnection(con);
            } catch (SQLException sqle) {
                pool.returnConnection(con);
                sqle.printStackTrace();
            }
            if (ex instanceof DuplicateLSIDException) {
                throw (DuplicateLSIDException) ex;
            }
        }
    }

    /**
     * Returns the name of database-specific function to get the last identity
     * value that was inserted by a connection.
     * 
     * @return String
     * @throws SQLException
     */
    protected abstract String identity() throws SQLException;

    protected int insertData(Connection con, String data) throws SQLException {
        PreparedStatement st = con
                .prepareStatement("INSERT INTO data (data) VALUES (?)");
        st.setString(1, data);
        st.executeUpdate();
        st.close();

        Statement index = con.createStatement(
                ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
        ResultSet generatedKeys = index.executeQuery("SELECT " + identity()
                + " from data");
        generatedKeys.first();
        int rowStoringData = generatedKeys.getInt(1);
        generatedKeys.close();
        return rowStoringData;
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#fetchDataThing(java.lang.String)
     */
    public DataThing fetchDataThing(String LSID) throws NoSuchLSIDException,
            ObjectPoolException {
        log.debug("Data request for " + LSID);
        Connection con = null;
        try {
            // DB ACCESS START
            // ***************************************************
            con = pool.borrowConnection();
            PreparedStatement p = con
                    .prepareStatement(
                            "SELECT t.thing FROM datathings t, lsid2datathings l WHERE t.id=l.id AND l.lsid=?",
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
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
                PreparedStatement st1 = con.prepareStatement(
                        "SELECT id FROM lsid2data WHERE lsid=?",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                st1.setString(1, dataElements[i].getAttributeValue("lsid"));
                ResultSet result1 = st1.executeQuery();
                result1.first();
                int id = result1.getInt(1);
                st1.close();

                PreparedStatement st2 = con.prepareStatement(
                        "SELECT data FROM data WHERE id=?",
                        ResultSet.TYPE_SCROLL_INSENSITIVE,
                        ResultSet.CONCUR_READ_ONLY);
                st2.setInt(1, id);
                ResultSet result2 = st2.executeQuery();
                result2.first();
                String data = result2.getString(1);
                st2.close();

                Element dataElementData = new Element("dataElementData",
                        DataThingXMLFactory.namespace);
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
                log.error("LSID " + LSID
                        + " not found in retrieved data object, index broken!");
                throw new NoSuchLSIDException();
            }
            if (o == theThing) {
                log.debug(LSID + " is a root object, returning it");
                return theThing;
            } else {
                log.debug("Extracting " + LSID
                        + " from data object and returning");
                // Have to split the thing down and return the subthing
                return theThing.extractChild(o);
            }
        } catch (SQLException sqle) {
            log.error("SQL Exception caught!", sqle);
            pool.returnConnection(con);
            sqle.printStackTrace();
        } catch (JDOMException jde) {
            log.error("JDOM Exception caught!", jde);
            pool.returnConnection(con);
            jde.printStackTrace();
        } catch (IOException ioe) {
            //
        }
        throw new NoSuchLSIDException();
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#hasData(java.lang.String)
     */
    public boolean hasData(String suppliedLSID) {
        log.debug("hasData request for LSID " + suppliedLSID);
        String[] parts = suppliedLSID.split(":");
        String namespace = parts[3];
        String LSID = suppliedLSID;
        if (namespace.equals("datathing") == false) {
            // Construct a new LSID with the namespace replaced by 'datathing'
            LSID = parts[0] + ":" + parts[1] + ":" + parts[2] + ":datathing:"
                    + parts[4];
            if (parts.length == 6) {
                // has version
                LSID = LSID + ":" + parts[5];
            }
        }
        Connection con = null;
        try {
            // DB ACCESS START **************************************
            con = pool.borrowConnection();
            PreparedStatement p = con.prepareStatement(
                    "SELECT id, reftype FROM lsid2datathings WHERE lsid=?",
                    ResultSet.TYPE_SCROLL_INSENSITIVE,
                    ResultSet.CONCUR_READ_ONLY);
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
                log.debug("hasData, LSID not found therefore false for LSID "
                        + LSID);
                return false;
            }
            // If the namespace was datathing then return true, this is enough
            if (namespace.equals("datathing")) {
                log
                        .debug("hasData returns true as " + LSID
                                + " is a datathing");
                return true;
            } else if (namespace.equals("raw")) {
                if (refType.equals("leaf")) {
                    log.debug("hasData returns true, " + LSID
                            + " is a datathing leaf node");
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
     * 
     * @throws ObjectPoolException
     */
    public String getMIMEType(String LSID) throws ObjectPoolException {
        Connection con = null;
        try {
            // DB ACCESS START ***********************************************
            con = pool.borrowConnection();
            PreparedStatement p = con
                    .prepareStatement(
                            "SELECT mimetype FROM lsid2datathings WHERE lsid = ? AND reftype='leaf'",
                            ResultSet.TYPE_SCROLL_INSENSITIVE,
                            ResultSet.CONCUR_READ_ONLY);
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
            sqle.printStackTrace();
            return null;
        } finally {
            //
        }
    }

}
