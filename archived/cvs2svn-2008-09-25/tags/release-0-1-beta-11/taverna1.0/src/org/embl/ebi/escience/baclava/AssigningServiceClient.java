/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

import com.ibm.lsid.LSIDException;
import com.ibm.lsid.client.LSIDAssigner;
import com.ibm.lsid.wsdl.SOAPLocation;
import java.net.URL;
import java.net.MalformedURLException;
import org.apache.log4j.Logger;
import java.util.Map;
import java.util.HashMap;
import java.util.Properties;

/**
 * An implementation of the LSIDProvider interface which connects
 * through to an instance of the LSID assigning service to acquire
 * LSIDs
 * @author Tom Oinn
 */
public class AssigningServiceClient implements LSIDProvider {
    
    static int count = 0;
    
    static Logger log = Logger.getLogger(AssigningServiceClient.class.getName());
    
    String targetAuthority = null;
    String serviceEndpoint = null;
    LSIDAssigner assigner = null;
    private String wfdefinitionNS, wfinstanceNS, datathingLeafNS, datathingCollectionNS;
    private Map perNamespaceAssigners = new HashMap();

    /**
     * Default constructor so an instance can be
     * created for use by the enactor framework.
     * Reads the system properties defined in the
     * mygrid.properties file to configure the
     * target authority name and the assigning
     * service endpoint.
     */
    public AssigningServiceClient() {
	this.serviceEndpoint = System.getProperty("taverna.lsid.asclient.endpoint");
	// Try to build a new URL from the endpoint
	if (serviceEndpoint != null) {
	    try {
		URL testValid = new URL(serviceEndpoint);
	    }
	    catch (MalformedURLException mue) {
		log.error("Unable to use the endpoint provided, not a valid URL",mue);
		serviceEndpoint = null;
	    }
	}
	if (serviceEndpoint == null) {
	    // Error, unable to establish an endpoint
	    log.error("No endpoint specified, unable to create the assigner");
	}
	else {

	    // Populate the target namespaces from the system properties
	    wfdefinitionNS = System.getProperty("taverna.lsid.asclient.ns.wfdefinition","WorkflowDefinition");
	    wfinstanceNS = System.getProperty("taverna.lsid.asclient.ns.wfinstance","WorkflowInstance");
	    datathingLeafNS = System.getProperty("taverna.lsid.asclient.ns.datathingleaf","DataThing");
	    datathingCollectionNS = System.getProperty("taverna.lsid.asclient.ns.datathingcollection","DataThing");
	    
	    // Create a new assigner
	    this.assigner = new LSIDAssigner(new SOAPLocation(serviceEndpoint));
	    log.debug("Created new assigning service client with endpoint "+serviceEndpoint);

	    // Check whether the supplied authority was null, and if so find a suitable
	    // authority for each namespace. Fail if we can't do this.
	    try {
		String[][] authorities = this.assigner.getAuthoritiesAndNamespaces();
		for (int i = 0; i < authorities.length; i++) {
		    String authority = authorities[i][0];
		    String namespace = authorities[i][1];
		    System.out.println("Found auth = "+authority+" for ns = "+namespace);
		    // Check whether the pair matches requirements for a namespace
		    // we want to use, and store it if it does
		    if (namespace.equals(wfdefinitionNS)) {
			perNamespaceAssigners.put(LSIDProvider.WFDEFINITION,
						  new LSIDInfo(namespace, authority, assigner));
			log.debug("Assigning service can assign workflow definition LSIDs using namespace "+
				  namespace+" in "+authority);
		    }
		    else if (namespace.equals(wfinstanceNS)) {
			perNamespaceAssigners.put(LSIDProvider.WFINSTANCE,
						  new LSIDInfo(namespace, authority, assigner));
			log.debug("Assigning service can assign workflow instance LSIDs using namespace "+
				  namespace+" in "+authority);
		    }
		    else if (namespace.equals(datathingLeafNS)) {
			perNamespaceAssigners.put(LSIDProvider.DATATHINGLEAF,
						  new LSIDInfo(namespace, authority, assigner));
			log.debug("Assigning service can assign datathing leaf LSIDs using namespace "+
				  namespace+" in "+authority);
		    }
		    else if (namespace.equals(datathingCollectionNS)) {
			perNamespaceAssigners.put(LSIDProvider.DATATHINGCOLLECTION,
						  new LSIDInfo(namespace, authority, assigner));
			log.debug("Assigning service can assign datathing collection LSIDs using namespace "+
				  namespace+" in "+authority);
		    }
		}
	    }
	    catch (LSIDException le) {
		log.error("Exception when trying to fetch available authorities",le);
		this.assigner = null;
	    }
	}
	
    }
    
    /**
     * Return a unique LSID based on the pattern acquired from the assigning service
     * and combined with a per-namespace int counter.
     */
    public String getID(LSIDProvider.NamespaceEnumeration namespace) {
	LSIDInfo value = (LSIDInfo)perNamespaceAssigners.get(namespace);
	if (value == null) {
	    log.error("No mapping found for namespace type "+namespace.toString());
	    return "";
	}
	return value.getNextLSID();
    }

    class LSIDInfo {
	private String baseLSID;
	private int currentCount = 0;
	public LSIDInfo(String namespace, String authority, LSIDAssigner l) {
	    try {
		this.baseLSID = l.getLSIDPattern(authority,
						 namespace,
						 new Properties());
		if (baseLSID.startsWith("urn:lsid")==false) {
		    // Hack to cope with either interpretation of the LSID
		    // assigning service specification
		    baseLSID = "urn:lsid:"+authority+":"+namespace+":"+baseLSID;
		}
	    }
	    catch (LSIDException le) {
		log.error("Unable to fetch a base LSID",le);
		this.baseLSID = "";
	    }
	}
	public synchronized String getNextLSID() {
	    return this.baseLSID+(currentCount++);
	}
    }

}
