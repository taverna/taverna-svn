/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JOptionPane;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.*;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflui.workbench.*;

/**
 * Creates a new AdvancedModelExplorer within the desktop pane
 * to edit the nested workflow
 * @author Tom Oinn
 */
public class WorkflowEditor implements ProcessorEditor {

    public ActionListener getListener(Processor theProcessor) {
	final WorkflowProcessor wp = (WorkflowProcessor)theProcessor;
	final ScuflModel nestedModel = wp.getInternalModel();
	return new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Create the new model explorer and show it
		    AdvancedModelExplorer editor = new AdvancedModelExplorer() {
			    public void attachToModel(ScuflModel theModel) {
				super.attachToModel(theModel);
				workOffline.setEnabled(false);
			    }
			    public String getName() {
				return super.getName()+" ["+wp.getName()+"]";
			    }
			};
		    UIUtils.createFrame(nestedModel, editor, 40, 140, 500, 300);
		}
	    };
    }
    
    public String getEditorDescription() {
	return "Edit nested workflow...";
    }

}
	    
