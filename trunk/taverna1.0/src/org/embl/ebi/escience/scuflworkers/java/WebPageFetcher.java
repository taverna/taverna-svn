/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.HashMap;
import java.util.Map;

// IO Imports
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;
import java.lang.StringBuffer;
import java.lang.System;



/**
 * Fetch a single web page from URL
 * @author Tom Oinn
 */
public class WebPageFetcher implements LocalWorker {
    
    private static final String NEWLINE = System.getProperty("line.separator");

    public String[] inputNames() {
	return new String[]{"url","base"};
    }
    public String[] inputTypes() {
	return new String[]{"'text/x-taverna-web-url'","'text/x-taverna-web-url'"};
    }
    public String[] outputNames() {
	return new String[]{"contents"};
    }
    public String[] outputTypes() {
	return new String[]{LocalWorker.HTML};
    }
    
    /**
     * Fetch the web page pointed to by the URL supplied as the 'url'
     * parameter into the service, the 'base' parameter specifies a 
     * URL to use as the base for relative URL resolution.
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	BufferedReader reader = null;
	try {
	    String inputURLString = (String)((DataThing)inputs.get("url")).getDataObject();
	    URL inputURL = null;
	    if (inputs.get("base")!=null) {
		inputURL = new URL(new URL((String)((DataThing)inputs.get("base")).getDataObject()),inputURLString);
	    }
	    else {
		inputURL = new URL(inputURLString);
	    }
	    StringBuffer result = new StringBuffer();
	    reader = new BufferedReader( new InputStreamReader(inputURL.openStream()) );
	    String line = null;
	    while ( (line = reader.readLine()) != null) {
		result.append(line);
		result.append(NEWLINE);
	    }
	    Map outputMap = new HashMap();
	    outputMap.put("contents",new DataThing(result.toString()));
	    return outputMap;
	}
	catch (IOException ioe) {
	    TaskExecutionException tee = new TaskExecutionException("Error fetching web page!");
	    tee.initCause(ioe);
	    throw tee;
	}
	finally {
	    try {
		if (reader != null) reader.close();
	    }
	    catch (IOException ex){
		System.err.println("Cannot close reader: " + reader);
	    }
	}
    }


}
