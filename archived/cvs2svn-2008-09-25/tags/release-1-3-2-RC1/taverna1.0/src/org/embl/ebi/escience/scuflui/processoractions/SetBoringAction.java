/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.processoractions;

import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import java.awt.event.*;
import javax.swing.*;
import java.awt.*;

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
