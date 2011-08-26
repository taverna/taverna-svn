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
 * Filename           $RCSfile: MySQLDataService.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 27-Apr-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import org.apache.log4j.Logger;

import uk.org.mygrid.provenance.util.PropertyMissingException;
import uk.org.mygrid.provenance.util.ProvenanceConfigurator;

/**
 * MySQL implementation of
 * {@link uk.org.mygrid.provenance.dataservice.AbstractJDBCDataService}
 * 
 * @author Daniele Turi
 * @version $Id: MySQLDataService.java,v 1.1 2007-12-14 12:49:07 stain Exp $
 */
public class MySQLDataService extends AbstractJDBCDataService {

    static Logger log = Logger.getLogger(MySQLDataService.class.getName());

    /**
     * Create a new JDBC backed DataService with the supplied properties.
     * 
     * @throws DataServiceCreationException
     */
    public MySQLDataService(Properties props) throws DataServiceException {
        super(props);
    }

    /**
     * Reurns false because both {@link #createTables()} and
     * {@link #destroyTables()} do the check for exists.
     * 
     * @see uk.org.mygrid.provenance.dataservice.AbstractJDBCDataService#tablesExist()
     */
    @Override
    public boolean tablesExist() {
        return false;
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
                    .executeUpdate("CREATE TABLE IF NOT EXISTS datathings (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                            + "                                       thing TEXT NOT NULL,"
                            + "                                       PRIMARY KEY(id)) TYPE = InnoDB;");
            log.info("Created table datathings");
            st
                    .executeUpdate("CREATE TABLE IF NOT EXISTS lsid2datathings (lsid CHAR(200) NOT NULL UNIQUE,"
                            + "                    id INT UNSIGNED NOT NULL REFERENCES datathings(id),"
                            + "                    reftype ENUM(\"datathing\",\"collection\",\"leaf\"),"
                            + "                    mimetype CHAR(200),"
                            + "                    PRIMARY KEY(lsid)) TYPE = InnoDB;");
            log.info("Created table lsid2datathings");
            st
                    .executeUpdate("CREATE TABLE IF NOT EXISTS data (id INT UNSIGNED NOT NULL AUTO_INCREMENT,"
                            + "                                       data TEXT NOT NULL,"
                            + "                                       PRIMARY KEY(id)) TYPE = InnoDB;");
            log.info("Created table data");
            st
                    .executeUpdate("CREATE TABLE IF NOT EXISTS lsid2data (lsid CHAR(200) NOT NULL,"
                            + "                                          id INT UNSIGNED NOT NULL REFERENCES data(id)"
                            + "                                          ) TYPE = InnoDB;");
            log.info("Created table lsid2data");
            st.executeUpdate("CREATE TABLE IF NOT EXISTS workflow"
                    + " (lsid CHAR(200) NOT NULL, "
                    + " workflow TEXT NOT NULL, "
                    + " PRIMARY KEY(lsid)) TYPE=InnoDB");
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
            st.executeUpdate("DROP TABLE IF EXISTS datathings");
            st.executeUpdate("DROP TABLE IF EXISTS lsid2datathings");
            st.executeUpdate("DROP TABLE IF EXISTS data");
            st.executeUpdate("DROP TABLE IF EXISTS lsid2data");
            st.executeUpdate("DROP TABLE IF EXISTS workflow");

            pool.returnConnection(con);
            log.debug("...finished dropping tables");
        } catch (Exception ex) {
            log.error(ex.getMessage(), ex);
            pool.returnConnection(con);
            throw new DataServiceException(ex);
        }
    }

//    public static void validate(Properties configuration) {
//        String connectionURL = null;
//        String driver = ProvenanceConfigurator.MYSQL_JDBC_DRIVER;
//        Class.forName(driver).newInstance();
//        connectionURL = getConnectionURL(configuration);
//        String username = getUser(configuration);
//        String password = getPassword(configuration);
//        DriverManager.getConnection(connectionURL, username, password);
//    }

    @Override
    public String getDriver() {
        String driver = ProvenanceConfigurator.MYSQL_JDBC_DRIVER;
        return driver;
    }

    @Override
    public String getConnectionURL() throws DataServiceException {
        return getConnectionURL(getConfiguration());
    }

    public static String getConnectionURL(Properties properties)
            throws DataServiceException {
        String connectionproperty = properties
                .getProperty(ProvenanceConfigurator.MYSQL_CONNECTION_URL);
        if (connectionproperty == null) {
            try {
                ProvenanceConfigurator
                        .missingPropertyMessage(ProvenanceConfigurator.MYSQL_CONNECTION_URL);
            } catch (PropertyMissingException e) {
                throw new DataServiceException(e);
            }
        }
        return connectionproperty;
    }

    @Override
    public String getUser() throws DataServiceException {
        return getUser(getConfiguration());
    }

    public static String getUser(Properties properties)
            throws DataServiceException {
        String userProperty = properties
                .getProperty(ProvenanceConfigurator.MYSQL_USER);
        if (userProperty == null) {
            try {
                ProvenanceConfigurator
                        .missingPropertyMessage(ProvenanceConfigurator.MYSQL_USER);
            } catch (PropertyMissingException e) {
                throw new DataServiceException(e);
            }
        }
        return userProperty;
    }

    @Override
    public String getPassword() {
        return getPassword(getConfiguration());
    }

    public static String getPassword(Properties properties) {
        return properties.getProperty(ProvenanceConfigurator.MYSQL_PASSWORD);
    }

    protected @Override
    String identity() throws SQLException {
        return "LAST_INSERT_ID()";
    }

    protected @Override
    int insertData(Connection con, String data) throws SQLException {
        PreparedStatement st = con
                .prepareStatement("INSERT INTO data (data) VALUES (?)");
        st.setString(1, data);
        st.executeUpdate();
        ResultSet generatedKeys = st.getGeneratedKeys();
        generatedKeys.first();
        int rowStoringData = generatedKeys.getInt(1);
        st.close();
        return rowStoringData;
    }

}
