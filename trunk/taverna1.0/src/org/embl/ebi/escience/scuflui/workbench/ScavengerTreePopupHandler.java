/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.*;
import java.awt.event.*;

import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.ScuflContextMenuFactory;
import org.embl.ebi.escience.scuflui.ScuflModelExplorer;
import java.lang.Object;



/**
 * A class to handle popup menus on nodes on the ScavengerTree tree
 * @author Tom Oinn
 */
public class ScavengerTreePopupHandler extends MouseAdapter {
    
    private ScavengerTree scavenger;
    
    public ScavengerTreePopupHandler(ScavengerTree theTree) {
	this.scavenger = theTree;
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
     * If the popup was over a ProcessorFactory implementation then present the 'add'
     * option to the user
     */
    void doEvent(MouseEvent e) {
	DefaultMutableTreeNode node = (DefaultMutableTreeNode)(scavenger.getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
	Object scuflObject = node.getUserObject();
	if (scavenger.model != null && 
	    scuflObject != null && 
	    scuflObject instanceof ProcessorFactory) {
	    
	    JPopupMenu menu = new JPopupMenu();
	    JMenuItem add = new JMenuItem("Add to model");
	    final ProcessorFactory pf = (ProcessorFactory)scuflObject;
	    add.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			String name = pf.toString()+"_"+ScavengerTreePopupHandler.this.scavenger.getNextCount();
			try {
			    pf.createProcessor(name, ScavengerTreePopupHandler.this.scavenger.model);
			}
			catch (Exception ex) {
			    throw new RuntimeException(ex.getMessage());
			}
		    }
		});
	    menu.add(add);
	    menu.show(scavenger, e.getX(), e.getY());
	}
    }

}
