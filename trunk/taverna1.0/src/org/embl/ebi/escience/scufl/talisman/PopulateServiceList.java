/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.talisman;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.xml.namespace.QName;
import javax.xml.rpc.ServiceException;
import org.apache.axis.client.Call; // ambiguous with: javax.xml.rpc.Call 
import org.apache.axis.client.Service; // ambiguous with: javax.xml.rpc.Service 
import org.embl.ebi.escience.talisman.*;

// RMI Imports
import java.rmi.RemoteException;

import java.lang.Object;
import java.lang.String;



/**
 * Talisman action to populate a SelectionList with the 
 * service list returned by the Soaplab analysis factory.
 * This will create a selection list where the values are
 * the full endpoint URLs and the display names are the
 * application names with categories. The name of the list
 * to populate is given in the 'list' parameter and the 
 * root URL for the Soaplab service (not including the AnalysisFactory bit)
 * in the 'soaplab' parameter.
 */
public class PopulateServiceList implements ActionWorker {

    public void process(HttpServletRequest request , HttpServletResponse response, Action action) 
	throws AbortActionException, NodeResolutionException, UnknownResolutionProtocolException {
	
	Trigger trigger = (Trigger)action.getParent();

	// Require the following parameters
	action.requireParameters("list,soaplab");
	
	SelectionList theList = Resolver.getSelection(action.props.getProperty("list"),action);
	String soaplabRoot = Resolver.getFieldValue(action.props.getProperty("soaplab"),action);
	
	Call call = null;
	try {
	    // Invoke the AnalysisFactory service to get the list of extant applications
	    call = (Call) new Service().createCall();
	}
	catch (ServiceException se) {
	    trigger.addError("Unable to create XML RPC call object during population of list : "+se.getMessage());
	    throw new AbortActionException();
	}
	// Endpoint i.e. http://industry.ebi.ac.uk/soap/soaplab/
	call.setTargetEndpointAddress(soaplabRoot+"AnalysisFactory");
	call.setOperationName(new QName("getAvailableAnalyses"));
	String[] results = null;
	try {
	    results = (String[])(call.invoke(new Object[0]));
	}
	catch (RemoteException re) {
	    trigger.addError("Unable to call the Soaplab service to get the list of applications : "+re.getMessage());
	    throw new AbortActionException();
	}
	// Clear the current set of options in the selection list
	theList.clearOptions();

	// Iterate over results, which will look something like class::application
	// and add the entries to the selection list. The values in the list will
	// be the real SOAP endpoints. These values are the same as should be used
	// in the constructor of the SoaplabProcessor. How handy...
	for (int i = 0; i < results.length; i++) {
	    String application = results[i];
	    String applicationEndpoint = soaplabRoot+application;
	    theList.addOption(application,applicationEndpoint);
	}

    }

}
