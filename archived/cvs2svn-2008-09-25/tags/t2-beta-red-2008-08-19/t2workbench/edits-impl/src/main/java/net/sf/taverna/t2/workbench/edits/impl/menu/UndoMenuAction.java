package net.sf.taverna.t2.workbench.edits.impl.menu;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;

import org.apache.log4j.Logger;

/**
 * Undo the last {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class UndoMenuAction extends AbstractUndoMenuAction {

	private static Logger logger = Logger.getLogger(UndoMenuAction.class);

	public UndoMenuAction() {
		super(10);
	}

	@Override
	protected Action createAction() {
		return new AbstractUndoAction("Undo") {
			@Override
			protected boolean isActive(Dataflow dataflow) {
				return editManager.canUndoDataflowEdit(dataflow);
			}

			@Override
			protected void performUndoOrRedo(Dataflow dataflow) {
				try {
					editManager.undoDataflowEdit(dataflow);
				} catch (RuntimeException e) {
					logger.warn("Could not undo for " + dataflow, e);
					JOptionPane.showMessageDialog(null,
							"Could not undo for dataflow " + dataflow + ":\n"
									+ e, "Could not undo",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

}
