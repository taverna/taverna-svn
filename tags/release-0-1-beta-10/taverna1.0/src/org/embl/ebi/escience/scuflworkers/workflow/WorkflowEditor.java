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
		    GenericUIComponentFrame editor = new GenericUIComponentFrame(nestedModel, new AdvancedModelExplorer());
		    editor.setSize(500,300);
		    editor.setLocation(40,140);
		    editor.setTitle(editor.getTitle()+" ["+wp.getName()+"]");
		    Workbench.workbench.desktop.add(editor);
		    editor.moveToFront();
		}
	    };
    }
    
    public String getEditorDescription() {
	return "Edit nested workflow...";
    }

}
	    
