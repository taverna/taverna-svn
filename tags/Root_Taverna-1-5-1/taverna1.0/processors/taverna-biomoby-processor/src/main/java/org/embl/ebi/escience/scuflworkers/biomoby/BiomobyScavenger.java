/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;

import java.util.Enumeration;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Map;
import java.util.Vector;

import javax.swing.tree.DefaultMutableTreeNode;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

/**
 * A Scavenger that knows how to get all the Biomoby services from a
 * specified Biomoby Central Registry. <p>
 *
 * @version $Id: BiomobyScavenger.java,v 1.2 2006-07-10 14:08:14 sowen70 Exp $
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
