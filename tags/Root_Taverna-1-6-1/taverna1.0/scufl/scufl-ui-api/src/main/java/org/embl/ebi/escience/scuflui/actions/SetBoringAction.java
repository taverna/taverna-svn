/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.actions;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.spi.ProcessorActionSPI;

/**
 * Set the boring flag on processors
 * @author Tom Oinn
 */
public class SetBoringAction implements ProcessorActionSPI {
    
    public ActionListener getListener(Processor processor) {
	final Processor theProcessor = processor;
	return new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    theProcessor.setBoring(!theProcessor.isBoring());
		}
	    };
    }

    public String getDescription() {
	return "Set as boring";
    }

    public boolean canHandle(Processor processor) {
	return (!processor.isBoring());
    }
    
    public ImageIcon getIcon() {
	return null;
    }
    
}
