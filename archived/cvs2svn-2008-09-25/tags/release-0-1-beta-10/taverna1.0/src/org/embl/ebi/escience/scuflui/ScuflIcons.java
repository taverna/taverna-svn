/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import javax.swing.ImageIcon;

import java.lang.Class;
import java.lang.ClassNotFoundException;



/**
 * A container for the various icons used by the Scufl ui components
 * @author Tom Oinn
 */
public class ScuflIcons {

    public static ImageIcon inputIcon, outputIcon, inputPortIcon, outputPortIcon, dataLinkIcon, constraintIcon, deleteIcon, zoomIcon, webIcon, openIcon, runIcon, refreshIcon, editIcon, classIcon, selectedClassIcon, findIcon, folderOpenIcon, folderClosedIcon, newInputIcon, newListIcon, inputValueIcon, xmlNodeIcon, leafIcon, windowRun, windowScavenger, windowInput, windowDiagram, windowExplorer;

    static {
	// Load the image files found in this package into the class.
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflIcons");
	    inputPortIcon = new ImageIcon(c.getResource("inputport.gif"));
	    outputPortIcon = new ImageIcon(c.getResource("outputport.gif"));
	    dataLinkIcon = new ImageIcon(c.getResource("datalink.gif"));
	    inputIcon = new ImageIcon(c.getResource("png/input.png"));
	    outputIcon = new ImageIcon(c.getResource("png/output.png"));
	    constraintIcon = new ImageIcon(c.getResource("constraint.gif"));
	    deleteIcon = new ImageIcon(c.getResource("delete.gif"));
	    zoomIcon = new ImageIcon(c.getResource("zoom.gif"));
	    webIcon = new ImageIcon(c.getResource("web.gif"));
	    openIcon = new ImageIcon(c.getResource("open.gif"));
	    runIcon = new ImageIcon(c.getResource("run.gif"));
	    refreshIcon = new ImageIcon(c.getResource("refresh.gif"));
	    editIcon = new ImageIcon(c.getResource("edit.gif"));
	    classIcon = new ImageIcon(c.getResource("class.gif"));
	    selectedClassIcon = new ImageIcon(c.getResource("selectedclass.gif"));
	    findIcon = new ImageIcon(c.getResource("find.gif"));
	    folderOpenIcon = new ImageIcon(c.getResource("folder-open.png"));
	    folderClosedIcon = new ImageIcon(c.getResource("folder-closed.png"));
	    newInputIcon = new ImageIcon(c.getResource("newinput.gif"));
	    newListIcon = new ImageIcon(c.getResource("newlist.gif"));
	    inputValueIcon = new ImageIcon(c.getResource("inputValue.gif"));
	    xmlNodeIcon = new ImageIcon(c.getResource("xml_node.gif"));
	    leafIcon = new ImageIcon(c.getResource("leaf.gif"));
	    windowRun = new ImageIcon(c.getResource("windows/run.gif"));
	    windowScavenger = new ImageIcon(c.getResource("windows/scavenger.gif"));
	    windowInput = new ImageIcon(c.getResource("windows/input.gif"));
	    windowDiagram = new ImageIcon(c.getResource("windows/diagram.gif"));
	    windowExplorer = new ImageIcon(c.getResource("windows/advancedModel.gif"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}	
    }

}
