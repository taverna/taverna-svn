/*******************************************************************************
 * Copyright (C) 2009 The University of Manchester   
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
package net.sf.taverna.t2.activities.sequencefile.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.Action;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.activities.sequencefile.SequenceFileActivity;
import net.sf.taverna.t2.activities.sequencefile.servicedescriptions.SequenceFileTemplateService;
import net.sf.taverna.t2.ui.menu.AbstractMenuAction;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workbench.views.graph.actions.DesignOnlyAction;
import net.sf.taverna.t2.workbench.views.graph.menu.InsertMenu;

/**
 * A menu action to add a SequenceFileActivity and a wrapping processor to the
 * workflow.
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class SequenceFileAddTemplateMenuAction extends AbstractMenuAction {

	private static final URI ADD_SEQUENCE_FILE_IMPORT_URI = URI
			.create("http://taverna.sf.net/2008/t2workbench/menu#graphMenuAddSequenceFile");

	public SequenceFileAddTemplateMenuAction() {
		super(InsertMenu.INSERT, 24, ADD_SEQUENCE_FILE_IMPORT_URI);
	}

	@Override
	protected Action createAction() {
		return new AddSequenceFileMenuAction();
	}

	protected class AddSequenceFileMenuAction extends DesignOnlyAction {
		AddSequenceFileMenuAction() {
			super();
			putValue(SMALL_ICON, ActivityIconManager.getInstance().iconForActivity(
					new SequenceFileActivity()));
			putValue(NAME, SequenceFileTemplateService.SERVICE_NAME);
			putValue(SHORT_DESCRIPTION, SequenceFileTemplateService.SERVICE_DESCRIPTION);
			putValue(Action.ACCELERATOR_KEY, KeyStroke.getKeyStroke(KeyEvent.VK_X,
					InputEvent.SHIFT_DOWN_MASK | InputEvent.ALT_DOWN_MASK));

		}

		public void actionPerformed(ActionEvent e) {

			WorkflowView.importServiceDescription(SequenceFileTemplateService
					.getServiceDescription(), false);

		}
	}
}
