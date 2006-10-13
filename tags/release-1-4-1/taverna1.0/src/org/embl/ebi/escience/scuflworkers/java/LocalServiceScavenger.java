/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.java;

import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that can create new LocalServiceProcessor nodes. <p/> This uses
 * the Java SPI model to locate instances of LocalWorker. List all
 * implementations under
 * <code>META-INF/services/org.embl.ebi.escience.scuflworkers.java.LocalWorker</code>.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 */
public class LocalServiceScavenger extends Scavenger {

	private static final long serialVersionUID = -901978754221092069L;

	private static Logger LOG = Logger.getLogger(LocalServiceScavenger.class);

	private static Map workerList = new HashMap();

	static {
		try {
			LOG.info("Loading LocalWorker implementations");
			Enumeration en = LocalServiceScavenger.class.getClassLoader().getResources(
					"META-INF/services/org.embl.ebi.escience.scuflworkers.java.LocalWorker");
			Properties tavernaProperties = new Properties();
			while (en.hasMoreElements()) {
				URL resourceURL = (URL) en.nextElement();
				LOG.debug("Loading workers from: " + resourceURL);
				tavernaProperties.load(resourceURL.openStream());
			}

			// Iterate over the available local properties
			for (Iterator i = tavernaProperties.keySet().iterator(); i.hasNext();) {
				String className = (String) i.next();
				String description = (String) tavernaProperties.get(className);
				String[] split = description.split(":");
				String category = "default";
				if (split.length == 2) {
					category = split[0];
					description = split[1];
				}
				LOG.debug("Worker: " + className + " category: " + category + " desc: " + description);
				workerList.put((String) tavernaProperties.get(className), new Scavenger(
						new LocalServiceProcessorFactory(className, description)));
			}
			LOG.info("Loaded: " + workerList);
		} catch (Exception e) {
			LOG.error("Failure in initialization of LocalWorker scavenger", e);
		}
	}

	/**
	 * Create a new local service scavenger
	 */
	public LocalServiceScavenger() throws ScavengerCreationException {
		super("Local Java widgets");
		// Get all the categories and create nodes
		Map nodeMap = new HashMap();
		for (Iterator i = workerList.keySet().iterator(); i.hasNext();) {
			String key = (String) i.next();
			Scavenger s = (Scavenger) workerList.get(key);
			String category = "default";
			if (key.split(":").length == 2) {
				category = key.split(":")[0];
			}
			// If the category doesn't exist create it
			DefaultMutableTreeNode categoryNode;
			if (nodeMap.containsKey(category)) {
				categoryNode = (DefaultMutableTreeNode) nodeMap.get(category);
			} else {
				categoryNode = new DefaultMutableTreeNode(category);
				nodeMap.put(category, categoryNode);
			}
			categoryNode.add(s);
		}
		// for all available local widgets, add them as
		// children to this scavenger.
		for (Iterator i = nodeMap.values().iterator(); i.hasNext();) {
			add((DefaultMutableTreeNode) i.next());
		}
	}

}
