////////////////////////////////////////////////////////////////////////////////
//
// � University of Southampton IT Innovation Centre, 2002
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
//                              $Date: 2003-10-09 12:19:32 $
//                              $Revision: 1.5 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.wsdl;

import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.Processor;
import uk.ac.soton.itinnovation.taverna.enactor.broker.LogLevel;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Vector;

// JDOM Imports
import org.jdom.Element;




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
	    Vector outNames = p.outNames;
	    synchronized (p.call) {
		Call call = (org.apache.axis.client.Call)p.call;
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
		
		Object ret = call.invoke(args);
		Map outputs = call.getOutputParams();
		HashMap map = new HashMap();
		for (int pos = 0; pos < p.outNames.size(); ++pos) {
		    String name = (String)outNames.get(pos);
		    Object value = outputs.get(name);
		    if ((value == null) && (pos == 0)) {
			map.put(name, makeThing(ret));
		    }
		    else {
			map.put(name, makeThing(value));
		    }
		}
		return map;
	    }
	}
	catch (Exception ex) {
	    ex.printStackTrace();
	    TaskExecutionException te = new TaskExecutionException("Error occured during invocation "+
								   ex.getMessage());
	    te.initCause(ex);
	    throw te;
	}
    }
    
    private DataThing makeThing(Object o) {
	if (o instanceof Number) {
	    return DataThingFactory.bake(o.toString());
	}
	else if (o instanceof Number[]) {
	    Number[] n = (Number[])o;
	    String[] stringArray = new String[n.length];
	    for (int i = 0; i < n.length; i++) {
		stringArray[i] = n[i].toString();
	    }
	    return DataThingFactory.bake(stringArray);
	}
	else if (o instanceof String[]) {
	    return DataThingFactory.bake((String[])o);
	}
	else if (o instanceof List) {
	    return DataThingFactory.bakeForSoaplab((List)o);
	}
	else {
	    return new DataThing(o);
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
