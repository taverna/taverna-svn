/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.biomoby.client.taverna.plugin.BiomobyProcessorInfoBean;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for handling Biomoby scavengers.
 * <p>
 * 
 * @version $Id: BiomobyScavengerHelper.java,v 1.3 2005/06/14 10:14:22 marsenger
 *          Exp $
 * @author Martin Senger
 */
public class BiomobyScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(BiomobyScavengerHelper.class);

	public String getScavengerDescription() {
		return "Add new Biomoby scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String baseURL = (String) JOptionPane.showInputDialog(s.getContainingFrame(),
						"Location (URL) of your Biomoby central registry?", "Biomoby location",
						JOptionPane.QUESTION_MESSAGE, null, null,
						"http://moby.ucalgary.ca/moby/MOBY-Central.pl");
				if (baseURL != null) {
					s.scavengingStarting("Adding BioMoby scavenger");
					try {
						s.addScavenger(new BiomobyScavenger(baseURL));
					} catch (ScavengerCreationException sce) {
						JOptionPane.showMessageDialog(s.getContainingFrame(), "Unable to create scavenger!\n" + sce.getMessage(),
								"Exception!", JOptionPane.ERROR_MESSAGE);
					}
					s.scavengingDone();
				}
			}
		};
	}

	public Set<Scavenger> getDefaults() {
		Set<Scavenger> result = new HashSet<Scavenger>();
		String urlList = System.getProperty("taverna.defaultbiomoby");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (String url : urls) {
				try {
					result.add(new BiomobyScavenger(url));
				} catch (ScavengerCreationException e) {
					logger.error("Error creating BiomobyScavenger for " + url, e);
				}
			}
		}
		return result;
	}
	
	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();

		Processor[] processors = model.getProcessorsOfType(BiomobyProcessor.class);
		for (Processor processor : processors) {
			String loc = ((BiomobyProcessor) processor).getMobyEndpoint();
			if (!existingLocations.contains(loc)) {
				existingLocations.add(loc);
				try {
					result.add(new BiomobyScavenger(loc));
				} catch (ScavengerCreationException e) {
					logger.warn("Error creating TalismanScavenger", e);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		//uses the icon for the new processor, this scavenger is now deprecated
		//and is here just for backward compatibility
		return new BiomobyProcessorInfoBean().icon();
	}

}
