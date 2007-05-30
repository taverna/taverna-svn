/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

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

// import java.lang.String;

/**
 * Helper for handling Biomoby scavengers. <p>
 * @deprecated
 */
public class BiomobyObjectScavengerHelper implements ScavengerHelper {
	
	private static Logger logger = Logger.getLogger(BiomobyObjectScavengerHelper.class);

    public String getScavengerDescription() {
        return "Add new Biomoby Object scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
        final ScavengerTree s = theScavenger;
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String baseURL = (String) JOptionPane
                        .showInputDialog(
                                s.getContainingFrame(),
                                "Location (URL) of your BioMoby Object RDF Document?",
                                "Biomoby object location",
                                JOptionPane.QUESTION_MESSAGE, null, null,
                                "http://biomoby.org/RESOURCES/MOBY-S/Objects");
                if (baseURL != null) {
                    try {
                        s.addScavenger(new BiomobyObjectScavenger(baseURL));
                    } catch (ScavengerCreationException sce) {
                        JOptionPane.showMessageDialog(s.getContainingFrame(),
                                "Unable to create scavenger!\n"
                                        + sce.getMessage(), "Exception!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
    }

    /**
     * This ScavengerHelper has no defaults
     */
	public Set<Scavenger> getDefaults() {
		return new HashSet<Scavenger>();
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();

		Processor[] processors = model.getProcessorsOfType(BiomobyObjectProcessor.class);
		for (Processor processor : processors) {
			String loc = ((BiomobyObjectProcessor) processor).getMobyEndpoint();
			if (!existingLocations.contains(loc)) {
				existingLocations.add(loc);
				try {
					result.add(new BiomobyObjectScavenger(loc));
				} catch (ScavengerCreationException e) {
					logger.warn("Error creating BiomobyObjectScavenger", e);
				}
			}
		}
		return result;
	}

	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new BiomobyObjectProcessorInfoBean().icon();
	}
    
	
    

}
