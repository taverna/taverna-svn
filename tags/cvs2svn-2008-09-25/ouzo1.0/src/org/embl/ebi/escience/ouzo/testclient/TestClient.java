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
import java.io.*;

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
	System.out.println("Resolving "+lsid.getLsid());
	LSIDWSDLWrapper wrapper = resolver.getWSDLWrapper();
	LSIDDataPort dataport = wrapper.getDataPortForProtocol(SOAP);
	if (dataport == null) {
	    System.out.println("No data available");
	}
       	else {
	    if (lsid.getNamespace().equals("datathing")) {
		printInputStreamAsXML(resolver.getData(dataport));
	    }
	    else if (lsid.getNamespace().equals("raw")) {
		InputStream is = resolver.getData(dataport);
		byte[] buffer = new byte[256];
		while (true) {
		    int bytesRead = is.read(buffer);
		    if (bytesRead == -1) break;
		    System.out.write(buffer, 0, bytesRead);
		}
	    }
	}
	System.out.println("Fetching metadata...");
	LSIDMetadataPort metadataport = wrapper.getMetadataPortForProtocol(SOAP);
        if (metadataport == null) {
	    System.out.println("No metadata available");
	}
	else {
	    MetadataResponse resp = resolver.getMetadata(metadataport);
	    printInputStreamAsXML(resp.getMetadata());
	}
	
    }
    private static void printInputStreamAsXML(InputStream is) throws Exception {
	SAXBuilder sb = new SAXBuilder();
	Document result = sb.build(is);
	XMLOutputter xo = new XMLOutputter();
	xo.setNewlines(true);
	xo.setIndent("  ");
	System.out.println(xo.outputString(result));
    }

}
