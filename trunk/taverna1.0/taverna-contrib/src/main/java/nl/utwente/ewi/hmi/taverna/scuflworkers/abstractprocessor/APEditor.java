/**
 * CVS
 * $Author: sowen70 $
 * $Date: 2006-07-11 15:08:48 $
 * $Revision: 1.1 $
 */
package nl.utwente.ewi.hmi.taverna.scuflworkers.abstractprocessor;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflui.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

/**
 * class for editing the abstract processors
 * 
 * @author Ingo Wassink
 * 
 */
public class APEditor implements ProcessorEditor {
	/**
	 * Method for getting the listener for the editor
	 * 
	 * @param theProcessor
	 * @return the action listener
	 */
	public ActionListener getListener(Processor theProcessor) {
		final APProcessor apProcessor = (APProcessor) theProcessor;
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				// If the workbench is present then create and display a new
				// panel to configure the script engine.
				UIUtils.createFrame(apProcessor.getModel(), new APConfigPanel(
						apProcessor), 100, 100, 400, 500);
			}
		};
	}

	public String getEditorDescription() {
		return "Edit abstract processor...";
	}
}
