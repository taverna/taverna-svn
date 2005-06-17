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

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

// import java.lang.String;

/**
 * Helper for handling Biomoby scavengers. <p>
 * 
 * @version $Id: BiomobyScavengerHelper.java,v 1.2 2004/10/01 13:38:21 mereden
 *          Exp $
 * @author Martin Senger
 */
public class BiomobyScavengerHelper implements ScavengerHelper {

    private String defaultResourceURL = "http://mobycentral.cbr.nrc.ca:8090/RESOURCES/MOBY-S/Objects";

    private String endpoint = "http://mobycentral.cbr.nrc.ca/cgi-bin/MOBY05/mobycentral.pl";

    public String getScavengerDescription() {
        return "Add new Biomoby scavenger...";
    }

public ActionListener getListener(ScavengerTree theScavenger) {
        final ScavengerTree s = theScavenger;
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                /*
                 * String resourceURL =
                 * "http://biomoby.org/RESOURCES/MOBY-S/Objects"; String baseURL =
                 * (String) JOptionPane .showInputDialog( null, "Location (URL)
                 * of your BioMoby central registry?", "Biomoby location",
                 * JOptionPane.QUESTION_MESSAGE, null, null,
                 * "http://mobycentral.cbr.nrc.ca/cgi-bin/MOBY05/mobycentral.pl");
                 * if (baseURL != null) { try { s.addScavenger(new
                 * BiomobyScavenger(baseURL,resourceURL)); } catch
                 * (ScavengerCreationException sce) {
                 * JOptionPane.showMessageDialog(null, "Unable to create
                 * scavenger!\n" + sce.getMessage(), "Exception!",
                 * JOptionPane.ERROR_MESSAGE); } }
                 */
                final JDialog dialog = new JDialog(Workbench.workbench,
                        "Add Your Custom BioMoby Registry & Object RDF", true);
                final BiomobyScavengerDialog msp = new BiomobyScavengerDialog();
                dialog.getContentPane().add(msp);
                JButton accept = new JButton("Okay");
                JButton cancel = new JButton("Cancel");
                msp.add(accept);
                msp.add(cancel);
                accept.addActionListener(new ActionListener() {
                    public void actionPerformed(ActionEvent ae2) {
                        //
                        if (dialog.isVisible()) {
                            try {
                                BiomobyScavenger bs = new BiomobyScavenger(
                                        (msp.getRegistryEndpoint().equals("") ? endpoint
                                                : msp.getRegistryEndpoint()),
                                        (msp.getRDFLocation().equals("") || msp.getRDFLocation().indexOf(" ") >= 0 ? defaultResourceURL
                                                : msp.getRDFLocation()));
                                s.addScavenger(bs);
                            } catch (ScavengerCreationException sce) {
                                JOptionPane.showMessageDialog(null, "Unable to create scavenger!\n" + sce.getMessage(), "Exception!",
                                         JOptionPane.ERROR_MESSAGE);
                                sce.printStackTrace();
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
    }}
