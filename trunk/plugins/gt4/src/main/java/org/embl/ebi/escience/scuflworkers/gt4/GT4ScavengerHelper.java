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
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
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

	                final JDialog dialog = new JDialog(s.getContainingFrame(),
	                        "Add Your Custom Service Query", true);
	                final GT4ScavengerDialog gtd = new GT4ScavengerDialog();
	                dialog.getContentPane().add(gtd);
	                JButton accept = new JButton("Okay");
	                JButton cancel = new JButton("Cancel");
	                gtd.add(accept);
	                gtd.add(cancel);
	                accept.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent ae2) {
	                        if (dialog.isVisible()) {
	                        	String indexURL = "";
	                            String queryCriteria = "";
	                            String queryValue = "";
	                            ServiceQuery squery = null;
	                            
	                            if (gtd.getIndexServiceURL().equals(""))
	                            	//default index URL
	                                indexURL = "http://cagrid-index.nci.nih.gov:8080/wsrf/services/DefaultIndexService";
	                            else
	                                indexURL = gtd.getIndexServiceURL();
	                            
	                            if (!gtd.getQueryCriteria().equals("None"))
	                                squery = new ServiceQuery(gtd.getQueryCriteria(),gtd.getQueryValue());
	                            
	                            try {
	                            	final String url = indexURL;
	                            	final ServiceQuery sq = squery;
	                            	Thread t = new Thread("Adding GT4 scavenger") {
	            						public void run() {
	            							s.scavengingStarting("Adding GT4 scavenger");
	            							try {
	            								GT4Scavenger gs = new GT4Scavenger(url, sq);
	                                            s.addScavenger(gs);
	            							} catch (ScavengerCreationException sce) {
	            								JOptionPane.showMessageDialog(s.getContainingFrame(), "Unable to create scavenger!\n" + sce.getMessage(),
	            										"Exception!", JOptionPane.ERROR_MESSAGE);
	            							}
	            							s.scavengingDone();
	            						}
	                            	};
	                            	t.start();
	                            } catch (Exception e) {
	                                JOptionPane
	                                        .showMessageDialog(s.getContainingFrame(),
	                                                "Unable to create scavenger!\n"
	                                                        + e.getMessage(),
	                                                "Exception!",
	                                                JOptionPane.ERROR_MESSAGE);
	                                logger.error("Exception thrown:", e);
	                            } finally {
	                                dialog.setVisible(false);
	                                dialog.dispose();
	                            }
	                        }
	                    }
	                });
	                cancel.addActionListener(new ActionListener() {
	                    public void actionPerformed(ActionEvent ae2) {
	                        if (dialog.isVisible()) {
	                            dialog.setVisible(false);
	                            dialog.dispose();
	                        }
	                    }
	                });
	                dialog.setResizable(false);
	                dialog.getContentPane().add(gtd);
	                dialog.setLocationRelativeTo(null);
	                dialog.pack();
	                dialog.setVisible(true);

	            }
	        };
	    }

	
	/* The old, simple GUI 
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
	*/

	/**
	 * returns the default Scavenger set
	 */
	   public synchronized Set<Scavenger> getDefaults() {
			Set<Scavenger> result = new HashSet<Scavenger>();
			String urlList = System.getProperty("taverna.defaultgt4");
			if (urlList != null) {
				String[] urls = urlList.split("\\s*,\\s*");
				for (String url : urls) {
					try {
						result.add(new GT4Scavenger(url,null));
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

			Processor[] processors = model.getProcessorsOfType(GT4Processor.class);
			for (Processor processor : processors) {
				String loc = ((GT4Processor) processor).getWSDLLocation();
				if (!existingLocations.contains(loc)) {
					existingLocations.add(loc);
					try {
						result.add(new GT4Scavenger(loc,null));
					} catch (ScavengerCreationException e) {
						logger.warn("Error creating Biomoby Scavenger", e);
					}
				}
			}
			return result;
		}
		
		/**
		 * Returns the icon for this scavenger
		 */
		public ImageIcon getIcon() {
			return new GT4ProcessorInfoBean().icon();
		}
}

