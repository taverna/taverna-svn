/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.wsdl;

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
 * Helper for handling WSDL scavengers.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class WSDLScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(WSDLScavengerHelper.class);

	public String getScavengerDescription() {
		return "Add new WSDL scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String wsdlLocation = (String) JOptionPane.showInputDialog(null, "Address of the WSDL document?",
						"WSDL location", JOptionPane.QUESTION_MESSAGE, null, null, "http://");
				if (wsdlLocation != null) {
					Runnable r = new Runnable() {
						public void run() {
							if (s.getParent() != null)
								s.getParentPanel().startProgressBar("Processing WSDL");
							try {
								s.addScavenger(new WSDLBasedScavenger(wsdlLocation));
							} catch (ScavengerCreationException sce) {
								JOptionPane.showMessageDialog(null, "Unable to create scavenger!\n" + sce.getMessage(),
										"Exception!", JOptionPane.ERROR_MESSAGE);
							}
							if (s.getParent() != null)
								s.getParentPanel().stopProgressBar();
						}
					};
					new Thread(r).start();
				}
			}
		};
	}

	/**
	 * returns the default Scavenger set
	 */
	public Set<Scavenger> getDefaults() {
		Set<Scavenger> result = new HashSet<Scavenger>();
		String urlList = System.getProperty("taverna.defaultwsdl");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (String url : urls) {
				try {
					result.add(new WSDLBasedScavenger(url));
				} catch (ScavengerCreationException e) {
					logger.error("Error creating WSDLBasedScavenger for " + url, e);
				}
			}
		}
		return result;
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();

		Processor[] processors = model.getProcessorsOfType(WSDLBasedProcessor.class);
		for (Processor processor : processors) {
			String loc = ((WSDLBasedProcessor) processor).getWSDLLocation();
			if (!existingLocations.contains(loc)) {
				existingLocations.add(loc);
				try {
					result.add(new WSDLBasedScavenger(loc));
				} catch (ScavengerCreationException e) {
					logger.warn("Error creating WSDLBasedScavenger", e);
				}
			}
		}
		return result;
	}
	
	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new WSDLProcessorInfoBean().icon();
	}

}
