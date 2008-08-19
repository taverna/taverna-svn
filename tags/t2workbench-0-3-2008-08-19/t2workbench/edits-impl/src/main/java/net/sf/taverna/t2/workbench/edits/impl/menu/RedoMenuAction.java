package net.sf.taverna.t2.workbench.edits.impl.menu;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;

import org.apache.log4j.Logger;

/**
 * Redo the previous {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class RedoMenuAction extends AbstractUndoMenuAction {

	private static Logger logger = Logger.getLogger(RedoMenuAction.class);

	public RedoMenuAction() {
		super(20);
	}

	@Override
	protected Action createAction() {
		return new AbstractUndoAction("Redo") {
			@Override
			protected boolean isActive(Dataflow dataflow) {
				return editManager.canRedoDataflowEdit(dataflow);
			}

			@Override
			protected void performUndoOrRedo(Dataflow dataflow) {
				try {
					editManager.redoDataflowEdit(dataflow);
				} catch (EditException e) {
					logger.warn("Could not redo for " + dataflow, e);
					JOptionPane.showMessageDialog(null,
							"Could not redo for dataflow " + dataflow + ":\n"
									+ e, "Could not redo",
							JOptionPane.ERROR_MESSAGE);
				} catch (RuntimeException e) {
					logger.warn("Could not redo for " + dataflow, e);
					JOptionPane.showMessageDialog(null,
							"Could not redo for dataflow " + dataflow + ":\n"
									+ e, "Could not redo",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
	}

}
