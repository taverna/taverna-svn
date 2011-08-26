/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import javax.swing.JComponent;
import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.ScuflUIComponent;


/**
 * An internal frame containing a class implementing the
 * scuflui.ScuflUIComponent interface. This implementation
 * places the component in a scroll pane inside an internal
 * frame, registers it with the model and creates an event
 * listener such that closing the internal frame will deregister
 * the component from the model.
 * @author Tom Oinn
 */
public class GenericUIComponentFrame extends JInternalFrame {

    ScuflUIComponent component;
   
    public GenericUIComponentFrame(ScuflModel model, ScuflUIComponent component) {
	super(component.getName(), true, true, true, true);
	this.component = component;
	JScrollPane pane = new JScrollPane((JComponent)component);
	getContentPane().add(pane);
	// Bind to the specified model
	component.attachToModel(model);
	// Unbind on window close
	addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) {
		    GenericUIComponentFrame.this.component.detachFromModel();
		}
	    });
	setSize(400,400);
	moveToFront();
	setVisible(true);
    }
    
}
