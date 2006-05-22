/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers;

import java.lang.reflect.Constructor;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;

import javax.swing.ImageIcon;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.AnnotationTemplate;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.XScufl;
import org.embl.ebi.escience.scufl.parser.XScuflFormatException;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.jdom.Element;

/**
 * Provides rendering and other hints for different processor implementations,
 * including preferred colours and icons. The data used by this class is loaded
 * at classload time from all 'taverna.properties' files found by the system
 * classloader, these files contain the processor specific configuration that
 * this class acts as an interface to. An example for the Soaplab processor type
 * is shown below :
 * 
 * <pre>
 *    taverna.processor.soaplabwsdl.class = org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor
 *    taverna.processor.soaplabwsdl.xml = org.embl.ebi.escience.scuflworkers.soaplab.SoaplabXMLHandler
 *    taverna.processor.soaplabwsdl.colour = lightgoldenrodyellow
 *    taverna.processor.soaplabwsdl.icon = org/embl/ebi/escience/scuflui/soaplab.gif
 *    taverna.processor.soaplabwsdl.taskclass = uk.ac.soton.itinnovation.taverna.enactor.entities.SoaplabTask
 * </pre>
 * 
 * To load additional processor types for enactment and display within the
 * workbench, you will need to create the appropriate helper classes such as the
 * XML handler and then point to the class names in a 'taverna.properties' file.
 * I suggest you package all these items into a single .jar file, in which case
 * simply ensuring that the classpath contains your .jar should allow everything
 * to work.
 * 
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
			unknownProcessorIcon = new ImageIcon(
					loader
							.getResource("org/embl/ebi/escience/scuflui/icons/explorer/unknownprocessor.png"));
			// Load up the values from any taverna.properties files located
			// by the class resource loader.
			Enumeration en = loader.getResources("taverna.properties");
			tavernaProperties = new Properties();
			while (en.hasMoreElements()) {
				URL resourceURL = (URL) en.nextElement();
				LOG.info("Loading resources from : " + resourceURL.toString());
				tavernaProperties.load(resourceURL.openStream());
			}
			// Should now have a populated properties list, set up the various
			// static Map objects for the colours etc.
			// Iterate over all property keys
			for (Iterator i = tavernaProperties.keySet().iterator(); i
					.hasNext();) {
				String key = (String) i.next();
				LOG.debug("key: " + key);
				String value = tavernaProperties.getProperty(key);
				LOG.debug("\t value: " + value);
				String[] keyElements = key.split("\\.");
				// Detect the processor keys
				if (keyElements.length == 4
						&& keyElements[1].equals("processor")) {
					String tagName = keyElements[2];
					// If this is the class name...
					// Form : taverna.processor.<TAGNAME>.class = <CLASSNAME>
					LOG.debug("\ttag name: " + tagName);
					if (keyElements[3].equals("class")) {
						// Store the class name <-> tag name mappings
						tagNameForClassName.put(value, tagName);
						classNameForTagName.put(tagName, value);
					}
					// Form : taverna.processor.<TAGNAME>.colour =
					// <RENDERINGHINT_COLOUR>
					else if (keyElements[3].equals("colour")) {
						// Configure default display colour for i.e. dot
						coloursForTagName.put(tagName, value);
					}
					// Form : taverna.processor.<TAGNAME>.icon =
					// <RENDERINGHINT_ICON>
					// *** NOW LOADS ON DEMAND ***
					// else if (keyElements[3].equals("icon")) {
					// Fetch resource icon...
					// iconForTagName.put(tagName,new
					// ImageIcon(loader.getResource(value)));
					// }
					// Form : taverna.processor.<TAGNAME>.taskclass =
					// <ENACTOR_TASK_CLASS>
					else if (keyElements[3].equals("taskclass")) {
						// Configure the taverna task for the enactor to run
						// this type of processor
						taskClassForTagName.put(tagName, value);
					}
					// Form : taverna.processor.<TAGNAME>.xml =
					// <XML_HANDLER_CLASS>
					else if (keyElements[3].equals("xml")) {
						// Configure and instantiate the XML handler for this
						// type of processor
						String handlerClassName = value;
						// Create an instance of the handler
						Class handlerClass = Class.forName(handlerClassName);
						XMLHandler xh = (XMLHandler) handlerClass.newInstance();
						xmlHandlerForTagName.put(tagName, xh);
					}
					// Form : taverna.processor.<TAGNAME>.editor =
					// <EDITOR_CLASS>
					else if (keyElements[3].equals("editor")) {
						// Configure and create the processor editor handler
						String editorClassName = value;
						// Create an instance...
						Class editorClass = Class.forName(editorClassName);
						ProcessorEditor pe = (ProcessorEditor) editorClass
								.newInstance();
						editorForTagName.put(tagName, pe);
					}
				}
				// Form : taverna.scavenger.<TAGNAME> = <SCAVENGERCLASS>
				// Use the scavenger class as a key, as this allows us to have
				// more than one scavenger per tag type. We have the tag type as
				// a value in order that the rendering code can get the icon
				// hint
				// for the type being created.
				keyElements = key.split("\\.", 3);
				if (keyElements.length == 3
						&& keyElements[1].equals("scavenger")) {
					// Get the set of scavenger creating classes
					String scavengerClassName = keyElements[2];
					String scavengerTagName = value;
					Object o = Class.forName(scavengerClassName).newInstance();
					if (o instanceof ScavengerHelper) {
						tagNameForScavenger.put(scavengerClassName,
								scavengerTagName);
					} else if (o instanceof Scavenger) {
						simpleScavengers.add(o);
					}
				}
			}

			LOG.debug("Populated xmlHanderForTagName: " + xmlHandlerForTagName);
		} catch (Exception e) {
			LOG.fatal("Error during initialisation for taverna properties!", e);
			// Don't exit, as this hides the stack trace etc!
			// System.exit(1);
		}
	}

	/**
	 * Return the set of instances of simple (null constructor) scavengers;
	 * these are added automatically to all service selection panels on
	 * creation.
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
	 * Given a processor instance, return the fully qualified class name of a
	 * TavernaTask for the myGrid enactor to invoke the operation represented by
	 * the processor
	 */
	public static String getTaskClassName(Processor p) {
		String className = p.getClass().getName();
		String tagName = (String) tagNameForClassName.get(className);
		if (tagName == null) {
			return null;
		}
		String taskClassName = (String) taskClassForTagName.get(tagName);
		if (taskClassName == null) {
			return null;
		}
		return taskClassName;
	}

	/**
	 * Given a processor instance, return a concrete task worker for that
	 * instance.
	 * 
	 * @return a ProcessorTaskWorker implementation or null if none can be
	 *         found.
	 */
	public static ProcessorTaskWorker getTaskWorker(Processor p) {
		String taskClassName = getTaskClassName(p);
		if (taskClassName == null) {
			return null;
		}
		// Assume there is a constructor that takes a single processor
		// as its argument
		try {
			Class[] constructorClasses = new Class[] { Processor.class };
			Class taskClass = Class.forName(taskClassName);
			Constructor taskConstructor = taskClass
					.getConstructor(constructorClasses);
			ProcessorTaskWorker taskWorker = (ProcessorTaskWorker) taskConstructor
					.newInstance(new Object[] { p });
			return taskWorker;
		} catch (Exception ex) {
			LOG.error("Could not get task worker " + taskClassName, ex);
			return null;
		}
	}

	/**
	 * Given a class name, return the tag name used by this helper class as an
	 * index for the other categories such as icons.
	 */
	public static String getTagNameForClassName(String className) {
		// LOG.debug("Request for tag name for : "+className);
		return (String) tagNameForClassName.get(className);
	}

	/**
	 * Given a tag name, return the in place editor for the processor
	 */
	public static ProcessorEditor getEditorForTagName(String tagName) {
		return (ProcessorEditor) editorForTagName.get(tagName);
	}

	/**
	 * Get the xml handler for a given tag name
	 */
	public static XMLHandler getXMLHandlerForTagName(String tagname) {
		return (XMLHandler) xmlHandlerForTagName.get(tagname);
	}

	/**
	 * Given a tag name, return the preferred image icon for that tag.
	 */
	public static ImageIcon getIconForTagName(String tagName) {
		if (iconForTagName == null) {
			// Initialise the icon store
			iconForTagName = new HashMap();
			for (Iterator i = tavernaProperties.keySet().iterator(); i
					.hasNext();) {
				String key = (String) i.next();
				String value = tavernaProperties.getProperty(key);
				String[] keyElements = key.split("\\.");
				if (keyElements.length == 4
						&& keyElements[1].equals("processor")) {
					String loadTagName = keyElements[2];
					// Form : taverna.processor.<TAGNAME>.icon =
					// <RENDERINGHINT_ICON>
					if (keyElements[3].equals("icon")) {
						// Fetch resource icon...
						iconForTagName.put(loadTagName, new ImageIcon(
								ProcessorHelper.class.getClassLoader()
										.getResource(value)));
					}
				}
			}
		}

		// LOG.debug("Request for icon for : "+tagName);
		ImageIcon icon = (ImageIcon) iconForTagName.get(tagName);
		if (icon == null) {
			return unknownProcessorIcon;
		}
		return icon;
	}

	/**
	 * Given a processor instance, return the preferred colour to be used for UI
	 * representations.
	 */
	public static String getPreferredColour(Processor p) {
		String className = p.getClass().getName();
		String tagName = (String) tagNameForClassName.get(className);
		if (tagName != null) {
			String colour = (String) coloursForTagName.get(tagName);
			if (colour != null) {
				return colour;
			}
		}
		return "white";
	}

	/**
	 * Given a processor instance, return an image icon to be used in, for
	 * example, tree renderer objects.
	 */
	public static ImageIcon getPreferredIcon(Processor p) {
		String className = p.getClass().getName();
		String tagName = (String) tagNameForClassName.get(className);
		if (tagName != null) {
			ImageIcon icon = getIconForTagName(tagName);
			if (icon != null) {
				return icon;
			}
		}
		return unknownProcessorIcon;
	}

	/**
	 * Given a processor instance, return the 'spec' block of XML that
	 * represents the processor in the XScufl language. This is the element
	 * directly inside the 's:processor' element and specifies specific
	 * information about this particular processor. Returns null if there is no
	 * handler.
	 */
	public static Element elementForProcessor(Processor p) {
		return elementForProcessor(p, true);
	}

	/**
	 * Given a processor instance, return the 'spec' block of XML that
	 * represents the processor in the XScufl language. This is the element
	 * directly inside the 's:processor' element and specifies specific
	 * information about this particular processor. Returns null if there is no
	 * handler.
	 * <p>
	 * If the decorations flag is set to true this will set the various
	 * attributes (maxretries, retrydelay, retrybackoff, critical, breakpoint),
	 * otherwise these will not be set. We need to be able to turn this off to
	 * allow the comparison to nodes in the services panel to function correctly -
	 * this is a textual comparison and these nodes never have these attributes.
	 */
	public static Element elementForProcessor(Processor p, boolean decorations) {
		String className = p.getClass().getName();
		String tagName = (String) tagNameForClassName.get(className);
		if (tagName == null) {
			return null;
		}
		XMLHandler xh = (XMLHandler) xmlHandlerForTagName.get(tagName);
		if (xh == null) {
			return null;
		}
		Element result = xh.elementForProcessor(p);
		if (decorations) {
			if (p.getRetries() != 0) {
				result.setAttribute("maxretries", Integer.toString(p
						.getRetries()));
			}
			if (p.getRetryDelay() != 0) {
				result.setAttribute("retrydelay", Integer.toString(p
						.getRetryDelay()));
			}
			if (p.getBackoff() != 1.0) {
				result.setAttribute("retrybackoff", Double.toString(p
						.getBackoff()));
			}
			if (p.getCritical()) {
				result.setAttribute("critical", "" + p.getCritical());
			}
			if (p.hasBreakpoint()) {
				result.setAttribute("breakpoint", "true");
			}
		}
		return result;
	}

	/**
	 * Return a factory capable of producing the supplied spec of processor. The
	 * element passed in is the 'spec element' refered to in the language
	 * reference. Returns null is there is no matching factory implementation
	 * bound to this spec element
	 */
	public static ProcessorFactory loadFactoryFromXML(Element specNode) {
		String tagName = specNode.getName();
		XMLHandler xh = (XMLHandler) xmlHandlerForTagName.get(tagName);
		if (xh == null) {
			return null;
		}
		return xh.getFactory(specNode);
	}

	/**
	 * Spit back a processor given a chunk of xml, the element passed in being
	 * the 'processor' tag return null if we can't handle it
	 */
	public static Processor loadProcessorFromXML(Element processorNode,
			ScuflModel model, String name) throws ProcessorCreationException,
			DuplicateProcessorNameException, XScuflFormatException {
		// Get the first available handler for this processor and use it to load
		LOG.debug("Attempting to load processor for: " + processorNode);
		Processor loadedProcessor = null;
		for (Iterator i = processorNode.getChildren().iterator(); i.hasNext()
				&& loadedProcessor == null;) {
			Element candidateElement = (Element) i.next();
			String elementName = candidateElement.getName();
			XMLHandler xh = (XMLHandler) xmlHandlerForTagName.get(elementName);
			if (xh == null) {
				continue;
			}
			LOG.debug("Possible help: " + candidateElement + " " + elementName
					+ " -> " + xh);
			// mrp: ouch - should we not be using candidateElement in place
			// of processorNode?
			loadedProcessor = xh.loadProcessorFromXML(processorNode, model,
					name);
			// Loaded the processor, now configure from the inner spec
			// element for retry policy.
			String maxRetryString = candidateElement
					.getAttributeValue("maxretries");
			if (maxRetryString != null) {
				loadedProcessor.setRetries(Integer.parseInt(maxRetryString));
			}
			String retryDelayString = candidateElement
					.getAttributeValue("retrydelay");
			if (retryDelayString != null) {
				loadedProcessor.setRetryDelay(Integer
						.parseInt(retryDelayString));
			}
			String retryBackoffString = candidateElement
					.getAttributeValue("retrybackoff");
			if (retryBackoffString != null) {
				loadedProcessor.setBackoff(Double
						.parseDouble(retryBackoffString));
			}
			String critical = candidateElement.getAttributeValue("critical");
			if (critical != null) {
				loadedProcessor.setCritical((new Boolean(critical)
						.booleanValue()));
			}
			String breakpoint = candidateElement
					.getAttributeValue("breakpoint");
			if (breakpoint != null) {
				if ((new Boolean(breakpoint).booleanValue()))
					loadedProcessor.addBreakpoint();
			}
		}
		if (loadedProcessor == null) {
			LOG.warn("No processor found for element: " + processorNode);
			return null;
		}

		// Appended to the name so the alternate processor have at least
		// a local name. Doesn't really matter, just better than leaving
		// them blank.
		int alternateCount = 1;
		// Iterate over all alternate definitions and load them into the
		// processor as appropriate
		List alternates = processorNode.getChildren("alternate", XScufl.XScuflNS);
		for (Iterator i = alternates.iterator(); i.hasNext();) {
			Element alternateElement = (Element) i.next();
			Processor alternateProcessor = loadProcessorFromXML(
					alternateElement, null, "alternate" + alternateCount++);
			AlternateProcessor ap = new AlternateProcessor(alternateProcessor);
			// Sort out the input mapping
			List inputMapping = alternateElement.getChildren("inputmap",
					XScufl.XScuflNS);
			for (Iterator j = inputMapping.iterator(); j.hasNext();) {
				Element inputMapItem = (Element) j.next();
				String key = inputMapItem.getAttributeValue("key");
				String value = inputMapItem.getAttributeValue("value");
				ap.getInputMapping().put(key, value);
			}
			// ..and the output mapping. See the javadoc
			// for the AlternateProcessor class for more
			// details about the mapping functionality.
			List outputMapping = alternateElement.getChildren("outputmap",
					XScufl.XScuflNS);
			for (Iterator j = outputMapping.iterator(); j.hasNext();) {
				Element outputMapItem = (Element) j.next();
				String key = outputMapItem.getAttributeValue("key");
				String value = outputMapItem.getAttributeValue("value");
				ap.getOutputMapping().put(key, value);
			}
			loadedProcessor.addAlternate(ap);
		}

		// Add the annotation templates
		List templates = processorNode.getChildren("template", XScufl.XScuflNS);
		for (Iterator i = templates.iterator(); i.hasNext();) {
			loadedProcessor.addAnnotationTemplate(new AnnotationTemplate(
					(Element) i.next()));
		}

		return loadedProcessor;
	}
}
