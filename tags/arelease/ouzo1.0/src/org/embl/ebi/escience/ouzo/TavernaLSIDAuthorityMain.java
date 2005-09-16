/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo;

import com.ibm.lsid.ExpiringResponse;
import com.ibm.lsid.LSID;
import com.ibm.lsid.server.LSIDServerException;
import com.ibm.lsid.server.LSIDServiceConfig;
import com.ibm.lsid.server.impl.HTTPDataLocation;
import com.ibm.lsid.server.impl.SOAPDataLocation;
import com.ibm.lsid.server.impl.SOAPMetaDataLocation;
import com.ibm.lsid.server.impl.SimpleAuthority;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetaDataPort;

// Utility Imports
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.embl.ebi.escience.ouzo.TavernaLSIDDataLookup;
import java.lang.Integer;
import java.lang.String;



/**
 * The main part of the LSID authority implementation for Taverna.
 * This code is taken from IBMs sample code and modified to suit,
 * there is no authorship claimed in the original file (or comments
 * of any kind, come on IBM, this is meant to be a tutorial for
 * heaven's sake).<p>
 * This class is the primary authority, it takes LSID values and
 * returns locations from where the appropriate data and metadata
 * for the value may be found. In the first case for this authority
 * we'll just return workflow definition documents, this seems like
 * a reasonably sensible thing to do, then move on to workflow
 * instances (abstract) and the intermediate values as and when 
 * the simple stuff is up and running.
 * @author Tom Oinn
 */
public class TavernaLSIDAuthorityMain extends SimpleAuthority {
    
    /**
     * The TavernaLSIDDataLookup code actually does the
     * heavy lifting of talking to the database to 
     * fetch information about workflows, intermediate
     * results etc.
     */
    private TavernaLSIDDataLookup lookup = null;
    
    /**
     * Create a new TavernaLSIDDataLookup object, this should
     * cause a connection to the underlying DB to be created
     * and all the various initialization routines required.
     */
    public void initAuthority(LSIDServiceConfig cf) throws LSIDServerException {
	lookup = new TavernaLSIDDataLookup();
    } 
    
    public ExpiringResponse getKnownURIs() throws LSIDServerException {
	return null;
    }
    
    /**
     * If metadata for the supplied object exists then return a
     * pointer to the metadata service embedded within this
     * authority, otherwise return null to indicate a fault condition.
     */
    public LSIDMetaDataPort[] getMetaDataLocations(LSID lsid, String url) {
	if (lookup == null) {
	    // Error if there's no data lookup object
	    return null;
	}
	int lsType;
	try {
	    lsType = lookup.lsidType(lsid);
	}
	catch (LSIDServerException ex) {
	    ex.printStackTrace();
	    lsType = TavernaLSIDDataLookup.UNKNOWN;
	}
	if (lsType == TavernaLSIDDataLookup.UNKNOWN) {
	    // Error if either unknown type (therefore no metadata) or
	    // an exception occurs when determining the type.
	    return null;
	}
	HostDescriptor hd = new HostDescriptor(url);
	// Found an item, has metadata potentially so return the port
	// within this authority to query it.
	return new LSIDMetaDataPort[] {
	    new SOAPMetaDataLocation(hd.baseURL + "metadata")
	};
    }
    
    public LSIDDataPort[] getDataLocations(LSID lsid, String url) {
	if (lookup == null) {
	    // Error if there's no data object
	    return null;
	}
	int lsType;
	try {
	    lsType = lookup.lsidType(lsid);
	}
	catch (LSIDServerException ex) {
	    ex.printStackTrace();
	    lsType = TavernaLSIDDataLookup.UNKNOWN;
	}
	if (lsType == TavernaLSIDDataLookup.UNKNOWN) {
	    // Unknown type so we can't determine where to get
	    // data from, return null to indicate the fault.
	    return null;
	}
	if (lsType == TavernaLSIDDataLookup.ABSTRACT) {
	    // Abstract type, therefore no actual data associated
	    // with it, return an empty list of endpoints.
	    return new LSIDDataPort[0];
	}
	HostDescriptor hd = new HostDescriptor(url);
	// Return a pair of endpoints for the data fetch, one for
	// SOAP (Web service) and one for basic HTTP GET
	return new LSIDDataPort[] {
	    new SOAPDataLocation(hd.baseURL + "data"),
	    new HTTPDataLocation(hd.host, hd.port, hd.pathPrefix + "/authority/data?" + lsid)
	};
    }
    
    private static final Pattern HOST_PTN = Pattern.compile("https?://([^/:]+)(?::(\\d+))?(.*)/authority(.*)");
    /**
     * Internal routine to parse URLs, used by the other methods in this
     * class but not visible externally. Uses the pattern defined above
     * to parse.
     */
    private class HostDescriptor {
	public String host;
	public int port;
	public String pathPrefix;
	public String baseURL;
	
	public HostDescriptor(String url) {
	    host = "localhost";
	    port = -1;
	    pathPrefix = "";
	    if (url != null || url.length() > 0) {
		Matcher m = HOST_PTN.matcher(url);
		if (m.lookingAt()) {
		    host = m.group(1);
		    if (m.group(2).length() > 0)
			port = Integer.parseInt(m.group(2));
		    pathPrefix = m.group(3);
		}
	    }
	    if (port > 0) {
		baseURL = "http://" + host + ":" + port + pathPrefix + "/authority/";
	    }
	    else {
		baseURL = "http://" + host + pathPrefix + "/authority/";
	    }
	}
    }
}
