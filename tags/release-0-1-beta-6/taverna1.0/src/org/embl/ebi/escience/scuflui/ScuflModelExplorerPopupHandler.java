/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scufl.Processor;




/**
 * A class to handle popup menus on nodes on the ScuflModelExplorer tree
 * @author Tom Oinn
 */
public class ScuflModelExplorerPopupHandler extends MouseAdapter {
    
    ScuflModelExplorer explorer;
    
    public ScuflModelExplorerPopupHandler(ScuflModelExplorer theExplorer) {
	this.explorer = theExplorer;
    }
   
    /**
     * Handle the mouse pressed event in case this is the platform
     * specific trigger for a popup menu
     */
    public void mousePressed(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    doEvent(e);
	}
    }
    
    /**
     * Similarly handle the mouse released event
     */
    public void mouseReleased(MouseEvent e) {
	if (e.isPopupTrigger()) {
	    doEvent(e);
	}
    }

    /**
     * If the event was a trigger for a popup then use the ScuflContextMenuFactory
     * to find a suitable JPopupMenu for the node that was clicked on and display
     * it. If we couldn't find anything suitable do nothing, all it means is that
     * there wasn't a menu available for that type of node.
     */
    void doEvent(MouseEvent e) {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)(explorer.getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
	Object scuflObject = node.getUserObject();
	if (scuflObject != null) {
	    try {
		final MouseEvent theMouseEvent = e;
		JPopupMenu theMenu = ScuflContextMenuFactory.getMenuForObject(scuflObject, explorer.model);
		if (scuflObject instanceof Processor) {
		    // show the properties display
		    final Processor theProcessor = (Processor)scuflObject;
		    theMenu.addSeparator();
		    JMenuItem properties = new JMenuItem("Properties...");
		    properties.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent a) {
				ScuflProcessorInfo spi = new ScuflProcessorInfo(theProcessor);
			    }
			});
		    theMenu.add(properties);
		}
		theMenu.show(explorer, e.getX(), e.getY());
	    }
	    catch (NoContextMenuFoundException ncmfe) {
		// just means that there wasn't a suitable menu for the selected node.
	    }
	}
    }

}
