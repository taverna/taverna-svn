package org.embl.ebi.escience.taverna;

import org.apache.axis.client.*;
import javax.xml.namespace.QName;
import org.apache.axis.AxisFault;
import java.util.*;
import java.io.*;


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
