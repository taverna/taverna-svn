/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.seqhound;

import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

// Utility Imports
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.*;
import javax.swing.tree.*;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessorFactory;
import org.apache.log4j.Logger;

import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;

import org.blueprint.seqhound.*;

import java.lang.reflect.*;


/**
 * A scavenger which introspects over the Seqhound java API to
 * fetch all methods available and expose them as components within
 * Taverna.
 * @author Tom Oinn
 */
public class SeqhoundScavenger extends Scavenger {
        
    static String[] internalOperations = {"Init","LogError","IsServerAlive","IsJseqremServerAlive","Fini","IsNetEntrezOn","IsInited","NetEntrezInit"};
    
    /**
     * Create a default seqhound scavenger talking to the blueprint initiative
     * public SeqHound server.
     */
    public SeqhoundScavenger() 
	throws ScavengerCreationException {
	this("seqhound.blueprint.org","/cgi-bin/seqrem",
	     "skinner.blueprint.org:8080","/jseqhound/jseqrem");
    }

    /**
     * Create a new seqhound service scavenger with the specified
     * server and all that.
     */
    public SeqhoundScavenger(String server,
			     String path,
			     String jseqremServer,
			     String jseqremPath)
	throws ScavengerCreationException {
	super("SeqHound @ "+server);
	// Fetch all the methods within the org.blueprint.seqhound.SeqHound class
	Class theClass = org.blueprint.seqhound.SeqHound.class;
	Method[] methods = theClass.getDeclaredMethods();
	for (int i = 0; i < methods.length; i++) {
	    Method theMethod = methods[i];
	    if (theMethod.getName().startsWith("SHound") &&
		isInternal(theMethod.getName()) == false) {
		SeqhoundProcessorFactory spf = new SeqhoundProcessorFactory(theMethod.getName(), server,
									    path, jseqremServer, jseqremPath);
		add(new DefaultMutableTreeNode(spf));
	    }
	}
    }
    
    static boolean isInternal(String methodName) {
	if (methodName.startsWith("SHound") == false) {
	    return true;
	}
	for (int i = 0; i < internalOperations.length; i++) {
	    if (methodName.equals("SHound"+internalOperations[i])) {
		return true;
	    }
	}
	return false;
    }
    
}
