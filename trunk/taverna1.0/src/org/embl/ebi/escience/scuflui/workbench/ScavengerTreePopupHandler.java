/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui.workbench;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;

import org.embl.ebi.escience.scuflui.workbench.ProcessorFactory;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import java.lang.Object;
import java.lang.String;



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
	if (scuflObject != null) {
	    
	    if (scuflObject instanceof ProcessorFactory && scavenger.model != null) {
		// Show the popup for adding new processors to the model
		JPopupMenu menu = new JPopupMenu();
		JMenuItem add = new JMenuItem("Add to model", Workbench.importIcon);
		final ProcessorFactory pf = (ProcessorFactory)scuflObject;
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    String name = (String)JOptionPane.showInputDialog(null,
									      "Name for the new processor?",
									      "Name required",
									      JOptionPane.QUESTION_MESSAGE,
									      null,
									      null,
									      "");
			    if (name != null) {
				try {
				    pf.createProcessor(name, ScavengerTreePopupHandler.this.scavenger.model);
				}
				catch (ProcessorCreationException pce) {
				    JOptionPane.showMessageDialog(null,
								  "Processor creation exception : \n"+pce.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
				catch (DuplicateProcessorNameException dpne) {
				    JOptionPane.showMessageDialog(null,
								  "Duplicate name : \n"+dpne.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
			    }
			}
		    });
		menu.add(add);
		menu.show(scavenger, e.getX(), e.getY());
	    }
	    else if (scuflObject instanceof String) {
		// Catch the click on the 'Available Processors' text to add
		// a new scavenger
		String choice = (String)scuflObject;
		if (choice.equals("Available Processors")) {
		    JPopupMenu menu = new JPopupMenu();
		    JMenuItem addSoaplab = new JMenuItem("Add new Soaplab scavenger...", ScavengerTreeRenderer.soaplabIcon);
		    JMenuItem addWSDL = new JMenuItem("Add new WSDL scavenger...", ScavengerTreeRenderer.wsdlIcon);
		    menu.add(addSoaplab);
		    menu.add(addWSDL);
		    addSoaplab.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				String baseURL = (String)JOptionPane.showInputDialog(null,
										     "Base location for your soaplab installation?",
										     "Soaplab location",
										     JOptionPane.QUESTION_MESSAGE,
										     null,
										     null,
										     "http://");
				if (baseURL!=null) {
				    try {
					ScavengerTreePopupHandler.this.scavenger.addScavenger(new SoaplabScavenger(baseURL));					
				    }
				    catch (ScavengerCreationException sce) {
					JOptionPane.showMessageDialog(null,
								      "Unable to create scavenger!\n"+sce.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}
			    }
			});
		    addWSDL.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				String wsdlLocation = (String)JOptionPane.showInputDialog(null,
											  "Address of the WSDL document?",
											  "WSDL location",
											  JOptionPane.QUESTION_MESSAGE,
											  null,
											  null,
											  "http://");
				if (wsdlLocation!=null) {
				    try {
					ScavengerTreePopupHandler.this.scavenger.addScavenger(new WSDLBasedScavenger(wsdlLocation));					
				    }
				    catch (ScavengerCreationException sce) {
					JOptionPane.showMessageDialog(null,
								      "Unable to create scavenger!\n"+sce.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}	
			    }
			});
		    menu.show(scavenger, e.getX(), e.getY());
		}
	    }
	}
    }

}
