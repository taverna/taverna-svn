////////////////////////////////////////////////////////////////////////////////
//
// © University of Southampton IT Innovation Centre, 2002
//
// Copyright in this library belongs to the IT Innovation Centre of
// 2 Venture Road, Chilworth Science Park, Southampton SO16 7NP, UK.
//
// This library is free software; you can redistribute it and/or
// modify it under the terms of the GNU Lesser General Public License
// as published by the Free Software Foundation; either version 2.1
// of the License, or (at your option) any later version.
//
// This library is distributed in the hope that it will be useful,
// but WITHOUT ANY WARRANTY; without even the implied warranty of
// MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
// GNU Lesser General Public License for more details.
//
// You should have received a copy of the GNU Lesser General Public
// License along with this library; if not, write to the Free Software
// Foundation Inc, 59 Temple Place, Suite 330, Boston MA 02111-1307 USA.
//
//      Created By          :   Darren Marvin
//      Created Date        :   2003/4/9
//      Created for Project :   MYGRID
//      Dependencies        :
//
//      Last commit info    :   $Author: mereden $
//                              $Date: 2003-10-02 16:54:25 $
//                              $Revision: 1.3 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.wsdl;

import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.baclava.factory.*;
import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
// Utility Imports
import java.util.HashMap;
import java.util.*;

// JDOM Imports
import org.jdom.Element;

// Network Imports
import java.net.*;




public class WSDLInvocationTask extends ProcessorTask  {
    
    private static Logger logger = Logger.getLogger(WSDLInvocationTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private static Service service = new org.apache.axis.client.Service();

    private Call getCall() {
	synchronized (service) {
	    try {
		return (Call) service.createCall();
	    }
	    catch (Exception ex) {
		throw new RuntimeException(ex);
	    }
	}
    }

    public WSDLInvocationTask(String id,Processor proc,LogLevel l, String userID, String userCtx) {
	super(id,proc,l,userID,userCtx);
    }
    
    public Map execute(Map inputMap) throws TaskExecutionException {
	try {
	    WSDLBasedProcessor p = (WSDLBasedProcessor)proc;
	    Map results = new HashMap();
	    Service service = new Service();
	    Call call = getCall();
	    try {
		call.setTargetEndpointAddress(new URL(p.getTargetEndpoint()));
	    }
	    catch (MalformedURLException mue) {
		throw new TaskExecutionException("URL for service endpoint was malformed : "+mue.getMessage());
	    }
	    call.setOperationName(new QName(p.getOperationName()));
	    Object[] args = new Object[p.getInputPorts().length];
	    for (int i = 0; i < args.length; i++) {
		DataThing theData = (DataThing)inputMap.get(p.getInputPorts()[i].getName());
		Object theDataObject = theData.getDataObject();
		// Check for the case of List of String and convert to a String[]
		if (theDataObject instanceof List) {
		    if (((List)theDataObject).isEmpty()) {
			theDataObject = new String[0];
		    }
		    else {
			Object firstItem = ((List)theDataObject).get(0);
			if (firstItem instanceof String) {
			    theDataObject = ((List)theDataObject).toArray(new String[0]);
			}
		    }
		}
		args[i] = theDataObject;
		if (args[i] == null) {
		    throw new TaskExecutionException("Null argument not allowed, check preceeding processors!");
		}
		System.out.println("Data thing for port "+p.getInputPorts()[i].getName());
		System.out.println(theData.getDataObject());
	    }
	    Object o = call.invoke(args);
	    DataThing outputThing = null;
	    if (o instanceof String[]) {
		outputThing = DataThingFactory.bake((String[])o);
	    }
	    else if (o instanceof List) {
		outputThing = DataThingFactory.bakeForSoaplab((List)o);
	    }
	    else {
		outputThing = new DataThing(o);
	    }
	    results.put(p.getOutputPorts()[0].getName(), outputThing);
	    return results;
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    TaskExecutionException te = new TaskExecutionException("Error occured during invocation "+
								   ex.getMessage());
	    te.initCause(ex);
	    throw te;
	}
    }
    
    public void cleanUpConcreteTask() {
	//
    }
    
    private Element provenanceElement = new Element("WSDLInvocation",PROVENANCE_NAMESPACE);
    /**
     * Retrieve provenance information for this task, concrete tasks should
     * overide this method and provide this information as an XML JDOM element
     */
    public Element getProvenance() {
	return provenanceElement;
    }
}
