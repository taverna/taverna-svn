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
 * Filename           $RCSfile: HypersonicDataService.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 27-Apr-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.org.mygrid.provenance.util.PropertyMissingException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * Hypersonic implementation of
 * {@link uk.org.mygrid.provenance.dataservice.AbstractJDBCDataService}.
 * 
 * @author Daniele Turi
 * @version $Id: HypersonicDataService.java,v 1.5 2006/05/30 15:48:04 soilands
 *          Exp $
 */
public class HypersonicDataService extends AbstractJDBCDataService {

    public static final String DATA_HSQL_TABLES = "/data/hsql/tables";

    static Logger log = Logger.getLogger(HypersonicDataService.class.getName());

    /**
     * Create a new JDBC backed DataService with the supplied properties.
     * 
     * @throws DataServiceCreationException
     */
    public HypersonicDataService(Properties props) throws DataServiceException {
        super(props);
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.provenance.dataservice.AbstractJDBCDataService#isTablesCreated()
     */
    @Override
    public boolean tablesExist() throws DataServiceException {
        Connection con = null;
        try {
            boolean tablesCreated = false;
            con = pool.borrowConnection();
            DatabaseMetaData metaData = con.getMetaData();
            ResultSet tables = metaData.getTables(con.getCatalog(), null,
                    "LSID2DATATHINGS", null);
            if (tables.next()) {
                log.debug("Tables already exist");
                tablesCreated = true;
            }
            tables.close();
            pool.returnConnection(con);
            return tablesCreated;
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            pool.returnConnection(con);
            throw new DataServiceException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#createTables()
     */
    public void createTables() throws DataServiceException {
        Connection con = null;
        try {
            con = pool.borrowConnection();
            Statement st = con.createStatement();
            st
                    .executeUpdate("CREATE TABLE datathings (id INT NOT NULL IDENTITY,"
                            + "                                       thing LONGVARCHAR NOT NULL,"
                            + "                                       PRIMARY KEY(id));");
            log.info("Created table datathings");
            st
                    .executeUpdate("CREATE TABLE lsid2datathings (lsid CHAR(200) NOT NULL,"
                            + "                    id INT NOT NULL,"
                            + "                    reftype VARCHAR(10),"
                            + "                    mimetype CHAR(200),"
                            + "                    PRIMARY KEY(lsid));");
            log.info("Created table lsid2datathings");
            st
                    .executeUpdate("CREATE TABLE data (id INT NOT NULL IDENTITY,"
                            + "                                       data LONGVARCHAR NOT NULL,"
                            + "                                       PRIMARY KEY(id));");
            log.info("Created table data");
            st
                    .executeUpdate("CREATE TABLE lsid2data (lsid CHAR(200) NOT NULL,"
                            + "                                          id INT NOT NULL"
                            + "                                          );");
            log.info("Created table lsid2data");
            st.executeUpdate("CREATE TABLE workflow"
            // FIXME: Is id really needed when lsid is supposed to be unique?
                    + " (lsid CHAR(200) NOT NULL, "
                    + " workflow LONGVARCHAR NOT NULL, "
                    + " PRIMARY KEY(lsid))");
            log.info("Created table workflow");

            pool.returnConnection(con);
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            pool.returnConnection(con);
            throw new DataServiceException(ex);
        }
    }

    /*
     * (non-Javadoc)
     * 
     * @see uk.org.mygrid.storage.DataService#destroyTables()
     */
    public void destroyTables() throws DataServiceException {
        Connection con = null;
        try {
            log.debug("Dropping tables...");
            con = pool.borrowConnection();

            Statement st = con.createStatement();
            st.executeUpdate("DROP TABLE datathings");
            st.executeUpdate("DROP TABLE lsid2datathings");
            st.executeUpdate("DROP TABLE data");
            st.executeUpdate("DROP TABLE lsid2data");
            st.executeUpdate("DROP TABLE workflow");

            pool.returnConnection(con);
            log.debug("...finished dropping tables");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            pool.returnConnection(con);
            throw new DataServiceException(ex);
        }
    }

    @Override
    public String getDriver() {
        return ProvenanceConfigurator.HSQL_JDBC_DRIVER;
    }

    @Override
    public String getConnectionURL() throws DataServiceException {
        String connectionproperty = getConfiguration().getProperty(
                ProvenanceConfigurator.HSQL_CONNECTION_URL, "jdbc:hsqldb:file:"
                        + ProvenanceConfigurator.PROVENANCE_STORE_HOME
                        + DATA_HSQL_TABLES);
        if (connectionproperty == null) {
            try {
                ProvenanceConfigurator
                        .missingPropertyMessage(ProvenanceConfigurator.HSQL_CONNECTION_URL);
            } catch (PropertyMissingException e) {
                throw new DataServiceException(e);
            }
        }
        return connectionproperty;
    }

    @Override
    public String getUser() throws DataServiceException {
        String userProperty = getConfiguration()
                .getProperty(ProvenanceConfigurator.HSQL_USER);
        if (userProperty == null) {
            try {
                ProvenanceConfigurator
                        .missingPropertyMessage(ProvenanceConfigurator.HSQL_USER);
            } catch (PropertyMissingException e) {
                throw new DataServiceException(e);
            }
        }
        return userProperty;
    }

    @Override
    public String getPassword() {
        return getConfiguration().getProperty(ProvenanceConfigurator.HSQL_PASSWORD);
    }

    protected String identity() {
        return "IDENTITY()";
    }

}
