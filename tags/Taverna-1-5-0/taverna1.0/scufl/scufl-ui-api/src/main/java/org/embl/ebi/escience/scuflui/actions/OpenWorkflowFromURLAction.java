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
 * Filename           $RCSfile: OpenWorkflowFromURLAction.java,v $
 * Revision           $Revision: 1.4 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-12-08 16:44:14 $
 *               by   $Author: stain $
 * Created on 20 Nov 2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui.actions;

import java.awt.Component;
import java.awt.event.ActionEvent;
import java.net.URL;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.JOptionPane;

import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ScuflModelSet;

/**
 *
 * @author David Withers
 */
@SuppressWarnings("serial")
public class OpenWorkflowFromURLAction extends AbstractAction {

	private Component parentComponent;
	
	public OpenWorkflowFromURLAction(Component parentComponent) {
		putValue(SMALL_ICON, TavernaIcons.openurlIcon);
		putValue(NAME, "Open workflow location ...");
		putValue(SHORT_DESCRIPTION, "Open a workflow from the web");
		this.parentComponent = parentComponent;
	}

	/* (non-Javadoc)
	 * @see java.awt.event.ActionListener#actionPerformed(java.awt.event.ActionEvent)
	 */
	public void actionPerformed(ActionEvent e) {
		ScuflModel model = new ScuflModel();

		Preferences prefs = Preferences.userNodeForPackage(OpenWorkflowFromURLAction.class);
		String currentUrl = prefs
				.get("currentUrl", "http://");
		
		try {
			String url = (String) JOptionPane.showInputDialog(parentComponent,
					"Enter the URL of a workflow definition to load",
					"Workflow URL", JOptionPane.QUESTION_MESSAGE, null, null,
					currentUrl);
			if (url != null) {
				prefs.put("currentUrl", url);
				// FIXME: Avoid tight coupling, refactor openFromURL to some common class
				new OpenWorkflowFromFileAction(parentComponent).openFromURL(new URL(url));
			}
		} catch (Exception ex) {
			JOptionPane.showMessageDialog(parentComponent,
					"Problem opening workflow from web : \n" + ex.getMessage(),
					"Error!", JOptionPane.ERROR_MESSAGE);
		}
	}

}
