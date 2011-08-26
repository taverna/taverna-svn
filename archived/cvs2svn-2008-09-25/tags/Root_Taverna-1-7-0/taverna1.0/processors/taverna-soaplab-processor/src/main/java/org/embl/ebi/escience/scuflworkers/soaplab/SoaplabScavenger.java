/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

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
		theBase=theBase.trim();
		base = theBase.endsWith("/") ? theBase : theBase + "/";
		// Get the categories for this installation
		try {
			List<SoaplabCategory> categories=SoaplabScavengerAgent.load(base);
			populateTree(categories);
			
		} catch (MissingSoaplabException e) {
			logger.warn("Unable to load categories for SoaplabScavenger",e);
			throw new ScavengerCreationException(e.getMessage());
		}
	}
	
	/**
	 * populates the tree with discovered categories, and services.
	 * @param categories
	 */
	private void populateTree(List<SoaplabCategory>categories) {
		for (SoaplabCategory category : categories) {
			DefaultMutableTreeNode categoryNode = new DefaultMutableTreeNode(category.getCategory());
			for (String service : category.getServices()) {
				SoaplabProcessorFactory f = new SoaplabProcessorFactory(base, service);
				f.setCategory(category.getCategory());
				categoryNode.add(new DefaultMutableTreeNode(f));
			}
			this.add(categoryNode);
		}
	}	

}
