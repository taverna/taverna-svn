/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo;

import com.ibm.lsid.LSID;
import com.ibm.lsid.server.LSIDServerException;
import java.sql.Connection;
import java.sql.DriverManager;

// IO Imports
import java.io.ByteArrayInputStream;
import java.io.InputStream;

import java.lang.Class;
import java.lang.Exception;
import java.lang.String;



/**
 * Performs the actual data fetch and interpretation
 * delegated from the various authority implementations.
 * @author Tom Oinn
 */
public class TavernaLSIDDataLookup {
    
    private static Connection connection = null;
    
    public static final int UNKNOWN = 1;
    public static final int ABSTRACT = 2;
    public static final int CONCRETE = 4;
    
    /**
     * Create a new instance, initialises the connection to HSQLDB
     * if this hasn't already been done.
     * @exception LSIDServerException thrown if the database driver
     * cannot be created or there is some other problem connecting
     * to the specified database.
     */
    public TavernaLSIDDataLookup() throws LSIDServerException {
	// Don't do anything if the connection to the database
	// has already been created.
	if (connection != null) {
	    return;
	}
	// Load the HSQLDB driver
	try {
	    Class.forName("org.hsqldb.jdbcDriver");
	    // For now just use a test database in the root directory
	    // ** TODO ** - make this configurable
	    connection = DriverManager.getConnection("jdbc:hsqldb:/testauthority","ouzo","");
	}
	catch (Exception e) {
	    throw new LSIDServerException(e, 500, "Cannot instantiate hsqldb driver");
	}
    }
    
    /**
     * Return the type of the supplied LSID value, whether
     * unknown, abstract or concrete. Abstract value may
     * have metadata but no data, concrete values may have
     * both and unknown values are pretty much useless.
     * @param lsid an LSID object to query, supplied by
     * the default IBM authority implementation to this
     * class.
     * @return int corresponding to the type of the given LSID.
     * @exception LSIDServerException thrown if the class
     * can't connect to query the database for some reason.
     */
    public int lsidType(LSID lsid) throws LSIDServerException {
	// Namespace, for now the only namespace that
	// we recognize is taverna-workflow. All other
	// LSIDs to this authority will return unknown,
	// workflow definitions are always concrete.
	String ns = lsid.getNamespace();
	String id = lsid.getObject();
	String ver = lsid.getRevision();
	
	// ** TODO ** - check in the database whether the given
	// workflow definition actually exists, and throw back
	// unknown type if it doesn't.
	if (ns.equalsIgnoreCase("taverna-workflow")) {
	    return CONCRETE;
	}
	else {
	    return UNKNOWN;
	}
    }
    
    /**
     * Return an InputStream for the given LSID value.
     * @param lsid the LSID value to get data for.
     * @return an InputStream object used to access
     * the data in the supplied LSID.
     * @exception LSIDServerException thrown if any
     * error occurs during connection to the database
     * or if the supplied LSID doesn't exist in this 
     * authority.
     */
    public InputStream lsidData(LSID lsid) throws LSIDServerException {
	String ns = lsid.getNamespace();
	String id = lsid.getObject();
	String ver = lsid.getRevision();
	
	// Refuse to return anything that isn't a taverna workflow
	if (ns.equalsIgnoreCase("taverna-workflow") == false) {
	    throw new LSIDServerException(201, "Unknown LSID");
	}

	// Return a dummy string for all LSID values that match
	// the namespace
	// ** TODO ** - actually implement this!
	String dummyResponse = "An LSID value here!";
	return new ByteArrayInputStream(dummyResponse.getBytes());
    }
    
}
