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
package net.sf.taverna.t2.workbench.run.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.awt.event.KeyEvent;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;

import net.sf.taverna.t2.lang.observer.Observable;
import net.sf.taverna.t2.lang.observer.Observer;
import net.sf.taverna.t2.lang.ui.ModelMap;
import net.sf.taverna.t2.lang.ui.ModelMap.ModelMapEvent;
import net.sf.taverna.t2.reference.ui.InvalidDataflowReport;
import net.sf.taverna.t2.workbench.ModelMapConstants;
//import net.sf.taverna.t2.workbench.edits.EditManager;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.DataflowValidationReport;

import net.sf.taverna.t2.workbench.report.ReportManager;

//import org.apache.log4j.Logger;

@SuppressWarnings("serial")
public class ValidateWorkflowAction extends AbstractAction {

	//private static Logger logger = Logger.getLogger(ValidateWorkflowAction.class);

	private static final String VALIDATE_WORKFLOW = "Validate workflow";

	//private EditManager editManager = EditManager.getInstance();

	private FileManager fileManager = FileManager.getInstance();

	private ModelMap modelMap = ModelMap.getInstance();

	private ModelMapObserver modelMapObserver = new ModelMapObserver();

	public ValidateWorkflowAction() {
		super(VALIDATE_WORKFLOW, WorkbenchIcons.searchIcon);
		putValue(Action.MNEMONIC_KEY, KeyEvent.VK_V);

		modelMap.addObserver(modelMapObserver);
		updateEnabledStatus(fileManager.getCurrentDataflow());
	}

	public void actionPerformed(ActionEvent ev) {
		Component parentComponent = null;
		if (ev.getSource() instanceof Component) {
			parentComponent = (Component) ev.getSource();
		}
		validateCurrentDataflow(parentComponent);
	}
	
	public boolean validateCurrentDataflow(Component parentComponent) {
		Dataflow dataflow = fileManager.getCurrentDataflow();
		ReportManager.getInstance().updateReport(dataflow, true);
		return true;
	}
	

	protected void updateEnabledStatus(Dataflow dataflow) {
		if (dataflow == null) {
			setEnabled(false);
		} else {
			setEnabled(true);
		}
	}

	private final class ModelMapObserver implements Observer<ModelMapEvent> {
		public void notify(Observable<ModelMapEvent> sender,
				ModelMapEvent message) throws Exception {
			if (message.getModelName().equals(
					ModelMapConstants.CURRENT_DATAFLOW)) {
				Dataflow dataflow = (Dataflow) message.getNewModel();
				updateEnabledStatus(dataflow);
			}
		}
	}

}
