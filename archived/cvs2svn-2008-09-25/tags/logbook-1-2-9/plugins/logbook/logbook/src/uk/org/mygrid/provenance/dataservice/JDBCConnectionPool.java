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
 * Filename           $RCSfile: JDBCConnectionPool.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-12-14 12:49:07 $
 *               by   $Author: stain $
 * Created on 03-May-2006
 *****************************************************************/
package uk.org.mygrid.provenance.dataservice;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import org.apache.log4j.Logger;


/**
 * Class extracted from
 * {@link org.embl.ebi.escience.baclava.store.JDBCBaclavaDataService}.
 * 
 * @author dturi
 * @version $Id: JDBCConnectionPool.java,v 1.1 2007-12-14 12:49:07 stain Exp $
 */
public class JDBCConnectionPool extends ObjectPool {

    String driver, dsn, usr, pwd;

    private static Logger log = Logger.getLogger(JDBCConnectionPool.class);

    public Connection borrowConnection() throws ObjectPoolException {
        log.debug("Thread " + Thread.currentThread().toString()
                + " requested connection.");
        Connection con = (Connection) super.checkOut();
        log.debug("Thread " + Thread.currentThread().toString()
                + " acquired connection.");
        return con;
    }

    public void returnConnection(Connection c) {
        super.checkIn(c);
        log.debug("Thread " + Thread.currentThread().toString()
                + " returned connection.");
    }

    public JDBCConnectionPool(String driver, String dsn, String usr,
            String pwd, int maxConnections) throws ObjectPoolException {
        try {
            Class.forName(driver).newInstance();
        } catch (Exception e) {

        }
        this.dsn = dsn;
        this.usr = usr;
        this.pwd = pwd;
        this.maxObjects = maxConnections;
        testConnection();
    }

    private void testConnection() throws ObjectPoolException {
        Connection connection = borrowConnection();
        returnConnection(connection);
    }

    Object create() throws ObjectPoolException {
        try {
            Connection con = DriverManager.getConnection(dsn, usr, pwd);
            con.setAutoCommit(false);
            return con;
        } catch (SQLException e) {
            throw new ObjectPoolException(e);
        }
    }

    void expire(Object o) {
        try {
            ((Connection) o).close();
        } catch (SQLException e) {
            log.error("Could not close connection", e);
        }
    }

    boolean validate(Object o) {
        try {
            return (!((Connection) o).isClosed());
        } catch (SQLException e) {
        	   	log.error("Could not validate connection", e);            
            return (false);
        }
    }


}
