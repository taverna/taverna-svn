/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

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
import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.workbench.Scavenger;
import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;


// import java.lang.String;

/**
 * Helper for handling Biomoby scavengers.
 * <p>
 * 
 * @version $Id: BiomobyScavengerHelper.java,v 1.2 2004/10/01 13:38:21 mereden
 *          Exp $
 * @author Martin Senger
 */
public class BiomobyScavengerHelper implements ScavengerHelper {
	
	private static Logger logger = Logger.getLogger(BiomobyScavengerHelper.class);

    @SuppressWarnings("unused")
	private String defaultResourceURL = "http://biomoby.org/RESOURCES/MOBY-S/Objects";

    private String endpoint = "http://mobycentral.icapture.ubc.ca/cgi-bin/MOBY05/mobycentral.pl";

    public String getScavengerDescription() {
        return "Add new Biomoby scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
        final ScavengerTree s = theScavenger;
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {

                final JDialog dialog = new JDialog(s.getContainingFrame(),
                        "Add Your Custom BioMoby Registry & Object RDF", true);
                final BiomobyScavengerDialog msp = new BiomobyScavengerDialog();
                dialog.getContentPane().add(msp);
                JButton accept = new JButton("Okay");
                JButton cancel = new JButton("Cancel");
                msp.add(accept);
                msp.add(cancel);
                accept.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae2) {
                        if (dialog.isVisible()) {
                            String registryEndpoint = "";
                            if (msp.getRegistryEndpoint().equals(""))
                                registryEndpoint = endpoint;
                            else
                                registryEndpoint = msp.getRegistryEndpoint();
                            try {
                                Central central = new CentralImpl(
                                        registryEndpoint);
                                MobyResourceRef mrr[] = central
                                        .getResourceRefs();
                                String resourceURL = null;
                                for (int x = 0; x < mrr.length; x++) {
                                    MobyResourceRef ref = mrr[x];
                                    if (!ref.getResourceName().equals("Object"))
                                        continue;
                                    resourceURL = ref.getResourceLocation()
                                            .toExternalForm();
                                    break;
                                }

                                if (resourceURL == null)
                                    throw new MobyException(
                                            "Could not retrieve the location of the Moby Datatype RDF Document from the given endpoint "
                                                    + registryEndpoint);
                                BiomobyScavenger bs = new BiomobyScavenger(
                                        registryEndpoint, resourceURL);
                                s.addScavenger(bs);
                            } catch (ScavengerCreationException sce) {
                                JOptionPane
                                        .showMessageDialog(s.getContainingFrame(),
                                                "Unable to create scavenger!\n"
                                                        + sce.getMessage(),
                                                "Exception!",
                                                JOptionPane.ERROR_MESSAGE);
                                sce.printStackTrace();
                            } catch (MobyException e) {
                                JOptionPane.showMessageDialog(s.getContainingFrame(),
                                        "Unable to create scavenger!\n"
                                                + e.getMessage(), "Exception!",
                                        JOptionPane.ERROR_MESSAGE);
                                e.printStackTrace();
                                e.printStackTrace();
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
                dialog.getContentPane().add(msp);
                dialog.pack();
                dialog.setVisible(true);

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
		return new BiomobyProcessorInfoBean().icon();
	}
}