/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import javax.swing.*;
import java.awt.event.*;
import javax.swing.tree.*;

import javax.xml.namespace.QName;
import org.apache.axis.client.Call;
import org.apache.axis.client.Service;

/**
 * A Scavenger that knows how to get all the Soaplab
 * services from a specified installation
 * @author Tom Oinn
 */
public class SoaplabScavenger extends Scavenger {
    
    /**
     * Create a new Soaplab scavenger, the base parameter should
     * be the base URL of the Soaplab service, i.e. if your
     * AnalysisFactory is at http://foo.bar/soap/AnalysisFactory
     * the parameter shuld be http://foo.bar/soap/
     */
    public SoaplabScavenger(String base) 
	throws ScavengerCreationException {
	super("Soaplab @ "+base);
	// Get the categories for this installation
	try {
	    Call call = (Call) new Service().createCall();
	    call.setTargetEndpointAddress(base+"AnalysisFactory");
	    call.setOperationName(new QName("getAvailableCategories"));
	    String[] categories = (String[])(call.invoke(new Object[0]));
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
	}
	catch (Exception e) {
	    throw new ScavengerCreationException(e.getMessage());
	}
	
    }

    

}
