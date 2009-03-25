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
import java.net.URI;
import java.util.ArrayList;
import java.util.List;

import javax.swing.AbstractAction;
import javax.swing.Action;

import org.apache.log4j.Logger;

import net.sf.taverna.t2.activities.rshell.RshellActivity;
import net.sf.taverna.t2.activities.rshell.RshellActivityConfigurationBean;
import net.sf.taverna.t2.ui.menu.AbstractContextualMenuAction;
import net.sf.taverna.t2.workbench.activityicons.ActivityIconManager;
import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.CompoundEdit;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Edit;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.EditsRegistry;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityConfigurationException;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * An action to add a Rshell activity + a wrapping processor to the workflow. 
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class AddRshellTemplateAction extends AbstractContextualMenuAction {

	private static final URI serviceTemplatesSection = URI
	.create("http://taverna.sf.net/2009/contextMenu/serviceTemplates");
	
	private static Logger logger = Logger.getLogger(AddRshellTemplateAction.class);

	public AddRshellTemplateAction() {
		super(serviceTemplatesSection, 30);
	}

	@Override
	public boolean isEnabled() {
		return super.isEnabled()
				&& getContextualSelection().getSelection() instanceof Dataflow;
	}

	@Override
	protected Action createAction() {

		AbstractAction action = new AbstractAction("Add Rshell", ActivityIconManager.getInstance()
				.iconForActivity(new RshellActivity())){

			public void actionPerformed(ActionEvent e) {
				Dataflow workflow = FileManager.getInstance().getCurrentDataflow();

				// Create a processor placeholder for a Rshell activity 
				// and check for duplicate processor names
				String suggestedProcessorName = "Rshell";
				suggestedProcessorName = Tools.uniqueProcessorName(suggestedProcessorName, workflow);
				Processor processor = EditsRegistry.getEdits().createProcessor(suggestedProcessorName);				
				// Create the Rshell activity and configure it
				RshellActivity rshellActivity = new RshellActivity();
				try {
					rshellActivity.configure(new RshellActivityConfigurationBean());
				} catch (ActivityConfigurationException ex) {
					logger.error("Configuring Rshell activity failed when trying to add it to the workflow model", ex);
					return;
				}
				// List of all edits to be done to the workflow
				List<Edit<?>> editList = new ArrayList<Edit<?>>();
				editList.add(EditsRegistry.getEdits().getAddActivityEdit(processor, rshellActivity));
				editList.add(EditsRegistry.getEdits().getAddProcessorEdit(workflow, processor));
				try {
					EditManager.getInstance().doDataflowEdit(workflow, new CompoundEdit(editList));
				} catch (EditException ex) {
					logger.error("Adding Rshell activity to the workflow model failed", ex);
				}						
			}
			
		};
		
		return action;
	}

}

