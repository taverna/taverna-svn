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

import org.embl.ebi.escience.scufl.*;

// Utility Imports
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.*;

// Network Imports
import java.net.URL;

// for converting XML
import java.io.*;
import org.jdom.*;
import org.jdom.input.*;
import org.jdom.output.*;
import org.xml.sax.*;

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
	    
	    InputPort myInput = proc.getInputPorts()[0];
	    String inputType = myInput.getSyntacticType();
	    // If this is 'text/xml' then the biomoby service consumes
	    // a simple and there is no processing to do as that's what we have
	    // If this is l('text/xml') biomoby expects a collection but
	    // we have a java List of simples - need to convert this into 
	    // a biomoby collection document
	    
	    String inputXML = null;
	    if (inputType.equals("'text/xml'")) {
		inputXML = (String)inputThing.getDataObject();
	    }
	    else {
		// List of strings containing simple biomoby objects
		List simpleInputs = (List)inputThing.getDataObject();
		// Iterate and create the collection, 
		// ....inputXML = collectionThing
	    }

	    // do the task and populate outputXML
	    String methodName = ((BiomobyProcessor)proc).getServiceName();
	    String serviceEndpoint = ((BiomobyProcessor) proc).getEndpoint().toExternalForm();
	    String outputXML =
		new CentralImpl (serviceEndpoint, "http://biomoby.org/").call (methodName, inputXML);
	    Map outputMap = new HashMap();

	    OutputPort myOutput = proc.getOutputPorts()[0];
	    String outputType = myOutput.getSyntacticType();
	    // Will be either 'text/xml' or l('text/xml')
	    if (outputType.equals("'text/xml'")) {
		outputMap.put ("output", new DataThing (outputXML));
	    }
	    else {
		List outputList = new ArrayList();
		// Drill into the output xml document creating
		// a list of strings containing simple types
		// add them to the outputList
		

// This is in the 'outputXML'		
// --------------------------
//        <?xml version="1.0" encoding="UTF-8"?>
//        <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
//          <moby:mobyContent>
//           <moby:mobyData  queryID='b1'>
//               <Collection articleName="mySequenceCollection">
//                  <Simple>
//                   <Object namespace="Genbank/gi" id="163483"/>
//                  </Simple>
//                  <Simple>
//                   <Object namespace="Genbank/gi" id="244355"/>
//                  </Simple>
//                  <Simple>
//                   <Object namespace="Genbank/gi" id="533253"/>
//                  </Simple>
//                  <Simple>
//                   <Object namespace="Genbank/gi" id="745290"/>
//                  </Simple>
//                </Collection>
//           </moby:mobyData>
//          </moby:mobyContent>
//        </moby:MOBY>

// And this is what I want to create - several times:
// --------------------------------------------------
//        <?xml version="1.0" encoding="UTF-8"?>
//        <moby:MOBY xmlns:moby="http://www.biomoby.org/moby">
//           <moby:mobyContent>
//               <moby:mobyData queryID='a1'>
//                    <Simple articleName=''>
//                       <Object namespace="Genbank/gi" id="163483"/>
//                    </Simple>
//               </moby:mobyData>
//           </moby:mobyContent>
//        </moby:MOBY>

		// Create a DOM document from the resulting XML
		SAXBuilder saxBuilder = new SAXBuilder();
                Document doc = saxBuilder.build (new InputSource (new StringReader (outputXML)));


		// Return the list
		outputMap.put("output", new DataThing(outputList));
	    }
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
