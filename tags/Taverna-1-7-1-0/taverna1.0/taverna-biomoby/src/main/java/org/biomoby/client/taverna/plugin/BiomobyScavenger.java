/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.util.ArrayList;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biomoby.shared.MobyException;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A Scavenger that knows how to get all the Biomoby services from a specified
 * Biomoby Central Registry.
 * <p>
 * 
 * @version $Id: BiomobyScavenger.java,v 1.6 2007-12-19 16:13:09 edwardkawas Exp $
 * @author Martin Senger
 */
public class BiomobyScavenger extends Scavenger {

	// private static Logger logger = Logger.getLogger(BiomobyScavenger.class);

	private static final long serialVersionUID = 3545233648191289400L;

	private BiomobyScavengerWorker helper;
	
	private boolean showAliveOnly = false;

	/**
	 * constructor that taverna's init uses
	 */
	@SuppressWarnings("unchecked")
	public BiomobyScavenger(String base) throws ScavengerCreationException {
		super("Biomoby @ " + base);
		// create the tree helper
		try {
			helper = new BiomobyScavengerWorker(base, null);
		} catch (MobyException e1) {
			throw new ScavengerCreationException(
					"There was a problem creating the Biomoby Scavenger:"
							+ e1.getMessage());
		}
		ArrayList<DefaultMutableTreeNode> list;
		try {
			list = helper.getServices();
		} catch (MobyException e) {
			throw new ScavengerCreationException(e.getMessage());
		}
		for(DefaultMutableTreeNode node : list) {
			add(node);
		}
		list = null;
		
		try {
			// create the datatype tree
			insert(helper.getDataTypes(), 0);
			
		} catch (MobyException e) {
			ScavengerCreationException sce = new ScavengerCreationException(
					"Could not create the Datatype ontology node: " + e.getMessage());
			sce.initCause(e);
			throw sce;
		}
		/* do this to speed things up for the user; here we 
		 * read the RDF from a file, rather than downloading 
		 * it again whenever the datatypes are used 
		 */
		try {
		String url = helper.getCacheImpl().getMOBYCENTRAL_REGISTRY_URL();
		String filelocation = helper.getCacheImpl().getDatatypeRDFLocation();
		org.biomoby.registry.meta.Registry reg = new org.biomoby.registry.meta.Registry(url,url,"http://domain.com/MOBY/Central" );
		org.biomoby.shared.MobyDataType.loadDataTypes(((new java.io.File(filelocation)).toURL()), reg);
		} catch (Exception e){}
	}

	/**
	 * constructor that taverna's init uses
	 */
	@SuppressWarnings("unchecked")
	public BiomobyScavenger(String base, String uri)
			throws ScavengerCreationException {
		super("Biomoby @ " + base + "," + uri);
		// get list of services and their authorities
		// create the tree helper
		try {
			helper = new BiomobyScavengerWorker(base, null);
		} catch (MobyException e1) {
			throw new ScavengerCreationException(
					"There was a problem creating the Biomoby Scavenger:"
							+ e1.getMessage());
		}
		ArrayList<DefaultMutableTreeNode> list;
		try {
			list = helper.getServices();
		} catch (MobyException e) {
			throw new ScavengerCreationException(e.getMessage());
		}
		for(DefaultMutableTreeNode node : list) {
			add(node);
		}

		try {
			// create the datatype tree
			insert(helper.getDataTypes(), 0);
			
		} catch (MobyException e) {
			ScavengerCreationException sce = new ScavengerCreationException(
					"Could not create the Datatype ontology node: " + e.getMessage());
			sce.initCause(e);
			throw sce;
		}
		/* do this to speed things up for the user; here we 
		 * read the RDF from a file, rather than downloading 
		 * it again whenever the datatypes are used 
		 */
		try {
		String url = helper.getCacheImpl().getMOBYCENTRAL_REGISTRY_URL();
		String filelocation = helper.getCacheImpl().getDatatypeRDFLocation();
		org.biomoby.registry.meta.Registry reg = new org.biomoby.registry.meta.Registry(url,url,uri );
		org.biomoby.shared.MobyDataType.loadDataTypes(((new java.io.File(filelocation)).toURL()), reg);
		} catch (Exception e){}
	}

	/**
	 * @return the helper
	 */
	public BiomobyScavengerWorker getScavengerWorker() {
		return helper;
	}

	/**
	 * @return the showAliveOnly
	 */
	public boolean isShowAliveOnly() {
		return showAliveOnly;
	}

	/**
	 * @param showAliveOnly the showAliveOnly to set
	 */
	public void setShowAliveOnly(boolean showAliveOnly) {
		this.showAliveOnly = showAliveOnly;
	}

}
