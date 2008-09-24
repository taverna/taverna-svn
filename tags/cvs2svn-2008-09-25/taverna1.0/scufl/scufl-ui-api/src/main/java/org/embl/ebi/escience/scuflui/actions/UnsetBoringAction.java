/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.actions;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scufl.Processor;

/**
 * Unset the boring flag on processors
 * @author Tom Oinn
 */
public class UnsetBoringAction extends SetBoringAction  {
    
    public String getDescription() {
	return "Set as interesting";
    }

    public boolean canHandle(Processor processor) {
	return (processor.isBoring());
    }
    
    public ImageIcon getIcon() {
	return null;
    }
    
}
