/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.util.*;
import java.net.*;
import java.io.*;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that can create new LocalServiceProcessor nodes
 * @author Tom Oinn
 */
public class LocalServiceScavenger extends Scavenger {
    
    private static List workerList = new ArrayList();

    static {
	try {
	    Enumeration en = ClassLoader.getSystemResources("taverna.local.properties");
	    Properties tavernaProperties = new Properties();
	    while (en.hasMoreElements()) {
		URL resourceURL = (URL)en.nextElement();
		tavernaProperties.load(resourceURL.openStream());
	    }
	    // Iterate over the available local properties
	    for (Iterator i = tavernaProperties.keySet().iterator(); i.hasNext();) {
		String className = (String)i.next();
		String description = (String)tavernaProperties.get(className);
		workerList.add(new Scavenger(new LocalServiceProcessorFactory(className, description)));
	    }
	}
	catch (Exception e) {
	    //
	}
    }

    /**
     * Create a new local service scavenger
     */
    public LocalServiceScavenger()
	throws ScavengerCreationException {
	super("Local Java widgets");
	// for all available local widgets, add them as 
	// children to this scavenger.
	for (Iterator i = workerList.iterator(); i.hasNext();) {
	    add((Scavenger)i.next());
	}
    }

}
