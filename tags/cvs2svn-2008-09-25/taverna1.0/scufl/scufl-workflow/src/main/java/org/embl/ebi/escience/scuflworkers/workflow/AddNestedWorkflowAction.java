/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: AddNestedWorkflowAction.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-04 16:34:32 $
 *               by   $Author: davidwithers $
 * Created on 05-Jul-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflworkers.workflow;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;

import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.actions.ScuflModelActionSPI;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;

@SuppressWarnings("serial")
public class AddNestedWorkflowAction extends ScuflModelActionSPI {
	private CreateNewNestedWorkflowAction createNew = new CreateNewNestedWorkflowAction();

	private OpenNestedWorkflowFromFileAction openFile = new OpenNestedWorkflowFromFileAction();

	private OpenNestedWorkflowFromURLAction openURL = new OpenNestedWorkflowFromURLAction();

	public AddNestedWorkflowAction() {
		putValue(SMALL_ICON, TavernaIcons.windowExplorer);
		putValue(NAME, "Add Nested Workflow");
		putValue(SHORT_DESCRIPTION, "Add Nested Workflow...");
	}

	public void actionPerformed(ActionEvent e) {
		JPopupMenu menu = new JPopupMenu("Add Nested Workflow");

		menu.add(new JMenuItem(createNew));

		menu.addSeparator();

		menu.add(new JMenuItem(openFile));

		menu.add(new JMenuItem(openURL));

		Component sourceComponent = (Component) e.getSource();
		menu.show(sourceComponent, 0, sourceComponent.getHeight());
	}

	public String getLabel() {
		return "New subworkflow";
	}

	class CreateNewNestedWorkflowAction extends AbstractAction {

		public CreateNewNestedWorkflowAction() {
			putValue(SMALL_ICON, TavernaIcons.newIcon);
			putValue(NAME, "New Workflow");
			putValue(SHORT_DESCRIPTION, "Create a new nested workflow");
		}

		public void actionPerformed(ActionEvent e) {
			createNestedWorkflow(null, (Component) e.getSource());
		}

	}

	class OpenNestedWorkflowFromFileAction extends AbstractAction {

		private JFileChooser fileChooser = new JFileChooser();

		public OpenNestedWorkflowFromFileAction() {
			putValue(SMALL_ICON, TavernaIcons.openIcon);
			putValue(NAME, "Open File...");
			putValue(SHORT_DESCRIPTION, "Open a nested workflow from a file");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {

			Preferences prefs = Preferences
					.userNodeForPackage(OpenNestedWorkflowFromFileAction.class);
			String curDir = prefs.get("currentDir", System
					.getProperty("user.home"));
			fileChooser.setDialogTitle("Open Nested Workflow");
			fileChooser.resetChoosableFileFilters();
			fileChooser.setFileFilter(new ExtensionFileFilter(
					new String[] { "xml" }));
			fileChooser.setCurrentDirectory(new File(curDir));
			int returnVal = fileChooser.showOpenDialog(null);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
				prefs.put("currentDir", fileChooser.getCurrentDirectory()
						.toString());
				File file = fileChooser.getSelectedFile();
				createNestedWorkflow(file.toURI().toString(), (Component) e
						.getSource());
			}
		}

	}

	public class OpenNestedWorkflowFromURLAction extends AbstractAction {

		public OpenNestedWorkflowFromURLAction() {
			putValue(SMALL_ICON, TavernaIcons.openurlIcon);
			putValue(NAME, "Open Location...");
			putValue(SHORT_DESCRIPTION, "Open a workflow from the web");
		}

		/*
		 * (non-Javadoc)
		 * 
		 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
		 */
		public void actionPerformed(ActionEvent e) {
			Preferences prefs = Preferences.userNodeForPackage(OpenNestedWorkflowFromURLAction.class);
			String currentUrl = prefs
					.get("currentUrl", "http://");

			String url = (String) JOptionPane.showInputDialog(null,
					"Enter the URL of a workflow definition to load",
					"Workflow URL", JOptionPane.QUESTION_MESSAGE, null, null,
					currentUrl);
			if (url != null) {
				prefs.put("currentUrl", url);
				createNestedWorkflow(url, (Component) e.getSource());
			}
		}

	}

	private void createNestedWorkflow(String url, Component component) {
		try {
			String name = model.getValidProcessorName("Nested Workflow");
			WorkflowProcessor p = null;
			if (url == null) {
				p = new WorkflowProcessor(model, name);
			} else {
				p = new WorkflowProcessor(model, name, url);
			}
			model.addProcessor(p);
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(component,
					"Unable to create subworkflow : \n" + ex.getMessage(),
					"Error", JOptionPane.ERROR_MESSAGE);
		}

	}

}
