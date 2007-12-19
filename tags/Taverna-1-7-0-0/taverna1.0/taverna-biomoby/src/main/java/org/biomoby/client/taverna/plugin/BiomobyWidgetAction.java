/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import java.awt.Dimension;
import java.net.URL;

import javax.swing.ImageIcon;
import javax.swing.JComponent;
import javax.swing.JTree;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.actions.AbstractProcessorAction;
import org.embl.ebi.escience.scuflworkers.java.LocalServiceProcessor;

public class BiomobyWidgetAction extends AbstractProcessorAction {
    public JComponent getComponent(Processor processor) {
        return new JTree(new String[] { processor.getName() });
    }

    public boolean canHandle(Processor processor) {
        if (processor instanceof LocalServiceProcessor) {
            LocalServiceProcessor local = (LocalServiceProcessor) processor;
            return local.getWorkerClassName().matches(
                    "^(\\w+\\.)+\\w+Moby\\w+$");
        }
        return false;
    }

    public String getDescription() {
        return "BioMoby Widget Action";
    }

    public ImageIcon getIcon() {
        Class cls = this.getClass();
        URL url = cls.getClassLoader().getResource(
                "org/biomoby/client/taverna/plugin/moby_small.gif");
        return new ImageIcon(url);
    }

    public Dimension getFrameSize() {
        return new Dimension(450, 450);
    }

}
