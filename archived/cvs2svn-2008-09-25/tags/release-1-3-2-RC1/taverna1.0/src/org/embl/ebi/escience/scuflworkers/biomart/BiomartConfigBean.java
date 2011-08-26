/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import org.ensembl.mart.lib.DetailedDataSource;


/**
 * Holds biomart configuration info
 * @author Tom Oinn
 */
public class BiomartConfigBean {
    
    String dbType, dbDriver, dbHost, dbPort, dbInstance, dbUser, dbPassword, dbSchema;
    String registryURL = null;
    
    public BiomartConfigBean(DetailedDataSource dds) {
	this.dbType = dds.getDatabaseType();
	this.dbDriver = dds.getJdbcDriverClassName();
	this.dbHost = dds.getHost();
	this.dbPort = dds.getPort();
	this.dbInstance = dds.getDatabaseName();
	this.dbUser = dds.getUser();
	this.dbPassword = ((dds.getPassword() == null || dds.getPassword().equals("")) ? null : dds.getPassword());
	this.dbSchema = dds.getSchema();
    }

    public void setRegistryURL(String registryURL) {
	this.registryURL = registryURL;
    }

    public BiomartConfigBean(String dbType,
			     String dbDriver,
			     String dbHost,
			     String dbPort,
			     String dbInstance,
			     String dbUser,
			     String dbPassword,
			     String schema) {
	this.dbType = dbType;
	this.dbDriver = dbDriver;
	this.dbHost = dbHost;
	this.dbPort = dbPort;
	this.dbInstance = dbInstance;
	this.dbUser = dbUser;
	this.dbPassword = ((dbPassword == null || dbPassword.equals("")) ? null : dbPassword);
	this.dbSchema = schema;
    }
    
    
			   
	
}
