/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;

import org.embl.ebi.escience.baclava.DataThing;
import org.jdom.*;
import java.io.*;
import org.jdom.input.*;
import java.util.*;
import org.embl.ebi.escience.scuflworkers.java.*;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * A local processor to extract simple data types from
 * biomoby data packets.
 * @author Tom Oinn
 */
public class ExtractMobyData implements LocalWorker {
    
    Namespace mobyNS = CreateMobyData.mobyNS;
    
    public String[] inputNames() {
	return new String[] {"mobydata"};
    }
    
    public String[] inputTypes() {
	return new String[] {"'text/xml'"};
    }
    
    public String[] outputNames() {
	return new String[] {"namespace","id","value","type"};
    }
    
    public String[] outputTypes() {
	return new String[] {LocalWorker.STRING,
			     LocalWorker.STRING,
			     LocalWorker.STRING,
			     LocalWorker.STRING};
    }
    
    public Map execute(Map inputs) throws TaskExecutionException {
	// Must contain a single input called mobydata, if not
	// then complain
	if (inputs.containsKey("mobydata")==false) {
	    throw new TaskExecutionException("Moby input data is mandatory");
	}
	String mobyDataString = (String)((DataThing)inputs.get("mobydata")).getDataObject();
	// Parse the XML
	Element mobyElement;
	try {
	    SAXBuilder builder = new SAXBuilder(false);
	    Document document = builder.build(new StringReader(mobyDataString));
	    mobyElement = document.getRootElement();
	}
	catch (JDOMException jde) {
	    throw new TaskExecutionException("Unable to parse biomoby data, check the XML! "+jde.getMessage());
	}
	// Now find the simple moby data
	Element mobyDataElement;
	try {
	    Element mobySimple = mobyElement.getChild("mobyContent",mobyNS).getChild("mobyData",mobyNS).getChild("Simple",mobyNS);
	    // Get the first immediate child of the <moby:Simple>
	    mobyDataElement = (Element)mobySimple.getChildren().get(0);
	}
	catch (NullPointerException npe) {
	    throw new TaskExecutionException("Unexpected structure within moby data, check the input XML.");
	}
	Map results = new HashMap();
	// Get the ID and namespace values, defaulting to the empty string if not present
	String idValue, namespaceValue;
	idValue = mobyDataElement.getAttributeValue("id");
	if (idValue == null) {
	    idValue = mobyDataElement.getAttributeValue("id",mobyNS,"");
	}
	namespaceValue = mobyDataElement.getAttributeValue("namespace");
	if (namespaceValue == null) {
	    namespaceValue = mobyDataElement.getAttributeValue("namespace",mobyNS,"");
	}
	results.put("id",new DataThing(idValue));
	results.put("namespace",new DataThing(namespaceValue));
	results.put("value",new DataThing(mobyDataElement.getTextTrim()));
	results.put("type",new DataThing(mobyDataElement.getName()));
	return results;
	    
    }
}
