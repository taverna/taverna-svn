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
