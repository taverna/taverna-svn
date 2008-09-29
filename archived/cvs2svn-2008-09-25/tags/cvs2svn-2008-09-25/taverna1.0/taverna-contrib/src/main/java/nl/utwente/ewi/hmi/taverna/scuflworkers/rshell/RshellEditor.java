/*
 * CVS
 * $Author: mereden $
 * $Date: 2006-09-28 16:36:57 $
 * $Revision: 1.2 $
 * University of Twente, Human Media Interaction Group
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.rshell;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

/**
 * An editor for the rserv processor, allows the script to be defined and input
 * and output ports added or removed.
 * 
 * @author Tom Oinn, Ingo Wassink
 */
public class RshellEditor implements ProcessorEditor {

	public ActionListener getListener(Processor theProcessor) {
		final RshellProcessor rProcessor = (RshellProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// If the workbench is present then create and display a new
				// panel to configure the script engine.
				UIUtils.createFrame(rProcessor.getModel(),
						new RshellConfigPanel(rProcessor), 100, 100, 400, 500);
			}
		};
	}

	public String getEditorDescription() {
		return "Configure Rshell...";
	}

}
