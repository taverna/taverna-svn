/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.GridLayout;

import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import org.embl.ebi.escience.scuflui.shared.ShadedLabel;

/**
 * a dialog for helping create scavengers for BioMoby registries that are not the default registry.
 *
 */
public class BiomobyScavengerActionDialog extends JPanel {

	private static final long serialVersionUID = -57047613557546674L;

    /**
     * Default constructor.
     *
     */
    public BiomobyScavengerActionDialog() {
        super();
        GridLayout layout = new GridLayout(0, 3, 5,5);
        setLayout(layout);
        add(new ShadedLabel("Update the cache for the registry: ", ShadedLabel.TAVERNA_BLUE, true));
        add(new JButton("OK"));
        add(new JButton("Help"));
        add(new ShadedLabel("Show only those services that are 'pingable': ", ShadedLabel.TAVERNA_BLUE, true));
        add(new JCheckBox("", false));
        add(new JButton("Help"));
        add(new ShadedLabel("Generate Moby Datatypes: ", ShadedLabel.TAVERNA_BLUE, true));
        add(new JButton("OK"));
        add(new JButton("Help"));
        add(new ShadedLabel("Get Cache Info: ", ShadedLabel.TAVERNA_BLUE, true));
        add(new JButton("OK"));
        add(new JButton("Help"));
        // TODO - add a drop down menu of previously used endpoints
        setPreferredSize(this.getPreferredSize());
        setMinimumSize(this.getPreferredSize());
        setMaximumSize(this.getPreferredSize());
    }
}
