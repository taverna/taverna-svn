/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.store;

import java.util.*;
import java.io.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.store.*;
import org.embl.ebi.escience.baclava.factory.*;
import org.jdom.*;
import org.jdom.output.*;
import org.jdom.input.*;
import org.apache.log4j.*;
import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import java.net.*;
import javax.xml.rpc.*;
import java.rmi.*;

/**
 * Connects to the DataService.jws axis service in the
 * Ouzo1.0 project to provide data / metadata storage
 * and LSID allocation
 * @author Tom Oinn
 */
public class RemoteSOAPStore implements BaclavaDataService, LSIDProvider {

    URL serviceEndpoint = null;

    static Logger log = Logger.getLogger(RemoteSOAPStore.class.getName());

    public RemoteSOAPStore() {
	this(System.getProperties());
    }

    public RemoteSOAPStore(Properties props) {
	String endpointString = props.getProperty("taverna.datastore.soap.endpoint");
	if (endpointString == null) {
	    log.error("Property taverna.datastore.soap.endpoint is not defined, cannot\n"+
		      "create the RemoteSOAPStore proxy object.");
	    throw new RuntimeException("No store configured in properties!");
	}
	try {
	    serviceEndpoint = new URL(endpointString);
	}
	catch (MalformedURLException mue) {
	    log.error("Endpoint URL is malformed",mue);
	    throw new RuntimeException("Malformed endpoint URL!");
	}
    }

    public void storeDataThing(DataThing theDataThing, boolean silent)
	throws DuplicateLSIDException {
	Document doc = new Document(DataThingXMLFactory.getElement(theDataThing));
	XMLOutputter xo = new XMLOutputter(Format.getPrettyFormat());
	String stringifiedThing = xo.outputString(doc);
	try {
	    Call call = (Call) new Service().createCall();
	    call.setTargetEndpointAddress(serviceEndpoint);
	    call.setOperationName(new QName("storeDataThing"));
	    Boolean operationSucceeded = (Boolean)(call.invoke(new Object[]{ stringifiedThing, new Boolean(silent)}));
	    if (operationSucceeded.booleanValue() == false && silent == false) {
		throw new DuplicateLSIDException();
	    }
	}
	catch (RemoteException re) {
	    log.error("Exception when calling remote service",re);
	    throw new RuntimeException("Failed to store datathing in SOAP store");
	}
	catch (ServiceException se) {
	    log.error("Service exception when calling remote service",se);
	    throw new RuntimeException("Service exception creating call object!");
	}
    }

    public DataThing fetchDataThing(String LSID) 
	throws NoSuchLSIDException {
	try {
	    Call call = (Call) new Service().createCall();
	    call.setTargetEndpointAddress(serviceEndpoint);
	    call.setOperationName(new QName("fetchDataThing"));
	    String stringifiedThing = (String)(call.invoke(new Object[]{ LSID }));
	    try {
		DataThing theDataThing = null;
		SAXBuilder builder = new SAXBuilder(false);
		Document doc = builder.build(new StringReader(stringifiedThing));
		theDataThing = new DataThing(doc.getRootElement());
		return theDataThing;
	    }
	    catch (JDOMException jde) {
		log.error("Error whilst reconstructing DataThing from XML",jde);
		throw new NoSuchLSIDException();
	    }
	}
	catch (Exception e) {
	    NoSuchLSIDException nsle = new NoSuchLSIDException();
	    nsle.initCause(e);
	    throw nsle;
	}
    }
    
    public void storeMetadata(String theMetadata) {
	try {
	    Call call = (Call) new Service().createCall();
	    call.setTargetEndpointAddress(serviceEndpoint);
	    call.setOperationName(new QName("storeMetadata"));
	    call.invoke(new Object[]{ theMetadata });
	}
	catch (RemoteException re) {
	    log.error("Remote exception when storing metadata",re);
	    throw new RuntimeException("Cannot store metadata!");
	}
	catch (ServiceException se) {
	    log.error("Service exception when calling remote service",se);
	    throw new RuntimeException("Service exception creating call object!");
	}
    }

    public String getID(LSIDProvider.NamespaceEnumeration namespaceObject) {
	String namespace = namespaceObject.toString();
	// Does the map already contain an ID for that
	// namespace?
	String ID = (String)idMap.get(namespace);
	if (ID != null) {
	    // Use the cached ID and append a counter to it
	    return ID + nextCounter++;
	}
       	else {
	    // regenerate the cached ID
	    try {
		Call call = (Call) new Service().createCall();
		call.setTargetEndpointAddress(serviceEndpoint);
		call.setOperationName(new QName("getID"));
		idMap.put(namespace, (String)(call.invoke(new Object[]{ namespace })));
		return (String)idMap.get(namespace)+nextCounter++;
	    }
	    catch (RemoteException re) {
		log.error("Remote exception when getting identifier",re);
		throw new RuntimeException("Cannot fetch metadata!");
	    }
	    catch (ServiceException se) {
		log.error("Service exception when calling remote service",se);
		throw new RuntimeException("Service exception creating call object!");
	    }
	}
    }
    private Map idMap = new HashMap();
    private int nextCounter = 0;
    
    // These are never used by the current implementation
    public boolean hasData(String LSID) {
	throw new UnsupportedOperationException();
    }
    public boolean hasMetadata(String LSID) {
	throw new UnsupportedOperationException();
    }
    public String getMetadata(String LSID) {
	throw new UnsupportedOperationException();
    }


}
