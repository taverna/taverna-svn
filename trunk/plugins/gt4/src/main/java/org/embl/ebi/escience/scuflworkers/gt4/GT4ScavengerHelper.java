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

import javax.swing.JLabel;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

/**
 * Helper for handling GT4 scavengers.
 * 
 * @author Wei Tan
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
	                JButton accept = new JButton("Send Service Query");
	                JButton cancel = new JButton("Cancel");
	            
	                gtd.add(accept);
	                //gtd.add(new JLabel("Send Service Query to Index Service"));
	                gtd.add(cancel);
	                gtd.addQuery.addActionListener(new ActionListener(){
	                	 public void actionPerformed(ActionEvent ae3) {
	                		 if (dialog.isVisible()) {
	                			 if(gtd.q_count<gtd.q_size){
	                				 gtd.queryList[gtd.q_count].setVisible(true);
	                				 gtd.queryValue[gtd.q_count].setVisible(true);
	                				 gtd.validate();
	                				 gtd.q_count++;
	                				 System.out.println("Add a New Query-- now q_count == " + gtd.q_count);
	                			 }
	                		 }
	                	 }
	                	
	                });
	                gtd.removeQuery.addActionListener(new ActionListener(){
	                	 public void actionPerformed(ActionEvent ae4) {
	                		 if (dialog.isVisible()) {
	                			 if(gtd.q_count>1){
	                				 gtd.queryList[gtd.q_count-1].setVisible(false);
	                				 gtd.queryValue[gtd.q_count-1].setVisible(false);
	                				 gtd.validate();
	                				 gtd.q_count--;
	                				 System.out.println("Remove a New Query-- now q_count == " + gtd.q_count);
	                			 }
	                		 }
	                	 }
	                	
	                });
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
	                            
	                            //gather service queries
	                            int [] flag = new int[gtd.q_count];
	                            int count = 0;
	                            for (int i=0;i<gtd.q_count;i++){
	                            	if(!gtd.getQueryCriteria(i).equals("None")&&!gtd.getQueryValue(i).equals("")){
	                            		count ++ ;
	                            		flag[i]=1;
	                            	}
	        
	                            }
	                            ServiceQuery [] sq= null;
	                            if(count>0){
	                            	sq = new ServiceQuery[count];
	 	                            int j = 0;
	 	                            for (int i=0;i<gtd.q_count;i++){
	 	                            	if(flag[i]==1){
	 	                            		sq[j++] = new ServiceQuery(gtd.getQueryCriteria(i),gtd.getQueryValue(i));
	 	                            		System.out.println("Adding Query: "+ sq[j-1].queryCriteria + "  = " + sq[j-1].queryValue);
	 	                          		
	 	                            	}	
	                            }
	                           
	                            }
	                            
	                            
	                            try {
	                            	final String url = indexURL;
	                            	final ServiceQuery[] f_sq = sq;
	                            	Thread t = new Thread("Adding GT4 scavenger") {
	            						public void run() {
	            							s.scavengingStarting("Adding GT4 scavenger");
	            							try {
	            								GT4Scavenger gs = new GT4Scavenger(url, f_sq);
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

