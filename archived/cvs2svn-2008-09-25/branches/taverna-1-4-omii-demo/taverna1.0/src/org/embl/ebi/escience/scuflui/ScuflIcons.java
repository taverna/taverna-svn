/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

/**
 * A container for the various icons used by the Scufl ui components
 * 
 * @author Tom Oinn
 */
public class ScuflIcons {

	public static ImageIcon inputIcon, savePNGIcon, outputIcon, inputPortIcon,
			outputPortIcon, dataLinkIcon, constraintIcon, deleteIcon, zoomIcon,
			webIcon, openIcon, runIcon, refreshIcon, editIcon, classIcon,
			selectedClassIcon, findIcon, folderOpenIcon, folderClosedIcon,
			newInputIcon, newListIcon, inputValueIcon, xmlNodeIcon, leafIcon,
			windowRun, windowScavenger, windowInput, windowDiagram,
			windowExplorer, saveIcon, importIcon, openurlIcon, pauseIcon,
			playIcon, stopIcon, breakIcon, rbreakIcon, tickIcon, nullIcon,
			openMenuIcon, saveMenuIcon, databaseIcon, renameIcon;

	static {
		// Load the image files found in this package into the class.
		try {
			Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflIcons");
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
			classIcon = new ImageIcon(c
					.getResource("icons/semantics/class.gif"));
			selectedClassIcon = new ImageIcon(c
					.getResource("icons/semantics/selectedclass.gif"));
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
			nullIcon = new ImageIcon(new java.awt.image.BufferedImage(1, 1,
					java.awt.image.BufferedImage.TYPE_INT_RGB));

		} catch (ClassNotFoundException cnfe) {
			//
		} catch (Exception ex) {
			ex.printStackTrace();
			System.out.println(ex.toString());
		}
	}

}
