/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.beanshell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellConfigPanel;
import org.embl.ebi.escience.scuflworkers.beanshell.BeanshellProcessor;
import java.lang.String;



/**
 * An editor for the beanshell processor, allows the 
 * script to be defined and input and output ports
 * added or removed.
 * @author Tom Oinn
 */
public class BeanshellEditor implements ProcessorEditor {
    
    public ActionListener getListener(Processor theProcessor) {
	final BeanshellProcessor bp = (BeanshellProcessor)theProcessor;
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // If the workbench is present then create and display a new
		    // panel to configure the script engine.
		    if (Workbench.workbench != null) {
			GenericUIComponentFrame thing = new GenericUIComponentFrame(Workbench.workbench.model,
										    new BeanshellConfigPanel(bp));
			thing.setSize(400,500);
			thing.setLocation(100,100);
			Workbench.workbench.desktop.add(thing);
			thing.moveToFront();
		    }
		}
	    };
    }
    
    public String getEditorDescription() {
	return "Configure beanshell...";
    }

} 
