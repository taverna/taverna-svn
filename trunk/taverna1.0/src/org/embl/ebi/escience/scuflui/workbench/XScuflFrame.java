/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import javax.swing.JInternalFrame;
import javax.swing.JScrollPane;
import javax.swing.event.InternalFrameAdapter;
import javax.swing.event.InternalFrameEvent;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.XScuflTextArea;


/**
 * An internal frame containing the XScufl text of the model
 * @author Tom Oinn
 */
public class XScuflFrame extends JInternalFrame {

    XScuflTextArea xscufl = new XScuflTextArea();
    
    public XScuflFrame(ScuflModel model) {
	super("XScufl", true, true, true, true);
	JScrollPane pane = new JScrollPane(xscufl);
	getContentPane().add(pane);
	// Bind to the specified model
	xscufl.attachToModel(model);
	// Unbind on window close
	addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) {
		    xscufl.detachFromModel();
		}
	    });
	pack();
	setVisible(true);
    }
    
}
