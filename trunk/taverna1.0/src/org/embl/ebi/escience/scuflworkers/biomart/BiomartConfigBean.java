/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

/**
 * Holds biomart configuration info
 * @author Tom Oinn
 */
public class BiomartConfigBean {
    
    String dbType, dbDriver, dbHost, dbPort, dbInstance, dbUser, dbPassword;
    
    public BiomartConfigBean(String dbType,
			     String dbDriver,
			     String dbHost,
			     String dbPort,
			     String dbInstance,
			     String dbUser,
			     String dbPassword) {
	this.dbType = dbType;
	this.dbDriver = dbDriver;
	this.dbHost = dbHost;
	this.dbPort = dbPort;
	this.dbInstance = dbInstance;
	this.dbUser = dbUser;
	this.dbPassword = ((dbPassword == null || dbPassword.equals("")) ? null : dbPassword);
    }
    
    
			   
	
}
