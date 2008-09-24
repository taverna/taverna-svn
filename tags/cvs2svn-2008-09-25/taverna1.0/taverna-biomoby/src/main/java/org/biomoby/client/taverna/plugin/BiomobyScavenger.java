/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.net.URL;
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
 * @version $Id: BiomobyScavenger.java,v 1.7 2008-09-05 14:34:41 edwardkawas Exp $
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
		this(base, null);
        }

	/**
	 * constructor that taverna's init uses
	 */
	@SuppressWarnings("unchecked")
	public BiomobyScavenger(String base, String uri)
			throws ScavengerCreationException {
		super("Biomoby @ " + base);
		// get list of services and their authorities
		// create the tree helper
		try {
			helper = new BiomobyScavengerWorker(base, uri);
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
		
		new Thread() {
        	    public void run() {
        		try {
        		    String url = helper.getRegistryUrl();
        		    String filelocation = helper.getDatatypesRdfUrl();
        		    org.biomoby.registry.meta.Registry reg = new org.biomoby.registry.meta.Registry(
        			    url, url, "http://domain.com/MOBY/Central");
        		    org.biomoby.shared.MobyDataType.loadDataTypes((new URL(
        			    filelocation)), reg);
        		} catch (Exception e) {
        		    e.printStackTrace();
        		}
        	    }
        	}.start();
        	new Thread() {
        	    public void run() {
        		try {
        		    String url = helper.getRegistryUrl();
        		    String filelocation = helper.getNamespacesRdfUrl();
        		    org.biomoby.registry.meta.Registry reg = new org.biomoby.registry.meta.Registry(
        			    url, url, "http://domain.com/MOBY/Central");
        		    /*org.biomoby.shared.MobyNamespace.loadNamespace((new URL(
        			    filelocation)), reg);*/
        		    org.biomoby.shared.MobyNamespace.getNamespace("foo",reg);
        		    
        		} catch (Exception e) {
        		    e.printStackTrace();
        		}
        	    }
        	}.start();
		
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
