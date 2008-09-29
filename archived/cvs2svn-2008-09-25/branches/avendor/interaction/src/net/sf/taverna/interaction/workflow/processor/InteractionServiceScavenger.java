/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow.processor;

import net.sf.taverna.interaction.workflow.*;
import net.sf.taverna.interaction.workflow.impl.*;

import org.embl.ebi.escience.scuflworkers.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;

import java.net.URL;
import java.net.MalformedURLException;

import javax.swing.tree.*;

/**
 * Collects the available interaction patterns within an interaction
 * service
 * @author Tom Oinn
 */
public class InteractionServiceScavenger extends Scavenger {
    
    /**
     * Create a new InteractionServiceScavenger rooted at the
     * specified baseURL
     */
    public InteractionServiceScavenger(String baseURL) 
	throws ScavengerCreationException {
	super("Interaction @ "+baseURL);
	try {
	    URL urlObject = new URL(baseURL);
	    InteractionService is =
		HTTPInteractionServiceProxy.connectTo(urlObject);
	    InteractionPattern[] patterns = is.getInteractionPatterns();
	    for (int i = 0; i < patterns.length; i++) {
		String patternName = patterns[i].getName();
		add(patternName.split("\\.")).
		    setUserObject(new InteractionServiceProcessorFactory(baseURL, patternName));
	    }
	}
	catch (Exception ex) {
	    if (ex instanceof ScavengerCreationException) {
		throw (ScavengerCreationException)ex;
	    }
	    ScavengerCreationException sce = 
		new ScavengerCreationException("Unable to create interaction scavenger : "+
					       ex.getMessage());
	    sce.initCause(ex);
	    throw sce;
	}
    }
    
    private DefaultMutableTreeNode add(String[] name) {
	DefaultMutableTreeNode currentNode = this;
	for (int i = 0; i < name.length; i++) {
	    String currentName = name[i];
	    DefaultMutableTreeNode foundNode = null;
	    for (int j = 0; 
		 j < currentNode.getChildCount() &&
		     foundNode == null; 
		 j++) {
		DefaultMutableTreeNode n = 
		    (DefaultMutableTreeNode)currentNode.getChildAt(j);
		if (n.getUserObject() instanceof String &&
		    ((String)n.getUserObject()).equals(currentName)) {
		    foundNode = n;
		}
	    }
	    if (foundNode == null) {
		DefaultMutableTreeNode newNode = 
		    new DefaultMutableTreeNode(currentName);
		currentNode.add(newNode);
		currentNode = newNode;
	    }
	    else {
		currentNode = foundNode;
	    }
	}
	return currentNode;
    }

}
