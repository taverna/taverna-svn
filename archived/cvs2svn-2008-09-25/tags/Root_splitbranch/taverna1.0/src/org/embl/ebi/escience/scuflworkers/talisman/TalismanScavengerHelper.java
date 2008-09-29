/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.talisman;

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
 * Helper for handling Talisman scavengers.
 * 
 * @author Tom Oinn
 */
public class TalismanScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(TalismanScavengerHelper.class);

	public String getScavengerDescription() {
		return "Add new Talisman scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				String scriptURL = (String) JOptionPane.showInputDialog(null, "Address of the TScript document?",
						"TScript location", JOptionPane.QUESTION_MESSAGE, null, null, "http://");
				if (scriptURL != null) {
					try {
						s.addScavenger(new TalismanScavenger(scriptURL));
					} catch (ScavengerCreationException sce) {
						JOptionPane.showMessageDialog(null, "Unable to create scavenger!\n" + sce.getMessage(),
								"Exception!", JOptionPane.ERROR_MESSAGE);
					}
				}
			}
		};
	}

	public Set<Scavenger> getDefaults() {
		return new HashSet<Scavenger>();
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();

		Processor[] processors = model.getProcessorsOfType(TalismanProcessor.class);
		for (Processor processor : processors) {
			String loc = ((TalismanProcessor) processor).getTScriptURL();
			if (!existingLocations.contains(loc)) {
				existingLocations.add(loc);
				try {
					result.add(new TalismanScavenger(loc));
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
		return new TalismanProcessorInfoBean().icon();
	}
}
