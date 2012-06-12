/*******************************************************************************
 * Copyright (C) 2007-2009 The University of Manchester
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
package net.sf.taverna.t2.workbench.file.importworkflow.menu;

import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.configuration.colour.ColourManager;
import net.sf.taverna.t2.workbench.configuration.workbench.WorkbenchConfiguration;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.file.importworkflow.actions.AddNestedWorkflowAction;
import net.sf.taverna.t2.workbench.views.graph.menu.InsertMenu;

import org.apache.log4j.Logger;

/**
 * An action to add a nested workflow activity + a wrapping processor to the
 * workflow.
 *
 * @author Alex Nenadic
 * @author Stian Soiland-Reyes
 *
 */
public class AddNestedWorkflowMenuAction extends AbstractMenuAction {

	private static final String ADD_NESTED_WORKFLOW = "Nested workflow";

	private static final URI ADD_NESTED_WORKFLOW_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddNestedWorkflow");

	private static Logger logger = Logger
			.getLogger(AddNestedWorkflowMenuAction.class);

	private EditManager editManager;
	private FileManager fileManager;
	private MenuManager menuManager;
	private ColourManager colourManager;
	private WorkbenchConfiguration workbenchConfiguration;

	public AddNestedWorkflowMenuAction() {
		super(InsertMenu.INSERT, 400, ADD_NESTED_WORKFLOW_URI);
	}

	@Override
	protected Action createAction() {
		AddNestedWorkflowAction a = new AddNestedWorkflowAction(editManager, fileManager,
				menuManager, colourManager, workbenchConfiguration);
		// Override name to avoid "Add "
		a.putValue(Action.NAME, ADD_NESTED_WORKFLOW);
		a.putValue(Action.SHORT_DESCRIPTION, ADD_NESTED_WORKFLOW);
		a.putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(
				KeyEvent.VK_N, InputEvent.SHIFT_DOWN_MASK
						| InputEvent.ALT_DOWN_MASK));
		return a;

	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setFileManager(FileManager fileManager) {
		this.fileManager = fileManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setColourManager(ColourManager colourManager) {
		this.colourManager = colourManager;
	}

	public void setWorkbenchConfiguration(WorkbenchConfiguration workbenchConfiguration) {
		this.workbenchConfiguration = workbenchConfiguration;
	}

}
