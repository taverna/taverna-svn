/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.taverna;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

import java.lang.Exception;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;



/**
 * Provides service listing functionality from an instance of Soaplab
 * @author Tom Oinn
 */
public class QueryServices {
    
    public static String[] getServices(String endpoint) {
	try {
	    Call call = (Call) new Service().createCall();
	    // Endpoint i.e. http://industry.ebi.ac.uk/soap/soaplab/AnalysisFactory
	    call.setTargetEndpointAddress(endpoint);
	    call.setOperationName(new QName("getAvailableAnalyses"));
	    String[] results = (String[])(call.invoke(new Object[0]));
	    return results;
	}
	catch (Exception e) {
	    throw new RuntimeException("Cannot connect to get analysis list : "+e.getMessage());
	}
    }
    
}
