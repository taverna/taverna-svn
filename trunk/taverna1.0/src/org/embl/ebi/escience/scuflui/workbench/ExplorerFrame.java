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
import org.embl.ebi.escience.scuflui.ScuflModelExplorer;


/**
 * An internal frame containing a ScuflModelExplorer tree
 * @author Tom Oinn
 */
public class ExplorerFrame extends JInternalFrame {

    ScuflModelExplorer explorer = new ScuflModelExplorer();
    
    public ExplorerFrame(ScuflModel model) {
	super("Scufl Model Explorer", true, true, true, true);
	JScrollPane pane = new JScrollPane(explorer);
	getContentPane().add(pane);
	// Bind to the specified model
	explorer.attachToModel(model);
	// Unbind on window close
	addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) {
		    explorer.detachFromModel();
		}
	    });
	pack();
	setVisible(true);
    }

}
