package org.embl.ebi.escience.scuflworkers.biomoby;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingFactory;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessor;
// import java.lang.Exception;
// import java.lang.Integer;
// import java.lang.Object;
// import java.lang.String;

import org.biomoby.client.*;
import org.biomoby.shared.*;


public class BiomobyTask implements ProcessorTaskWorker {

    private static Logger logger = Logger.getLogger(BiomobyTask.class);
    private static final int INVOCATION_TIMEOUT = 0;
    private Processor proc;
    
    public BiomobyTask(Processor p) {
	this.proc = p;
    }
    
    public Map execute(Map inputMap) throws TaskExecutionException {

	try {
	    DataThing inputThing = (DataThing)inputMap.get("input");
	    String inputXML = (String)inputThing.getDataObject();
	
	    // do the task and populate outputXML
	    String methodName = ((BiomobyProcessor)proc).getServiceName();
	    String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint().toExternalForm();
	    String outputXML = new CentralImpl (serviceEndpoint).call (methodName, inputXML);
	    Map outputMap = new HashMap();
	    outputMap.put ("output", new DataThing (outputXML));

	    return outputMap;
	} catch (Exception ex) {
	    ex.printStackTrace();
	    logger.error("Error invoking biomoby service for biomoby", ex);
	    TaskExecutionException tee = new TaskExecutionException("Task failed due to problem invoking biomoby service");
	    tee.initCause(ex);
	    throw tee;
	}
    }
}
