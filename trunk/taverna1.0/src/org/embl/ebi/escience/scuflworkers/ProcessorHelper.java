/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;

import javax.swing.ImageIcon;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflworkers.XMLHandler;

// Utility Imports
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

// JDOM Imports
import org.jdom.Element;

// Network Imports
import java.net.URL;

import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Provides rendering and other hints for different processor
 * implementations, including preferred colours and icons.
 * @author Tom Oinn
 */
public class ProcessorHelper {

    static Map coloursForTagName = new HashMap();
    static Map tagNameForClassName = new HashMap();
    static Map classNameForTagName = new HashMap();
    static Map iconForTagName = new HashMap();
    static Map taskClassForTagName = new HashMap();
    static Map xmlHandlerForTagName = new HashMap();

    static {
	try {
	    // Load up the values from any taverna.properties files located
	    // by the class resource loader.
	    Enumeration en = ClassLoader.getSystemResources("taverna.properties");
	    Properties tavernaProperties = new Properties();
	    while (en.hasMoreElements()) {
		URL resourceURL = (URL)en.nextElement();
		System.out.println("Loading resources from : "+resourceURL.toString());
		tavernaProperties.load(resourceURL.openStream());
	    }
	    // Should now have a populated properties list, set up the various
	    // static Map objects for the colours etc.
	    // Iterate over all property keys
	    for (Iterator i = tavernaProperties.keySet().iterator(); i.hasNext(); ) {
		String key = (String)i.next();
		String value = tavernaProperties.getProperty(key);
		System.out.println(key+" == "+value);
		String[] keyElements = key.split("\\.");
		// Detect the processor keys
		if (keyElements[1].equals("processor")) {
		    String tagName = keyElements[2];
		    // If this is the class name...
		    if (keyElements[3].equals("class")) {
			// Store the class name <-> tag name mappings
			tagNameForClassName.put(value,tagName);
			classNameForTagName.put(tagName,value);
		    }
		    else if (keyElements[3].equals("colour")) {
			// Configure default display colour for i.e. dot
			coloursForTagName.put(tagName,value);
		    }
		    else if (keyElements[3].equals("icon")) {
			// Fetch resource icon...
			iconForTagName.put(tagName,new ImageIcon(ClassLoader.getSystemResource(value)));
		    }
		    else if (keyElements[3].equals("taskclass")) {
			// Configure the taverna task for the enactor to run this type of processor
			taskClassForTagName.put(tagName, value);
		    }
		    else if (keyElements[3].equals("xml")) {
			// Configure and instantiate the XML handler for this type of processor
			String handlerClassName = value;
			// Create an instance of the handler
			Class handlerClass = Class.forName(handlerClassName);
			XMLHandler xh = (XMLHandler)handlerClass.newInstance();
			xmlHandlerForTagName.put(tagName, xh);
		    }
		    
		}
	    }
	}
	catch (Exception e) {
	    System.out.println("Error during initialisation for taverna properties! : "+e.getMessage());
	    e.printStackTrace();
	    //System.exit(1);
	}
    }

    public static String getTaskClassName(Processor p) {
	String className = p.getClass().getName();
	String tagName = (String)tagNameForClassName.get(className);
	if (tagName != null) {
	    String taskClassName = (String)taskClassForTagName.get(tagName);
	    if (taskClassName != null) {
		return taskClassName;
	    }
	}
	return null;
    }

    public static String getPreferredColour(Processor p) {
	String className = p.getClass().getName();
	String tagName = (String)tagNameForClassName.get(className);
	if (tagName != null) {
	    String colour = (String)coloursForTagName.get(tagName);
	    if (colour != null) {
		return colour;
	    }
	}
	return "white";
    }

    public static ImageIcon getPreferredIcon(Processor p) {
	String className = p.getClass().getName();
	String tagName = (String)tagNameForClassName.get(className);
	if (tagName != null) {
	    ImageIcon icon = (ImageIcon)iconForTagName.get(tagName);
	    if (icon != null) {
		return icon;
	    }
	}
	return null;
    }

    public static Element elementForProcessor(Processor p) {
	String className = p.getClass().getName();
	String tagName = (String)tagNameForClassName.get(className);
	if (tagName != null) {
	    XMLHandler xh = (XMLHandler)xmlHandlerForTagName.get(tagName);
	    if (xh != null) {
		return xh.elementForProcessor(p);
	    }
	}
	return null;
    }

    /**
     * Spit back a processor given a chunk of xml, the element passed in being the 'processor' tag
     * return null if we can't handle it
     */
    public static Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
	throws ProcessorCreationException, DuplicateProcessorNameException, XScuflFormatException {
	// Get the element name of the first child
	String tagName = ((Element)(processorNode.getChildren().get(0))).getName();
	XMLHandler xh = (XMLHandler)xmlHandlerForTagName.get(tagName);
	if (xh != null) {
	    return xh.loadProcessorFromXML(processorNode, model, name);
	}
	return null;
    }
}
