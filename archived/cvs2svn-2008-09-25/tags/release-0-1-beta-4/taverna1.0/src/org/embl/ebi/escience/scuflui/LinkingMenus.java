/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.embl.ebi.escience.scufl.*;

import org.embl.ebi.escience.scuflui.ScuflIcons;
/**
 * A static method to generate appropriate menu items
 * to link from ports in a Scufl model
 * @author Tom Oinn
 */
public class LinkingMenus {

    /**
     * Return a menu presenting all the possible sink ports
     * that the source port could be linked to. Currently 
     * doesn't pay any heed to the typing and just shows all
     * possible sinks.
     */
    public static JPopupMenu linkFrom(Port sourcePort) {
	final Port fromPort = sourcePort;
	final ScuflModel model = sourcePort.getProcessor().getModel();
	JPopupMenu theMenu = new JPopupMenu("Possible targets");
	// Is this a workflow source? If so give the option to delete it
	if (fromPort.getProcessor() == model.getWorkflowSourceProcessor()) {
	    JMenuItem delete = new JMenuItem("Remove from model",ScuflIcons.deleteIcon);
	    delete.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			fromPort.getProcessor().removePort(fromPort);
		    }
		});
	    theMenu.add(delete);
	    theMenu.addSeparator();
	}
	JMenuItem title = new JMenuItem("Link '"+sourcePort.getName()+"' to....");
	title.setEnabled(false);
	theMenu.add(title);
	theMenu.addSeparator();
	JMenu workflowSinks = new JMenu("Workflow outputs");
	workflowSinks.setIcon(ScuflIcons.outputIcon);
	theMenu.add(workflowSinks);
	// Add the possible workflow sink ports
	Port[] wsp = model.getWorkflowSinkPorts();
	for (int i = 0; i < wsp.length; i++) {
	    JMenuItem wspitem = new JMenuItem(wsp[i].getName(), 
					      ScuflIcons.outputIcon);
	    final Port toPort = wsp[i];
	    wspitem.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			try {
			    model.addDataConstraint(new DataConstraint(model, fromPort, toPort));
			}
			catch (DataConstraintCreationException dcce) {
			    //
			}
		    }
		});
	    workflowSinks.add(wspitem);
	}

	theMenu.addSeparator();
	Processor[] processors = sourcePort.getProcessor().getModel().getProcessors();
	for (int i = 0; i < processors.length; i++) {
	    ImageIcon icon = null;
	    if (processors[i] instanceof SoaplabProcessor) {
		icon = ScuflIcons.soaplabIcon;
	    }
	    else if (processors[i] instanceof WSDLBasedProcessor) {
		icon = ScuflIcons.wsdlIcon;
	    }
	    else if (processors[i] instanceof TalismanProcessor) {
		icon = ScuflIcons.talismanIcon;
	    }
	    else if (processors[i] instanceof WorkflowProcessor) {
		icon = ScuflIcons.workflowIcon;
	    }
	    JMenu processorMenu = new JMenu(processors[i].getName());
	    processorMenu.setIcon(icon);
	    theMenu.add(processorMenu);
	    // Get all the input ports for this processor
	    InputPort[] inputs = processors[i].getInputPorts();
	    for (int j = 0; j < inputs.length; j++) {
		final Port toPort = inputs[j];
		final JMenuItem ip = new JMenuItem(inputs[j].getName(), ScuflIcons.inputPortIcon);
		processorMenu.add(ip);
		ip.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    try {
				model.addDataConstraint(new DataConstraint(model, fromPort, toPort));
			    }
			    catch (DataConstraintCreationException dcce) {
				//
			    }
			}
		    });
	    }
	}
	return theMenu;
    }


}
