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
import org.embl.ebi.escience.baclava.factory.*;
import org.embl.ebi.escience.baclava.store.*;

import org.jdom.*;
import org.jdom.output.*;

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
	LSID theLSID = request.getLsid();
	String theLSIDString = theLSID.getLsid();
	System.out.println("Request for LSID : "+theLSIDString);
	try {
	    DataThing theThing = theDataService.fetchDataThing(theLSIDString);
	    
	    Document doc = new Document(DataThingXMLFactory.getElement(theThing));
	    XMLOutputter xo = new XMLOutputter();
	    xo.setIndent("  ");
	    xo.setNewlines(true);
	    String xmlRepresentation = xo.outputString(doc);
	    return new ByteArrayInputStream(xmlRepresentation.getBytes());
	}
	catch (NoSuchLSIDException lsle) {
	    lsle.printStackTrace();
	    return null;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    LSIDServerException lse = new LSIDServerException("Problem accessing the data store");
	    lse.initCause(ex);
	    throw lse;
	}
    }

    
    /**
     * Get the metadata for a particular LSID
     * @return InputStream an input stream to the metadata, 
     * null if no data exists 
     */
    public MetadataResponse getMetadata(LSIDRequestContext request,
					String[] acceptedFormats)
	throws LSIDServerException {
	// Always return null for now, will return chunks of RDF
	// in the near future
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
