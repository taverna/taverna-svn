/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rdfgenerator;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashSet;
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
 * Helper for handling RDFGenerator scavengers.
 * @author Tom Oinn
 */
public class RDFGeneratorScavengerHelper implements ScavengerHelper {
	
	private static Logger logger = Logger.getLogger(RDFGeneratorScavengerHelper.class);

    public String getScavengerDescription() {
	return "Add new RDF Generator scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
	final ScavengerTree s = theScavenger;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			s.addScavenger(new RDFGeneratorScavenger());
		    }
		    catch (ScavengerCreationException sce) {
			JOptionPane.showMessageDialog(s.getContainingFrame(),
						      "Unable to create scavenger!\n"+sce.getMessage(),
						      "Exception!",
						      JOptionPane.ERROR_MESSAGE);
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

		Processor[] processors = model.getProcessorsOfType(RDFGeneratorProcessor.class);
		if (processors.length>0) {
			try {
				result.add(new RDFGeneratorScavenger());
			} catch (ScavengerCreationException e) {
				logger.warn("Error creating RDFGeneratorScavenger", e);
			}
		}
		return result;
	}
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new RDFGeneratorProcessorInfoBean().icon();
	}
    
}
