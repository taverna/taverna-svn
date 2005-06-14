/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyScavenger;
// import java.lang.String;



/**
 * Helper for handling Biomoby scavengers. <p>
 *
 * @version $Id: BiomobyScavengerHelper.java,v 1.3 2005-06-14 10:14:22 marsenger Exp $
 * @author Martin Senger
 */
public class BiomobyScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Biomoby scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    String baseURL = (String)JOptionPane.showInputDialog
			(null,
			 "Location (URL) of your Biomoby central registry?",
			 "Biomoby location",
			 JOptionPane.QUESTION_MESSAGE,
			 null,
			 null,
			 "http://mobycentral.cbr.nrc.ca/cgi-bin/MOBY05/mobycentral.pl");
		    if (baseURL!=null) {
			try {
			    s.addScavenger(new BiomobyScavenger(baseURL));					
			}
			catch (ScavengerCreationException sce) {
			    JOptionPane.showMessageDialog(null,
							  "Unable to create scavenger!\n"+sce.getMessage(),
							  "Exception!",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    };
    }
    
}
