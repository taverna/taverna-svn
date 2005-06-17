/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.GridLayout;

import javax.swing.JPanel;
import javax.swing.JTextField;

import org.embl.ebi.escience.scuflui.ShadedLabel;

/**
 * a dialog for helping create scavengers for BioMoby registries that are not the default registry.
 *
 */
public class BiomobyScavengerDialog extends JPanel {

    private JTextField registryEndpoint = new JTextField("http://mobycentral.cbr.nrc.ca/cgi-bin/MOBY05/mobycentral.pl");

    private JTextField rdfLocation = new JTextField("http://biomoby.org/RESOURCES/MOBY-S/Objects");

    public BiomobyScavengerDialog() {
        super();
        GridLayout layout = new GridLayout(3, 2);
        setLayout(layout);
        add(new ShadedLabel("Location (URL) of your BioMoby central registry: ", ShadedLabel.TAVERNA_BLUE, true));
        registryEndpoint.setToolTipText("BioMoby Services will be retrieved from the endpoint that you specify here!");
        add(registryEndpoint);
        add(new ShadedLabel("Location (URL) of your BioMoby Object RDF Document: ", ShadedLabel.TAVERNA_GREEN, true));
        rdfLocation.setToolTipText("BioMoby Objects will be retrieved from the RDF document specified here!");
        add(rdfLocation);
        setPreferredSize(this.getPreferredSize());
        setMinimumSize(this.getPreferredSize());
        setMaximumSize(this.getPreferredSize());
    }

    public String getRegistryEndpoint() {
        return registryEndpoint.getText();
    }

    public String getRDFLocation() {
        return rdfLocation.getText();
    }

}
