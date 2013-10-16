/**
 * 
 */
package net.sf.taverna.t2.component.ui.menu;

import java.awt.Dialog;
import java.awt.event.ActionEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;

import net.sf.taverna.t2.activities.dataflow.servicedescriptions.DataflowActivityIcon;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * @author alanrw
 * 
 */
public class NestedWorkflowCreatorMenuAction extends
		AbstractContextualMenuAction {

	FileManager fm = FileManager.getInstance();

	private static final URI configureSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/configure");

	public NestedWorkflowCreatorMenuAction() {
		super(configureSection, 70);
	}

	@Override
	public boolean isEnabled() {
		Object selection = getContextualSelection().getSelection();
		if (!super.isEnabled()) {
			return false;
		}
		if (selection == null) {
			return false;
		}
		if (selection instanceof Processor) {
			return true;
		}
		if (!(selection instanceof Dataflow)) {
			return false;
		}
		Dataflow d = (Dataflow) selection;
		if (d.getProcessors().isEmpty()) {
			return false;
		}
		return true;
	}

	@Override
	protected Action createAction() {
		return new AbstractAction("Create nested workflow...",
				DataflowActivityIcon.getDataflowIcon()) {
			private static final long serialVersionUID = -3121307982540205215L;

			public void actionPerformed(ActionEvent e) {
				Object o = getContextualSelection().getSelection();
				final Dialog dialog = new NestedWorkflowCreationDialog(null, o,
						fm.getCurrentDataflow());
				dialog.setVisible(true);
			}
		};
	}

}
