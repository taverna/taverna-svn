/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, the University of Chicago
 */
package org.embl.ebi.escience.scuflworkers.gt4;

import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A Scavenger that knows how to get all the GT4 services from a specified
 * installation
 * 
 * @author Wei Tan
 */

public class GT4Scavenger extends Scavenger {

	private static Logger logger = Logger.getLogger(GT4Scavenger.class);
	private static final long serialVersionUID = -57047613557546670L;

	/**
	 * The base URL of the GT4 index service
	 */
	String indexURL;
	public GT4Scavenger(){
		super("blank");
	}

	/**
	 * Create a new GT4 scavenger, the base parameter should be the base URL
	 */
	public GT4Scavenger(String theURL,ServiceQuery[] sq) throws ScavengerCreationException {
		// Making sure there is / at the end of theBase
		
		//	super("GT4 Services@ " + theURL);
		//a=(b>0)?4:25;
		super((sq==null||sq.length==0)? "GT4 Services@ "+  theURL: "GT4 Services@ "+  theURL + sq[0].queryCriteria +" == " + sq[0].queryValue + new java.util.Random().nextInt());
		
		indexURL=theURL.trim();
		
		//TODO remove this line
		//indexURL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
		
		
		try {
			
			List<GT4Service> services=GT4ScavengerAgent.load(indexURL,sq);
			populateTree(services);
			System.out.println("Scavenger generation complete.");
			
		} catch (Exception e) {
			logger.warn("Unable to load index for GT4Scavenger",e);
			throw new ScavengerCreationException(e.getMessage());
		}
	}
	
	/**
	 * populates the tree with discovered categories, and services.
	 * @param services
	 */
	private void populateTree(List<GT4Service>services) {
		for (GT4Service service : services) {
			System.out.println("**Load GT4 service with address: "+ service.getServiceWSDLLocation());
			DefaultMutableTreeNode serviceNode = new DefaultMutableTreeNode(service.getServiceName());
			for (String operation : service.getOperations()) {
			System.out.println("*Load operation: " +operation);
				GT4ProcessorFactory f = new GT4ProcessorFactory(service.getServiceWSDLLocation(), operation);
				serviceNode.add(new DefaultMutableTreeNode(f));
			}
			this.add(serviceNode);
		}
	}	
	

}
