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
				final String wsdlLocation = (String) JOptionPane
						.showInputDialog(s.getContainingFrame(),
								"Address of the WSDL document?",
								"WSDL location", JOptionPane.QUESTION_MESSAGE,
								null, null, "http://");
				if (wsdlLocation != null) {
					Runnable r = new Runnable() {
						public void run() {
							s.scavengingStarting("Processing WSDL");
							try {
								s.addScavenger(new WSDLBasedScavenger(
										wsdlLocation));
							} catch (ScavengerCreationException sce) {
								JOptionPane
										.showMessageDialog(s
												.getContainingFrame(),
												"Unable to create scavenger!\n"
														+ sce.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
							s.scavengingDone();
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
		int MAX_THREADS=5;
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<WSDLScavengerThread> threads = new ArrayList<WSDLScavengerThread>();
		String urlList = System.getProperty("taverna.defaultwsdl");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (String url : urls) {
				try {
					if (threads.size()<MAX_THREADS) { //limit the number of concurrent threads, incase there are many defined in mygrid.properties
						WSDLScavengerThread thread = new WSDLScavengerThread(url);
						thread.start();
						threads.add(thread);
					}
					else {
						result.add(new WSDLBasedScavenger(url));
					}
				} catch (ScavengerCreationException e) {
					logger.error(
							"Error creating default WSDLBasedScavenger for " + url + ": "+e.getMessage());
				}
			}
		}
		for (WSDLScavengerThread thread : threads) {
			try {
				thread.join();
			} catch (InterruptedException e) {
				logger.error("Interuption error joining scavenger thread:",e);
			}
			if (thread.getScavenger()!=null) result.add(thread.getScavenger());
		}
		return result;
	}

	public Set<Scavenger> getFromModel(ScuflModel model) {
		Set<Scavenger> result = new HashSet<Scavenger>();
		List<String> existingLocations = new ArrayList<String>();
		Processor[] processors = model
				.getProcessorsOfType(WSDLBasedProcessor.class);
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
	
	class WSDLScavengerThread extends Thread
	{

		WSDLBasedScavenger scavenger;
		String location;
		
		WSDLScavengerThread(String location) {
			super("WSDLScavenger thread");
			this.location=location;
		}
		
		@Override
		public void run() {
			try {
				scavenger=new WSDLBasedScavenger(location);
			} catch (ScavengerCreationException e) {
				logger.error("Error creating WSDLBasedScavenger",e);
			}
		}
		
		public WSDLBasedScavenger getScavenger() {
			return scavenger;
		}
		
	}

}
