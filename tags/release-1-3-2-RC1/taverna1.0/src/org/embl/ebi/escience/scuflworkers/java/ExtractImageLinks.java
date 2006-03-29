/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import org.embl.ebi.escience.baclava.DataThing;
import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

// Utility Imports
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import org.embl.ebi.escience.scuflworkers.java.LocalWorker;
import java.lang.String;
import java.lang.System;



/**
 * Extract a list of all image links in the supplied html document
 * @author Tom Oinn
 */
public class ExtractImageLinks implements LocalWorker {
    
    private static final String NEWLINE = System.getProperty("line.separator");

    public String[] inputNames() {
	return new String[]{"document"};
    }
    public String[] inputTypes() {
	return new String[]{"'text/html'"};
    }
    public String[] outputNames() {
	return new String[]{"imagelinks"};
    }
    public String[] outputTypes() {
	return new String[]{"l('text/x-taverna-web-url')"};
    }
    
    /**
     * Fetch the web page pointed to by the URL supplied as the 'url'
     * parameter into the service, the 'base' parameter specifies a 
     * URL to use as the base for relative URL resolution.
     */
    public Map execute(Map inputs) throws TaskExecutionException {
	String content = (String)((DataThing)(inputs.get("document"))).getDataObject();
	String lowerCaseContent = content.toLowerCase();
	int index = 0;
	List urlList = new ArrayList();
	while ((index = lowerCaseContent.indexOf("<img", index)) != -1) {
	    if ((index = lowerCaseContent.indexOf("src", index)) == -1) 
		break;
	    if ((index = lowerCaseContent.indexOf("=", index)) == -1) 
		break;
	    index++;
	    String remaining = content.substring(index);
	    StringTokenizer st = new StringTokenizer(remaining, "\t\n\r\">#");
	    String strLink = st.nextToken();
	    urlList.add(strLink);
	}
	Map outputs = new HashMap();
	outputs.put("imagelinks",new DataThing(urlList));
	return outputs;
    }


}
