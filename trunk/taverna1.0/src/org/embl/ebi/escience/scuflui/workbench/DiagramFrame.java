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
import org.embl.ebi.escience.scuflui.ScuflDiagram;


/**
 * An internal frame containing a ScuflDiagram
 * @author Tom Oinn
 */
public class DiagramFrame extends JInternalFrame {

    ScuflDiagram diagram = new ScuflDiagram();
    
    public DiagramFrame(ScuflModel model) {
	super("Scufl Diagram", true, true, true, true);
	JScrollPane pane = new JScrollPane(diagram);
	getContentPane().add(pane);
	// Bind to the specified model
	diagram.attachToModel(model);
	// Unbind on window close
	addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) {
		    diagram.detachFromModel();
		}
	    });
	pack();
	setVisible(true);
    }

}
