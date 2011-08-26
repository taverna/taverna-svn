/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.inferno;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper to specify an Inferno SGS location
 * 
 * @author Tom Oinn
 */
public class InfernoScavengerHelper implements ScavengerHelper {

	public String getScavengerDescription() {
		return "Add new Styx Grid Service...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String location = (String) JOptionPane.showInputDialog(null,
						"Location of SGS in the form <host>:<port>",
						"SGS Location", JOptionPane.QUESTION_MESSAGE, null,
						null, "localhost:8080");
				if (location == null) {
					return;
				}
				try {
					String[] parts = location.split(":");
					if (parts.length != 2) {
						throw new ScavengerCreationException(
								"Location must be in the form <host>:<port>");
					}
					int port;
					String host;
					try {
						port = Integer.parseInt(parts[1]);
						host = parts[0];
					} catch (NumberFormatException nfe) {
						throw new ScavengerCreationException(
								"Port must be a valid integer i.e. 8080");
					}
					s.addScavenger(new InfernoScavenger(host, port));
				} catch (ScavengerCreationException sce) {
					JOptionPane.showMessageDialog(null,
							"Unable to create scavenger!\n" + sce.getMessage(),
							"Exception!", JOptionPane.ERROR_MESSAGE);
				}
			}

		};
	}
}
