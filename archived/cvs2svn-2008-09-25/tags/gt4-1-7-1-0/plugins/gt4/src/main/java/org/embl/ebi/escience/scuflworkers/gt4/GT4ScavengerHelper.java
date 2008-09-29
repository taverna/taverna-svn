/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Wei Tan, the University of Chicago
 */
package org.embl.ebi.escience.scuflworkers.gt4;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.swing.ImageIcon;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for handling GT4 scavengers.
 * 
 * @author Wei Tan
 * @author Ravi Madduri
 */
public class GT4ScavengerHelper implements ScavengerHelper {

	private static Logger logger = Logger.getLogger(GT4ScavengerHelper.class);

	public String getScavengerDescription() {
		return "Add new GT4 scavenger...";
	}

	public ActionListener getListener(ScavengerTree theScavenger) {
		final ScavengerTree s = theScavenger;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				final String indexLocation = (String) JOptionPane
						.showInputDialog(s.getContainingFrame(),
								"Address of the GT4 Service Index?",
								"Discovery location", JOptionPane.QUESTION_MESSAGE,
								null, null, "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService");
				if (indexLocation != null) {
					Runnable r = new Runnable() {
						public void run() {
							s.scavengingStarting("Processing Index");
							try {
								s.addScavenger(new GT4Scavenger(
										indexLocation));
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
					new Thread(r, "GT4 Scavenger processing").start();
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
		List<GT4ScavengerThread> threads = new ArrayList<GT4ScavengerThread>();
		String urlList = System.getProperty("taverna.defaultgt4");
		if (urlList != null) {
			String[] urls = urlList.split("\\s*,\\s*");
			for (String url : urls) {
				try {
					if (threads.size()<MAX_THREADS) { //limit the number of concurrent threads, incase there are many defined in mygrid.properties
						GT4ScavengerThread thread = new GT4ScavengerThread(url);
						thread.start();
						threads.add(thread);
					}
					else {
						result.add(new GT4Scavenger(url));
					}
				} catch (ScavengerCreationException e) {
					logger.error(
							"Error creating default WSDLBasedScavenger for " + url + ": "+e.getMessage());
				}
			}
		}
		for (GT4ScavengerThread thread : threads) {
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
		
		
		return result;
	}

	/**
	 * Returns the icon for this scavenger
	 */
	public ImageIcon getIcon() {
		return new GT4ProcessorInfoBean().icon();
	}
	
	class GT4ScavengerThread extends Thread
	{

		GT4Scavenger scavenger;
		String location;
		
		GT4ScavengerThread(String location) {
			super("GT4Scavenger thread");
			this.location=location;
		}
		
		@Override
		public void run() {
			try {
				scavenger=new GT4Scavenger(location);
			} catch (ScavengerCreationException e) {
				logger.error("Error creating WSDLBasedScavenger",e);
			}
		}
		
		public GT4Scavenger getScavenger() {
			return scavenger;
		}
		
	}

}
