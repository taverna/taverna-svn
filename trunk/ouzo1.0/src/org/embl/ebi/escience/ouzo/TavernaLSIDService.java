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
	
	String[] parts = theLSIDString.split(":");
	String namespace = parts[3];
	String theDataThingLSIDString = theLSIDString;
	if (namespace.equals("datathing") == false) {
	    // Construct a new LSID with the namespace replaced by 'datathing'
	    theDataThingLSIDString = parts[0]+":"+parts[1]+":"+parts[2]+":datathing:"+parts[4];
	    if (parts.length == 6) {
		// has version
		theDataThingLSIDString = theDataThingLSIDString + ":" + parts[5];
	    }
	}
	
	try {
	    DataThing theThing = theDataService.fetchDataThing(theDataThingLSIDString);
	    if (namespace.equals("datathing")) {
		Document doc = new Document(DataThingXMLFactory.getElement(theThing));
		XMLOutputter xo = new XMLOutputter();
		xo.setIndent("  ");
		xo.setNewlines(true);
		String xmlRepresentation = xo.outputString(doc);
		return new ByteArrayInputStream(xmlRepresentation.getBytes());
	    }
	    else if (namespace.equals("raw")) {
		Object o = theThing.getDataObject();
		if (o instanceof String) {
		    return new ByteArrayInputStream(((String)o).getBytes());
		}
		else if (o instanceof byte[]) {
		    return new ByteArrayInputStream((byte[])o);
		}
	    }
	    throw new NoSuchLSIDException();
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
	return theDataService.hasData(request.getLsid().getLsid());
    }

    
    /**
     * Returns whether or not the given LSID has metadata. The 
     * implementer may assume that the LSID is valid 
     */
    public boolean hasMetadata(LSIDRequestContext request) {
	return theDataService.hasMetadata(request.getLsid().getLsid());
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
