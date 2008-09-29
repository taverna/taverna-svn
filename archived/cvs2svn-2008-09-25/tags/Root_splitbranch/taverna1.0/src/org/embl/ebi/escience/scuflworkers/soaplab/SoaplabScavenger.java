/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.utils.Soap;

/**
 * A Scavenger that knows how to get all the Soaplab services from a specified
 * installation
 * 
 * @author Tom Oinn, Stian Soiland, Stuart Owen
 */

public class SoaplabScavenger extends Scavenger {

	private static Logger logger = Logger.getLogger(SoaplabScavenger.class);

	/**
	 * The base URL of the Soaplab service
	 */
	String base;

	/**
	 * Create a new Soaplab scavenger, the base parameter should be the base URL
	 * of the Soaplab service, i.e. if your AnalysisFactory is at
	 * http://foo.bar/soap/AnalysisFactory the parameter should be
	 * http://foo.bar/soap/
	 */
	public SoaplabScavenger(String theBase) throws ScavengerCreationException {
		// Making sure there is / at the end of theBase
		super("Soaplab @ " + (theBase.endsWith("/") ? theBase : theBase + "/"));
		// Of course we have to do this again since we are not allowed to do
		// such stuff before super()
		base = theBase.endsWith("/") ? theBase : theBase + "/";
		// Get the categories for this installation
		boolean foundAnInstallation = loadCategories(base + "AnalysisFactory");
		// Yes, bitwise OR is on purpose, to make sure the second
		// loadCategories() is always run. Do NOT replace with
		// foundInstallation = foundInstallation || getCategories(..)
		foundAnInstallation |= loadCategories(base + "GowlabFactory");
		if (!foundAnInstallation) {
			// Neither Soaplab nor Gowlab were found, probably a fault
			throw new ScavengerCreationException("Unable to locate a soaplab installation at \n" + base);
		}
	}

	/**
	 * Create the category tree and fill it with SoapLab processor factories as
	 * described by categoryBase
	 * 
	 * @param categoryBase
	 *            The base for the category, example
	 *            "http://www.ebi.ac.uk/soaplab/services/AnalysisFactory"
	 * @return
	 */
	boolean loadCategories(String categoryBase) {
		boolean foundSome = false;
		String[] categories;
		try {
			categories = (String[]) Soap.callWebService(categoryBase, "getAvailableCategories");
		} catch (Exception e) {
			logger.debug("Missing category: "+categoryBase, e);
			return false;
		}
		// Iterate over all the categories, creating new child nodes
		for (int i = 0; i < categories.length; i++) {
			String[] services;
			try {
				services = (String[]) Soap
						.callWebService(categoryBase, "getAvailableAnalysesInCategory", categories[i]);
			} catch (Exception e) {
				logger.info("Skipping category " + categories[i], e);
				continue;
			}
			if (services.length == 0) {
				// Avoid creating empty treenodes
				continue;
			}
			DefaultMutableTreeNode category = new DefaultMutableTreeNode(categories[i]);
			foundSome = true;
			// Iterate over the services
			for (int j = 0; j < services.length; j++) {
				SoaplabProcessorFactory f = new SoaplabProcessorFactory(base, services[j]);
				category.add(new DefaultMutableTreeNode(f));
			}
			this.add(category);
		}
		return foundSome;
	}

}
