/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;

import java.lang.reflect.Constructor;
import javax.swing.ImageIcon;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;

// Utility Imports
import java.util.*;

// JDOM Imports
import org.jdom.Element;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.XMLHandler;
import org.apache.log4j.Logger;

import java.lang.Class;
import java.lang.ClassLoader;
import java.lang.Exception;
import java.lang.Object;
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
  private static Logger LOG = Logger.getLogger(ProcessorHelper.class);

    static Map coloursForTagName = new HashMap();
    static Map tagNameForClassName = new HashMap();
    static Map classNameForTagName = new HashMap();
    static Map iconForTagName = null;
    static Map taskClassForTagName = new HashMap();
    static Map xmlHandlerForTagName = new HashMap();
    static Map tagNameForScavenger = new HashMap();
    static Map editorForTagName = new HashMap();
    static Set simpleScavengers = new HashSet();
    static Properties tavernaProperties = null;

    static ImageIcon unknownProcessorIcon;

  static {
    try {
      // Get the classloader for this class
      ClassLoader loader = ProcessorHelper.class.getClassLoader();
      if (loader == null) {
	  loader = Thread.currentThread().getContextClassLoader();
      }
      // Load the 'unknown processor' image icon
      unknownProcessorIcon = new ImageIcon(loader.getResource("org/embl/ebi/escience/scuflui/png/unknownprocessor.png"));
      // Load up the values from any taverna.properties files located
      // by the class resource loader.
      Enumeration en = loader.getResources("taverna.properties");
      tavernaProperties = new Properties();
      while (en.hasMoreElements()) {
        URL resourceURL = (URL)en.nextElement();
        LOG.warn("Loading resources from : "+resourceURL.toString());
        tavernaProperties.load(resourceURL.openStream());
      }
      // Should now have a populated properties list, set up the various
      // static Map objects for the colours etc.
      // Iterate over all property keys
      for (Iterator i = tavernaProperties.keySet().iterator(); i.hasNext(); ) {
        String key = (String)i.next();
        LOG.debug("key: " + key);
        String value = tavernaProperties.getProperty(key);
        LOG.debug("\t value: "+value);
        String[] keyElements = key.split("\\.");
        // Detect the processor keys
        if (keyElements.length == 4 && keyElements[1].equals("processor")) {
          String tagName = keyElements[2];
          // If this is the class name...
          // Form : taverna.processor.<TAGNAME>.class = <CLASSNAME>
          LOG.debug("\ttag name: " + tagName);
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
          // *** NOW LOADS ON DEMAND ***
          //else if (keyElements[3].equals("icon")) {
          // Fetch resource icon...
          //	iconForTagName.put(tagName,new ImageIcon(loader.getResource(value)));
          //}
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
          Object o = Class.forName(scavengerClassName).newInstance();
          if (o instanceof ScavengerHelper) {
            tagNameForScavenger.put(scavengerClassName, scavengerTagName);
          }
          else if (o instanceof Scavenger) {
            simpleScavengers.add(o);
          }
        }
      }

      LOG.debug("Populated xmlHanderForTagName: " + xmlHandlerForTagName);
    }
    catch (Exception e) {
      System.out.println("Error during initialisation for taverna properties! : "+e.getMessage());
      e.printStackTrace();
      // Don't exit, as this hides the stack trace etc!
      // System.exit(1);
    }
  }

    /**
     * Return the set of instances of simple (null constructor)
     * scavengers; these are added automatically to all service
     * selection panels on creation.
     */
    public static Set getSimpleScavengerSet() {
	return simpleScavengers;
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
     * Given a processor instance, return a concrete task worker
     * for that instance.
     * @return a ProcessorTaskWorker implementation or null
     * if none can be found.
     */
    public static ProcessorTaskWorker getTaskWorker(Processor p) {
	String taskClassName = getTaskClassName(p);
	if (taskClassName != null) {
	    // Assume there is a constructor that takes a single processor
	    // as its argument
	    try {
		Class[] constructorClasses = new Class[] {Processor.class};
		Class taskClass = Class.forName(taskClassName);
		Constructor taskConstructor = taskClass.getConstructor(constructorClasses);
		ProcessorTaskWorker taskWorker = (ProcessorTaskWorker)taskConstructor.newInstance(new Object[]{p});
		return taskWorker;
	    }
	    catch (Exception ex) {
		ex.printStackTrace();
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
	if (iconForTagName == null) {
	    // Initialise the icon store
	    iconForTagName = new HashMap();
	    for (Iterator i = tavernaProperties.keySet().iterator(); i.hasNext(); ) {
		String key = (String)i.next();
		String value = tavernaProperties.getProperty(key);
		String[] keyElements = key.split("\\.");
		if (keyElements.length == 4 && keyElements[1].equals("processor")) {
		    String loadTagName = keyElements[2];
		    // Form : taverna.processor.<TAGNAME>.icon = <RENDERINGHINT_ICON>
		    if (keyElements[3].equals("icon")) {
			// Fetch resource icon...
			iconForTagName.put(loadTagName,new ImageIcon(ProcessorHelper.class.getClassLoader().getResource(value)));
		    }
		}
	    }
	}

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
		Element result = xh.elementForProcessor(p);
		if (p.getRetries()!=0) {
		    result.setAttribute("maxretries",Integer.toString(p.getRetries()));
		}
		if (p.getRetryDelay()!=0) {
		    result.setAttribute("retrydelay",Integer.toString(p.getRetryDelay()));
		}
		if (p.getBackoff()!=1.0) {
		    result.setAttribute("retrybackoff",Double.toString(p.getBackoff()));
		}
		return result;
	    }
	}
	return null;
    }

    /**
     * Return a factory capable of producing the supplied spec of processor. The element
     * passed in is the 'spec element' refered to in the language reference. Returns null
     * is there is no matching factory implementation bound to this spec element
     */
    public static ProcessorFactory loadFactoryFromXML(Element specNode) {
	String tagName = specNode.getName();
	XMLHandler xh = (XMLHandler)xmlHandlerForTagName.get(tagName);
	if (xh == null) {
	    return null;
	}
	else {
	    return xh.getFactory(specNode);
	}
    }

    /**
     * Spit back a processor given a chunk of xml, the element passed in being the 'processor' tag
     * return null if we can't handle it
     */
    public static Processor loadProcessorFromXML(Element processorNode, ScuflModel model, String name)
            throws ProcessorCreationException, DuplicateProcessorNameException, XScuflFormatException {
      // Get the first available handler for this processor and use it to load
      LOG.debug("Attempting to load processor for: " + processorNode);
      Processor loadedProcessor = null;
      for (Iterator i = processorNode.getChildren().iterator();
           i.hasNext() && loadedProcessor==null; )
      {
        Element candidateElement = (Element) i.next();
        String elementName = candidateElement.getName();
        XMLHandler xh = (XMLHandler) xmlHandlerForTagName.get(elementName);
        LOG.debug("Possible help: " + candidateElement + " " + elementName + " -> " + xh);
        if (xh != null) {
          // mrp: ouch - should we not be using candidateElement in place of processorNode?
          loadedProcessor = xh.loadProcessorFromXML(processorNode, model, name);
          // Loaded the processor, now configure from the inner spec element
          // for retry policy.
          String maxRetryString = candidateElement.getAttributeValue("maxretries");
          if (maxRetryString != null) {
            loadedProcessor.setRetries(Integer.parseInt(maxRetryString));
          }
          String retryDelayString = candidateElement.getAttributeValue("retrydelay");
          if (retryDelayString != null) {
            loadedProcessor.setRetryDelay(Integer.parseInt(retryDelayString));
          }
          String retryBackoffString = candidateElement.getAttributeValue("retrybackoff");
          if (retryBackoffString != null) {
            loadedProcessor.setBackoff(Double.parseDouble(retryBackoffString));
          }
        }
      }

      // Appended to the name so the alternate processor have at least
      // a local name. Doesn't really matter, just better than leaving
      // them blank.
      int alternateCount = 1;
      if (loadedProcessor!=null) {
        // Iterate over all alternate definitions and load them into the
        // processor as appropriate
        List l = processorNode.getChildren("alternate",XScufl.XScuflNS);
        for (Iterator i = l.iterator(); i.hasNext(); ) {
          Element alternateElement = (Element)i.next();
          Processor alternateProcessor = loadProcessorFromXML(alternateElement, null, "alternate"+alternateCount++);
          AlternateProcessor ap = new AlternateProcessor(alternateProcessor);
          // Sort out the input mapping
          List inputMapping = alternateElement.getChildren("inputmap",XScufl.XScuflNS);
          for (Iterator j = inputMapping.iterator(); j.hasNext();) {
            Element inputMapItem = (Element)j.next();
            String key = inputMapItem.getAttributeValue("key");
            String value = inputMapItem.getAttributeValue("value");
            ap.getInputMapping().put(key, value);
          }
          // ..and the output mapping. See the javadoc
          // for the AlternateProcessor class for more
          // details about the mapping functionality.
          List outputMapping = alternateElement.getChildren("outputmap",XScufl.XScuflNS);
          for (Iterator j = outputMapping.iterator(); j.hasNext();) {
            Element outputMapItem = (Element)j.next();
            String key = outputMapItem.getAttributeValue("key");
            String value = outputMapItem.getAttributeValue("value");
            ap.getOutputMapping().put(key, value);
          }
          loadedProcessor.addAlternate(ap);
        }

      }
      if (loadedProcessor != null) {
        // Add the annotation templates
        List l = processorNode.getChildren("template", XScufl.XScuflNS);
        for (Iterator i = l.iterator(); i.hasNext();) {
          loadedProcessor.addAnnotationTemplate(new AnnotationTemplate((Element)i.next()));
        }

      }

      if (loadedProcessor == null) {
        LOG.warn("No processor found for element: " + processorNode);
      }

      return loadedProcessor;
    }
}
