/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabScavenger;
import java.lang.String;

/**
 * Helper for handling Soaplab scavengers.
 * 
 * @author Tom Oinn
 */
public class SoaplabScavengerHelper implements ScavengerHelper {

	public String getScavengerDescription() {
		return "Add new Soaplab scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String baseURL = (String) JOptionPane.showInputDialog(
						null, "Base location for your soaplab installation?",
						"Soaplab location", JOptionPane.QUESTION_MESSAGE, null,
						null, "http://www.ebi.ac.uk/soaplab/services/");
				if (baseURL != null) {
					new Thread() {
						public void run() {
							if (s.getParentPanel()!=null) s.getParentPanel().startProgressBar("Adding SOAPLab scavenger");
							try {
								s.addScavenger(new SoaplabScavenger(baseURL));
							} catch (ScavengerCreationException sce) {
								JOptionPane
										.showMessageDialog(null,
												"Unable to create scavenger!\n"
														+ sce.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
							s.getParentPanel().stopProgressBar();
						}
					}.start();
				}
			}
		};
	}

}
