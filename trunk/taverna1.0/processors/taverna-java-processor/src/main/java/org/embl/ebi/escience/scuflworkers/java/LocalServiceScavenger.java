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
import java.util.List;
import java.util.Map;
import java.util.Properties;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A scavenger that can create new LocalServiceProcessor nodes. Local Services
 * are discovered using the LocalWorker SPI pattern, with the processor
 * description and category held in localworkers.properties mapped to the
 * classname.
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * @author Stuart Owen
 */
public class LocalServiceScavenger extends Scavenger {

	private static final long serialVersionUID = -901978754221092069L;

	private static Logger logger = Logger
			.getLogger(LocalServiceScavenger.class);

	private static Map<String, Scavenger> workerList = new HashMap<String, Scavenger>();

	static {
		try {
			Properties properties = new Properties();
			List<LocalWorker> workers = LocalWorkerRegistry.instance()
					.getLocalWorkers();
			for (LocalWorker worker : workers) {
				String description = properties.getProperty(worker.getClass()
						.getName());
				if (description == null) {
					// use the workers classloader to find the properties file
					// (because of Raven).
					Enumeration en = worker.getClass().getClassLoader()
							.getResources("localworkers.properties");
					while (en.hasMoreElements()) {
						URL resURL = (URL) en.nextElement();
						properties.load(resURL.openStream());
					}
					description = properties.getProperty(worker.getClass()
							.getName());
				}

				if (description == null) {
					logger
							.warn("No description in localworkers.properties file found for: "
									+ worker.getClass().getName());
				} else {
					String[] split = description.split(":");
					String shortDescription = description;
					if (split.length == 2) {
						shortDescription = split[1];
					}
					workerList.put(description, new Scavenger(
							new LocalServiceProcessorFactory(worker.getClass()
									.getName(), shortDescription)));
				}
			}
		} catch (Exception e) {
			logger.error("Failure in initialization of LocalWorker scavenger",
					e);
		}
	}

	/**
	 * Create a new local service scavenger
	 */
	public LocalServiceScavenger() throws ScavengerCreationException {
		super("Local Java widgets");
		// Get all the categories and create nodes
		Map<String, DefaultMutableTreeNode> nodeMap = new HashMap<String, DefaultMutableTreeNode>();
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
