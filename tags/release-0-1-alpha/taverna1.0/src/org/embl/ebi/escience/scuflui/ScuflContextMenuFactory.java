/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;

import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import java.lang.Class;
import java.lang.ClassNotFoundException;
import java.lang.Object;
import java.lang.System;



/**
 * A static factory method to return an instance of JPopupMenu that is
 * appropriate to the supplied object. For instance, if you supply it
 * with a Processor implementation it will give options to view the processor
 * details, delete it etc.
 * @author Tom Oinn
 */
public class ScuflContextMenuFactory {
    
    static ImageIcon deleteIcon;

    /**
     * Load images for menus
     */
    static {
	try {
	    Class c = Class.forName("org.embl.ebi.escience.scuflui.ScuflContextMenuFactory");
	    deleteIcon = new ImageIcon(c.getResource("delete.gif"));
	}
	catch (ClassNotFoundException cnfe) {
	    //
	}
    }

    /**
     * Creates a JPopupMenu appropriate to the object supplied. If it
     * doesn't understand the object it's been given it will throw a 
     * NoContextMenuFoundException back at you.
     */
    public static JPopupMenu getMenuForObject(Object theObject) 
	throws NoContextMenuFoundException {
	if (theObject == null) {
	    throw new NoContextMenuFoundException("Supplied user object was null, giving up.");
	}
	// Trivial dummy implementation, only understands how to build processor menus.
	if (theObject instanceof Processor) {
	    return getProcessorMenu((Processor)theObject);
	}
	else if (theObject instanceof Port) {
	    /**
	     // Is the port a workflow source?
	     Port thePort = (Port)theObject;
	     if (thePort.isSource()) {
	     theMenu.add(new JMenuItem("Workflow source port"));
	     return theMenu;
	     }
	     else if (thePort.isSink()) {
	     theMenu.add(new JMenuItem("Workflow sink port"));
	     return theMenu;
	     }
	    */
	}
	
	throw new NoContextMenuFoundException("Didn't know how to create a context menu for a "+theObject.getClass().toString());
    }

    private static JPopupMenu getProcessorMenu(Processor processor) {
	final Processor theProcessor = processor;
	JPopupMenu theMenu = new JPopupMenu();
	JMenuItem title = new JMenuItem(theProcessor.getName());
	title.setEnabled(false);
	theMenu.add(title);
	JMenuItem delete = new JMenuItem("Remove from model",deleteIcon);
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    System.out.println("Deleting a node...");
		    theProcessor.getModel().destroyProcessor(theProcessor);
		}
	    });
	
	theMenu.add(delete);
	return theMenu;
    }
    
}

