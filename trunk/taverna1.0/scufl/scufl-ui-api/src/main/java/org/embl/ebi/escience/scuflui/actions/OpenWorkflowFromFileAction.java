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
 * Filename           $RCSfile: OpenWorkflowFromFileAction.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-11-30 16:51:48 $
 *               by   $Author: stain $
 * Created on 20 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.io.File;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;
import org.embl.ebi.escience.scuflui.shared.WorkflowChanges;

/**
 * 
 * @author David Withers
 */
@SuppressWarnings("serial")
public class OpenWorkflowFromFileAction extends AbstractAction {

	private final JFileChooser fileChooser = new JFileChooser();
	private Component parentComponent;

	public OpenWorkflowFromFileAction(Component parentComponent) {
		putValue(SMALL_ICON, TavernaIcons.openIcon);
		putValue(NAME, "Open workflow ...");
		putValue(SHORT_DESCRIPTION, "Open a workflow from a file");
		this.parentComponent=parentComponent;
	}

	/*
	 * (non-Javadoc)
	 * 
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		final ScuflModel model = new ScuflModel();

		Preferences prefs = Preferences.userNodeForPackage(OpenWorkflowFromFileAction.class);
		String curDir = prefs
				.get("currentDir", System.getProperty("user.home"));
		fileChooser.setDialogTitle("Open Workflow");
		fileChooser.resetChoosableFileFilters();
		fileChooser.setFileFilter(new ExtensionFileFilter(
				new String[] { "xml" }));
		fileChooser.setCurrentDirectory(new File(curDir));
		int returnVal = fileChooser.showOpenDialog(parentComponent);
		if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fileChooser.getCurrentDirectory()
					.toString());
			final File file = fileChooser.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable() {
				public void run() {
					boolean workflowOpened = false;
					try {
						// todo: does the update need running in the AWT thread?
						// perhaps this thread should be spawned in populate?
						XScuflParser.populate(file.toURL().openStream(), model,
								null);
						workflowOpened = true;
					} catch (Exception ex) {
						model.clear();
						JOptionPane
								.showMessageDialog(
										parentComponent,
										"Problem opening workflow from file : \n\n"
												+ ex.getMessage()
												+ "\n\nLoading workflow in offline mode, "
												+ "this will allow you to remove any defunct operations.",
										"Error", JOptionPane.ERROR_MESSAGE);
						try {
							model.setOffline(true);
							XScuflParser.populate(file.toURL().openStream(),
									model, null);
							workflowOpened = true;
						} catch (Exception e) {
							JOptionPane.showMessageDialog(parentComponent,
									"Problem opening workflow from file : \n\n"
											+ ex.getMessage(), "Error",
									JOptionPane.ERROR_MESSAGE);
						}
					}
					if (workflowOpened) {
						ScuflModelSet.getInstance().addModel(model);
					}
					WorkflowChanges.getInstance().syncedWithFile(model, file);
				}
			}).start();
		}
	}

}
