/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;

import javax.swing.tree.DefaultMutableTreeNode;
// import javax.xml.namespace.QName;
// import org.apache.axis.client.Call;
// import org.apache.axis.client.Service;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessorFactory;
// import java.lang.Exception;
// import java.lang.Object;
// import java.lang.RuntimeException;
// import java.lang.String;

import org.biomoby.client.*;
import org.biomoby.shared.*;

import java.util.*;

/**
 * A Scavenger that knows how to get all the Biomoby services from a
 * specified Biomoby Central Registry. <p>
 *
 * @version $Id: BiomobyScavenger.java,v 1.1 2004-04-01 14:31:34 mereden Exp $
 * @author Martin Senger
 */
public class BiomobyScavenger extends Scavenger {
    
    /**
     * Create a new Biomoby scavenger, the base parameter should
     * be the base URL of the Biomoby Central Registry.
     */
    public BiomobyScavenger(String base) 
	throws ScavengerCreationException {
	super("Biomoby @ "+base);

	// get list of services and their authorities
	try {
	    Central worker = new CentralImpl (base);
	    Map names = worker.getServiceNames();

	    Hashtable byAuthority = new Hashtable();
	    for (Iterator it = names.entrySet().iterator(); it.hasNext(); ) {
		Map.Entry entry = (Map.Entry)it.next();
		String serviceName = (String)entry.getKey();
		String authorityName = (String)entry.getValue();
		Vector services;
		if (byAuthority.containsKey (authorityName))
		    services = (Vector)byAuthority.get (authorityName);
		else
		    services = new Vector();
		services.addElement (serviceName);
		byAuthority.put (authorityName, services);
	    }
	    
	    for (Enumeration en = byAuthority.keys(); en.hasMoreElements(); ) {
		String authorityName = (String)en.nextElement();
		Vector v = (Vector)byAuthority.get (authorityName);
		DefaultMutableTreeNode authority = new DefaultMutableTreeNode (authorityName);
		add (authority);
		for (Enumeration en2 = v.elements(); en2.hasMoreElements(); ) {
		    String serviceName = (String)en2.nextElement();
		    BiomobyProcessorFactory f = new BiomobyProcessorFactory (base, authorityName, serviceName);
		    authority.add (new DefaultMutableTreeNode (f));
		}
	    }

	} catch (Exception e) {
	    ScavengerCreationException sce = new ScavengerCreationException (e.getMessage());
	    sce.initCause (e);
	    throw sce;
	}
    }
}
