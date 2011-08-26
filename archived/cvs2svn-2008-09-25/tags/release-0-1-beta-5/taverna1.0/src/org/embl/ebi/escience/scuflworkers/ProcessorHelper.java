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

import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.String;
import java.lang.System;



/**
 * Provides rendering and other hints for different processor
 * implementations, including preferred colours and icons. The
 * data used by this class is loaded at classload time from 
 * all 'taverna.properties' files found by the system classloader,
 * these files contain the processor specific configuration that
 * this class acts as an interface to. An example for the Soaplab
 * processor type is shown below :
 * <pre>
 * taverna.processor.soaplabwsdl.class = org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor
 * taverna.processor.soaplabwsdl.xml = org.embl.ebi.escience.scuflworkers.soaplab.SoaplabXMLHandler
 * taverna.processor.soaplabwsdl.colour = lightgoldenrodyellow
 * taverna.processor.soaplabwsdl.icon = org/embl/ebi/escience/scuflui/soaplab.gif
 * taverna.processor.soaplabwsdl.taskclass = uk.ac.soton.itinnovation.taverna.enactor.entities.SoaplabTask
 * </pre>
 * To load additional processor types for enactment and display within
 * the workbench, you will need to create the appropriate helper
 * classes such as the XML handler and then point to the class names
 * in a 'taverna.properties' file. I suggest you package all these items
 * into a single .jar file, in which case simply ensuring that the
 * classpath contains your .jar should allow everything to work.
 * @author Tom Oinn
 */
public class ProcessorHelper {

    static Map coloursForTagName = new HashMap();
    static Map tagNameForClassName = new HashMap();
    static Map classNameForTagName = new HashMap();
    static Map iconForTagName = new HashMap();
    static Map taskClassForTagName = new HashMap();
    static Map xmlHandlerForTagName = new HashMap();
    static Map tagNameForScavenger = new HashMap();
    static Map editorForTagName = new HashMap();

    static ImageIcon unknownProcessorIcon;

    static {
	try {
	    // Load the 'unknown processor' image icon
	    unknownProcessorIcon = new ImageIcon(ClassLoader.getSystemResource("org/embl/ebi/escience/scuflui/unknownprocessor.gif"));
	    // Load up the values from any taverna.properties files located
	    // by the class resource loader.
	    Enumeration en = ClassLoader.getSystemResources("taverna.properties");
	    Properties tavernaProperties = new Properties();
	    while (en.hasMoreElements()) {
		URL resourceURL = (URL)en.nextElement();
		//System.out.println("Loading resources from : "+resourceURL.toString());
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
		if (keyElements.length == 4 && keyElements[1].equals("processor")) {
		    String tagName = keyElements[2];
		    // If this is the class name...
		    // Form : taverna.processor.<TAGNAME>.class = <CLASSNAME>
		    if (keyElements[3].equals("class")) {
			// Store the class name <-> tag name mappings
			tagNameForClassName.put(value,tagName);
			classNameForTagName.put(tagName,value);
		    }
		    // Form : taverna.processor.<TAGNAME>.colour = <RENDERINGHINT_COLOUR>
		    else if (keyElements[3].equals("colour")) {
			// Configure default display colour for i.e. dot
			coloursForTagName.put(tagName,value);
		    }
		    // Form : taverna.processor.<TAGNAME>.icon = <RENDERINGHINT_ICON>
		    else if (keyElements[3].equals("icon")) {
			// Fetch resource icon...
			iconForTagName.put(tagName,new ImageIcon(ClassLoader.getSystemResource(value)));
		    }
		    // Form : taverna.processor.<TAGNAME>.taskclass = <ENACTOR_TASK_CLASS>
		    else if (keyElements[3].equals("taskclass")) {
			// Configure the taverna task for the enactor to run this type of processor
			taskClassForTagName.put(tagName, value);
		    }
		    // Form : taverna.processor.<TAGNAME>.xml = <XML_HANDLER_CLASS>
		    else if (keyElements[3].equals("xml")) {
			// Configure and instantiate the XML handler for this type of processor
			String handlerClassName = value;
			// Create an instance of the handler
			Class handlerClass = Class.forName(handlerClassName);
			XMLHandler xh = (XMLHandler)handlerClass.newInstance();
			xmlHandlerForTagName.put(tagName, xh);
		    }
		    // Form : taverna.processor.<TAGNAME>.editor = <EDITOR_CLASS>
		    else if (keyElements[3].equals("editor")) {
			// Configure and create the processor editor handler
			String editorClassName = value;
			// Create an instance...
			Class editorClass = Class.forName(editorClassName);
			ProcessorEditor pe = (ProcessorEditor)editorClass.newInstance();
			editorForTagName.put(tagName, pe);
		    }
		}
		// Form : taverna.scavenger.<TAGNAME> = <SCAVENGERCLASS>
		// Use the scavenger class as a key, as this allows us to have
		// more than one scavenger per tag type. We have the tag type as
		// a value in order that the rendering code can get the icon hint
		// for the type being created.
		keyElements = key.split("\\.",3);
		if (keyElements.length == 3 && keyElements[1].equals("scavenger")) {
		    // Get the set of scavenger creating classes
		    String scavengerClassName = keyElements[2];
		    String scavengerTagName = value;
		    tagNameForScavenger.put(scavengerClassName, scavengerTagName);
		}
	    }
	}
	catch (Exception e) {
	    System.out.println("Error during initialisation for taverna properties! : "+e.getMessage());
	    e.printStackTrace();
	    // Don't exit, as this hides the stack trace etc!
	    // System.exit(1);
	}
    }

    /**
     * Return the map of scavenger class names to tag names
     */
    public static Map getScavengerToTagNames() {
	return tagNameForScavenger;
    }

    /**
     * Given a processor instance, return the fully qualified class name
     * of a TavernaTask for the myGrid enactor to invoke the operation
     * represented by the processor
     */
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

    /**
     * Given a class name, return the tag name used by this helper
     * class as an index for the other categories such as icons.
     */
    public static String getTagNameForClassName(String className) {
	//System.out.println("Request for tag name for : "+className);
	return (String)tagNameForClassName.get(className);
    }

    /**
     * Given a tag name, return the in place editor for the processor
     */
    public static ProcessorEditor getEditorForTagName(String tagName) {
	return (ProcessorEditor)editorForTagName.get(tagName);
    }
    

    /**
     * Given a tag name, return the preferred image icon for that
     * tag.
     */
    public static ImageIcon getIconForTagName(String tagName) {
	//System.out.println("Request for icon for : "+tagName);
	ImageIcon icon = (ImageIcon)iconForTagName.get(tagName);
	if (icon == null) { 
	    return unknownProcessorIcon;
	}
	else {
	    return icon;
	}
    }

    /**
     * Given a processor instance, return the preferred colour
     * to be used for UI representations.
     */
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

    /**
     * Given a processor instance, return an image icon to be
     * used in, for example, tree renderer objects.
     */
    public static ImageIcon getPreferredIcon(Processor p) {
	String className = p.getClass().getName();
	String tagName = (String)tagNameForClassName.get(className);
	if (tagName != null) {
	    ImageIcon icon = (ImageIcon)iconForTagName.get(tagName);
	    if (icon != null) {
		return icon;
	    }
	}
	return unknownProcessorIcon;
    }

    /**
     * Given a processor instance, return the 'spec' block of 
     * XML that represents the processor in the XScufl language.
     * This is the element directly inside the 's:processor' element
     * and specifies specific information about this particular
     * processor. Returns null if there is no handler.
     */
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
