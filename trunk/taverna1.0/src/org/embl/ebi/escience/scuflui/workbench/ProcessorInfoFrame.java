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
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.ScuflProcessorInfo;


/**
 * An internal frame containing a processor information
 * panel.
 * @author Tom Oinn
 */
public class ProcessorInfoFrame extends JInternalFrame {
    
    public ProcessorInfoFrame(Processor theProcessor) {
	super("Info for processor "+theProcessor.getName(), true, true);
	addInternalFrameListener(new InternalFrameAdapter() {
		public void internalFrameClosing(InternalFrameEvent e) {
		    // don't need to do anything here, I think.
		}
	    });
	JScrollPane pane = new JScrollPane(new ScuflProcessorInfo(theProcessor));
	getContentPane().add(pane);
	pack();
	setVisible(true);
    }

}
