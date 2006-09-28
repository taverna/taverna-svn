/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.workflow;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.ImageIcon;
import javax.swing.JSplitPane;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflui.AdvancedModelExplorer;
import org.embl.ebi.escience.scuflui.ScuflDiagramPanel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;

/**
 * Creates a new AdvancedModelExplorer within the desktop pane
 * to edit the nested workflow. This class shouldn't be used
 * as we're going to add multi-model support via Raven and Zaria
 * @author Tom Oinn
 * @deprecated
 */
public class WorkflowEditor implements ProcessorEditor {
	
	public ActionListener getListener(Processor theProcessor) {
		final WorkflowProcessor wp = (WorkflowProcessor)theProcessor;
		final ScuflModel nestedModel = wp.getInternalModel();
		return new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				WorkflowModelViewSPI combinedEditor = new CombinedEditor(wp);
				UIUtils.createFrame(nestedModel, combinedEditor, 40, 140, 500, 600);
			}
		};
	}
	
	public String getEditorDescription() {
		return "Edit nested workflow...";
	}
	
	class CombinedEditor extends JSplitPane implements WorkflowModelViewSPI {
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
			if (wp == null) {
				return "Nested workflow editor";
			}
			return "Nested workflow editor for: " + wp.getName();
		}
		public void onDisplay() {
			// TODO Auto-generated method stub
			
		}
		public void onDispose() {
			detachFromModel();
			
		}
	}
}

