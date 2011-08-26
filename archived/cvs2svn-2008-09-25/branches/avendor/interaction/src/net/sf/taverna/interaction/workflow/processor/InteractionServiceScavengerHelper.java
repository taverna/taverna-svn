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

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper to specify the baseURL for an Interaction Service scavenger
 * @author Tom Oinn
 */
public class InteractionServiceScavengerHelper implements ScavengerHelper {
    
    public String getScavengerDescription() {
	return "Add new Interaction Service...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    String location = (String)JOptionPane.showInputDialog
			(null,
			 "Base URL for the Interaction Service",
			 "Interaction Service Location",
			 JOptionPane.QUESTION_MESSAGE,
			 null,
			 null,
			 "http://localhost:8080/"); 
		    if (location != null) {
			try {
			    s.addScavenger(new InteractionServiceScavenger(location));
			}
			catch (ScavengerCreationException sce) {
			    JOptionPane.showMessageDialog
				(null,
				 "Unable to create scavenger!\n"+sce.getMessage(),
				 "Exception!",
				 JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    };
    }

}
