/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JOptionPane;

import org.biomoby.client.CentralImpl;
import org.biomoby.shared.Central;
import org.biomoby.shared.MobyException;
import org.biomoby.shared.MobyResourceRef;
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
                                        .showMessageDialog(null,
                                                "Unable to create scavenger!\n"
                                                        + sce.getMessage(),
                                                "Exception!",
                                                JOptionPane.ERROR_MESSAGE);
                                sce.printStackTrace();
                            } catch (MobyException e) {
                                JOptionPane.showMessageDialog(null,
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
}