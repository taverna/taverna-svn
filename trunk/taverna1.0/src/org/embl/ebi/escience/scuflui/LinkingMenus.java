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
import javax.swing.JLabel;
import java.awt.Color;
//import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
//import org.embl.ebi.escience.scuflui.workbench.Workbench;

import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflSemanticMarkupEditor;
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
	    theMenu.add(new ShadedLabel("Workflow Input : "+sourcePort.getName(), ShadedLabel.TAVERNA_GREEN));
	    theMenu.addSeparator();
	    JMenuItem delete = new JMenuItem("Remove from model",ScuflIcons.deleteIcon);
	    delete.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			fromPort.getProcessor().removePort(fromPort);
		    }
		});
	    theMenu.add(delete);
	    JMenuItem edit = new JMenuItem("Edit metadata...", ScuflIcons.editIcon);
	    edit.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			UIUtils.createFrame(model, new ScuflSemanticMarkupEditor(fromPort.getMetadata()), 100, 100, 400, 600);
		    }
		});
	    theMenu.add(edit);
	    theMenu.addSeparator();
	    theMenu.add(new ShadedLabel("Connect to...", ShadedLabel.TAVERNA_ORANGE));
	    theMenu.addSeparator();
	}
	else {
	    theMenu.add(new ShadedLabel("Connect ouptut \""+sourcePort.getName()+"\" to...", ShadedLabel.TAVERNA_GREEN));
	    theMenu.addSeparator();
	}
	//JMenuItem title = new JMenuItem("Link '"+sourcePort.getName()+"' to...");
	//title.setEnabled(false);
	//theMenu.add(title);

	Port[] wsp = model.getWorkflowSinkPorts();
	Processor[] processors = sourcePort.getProcessor().getModel().getProcessors();
	
	/**JLabel heading = new JLabel("<html><body><b>Link <font color=\"purple\">"+sourcePort.getName()+"</font> to...</b></body></html>");
	   heading.setBackground(Color.WHITE);
	   heading.setOpaque(true);
	   theMenu.add(heading);
	*/
	/**
	   theMenu.add(new ShadedLabel("Link to...", ShadedLabel.TAVERNA_ORANGE));
	   theMenu.addSeparator();
	*/
	   JMenu workflowSinks = new JMenu("Workflow outputs");
	workflowSinks.setIcon(ScuflIcons.outputIcon);
	
	// Do the workflow sink ports
	if (wsp.length > 0) {
	    theMenu.add(workflowSinks);
	    theMenu.addSeparator();
	}
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
	theMenu.add(new ShadedLabel("Processors",ShadedLabel.TAVERNA_BLUE));
	theMenu.addSeparator();
	// Do the target processors
	for (int i = 0; i < processors.length; i++) {
	    // Get all the input ports for this processor
	    InputPort[] inputs = processors[i].getInputPorts();
	    if (inputs.length > 0 && processors[i] != sourcePort.getProcessor()) {
		ImageIcon icon = null;
		icon = org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredIcon(processors[i]);
		JMenu processorMenu = new JMenu(processors[i].getName());
		processorMenu.add(new ShadedLabel("Choose an Input",ShadedLabel.TAVERNA_ORANGE));
		processorMenu.addSeparator();
		processorMenu.setIcon(icon);
		int offset = 0;
		int menuSize = 15;
		JMenu currentMenu = processorMenu;
		boolean finished = false;
		theMenu.add(processorMenu);
		while (!finished) {
		    if (inputs.length > menuSize) {
			currentMenu = new JMenu("Inputs "+(offset+1)+" to "+((offset + menuSize > inputs.length)?inputs.length:offset+menuSize)); 
			processorMenu.add(currentMenu);
			currentMenu.add(new ShadedLabel("Inputs "+(offset+1)+" to "+((offset + menuSize > inputs.length)?inputs.length:offset+menuSize), ShadedLabel.TAVERNA_ORANGE));
			currentMenu.addSeparator();
		    }
		    for (int j = offset; (j < inputs.length) && 
			     (j < offset + menuSize); j++) {
			final Port toPort = inputs[j];
			final JMenuItem ip = new JMenuItem(inputs[j].getName(), ScuflIcons.inputPortIcon);
			currentMenu.add(ip);
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
		    offset += menuSize;
		    if (offset >= inputs.length) {
			finished = true;
		    }
		}
	    }
	}
	return theMenu;
    }


}
