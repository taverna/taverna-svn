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
import org.jdom.input.*;
import org.apache.log4j.*;

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
    public static BaclavaDataService theDataService = null;
    
    static Logger log = Logger.getLogger(TavernaLSIDService.class.getName());


    /**
     * initialize the service, will create a connection to
     * the real underlying data service.
     */
    public void initService(LSIDServiceConfig config) 
	throws LSIDServerException {
	System.out.println("Creating new TavernaLSIDService instance");
	
	if (theDataService == null) {
	    Properties serviceProps = new Properties();
	    for (Enumeration en = config.getPropertyNames(); en.hasMoreElements();) {
		String propertyName = (String)en.nextElement();
		String propertyValue = config.getProperty(propertyName);
		serviceProps.setProperty(propertyName, propertyValue);
	    }
	    theDataService = new JDBCBaclavaDataService(serviceProps);
	    System.out.println("Created new TavernaLSIDService and data service object");
	}
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

    
    private static final String RDF_NS = "http://www.w3.org/1999/02/22-rdf-syntax-ns#";
    private static final String DC_NS = "http://purl.org/dc/elements/1.1/";
    private static final String I3CP_NS= "urn:lsid:i3c.org:predicates:";
    private static final String TAVERNA_PROVENANCE_NS = "urn:lsid:net.sf.taverna:predicates:";
    private static final String I3C_CONTENT= "urn:lsid:i3c.org:types:content";
    //private static final String I3C_SPROT= "urn:lsid:i3c.org:formats:sprot";
    //private static final String I3C_FASTA= "urn:lsid:i3c.org:formats:fasta";
    private static final String TAVERNA_DATATHING = "urn:lsid:net.sf.taverna:types:datathing";
    private static final String TAVERNA_RAW = "urn:lsid:net.sf.taverna:types:raw";
    private static final String TAVERNA_MIME_PREFIX = "urn:lsid:net.sf.taverna:mimetypes:";

    /**
     * Get the metadata for a particular LSID
     * @return InputStream an input stream to the metadata, 
     * null if no data exists 
     */
    public MetadataResponse getMetadata(LSIDRequestContext request,
					String[] acceptedFormats)
	throws LSIDServerException {
	StringBuffer result= new StringBuffer();
	result.append("<?xml version=\"1.0\"?>\n<rdf:RDF");
	result.append(" xmlns:rdf=\""+RDF_NS+"\"");
	result.append(" xmlns:dc=\""+DC_NS+"\"");
	result.append(" xmlns:i3cp=\""+I3CP_NS+"\"");
	result.append(" xmlns:tavp=\""+TAVERNA_PROVENANCE_NS+"\"");
	result.append(">\n");

	// If the namespace is 'datathing' then set metadata appropriately
	if (request.getLsid().getNamespace().equals("datathing")) {
	    appendTripleResource(result, request.getLsid().getLsid(), "rdf:type", I3C_CONTENT);	    
	    appendTripleResource(result, request.getLsid().getLsid(), "dc:format", TAVERNA_DATATHING);

	    try {
		LSID rawFormatLSID = new LSID(request.getLsid().getAuthority().getAuthority(),
					      "raw",
					      request.getLsid().getObject(),
					      request.getLsid().getRevision());
		if (theDataService.hasData(rawFormatLSID.getLsid())) {
		    appendTripleResource(result, request.getLsid().getLsid(), "i3cp:storedas", rawFormatLSID.getLsid());
		}
	    }
	    catch (MalformedLSIDException mle) {
		//
	    }
	}
	else if (request.getLsid().getNamespace().equals("raw")) {
	    appendTripleResource(result, request.getLsid().getLsid(), "rdf:type", I3C_CONTENT);
	    try {
		LSID datathingFormatLSID = new LSID(request.getLsid().getAuthority().getAuthority(),
						    "datathing",
						    request.getLsid().getObject(),
						    request.getLsid().getRevision());
		String mime = ((JDBCBaclavaDataService)theDataService).getMIMEType(datathingFormatLSID.getLsid());
		if (mime == null) {
		    throw new Exception();
		}
		String[] mimeParts = mime.split("/");
		// Avoid the '/' character in LSID, it's not valid
		mime = mimeParts[0]+"."+mimeParts[1];
		appendTripleResource(result, request.getLsid().getLsid(), "dc:format", TAVERNA_MIME_PREFIX+mime);
	    }
	    catch (Exception ex) {
		appendTripleResource(result, request.getLsid().getLsid(), "dc:format", TAVERNA_RAW);
	    }
	}
	String additionalMetadata = theDataService.getMetadata(request.getLsid().getLsid());
	if (additionalMetadata!=null) {
	    result.append(additionalMetadata);
	}
	result.append("</rdf:RDF>");
	try {
	    XMLOutputter xo = new XMLOutputter();
	    xo.setNewlines(true);
	    xo.setIndent("  ");
	    String provenance = xo.outputString(new SAXBuilder(false).build(new StringReader(result.toString())));
	    log.debug("Returned provenance data : "+provenance);
	    InputStream metadataStream = new ByteArrayInputStream(provenance.getBytes());
	    return new MetadataResponse(metadataStream, null);
	}
	catch (JDOMException jde) {
	    log.error("Error normalizing XML provenance!\n"+result.toString(),jde);
	    return new MetadataResponse(new ByteArrayInputStream(("<rdf:RDF xmlns:rdf=\""+RDF_NS+"\"/>").getBytes()),null);
	}
    }
    private void appendTripleResource(StringBuffer src, String subj, String pred, String obj) {
	src.append("<rdf:Description rdf:about=\""+subj+"\">\n");
	src.append("  <"+pred+" rdf:resource=\""+obj+"\"/>\n");
	src.append("</rdf:Description>\n");
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
	// All entries with data also have metadata, in this current
	// implementation there are no abstract entities.
	return (theDataService.hasMetadata(request.getLsid().getLsid()) || hasData(request));
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
