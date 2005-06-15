/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomart;

import javax.swing.*;
import java.awt.*;
import java.awt.event.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflui.workbench.*;
import org.embl.ebi.escience.scuflworkers.*;

/**
 * Helper for creating Biomart scavengers
 * @author Tom Oinn
 */
public class BiomartRegistryScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
	return "Add new Biomart registry...";
    }
    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    final String baseURL = (String)JOptionPane.showInputDialog(null,
									       "Biomart registry location?",
									       "Registry location",
									       JOptionPane.QUESTION_MESSAGE,
									       null,
									       null,
									       "http://www.ebi.ac.uk/~tmo/defaultMartRegistry.xml");
		    if (baseURL!=null) {
			new Thread() {
			    public void run() {
				try {
				    s.addScavenger(new BiomartRegistryScavenger(baseURL));					
				}
				catch (ScavengerCreationException sce) {
				    JOptionPane.showMessageDialog(null,
								  "Unable to create scavenger!\n"+sce.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
			    }
			}.start();
		    }
		}
	    };
    }
    
}
