/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;




/**
 * Helper for handling Talisman scavengers.
 * @author Tom Oinn
 */
public class TalismanScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Talisman scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    String scriptURL = (String)JOptionPane.showInputDialog(null,
									   "Address of the TScript document?",
									   "TScript location",
									   JOptionPane.QUESTION_MESSAGE,
									   null,
									   null,
									   "http://");
		    if (scriptURL!=null) {
			try {
			    s.addScavenger(new TalismanScavenger(scriptURL));					
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
