/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellScavenger;
import java.lang.String;



/**
 * Helper for handling Beanshell scavengers.
 * @author Tom Oinn
 */
public class BeanshellScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Beanshell scavenger...";
    }
    
    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    String value = "edit me!";
		    try {
			s.addScavenger(new BeanshellScavenger());
		    }
		    catch (ScavengerCreationException sce) {
			JOptionPane.showMessageDialog(null,
						      "Unable to create scavenger!\n"+sce.getMessage(),
						      "Exception!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    };
    }
    
}
