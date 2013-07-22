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
package net.sf.taverna.t2.activities.rshell.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.activities.rshell.servicedescriptions.RshellTemplateService;
import net.sf.taverna.t2.servicedescriptions.ServiceDescriptionRegistry;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.ui.menu.DesignOnlyAction;
import net.sf.taverna.t2.ui.menu.MenuManager;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.selection.SelectionManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.menu.InsertMenu;
import uk.org.taverna.commons.services.ServiceRegistry;

/**
 * An action to add a Rshell activity + a wrapping processor to the workflow.
 *
 * @author Alex Nenadic
 * @author Alan R Williams
 *
 */
@SuppressWarnings("serial")
public class AddRshellTemplateMenuAction extends AbstractMenuAction {

	private static final String ADD_RSHELL = "RShell";

	private static final URI ADD_RSHELL_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddRShell");

	private EditManager editManager;

	private MenuManager menuManager;

	private SelectionManager selectionManager;

	private ActivityIconManager activityIconManager;

	private ServiceDescriptionRegistry serviceDescriptionRegistry;

	private ServiceRegistry serviceRegistry;

	public AddRshellTemplateMenuAction() {
		super(InsertMenu.INSERT, 600, ADD_RSHELL_URI);
	}

	@Override
	protected Action createAction() {
		return new AddRShellMenuAction();
	}

	protected class AddRShellMenuAction extends AbstractAction implements DesignOnlyAction {
		AddRShellMenuAction() {
			super();
			putValue(SMALL_ICON, activityIconManager.iconForActivity(RshellTemplateService.ACTIVITY_TYPE));
			putValue(NAME, ADD_RSHELL);
			putValue(SHORT_DESCRIPTION, "RShell");
			putValue(
					Action.ACCELERATOR_KEY,
					KeyStroke.getKeyStroke(KeyEvent.VK_R, InputEvent.SHIFT_DOWN_MASK
							| InputEvent.ALT_DOWN_MASK));

		}

		public void actionPerformed(ActionEvent e) {
			WorkflowView.importServiceDescription(
					serviceDescriptionRegistry.getServiceDescription(RshellTemplateService.ACTIVITY_TYPE), false, editManager,
					menuManager, selectionManager, serviceRegistry);
		}
	}

	public void setEditManager(EditManager editManager) {
		this.editManager = editManager;
	}

	public void setMenuManager(MenuManager menuManager) {
		this.menuManager = menuManager;
	}

	public void setSelectionManager(SelectionManager selectionManager) {
		this.selectionManager = selectionManager;
	}

	public void setActivityIconManager(ActivityIconManager activityIconManager) {
		this.activityIconManager = activityIconManager;
	}

	public void setServiceDescriptionRegistry(ServiceDescriptionRegistry serviceDescriptionRegistry) {
		this.serviceDescriptionRegistry = serviceDescriptionRegistry;
	}

	public void setServiceRegistry(ServiceRegistry serviceRegistry) {
		this.serviceRegistry = serviceRegistry;
	}

}
