/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.rserv;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

/**
 * An editor for the rserv processor, allows the script to be defined and input
 * and output ports added or removed.
 * 
 * @author Tom Oinn
 */
// FIXME: This is the very same class as BeanshellEditor !
public class RservEditor implements ProcessorEditor {

	public ActionListener getListener(Processor theProcessor) {
		final RservProcessor bp = (RservProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// If the workbench is present then create and display a new
				// panel to configure the script engine.
				UIUtils.createFrame(bp.getModel(), new RservConfigPanel(bp),
						100, 100, 400, 500);
				/**
				 * if (Workbench.workbench != null) { GenericUIComponentFrame
				 * thing = new
				 * GenericUIComponentFrame(Workbench.workbench.model, new
				 * RservConfigPanel(bp)); thing.setSize(400,500);
				 * thing.setLocation(100,100);
				 * Workbench.workbench.desktop.add(thing); thing.moveToFront(); }
				 */
			}
		};
	}

	public String getEditorDescription() {
		return "Configure rserv...";
	}

}
