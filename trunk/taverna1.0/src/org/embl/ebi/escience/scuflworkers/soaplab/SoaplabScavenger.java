/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import javax.swing.tree.DefaultMutableTreeNode;
import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessorFactory;
import java.lang.Exception;
import java.lang.Object;
import java.lang.RuntimeException;
import java.lang.String;



/**
 * A Scavenger that knows how to get all the Soaplab
 * services from a specified installation
 * @author Tom Oinn
 */
public class SoaplabScavenger extends Scavenger {
    	   
    boolean foundAnInstallation = false;

    /**
     * Create a new Soaplab scavenger, the base parameter should
     * be the base URL of the Soaplab service, i.e. if your
     * AnalysisFactory is at http://foo.bar/soap/AnalysisFactory
     * the parameter shuld be http://foo.bar/soap/
     */
    public SoaplabScavenger(String base) 
	throws ScavengerCreationException {
	super("Soaplab @ "+(base.endsWith("/")?base:base+"/"));
	// Get the categories for this installation
	try {
	    if (!base.endsWith("/")) {
		base = base + "/";
		// throw new RuntimeException("The url for soaplab should look like http://foo/bar/, see the faq for more information");
	    }
	    String[] categories = null;
	    Call call = (Call) new Service().createCall();
	    try {
		call.setTargetEndpointAddress(base+"AnalysisFactory");
		call.setOperationName(new QName("getAvailableCategories"));
		categories = (String[])(call.invoke(new Object[0]));
		// Iterate over all the categories, creating new child nodes
		for (int i = 0; i < categories.length; i++) {
		    DefaultMutableTreeNode category = new DefaultMutableTreeNode(categories[i]);
		    add(category);
		    call = (Call) new Service().createCall();
		    call.setTargetEndpointAddress(base+"AnalysisFactory");
		    call.setOperationName(new QName("getAvailableAnalysesInCategory"));
		    String[] services = (String[])(call.invoke(new String[]{categories[i]}));
		    // Iterate over the services
		    for (int j = 0; j < services.length; j++) {
			SoaplabProcessorFactory f = new SoaplabProcessorFactory(base, services[j]);
			category.add(new DefaultMutableTreeNode(f));
		    }
		}
		foundAnInstallation = true;
	    } catch (Exception e) {
		// Ignore
	    }
	    try {
		call.setTargetEndpointAddress(base+"GowlabFactory");
		call.setOperationName(new QName("getAvailableCategories"));
		categories = (String[])(call.invoke(new Object[0]));
		// Iterate over all the categories, creating new child nodes
		for (int i = 0; i < categories.length; i++) {
		    DefaultMutableTreeNode category = new DefaultMutableTreeNode(categories[i]);
		    add(category);
		    call = (Call) new Service().createCall();
		    call.setTargetEndpointAddress(base+"GowlabFactory");
		    call.setOperationName(new QName("getAvailableAnalysesInCategory"));
		    String[] services = (String[])(call.invoke(new String[]{categories[i]}));
		    // Iterate over the services
		    for (int j = 0; j < services.length; j++) {
			SoaplabProcessorFactory f = new SoaplabProcessorFactory(base, services[j]);
			category.add(new DefaultMutableTreeNode(f));
		    }
		}
		foundAnInstallation = true;
	    } catch (Exception e) {
		// Do nothing
	    }
	}
	catch (Exception e) {
	    ScavengerCreationException sce = new ScavengerCreationException(e.getMessage());
	    sce.initCause(e);
	    throw sce;
	}
	if (foundAnInstallation == false) {
	    // Neither Soaplab nor Gowlab were found, probably a fault
	    ScavengerCreationException sce = new ScavengerCreationException("Unable to locate a soaplab installation at \n"+base);
	    throw sce;
	}
    }

}
