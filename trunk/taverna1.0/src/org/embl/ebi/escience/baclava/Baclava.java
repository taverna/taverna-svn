/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava;

// Utility Imports
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

// JDOM Imports
import org.jdom.Namespace;

// Network Imports
import java.net.URL;

import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Provides general functionality, mostly in the form
 * of static methods, for bundling and unbundling the
 * myGrid data document format.
 * @author Tom Oinn
 */
public abstract class Baclava {
    
    static Map handlerClassForClass = new HashMap();

    /**
     * Load the plugin properties from the baclava.properties file
     * in order to locate appropriate handlers for the various
     * different data types when attempting to serialize from objects
     */
    static {
	try {
	    Enumeration en = ClassLoader.getSystemResources("org/embl/ebi/escience/baclava/baclava.properties");
	    Properties baclavaProperties = new Properties();
	    while (en.hasMoreElements()) {
		URL resourceURL = (URL)en.nextElement();
		System.out.println("Loading baclava resources from : "+resourceURL.toString());
		baclavaProperties.load(resourceURL.openStream());
	    }
	    // Iterate over all handler references, these appear as 
	    // handler:<CLASSNAME> = <HANDLER_CLASS>
	    for (Iterator i = baclavaProperties.keySet().iterator(); i.hasNext(); ) {
		String key = (String)i.next();
		String[] keyElements = key.split(":",2);
		// Class name of class that the handler can deal with
		String className = keyElements[1];
		// Class name of the handler class to map to
		String handlerClassName = baclavaProperties.getProperty(key);
		// Create a Class object for the class to be handled
		Class theClass = Class.forName(className);
		Class theHandlerClass = Class.forName(handlerClassName);
		// Store an instance of the handler class in the value field
		// of the map, with the Class object for the class it deals 
		// with as the key
		handlerClassForClass.put(theClass, theHandlerClass.newInstance());
	    }
	}
	catch (Exception ex) {
	    System.out.println("Error initialising Baclava : "+ex.toString());
	    ex.printStackTrace();
	}
    }

    /**
     * The namespace prefix for tags in the myGrid data
     * document format
     */
    public static Namespace BaclavaNS = 
	Namespace.getNamespace("baclava",
			       "http://org.embl.ebi.escience/baclava/0.1alpha");

}
