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
//                              $Date: 2005-04-27 13:49:43 $
//                              $Revision: 1.12 $
//
///////////////////////////////////////////////////////////////////////////////////////

package org.embl.ebi.escience.scuflworkers.soaplab;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;
import uk.ac.soton.itinnovation.taverna.enactor.entities.ProcessorTask;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import java.lang.Exception;
import java.lang.Integer;
import java.lang.Object;
import java.lang.String;



public class SoaplabTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(SoaplabTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private SoaplabProcessor proc;
    
    public SoaplabTask(Processor p) {
	this.proc = (SoaplabProcessor)p;
    }
    
    public Map execute(Map inputMap, ProcessorTask parentTask) throws TaskExecutionException {
	
	try{
	    
	    HashMap outMap = null;
	    
	    // Copy the contents of the DataThing set in the input map to 
	    // a new Map object which just contains the raw data objects
	    Map soaplabInputMap = new HashMap();
	    for (Iterator i = inputMap.keySet().iterator(); i.hasNext() ; ) {
		String parameterName = (String)i.next();
		Object parameterValue = ((DataThing)inputMap.get(parameterName)).getDataObject();
		soaplabInputMap.put(parameterName, parameterValue);
	    }
	    
	    // Invoke the web service...
	    Call call = (Call) new Service().createCall();
	    call.setTimeout(new Integer(0));
	    URL soaplabWSDLURL = ((SoaplabProcessor) proc).getEndpoint();
	    String soaplabWSDL = soaplabWSDLURL.toExternalForm();
	    call.setTargetEndpointAddress(soaplabWSDLURL);
	    
	    // Invoke the job and wait for it to complete
	    call.setOperationName(new QName("createAndRun"));
	    String jobID = (String)call.invoke(new Object[] { soaplabInputMap });
	    // Get the array of desired outputs to avoid pulling everything back
	    OutputPort[] boundOutputs = this.proc.getBoundOutputPorts();
	    String[] outputPortNames = new String[boundOutputs.length];
	    for (int i = 0; i < outputPortNames.length; i++) {
		outputPortNames[i] = boundOutputs[i].getName();
		System.out.println("Adding output : "+outputPortNames[i]);
	    }
	    
	    if (this.proc.isPollingDefined() == false) {
		// If we're not polling then use this behaviour
		call.setOperationName(new QName("waitFor"));
		call.invoke(new Object[] { jobID });
	    }
	    else {
		// Wait for the polling interval then request a status
		// and do this until the status is terminal.
		boolean polling = true;
		// Number of milliseconds to wait before the first status
		// request.
		int pollingInterval = this.proc.getPollingInterval();
		while (polling) {
		    try {
			Thread.sleep(pollingInterval);
		    }
		    catch (InterruptedException ie) {
			// do nothing
		    }
		    call.setOperationName(new QName("getStatus"));
		    String statusString = (String)call.invoke(new Object[] { jobID });
		    System.out.println("Polling, status is : "+statusString);
		    if (statusString.equals("RUNNING") ||
			statusString.equals("CREATED")) {
			pollingInterval = (int)((double)pollingInterval * this.proc.getPollingBackoff());
			if (pollingInterval > this.proc.getPollingIntervalMax()) {
			    pollingInterval = this.proc.getPollingIntervalMax();
			}
		    }
		    else {
			// Either completed with an error or success
			polling = false;
		    }		    
		}
	    }

	    // Get the status code
	    call.setOperationName(new QName("getStatus"));
	    String statusString = (String)call.invoke(new Object[] { jobID });
	    if (statusString.equals("TERMINATED_BY_ERROR")) {
		// Get the report
		call.setOperationName(new QName("getSomeResults"));
		HashMap temp = new HashMap((Map)call.invoke(new Object[] { jobID, new String[]{"report"}}));
		String reportText = (String)temp.get("report");
		throw new TaskExecutionException("Soaplab call returned an error : "+reportText);
	    }
	    
	    // Get the results required by downstream processors
	    call.setOperationName(new QName("getSomeResults"));
	    HashMap outputMap = new HashMap((Map)call.invoke(new Object[] { jobID, outputPortNames }));
	    
	    // Tell soaplab that we don't need this session any more
	    call.setOperationName(new QName("destroy"));
	    call.invoke(new Object[] { jobID });
	    
	    outMap = new HashMap();
	    // Build the map of DataThing objects
	    for (Iterator i = outputMap.keySet().iterator(); i.hasNext(); ) {
		String parameterName = (String)i.next();
		Object outputObject = outputMap.get(parameterName);
		DataThing outputThing = null;
		logger.debug("Soaplab : parameter '"+parameterName+"' has type '"+outputObject.getClass().getName()+"'");
		if (outputObject instanceof String[]) {
		    outputThing = DataThingFactory.bake((String[])outputObject);
		}
		else if (outputObject instanceof byte[][]) {
		    // Create a List of byte arrays, this will
		    // map to l('application/octet-stream') in
		    // the output document.
		    outputThing = DataThingFactory.bake((byte[][])outputObject);
		}
		else if (outputObject instanceof List) {
		    outputThing = DataThingFactory.bakeForSoaplab((List)outputObject);
		}
		else {
		    // Fallthrough case, just put the object directly
		    // into the DataThing, this mostly applies to
		    // output of type byte[] or string, both of which
		    // are handled perfectly sensibly by default.
		    outputThing = new DataThing(outputObject);
		}		
		outMap.put(parameterName, outputThing);
	    }
	    
	    //success
	    return outMap;
	}
	
	catch(Exception ex) {
	    ex.printStackTrace();
	    if (ex instanceof TaskExecutionException) {
		throw (TaskExecutionException)ex;
	    }
	    logger.error("Error invoking soaplab service for soaplab", ex);
	    TaskExecutionException tee = new TaskExecutionException("Task failed due to problem invoking soaplab service");
	    tee.initCause(ex);
	    throw tee;
	}
	
    }
    
}
