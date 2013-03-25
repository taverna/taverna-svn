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
package net.sf.taverna.t2.workbench.views.graph.actions;

import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;
import java.util.Set;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.observer.SwingAwareObserver;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowInputPortAction;
import net.sf.taverna.t2.workbench.design.actions.EditDataflowOutputPortAction;
import net.sf.taverna.t2.workbench.design.actions.RenameProcessorAction;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workbench.selection.DataflowSelectionModel;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.selection.events.DataflowSelectionMessage;
import net.sf.taverna.t2.workbench.selection.events.WorkflowBundleSelectionEvent;
import net.sf.taverna.t2.workbench.selection.events.SelectionManagerEvent;
import uk.org.taverna.scufl2.api.container.WorkflowBundle;
import uk.org.taverna.scufl2.api.core.Processor;
import uk.org.taverna.scufl2.api.port.InputWorkflowPort;
import uk.org.taverna.scufl2.api.port.OutputWorkflowPort;

/**
 * An action that allows user to rename workflow input, output or
 * processor, in case one of these is currently selected in the Graph View.
 *
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class RenameWFInputOutputProcessorAction extends AbstractAction implements DesignOnlyAction {

	/* Current workflow's selection model event observer.*/
	private Observer<DataflowSelectionMessage> workflowSelectionObserver = new DataflowSelectionObserver();

	private final EditManager editManager;
	private final SelectionManager selectionManager;

	public RenameWFInputOutputProcessorAction(EditManager editManager, final SelectionManager selectionManager) {
		super();
		this.editManager = editManager;
		this.selectionManager = selectionManager;
		putValue(SMALL_ICON, WorkbenchIcons.renameIcon);
		putValue(NAME, "Rename");
		putValue(SHORT_DESCRIPTION, "Rename inputs, outputs or services");
		putValue(Action.ACCELERATOR_KEY,
				KeyStroke.getKeyStroke(KeyEvent.VK_F2, 0));
		setEnabled(false);

		selectionManager.addObserver(new SelectionManagerObserver());

	}

	public void actionPerformed(ActionEvent e) {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel dataFlowSelectionModel = selectionManager.getDataflowSelectionModel(workflowBundle);
		// Get selected port
		Set<Object> selectedWFComponents = dataFlowSelectionModel
				.getSelection();
		if (selectedWFComponents.size() > 1){
			JOptionPane
					.showMessageDialog(
							null,
							"Only one workflow component should be selected for this action.",
							"Warning", JOptionPane.WARNING_MESSAGE);
		}
		else{
			Object selectedWFComponent = selectedWFComponents.toArray()[0];
			if (selectedWFComponent instanceof InputWorkflowPort) {
				InputWorkflowPort port = (InputWorkflowPort) selectedWFComponent;
				new EditDataflowInputPortAction(port.getParent(),
						port, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof OutputWorkflowPort){
				OutputWorkflowPort port = (OutputWorkflowPort) selectedWFComponent;
				new EditDataflowOutputPortAction(port.getParent(),
						port, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else if (selectedWFComponent instanceof Processor){
				Processor processor = (Processor) selectedWFComponent;
				new RenameProcessorAction(processor.getParent(),
						processor, null, editManager, selectionManager)
						.actionPerformed(e);
			}
			else{ // should not happen as the button will be disabled otherwise, but ...
				JOptionPane
				.showMessageDialog(
						null,
						"This action does not apply for the selected component.",
						"Warning", JOptionPane.WARNING_MESSAGE);
			}
		}
	}

	/**
	 * Check if action should be enabled or disabled and update its status.
	 */
	public void updateStatus() {
		WorkflowBundle workflowBundle = selectionManager.getSelectedWorkflowBundle();
		DataflowSelectionModel selectionModel = selectionManager.getDataflowSelectionModel(workflowBundle);

		// List of all selected objects in the graph view
		Set<Object> selection = selectionModel.getSelection();

		if (selection.isEmpty()){
			setEnabled(false);
		}
		else{
			// Take the first selected item - we only support single selections anyway
			Object selected = selection.toArray()[0];
			if ((selected instanceof Processor) ||
					(selected instanceof InputWorkflowPort) ||
					(selected instanceof OutputWorkflowPort)){
				setEnabled(true);
			}
			else{
				setEnabled(false);
			}
		}
	}

	/**
	 * Observes events on workflow Selection Manager, i.e. when a workflow
	 * node is selected in the graph view, and enables/disables this action accordingly.
	 */
	private final class DataflowSelectionObserver extends SwingAwareObserver<DataflowSelectionMessage> {
		@Override
		public void notifySwing(Observable<DataflowSelectionMessage> sender, DataflowSelectionMessage message) {
			updateStatus();
		}
	}

	private final class SelectionManagerObserver extends SwingAwareObserver<SelectionManagerEvent> {
		@Override
		public void notifySwing(Observable<SelectionManagerEvent> sender, SelectionManagerEvent message) {
			if (message instanceof WorkflowBundleSelectionEvent) {
				WorkflowBundleSelectionEvent workflowBundleSelectionEvent = (WorkflowBundleSelectionEvent) message;
				WorkflowBundle oldFlow = workflowBundleSelectionEvent.getPreviouslySelectedWorkflowBundle();
				WorkflowBundle newFlow = workflowBundleSelectionEvent.getSelectedWorkflowBundle();
				// Update the buttons status as current dataflow has changed
				updateStatus();

				// Remove the workflow selection model listener from the previous (if any)
				// and add to the new workflow (if any)
				if (oldFlow != null) {
					selectionManager.getDataflowSelectionModel(oldFlow).removeObserver(workflowSelectionObserver);
				}

				if (newFlow != null) {
					selectionManager.getDataflowSelectionModel(newFlow).addObserver(workflowSelectionObserver);
				}
			}
		}

	}

}
