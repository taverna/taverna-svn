/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for handling Soaplab scavengers.
 * 
 * @author Tom Oinn
 */
public class SoaplabScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(SoaplabScavengerHelper.class);

	public String getScavengerDescription() {
		return "Add new Soaplab scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String baseURL = (String) JOptionPane.showInputDialog(s.getContainingFrame(),
						"Base location for your soaplab installation?", "Soaplab location",
						JOptionPane.QUESTION_MESSAGE, null, null, "http://www.ebi.ac.uk/soaplab/services/");
				if (baseURL != null) {
					new Thread("Adding SOAPlab Scavenger") {
						public void run() {
							s.scavengingStarting("Adding SOAPLab scavenger");
							try {
								s.addScavenger(new SoaplabScavenger(baseURL));
							} catch (ScavengerCreationException sce) {
								JOptionPane.showMessageDialog(s.getContainingFrame(), "Unable to create scavenger!\n" + sce.getMessage(),
										"Exception!", JOptionPane.ERROR_MESSAGE);
							}
							s.scavengingDone();
						}
					}.start();
				}
			}
		};
	}

	public Set<Scavenger> getDefaults() {
		Set<Scavenger> result = new HashSet<Scavenger>();
		String urlList = System.getProperty("taverna.defaultsoaplab");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (String url : urls) {
				try {
					result.add(new SoaplabScavenger(url));
				} catch (ScavengerCreationException e) {
					logger.error("Error creating SoaplabScavenger for " + url, e);
				}
			}
		}
		return result;
	}
	
	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();

		Processor[] processors = model.getProcessorsOfType(SoaplabProcessor.class);
		for (Processor processor : processors) {
			String endpoint = ((SoaplabProcessor) processor).getEndpoint().toString();
			if (!existingLocations.contains(endpoint)) {
				existingLocations.add(endpoint);
				String[] parts = endpoint.split("/");
				String base = "";
				for (int j = 0; j < parts.length - 1; j++) {
					base = base + parts[j] + "/";
				}
				try {
					result.add(new SoaplabScavenger(base));
				} catch (ScavengerCreationException e) {
					logger.warn("Error creating SoaplabScavenger", e);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new SoaplabProcessorInfoBean().icon();
	}

}
