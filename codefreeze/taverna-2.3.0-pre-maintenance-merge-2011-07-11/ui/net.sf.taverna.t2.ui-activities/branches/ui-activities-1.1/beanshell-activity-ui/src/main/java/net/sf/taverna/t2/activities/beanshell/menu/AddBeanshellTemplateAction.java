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
package net.sf.taverna.t2.activities.beanshell.menu;

import java.awt.event.ActionEvent;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.net.URI;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JComponent;
import javax.swing.KeyStroke;

import net.sf.taverna.t2.activities.beanshell.BeanshellActivity;
import net.sf.taverna.t2.activities.beanshell.servicedescriptions.BeanshellTemplateService;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.ui.workflowview.WorkflowView;
import net.sf.taverna.t2.workflowmodel.Dataflow;

import org.apache.log4j.Logger;

/**
 * An action to add a beanshell activity + a wrapping processor to the workflow.
 * 
 * @author Alex Nenadic
 * 
 */
@SuppressWarnings("serial")
public class AddBeanshellTemplateAction extends AbstractContextualMenuAction {

	private static final String ADD_BEANSHELL = "Beanshell";

	private static final URI insertSection = URI
			.create("http://taverna.sf.net/2009/contextMenu/insert");

	private static Logger logger = Logger
			.getLogger(AddBeanshellTemplateAction.class);

	public AddBeanshellTemplateAction() {
		super(insertSection, 30);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Dataflow;
	}

	@Override
	protected Action createAction() {

		return new AddBeanshellAction();
	}
	
	protected class AddBeanshellAction extends AbstractAction {
		AddBeanshellAction () {
			super (ADD_BEANSHELL, ActivityIconManager.getInstance().iconForActivity(
						new BeanshellActivity()));
		}

		public void actionPerformed(ActionEvent e) {
			Dataflow workflow = FileManager.getInstance()
			.getCurrentDataflow();

	WorkflowView.importServiceDescription(BeanshellTemplateService.getServiceDescription(),
			false);
		}
	}

}
