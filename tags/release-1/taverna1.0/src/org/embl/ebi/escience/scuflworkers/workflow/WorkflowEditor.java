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
import javax.swing.JSplitPane;
import javax.swing.ImageIcon;

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
		    ScuflUIComponent combinedEditor = new CombinedEditor(wp);
		    UIUtils.createFrame(nestedModel, combinedEditor, 40, 140, 500, 600);
		}
	    };
    }
    
    public String getEditorDescription() {
	return "Edit nested workflow...";
    }
    
    class CombinedEditor extends JSplitPane implements ScuflUIComponent {
	private WorkflowProcessor wp;
	public CombinedEditor(WorkflowProcessor wproc) {
	    super(HORIZONTAL_SPLIT);
	    setTopComponent(explorer);
	    setBottomComponent(diagram);
	    this.wp = wproc;
	}
	AdvancedModelExplorer explorer = new AdvancedModelExplorer() {
		public void attachToModel(ScuflModel theModel) {
		    super.attachToModel(theModel);
		    workOffline.setEnabled(false);
		}
	    };
	ScuflDiagramPanel diagram = new ScuflDiagramPanel();
	public void attachToModel(ScuflModel theModel) {
	    explorer.attachToModel(theModel);
	    diagram.attachToModel(theModel);
	}
	public void detachFromModel() {
	    explorer.detachFromModel();
	    diagram.detachFromModel();
	}
	public ImageIcon getIcon() {
	    return explorer.getIcon();
	}
	public String getName() {
	    return "Nested workflow editor for : "+wp.getName();
	}
    }
}
	    
