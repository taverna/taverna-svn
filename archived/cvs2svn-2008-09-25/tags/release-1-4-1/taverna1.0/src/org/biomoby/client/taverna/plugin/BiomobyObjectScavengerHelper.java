/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JOptionPane;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;

// import java.lang.String;

/**
 * Helper for handling Biomoby scavengers. <p>
 * @deprecated
 */
public class BiomobyObjectScavengerHelper implements ScavengerHelper {

    public String getScavengerDescription() {
        return "Add new Biomoby Object scavenger...";
    }

    public ActionListener getListener(ScavengerTree theScavenger) {
        final ScavengerTree s = theScavenger;
        return new ActionListener() {
            public void actionPerformed(ActionEvent ae) {
                String baseURL = (String) JOptionPane
                        .showInputDialog(
                                null,
                                "Location (URL) of your BioMoby Object RDF Document?",
                                "Biomoby object location",
                                JOptionPane.QUESTION_MESSAGE, null, null,
                                "http://biomoby.org/RESOURCES/MOBY-S/Objects");
                if (baseURL != null) {
                    try {
                        s.addScavenger(new BiomobyObjectScavenger(baseURL));
                    } catch (ScavengerCreationException sce) {
                        JOptionPane.showMessageDialog(null,
                                "Unable to create scavenger!\n"
                                        + sce.getMessage(), "Exception!",
                                JOptionPane.ERROR_MESSAGE);
                    }
                }
            }
        };
    }

}
