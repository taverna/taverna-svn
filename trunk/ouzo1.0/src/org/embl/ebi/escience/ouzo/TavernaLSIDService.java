/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo;

import com.ibm.lsid.server.impl.*;
import com.ibm.lsid.server.*;
import com.ibm.lsid.*;

import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.store.*;

import java.util.*;
import java.io.*;

/**
 * An extension of the SimpleResolutionService that
 * connects to a BaclavaDataService instance to serve
 * data and metadata in the form of DataThing documents.<p>
 * Javadoc has been copied verbatim from the IBM docs
 * to ease development.
 * @author Tom Oinn
 */
public class TavernaLSIDService extends SimpleResolutionService {
    
    /**
     * The data service object that actually connects to 
     * the database etc
     */
    private BaclavaDataService theDataService;
    

    /**
     * Initialize the service, will create a connection to
     * the real underlying data service.
     */
    public void initService(LSIDServiceConfig config) 
	throws LSIDServerException {
	System.out.println("Creating new TavernaLSIDService instance");
	
	Properties serviceProps = new Properties();
	for (Enumeration en = config.getPropertyNames(); en.hasMoreElements();) {
	    String propertyName = (String)en.nextElement();
	    String propertyValue = config.getProperty(propertyName);
	    System.out.println("  "+propertyName+" = "+propertyValue);
	    serviceProps.setProperty(propertyName, propertyValue);
	}
	
	theDataService = new JDBCBaclavaDataService(serviceProps);
	System.out.println("Created new TavernaLSIDService and data service object");
    }

    
    /**
     * Get the data for a particular LSID
     * @return InputStream an input stream to the data, 
     * null if no data exists 
     */
    public InputStream getData(LSIDRequestContext request)
	throws LSIDServerException {
	return null;
    }

    
    /**
     * Get the metadata for a particular LSID
     * @return InputStream an input stream to the metadata, 
     * null if no data exists 
     */
    public MetadataResponse getMetadata(LSIDRequestContext request,
					String[] acceptedFormats)
	throws LSIDServerException {
	return null;
    }


    /**
     * Returns the name of the service, in this
     * case the string 'TavernaLSIDService'
     */
    public String getServiceName() {
	return "TavernaLSIDService";
    }


    /**
     * Returns whether or not the given LSID has metadata. The 
     * implementer may assume that the LSID is valid 
     */
    public boolean hasData(LSIDRequestContext request) {
	return true;
    }

    
    /**
     * Returns whether or not the given LSID has metadata. The 
     * implementer may assume that the LSID is valid 
     */
    public boolean hasMetadata(LSIDRequestContext request) {
	return false;
    }
    

    /**
     * Validate an LSID. This method is called be SimpleResolutionService 
     * to determine if a WSDL should be returned for the given LSID. If 
     * the LSID is invalid, an LSIDServerException should be thrown.
     * @throws LSIDServerException if the LSID is not a valid Taverna LSID
     */
    public void validate(LSIDRequestContext request) {
	//
    }


}
