/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester
 *
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 *
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.t2.workbench.edits.impl.menu;

import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.edits.Edit;
import net.sf.taverna.t2.workbench.edits.EditException;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;

import org.apache.log4j.Logger;

import uk.org.taverna.scufl2.api.container.WorkflowBundle;

/**
 * Redo the previous {@link Edit} done on the current workflow using the
 * {@link EditManager}.
 *
 * @author Stian Soiland-Reyes
 *
 */
public class RedoMenuAction extends AbstractMenuAction {

	private static Logger logger = Logger.getLogger(RedoMenuAction.class);
	private final EditManager editManager;
	private SelectionManager selectionManager;
	private AbstractUndoAction undoAction;

	public RedoMenuAction(EditManager editManager) {
		super(UndoMenuSection.UNDO_SECTION_URI, 20);
		this.editManager = editManager;
	}

	@Override
	protected Action createAction() {
		undoAction = new AbstractUndoAction("Redo", editManager) {
			@Override
			protected boolean isActive(WorkflowBundle workflowBundle) {
				return editManager.canRedoDataflowEdit(workflowBundle);
			}

			@Override
			protected void performUndoOrRedo(WorkflowBundle workflowBundle) {
				try {
					editManager.redoDataflowEdit(workflowBundle);
				} catch (EditException e) {
					logger.warn("Could not redo for " + workflowBundle, e);
					JOptionPane.showMessageDialog(null,
							"Could not redo for workflow " + workflowBundle + ":\n"
									+ e, "Could not redo",
							JOptionPane.ERROR_MESSAGE);
				} catch (RuntimeException e) {
					logger.warn("Could not redo for " + workflowBundle, e);
					JOptionPane.showMessageDialog(null,
							"Could not redo for workflow " + workflowBundle + ":\n"
									+ e, "Could not redo",
							JOptionPane.ERROR_MESSAGE);
				}
			}
		};
		undoAction.setSelectionManager(selectionManager);
		return undoAction;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
		if (undoAction != null) {
			undoAction.setSelectionManager(selectionManager);
		}
	}

}
