/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.ouzo.testclient;

import com.ibm.lsid.MetadataResponse;
import com.ibm.lsid.client.LSIDAuthority;
import com.ibm.lsid.client.LSIDResolver;
import com.ibm.lsid.client.async.AsyncLSIDResolver;
import com.ibm.lsid.client.async.ResolutionListener;
import com.ibm.lsid.client.metadata.LSIDMetadata;
import com.ibm.lsid.client.metadata.rdf.jena.JenaMetadataStore;
import com.ibm.lsid.client.metadata.rdf.xslt.XSLTMetadata;
import com.ibm.lsid.wsdl.LSIDDataPort;
import com.ibm.lsid.wsdl.LSIDMetadataPort;
import com.ibm.lsid.wsdl.LSIDWSDLWrapper;
import com.ibm.lsid.wsdl.WSDLConstants;
import com.ibm.lsid.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;

/**
 * Makes a test call to the LSID authority located on 
 * localhost:8080/authority using SOAP. The first and
 * only argument should contain an LSID to fetch.
 * @author Tom Oinn
 */
public class TestClient implements WSDLConstants {
    
    public static void main(String[] args) throws Exception {
	String lsidString = args[0];
	LSID lsid = new LSID(lsidString);
	LSIDResolver resolver = new LSIDResolver(lsid);
	System.out.println("Found a resolver for "+lsid.toString());
	LSIDWSDLWrapper wrapper = resolver.getWSDLWrapper();
	LSIDDataPort dataport = wrapper.getDataPortForProtocol(SOAP);
	if (dataport == null) {
	    System.out.println("No data available");
	}
	else {
	    SAXBuilder sb = new SAXBuilder();
	    Document result = sb.build(resolver.getData(dataport));
	    XMLOutputter xo = new XMLOutputter();
	    xo.setNewlines(true);
	    xo.setIndent("  ");
	    System.out.println(xo.outputString(result));
	}
    }

}
