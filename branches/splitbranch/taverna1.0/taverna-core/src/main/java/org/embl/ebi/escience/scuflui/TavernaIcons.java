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
 * Filename           $RCSfile: TavernaIcons.java,v $
 * Revision           $Revision: 1.1.2.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-07-05 16:41:15 $
 *               by   $Author: davidwithers $
 * Created on 28-Jun-2006
 *****************************************************************/
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

/**
 * A container for the various icons used by the Taverna ui components
 * 
 * @author David Withers
 */
public class TavernaIcons {

	public static ImageIcon deleteIcon, zoomIcon, webIcon, openIcon, runIcon,
			refreshIcon, editIcon, inputIcon, savePNGIcon, outputIcon,
			inputPortIcon, outputPortIcon, dataLinkIcon, constraintIcon,
			findIcon, folderOpenIcon, folderClosedIcon, newInputIcon,
			newListIcon, inputValueIcon, xmlNodeIcon, leafIcon, windowRun,
			windowScavenger, windowInput, windowDiagram, windowExplorer,
			saveIcon, importIcon, openurlIcon, pauseIcon, playIcon, stopIcon,
			breakIcon, rbreakIcon, tickIcon, openMenuIcon, saveMenuIcon,
			databaseIcon, renameIcon;

	static {
		// Load the image files found in this package into the class.
		try {
			Class c = Class
					.forName("org.embl.ebi.escience.scuflui.TavernaIcons");
			inputPortIcon = new ImageIcon(c
					.getResource("icons/explorer/inputport.png"));
			outputPortIcon = new ImageIcon(c
					.getResource("icons/explorer/outputport.png"));
			dataLinkIcon = new ImageIcon(c
					.getResource("icons/explorer/datalink.gif"));
			inputIcon = new ImageIcon(c.getResource("icons/explorer/input.png"));
			outputIcon = new ImageIcon(c
					.getResource("icons/explorer/output.png"));
			constraintIcon = new ImageIcon(c
					.getResource("icons/explorer/constraint.gif"));
			deleteIcon = new ImageIcon(c
					.getResource("icons/generic/delete.gif"));
			zoomIcon = new ImageIcon(c.getResource("icons/generic/zoom.gif"));
			webIcon = new ImageIcon(c.getResource("icons/generic/web.gif"));
			openIcon = new ImageIcon(c.getResource("icons/generic/open.gif"));
			runIcon = new ImageIcon(c.getResource("icons/generic/run.gif"));
			refreshIcon = new ImageIcon(c
					.getResource("icons/generic/refresh.gif"));
			editIcon = new ImageIcon(c.getResource("icons/generic/edit.gif"));
			findIcon = new ImageIcon(c.getResource("icons/generic/find.gif"));
			folderOpenIcon = new ImageIcon(c
					.getResource("icons/generic/folder-open.png"));
			folderClosedIcon = new ImageIcon(c
					.getResource("icons/generic/folder-closed.png"));
			newInputIcon = new ImageIcon(c
					.getResource("icons/generic/newinput.gif"));
			newListIcon = new ImageIcon(c
					.getResource("icons/generic/newlist.gif"));
			inputValueIcon = new ImageIcon(c
					.getResource("icons/generic/inputValue.gif"));
			xmlNodeIcon = new ImageIcon(c
					.getResource("icons/generic/xml_node.gif"));
			leafIcon = new ImageIcon(c.getResource("icons/generic/leaf.gif"));
			windowRun = new ImageIcon(c.getResource("icons/windows/run.gif"));
			windowScavenger = new ImageIcon(c
					.getResource("icons/windows/scavenger.gif"));
			windowInput = new ImageIcon(c
					.getResource("icons/windows/input.gif"));
			windowDiagram = new ImageIcon(c
					.getResource("icons/windows/diagram.gif"));
			windowExplorer = new ImageIcon(c
					.getResource("icons/windows/advancedModel.gif"));
			saveIcon = new ImageIcon(c.getResource("icons/generic/save.gif"));
			saveMenuIcon = new ImageIcon(c
					.getResource("icons/generic/savemenu.gif"));
			savePNGIcon = new ImageIcon(c
					.getResource("icons/generic/savepng.gif"));
			importIcon = new ImageIcon(c
					.getResource("icons/generic/import.gif"));
			openurlIcon = new ImageIcon(c
					.getResource("icons/generic/openurl.gif"));
			openIcon = new ImageIcon(c.getResource("icons/generic/open.gif"));
			openMenuIcon = new ImageIcon(c
					.getResource("icons/generic/openmenu.gif"));
			pauseIcon = new ImageIcon(c.getResource("icons/generic/pause.gif"));
			playIcon = new ImageIcon(c.getResource("icons/generic/play.gif"));
			stopIcon = new ImageIcon(c.getResource("icons/generic/stop.gif"));
			breakIcon = new ImageIcon(c.getResource("icons/generic/break.gif"));
			rbreakIcon = new ImageIcon(c
					.getResource("icons/generic/rbreak.gif"));
			tickIcon = new ImageIcon(c.getResource("icons/generic/tick.gif"));
			renameIcon = new ImageIcon(c
					.getResource("icons/generic/rename.png"));
			databaseIcon = new ImageIcon(c
					.getResource("icons/generic/database.gif"));

		} catch (ClassNotFoundException cnfe) {
			//
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}
}
