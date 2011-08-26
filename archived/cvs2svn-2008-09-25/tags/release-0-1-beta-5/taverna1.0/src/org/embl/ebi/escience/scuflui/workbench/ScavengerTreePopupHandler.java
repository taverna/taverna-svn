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
import javax.swing.ImageIcon;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;

// Utility Imports
import java.util.Iterator;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.WebScavenger;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import java.lang.Class;
import java.lang.Exception;
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
		menu.add(add);
		// If this is a workflow factory then we might as well give
		// the user the option to import the complete workflow as 
		// well as to wrap it in a processor
		if (scuflObject instanceof WorkflowProcessorFactory) {
		    JMenuItem imp = new JMenuItem("Import workflow...", ScuflIcons.webIcon);
		    final String definitionURL = ((WorkflowProcessorFactory)scuflObject).getDefinitionURL();
		    imp.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				try {
				    String prefix = (String)JOptionPane.showInputDialog(null,
											"Optional name prefix?",
											"Prefix",
											JOptionPane.QUESTION_MESSAGE,
											null,
											null,
											"");
				    if (prefix != null) {
					if (prefix.equals("")) {
					    prefix = null;
					}
					XScuflParser.populate((new URL(definitionURL)).openStream(),
							      ScavengerTreePopupHandler.this.scavenger.model,
							      prefix);
				    }
				}
				catch (Exception ex) {
				    JOptionPane.showMessageDialog(null,
								  "Problem opening XScufl from web : \n"+ex.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
			    }
			});
		    menu.add(imp);
		}
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
		menu.show(scavenger, e.getX(), e.getY());
	    }
	    else if (scuflObject instanceof String) {
		// Catch the click on the 'Available Processors' text to add
		// a new scavenger
		String choice = (String)scuflObject;
		if (choice.equals("Available Processors")) {
		    JPopupMenu menu = new JPopupMenu();
		    JMenuItem title = new JMenuItem("Create new scavenger");
		    title.setEnabled(false);
		    menu.add(title);
		    menu.addSeparator();
		    // Iterate over the scavenger creator list from the ProcessorHelper class
		    for (Iterator i = ProcessorHelper.getScavengerToTagNames().keySet().iterator(); i.hasNext(); ) {
			String scavengerClassName = (String)i.next();
			ImageIcon scavengerIcon = ProcessorHelper.getIconForTagName((String)(ProcessorHelper.getScavengerToTagNames().get(scavengerClassName)));
			// Instantiate a ScavengerHelper...
			try {
			    Class scavengerHelperClass = Class.forName(scavengerClassName);
			    ScavengerHelper sh = (ScavengerHelper)scavengerHelperClass.newInstance();
			    String scavengerDescription = sh.getScavengerDescription();
			    JMenuItem scavengerMenuItem = new JMenuItem(scavengerDescription,scavengerIcon);
			    scavengerMenuItem.addActionListener(sh.getListener(ScavengerTreePopupHandler.this.scavenger));
			    menu.add(scavengerMenuItem);
			}
			catch (Exception ex) {
			    // Just for now...
			    ex.printStackTrace();
			}
		    }
		    JMenuItem addWeb = new JMenuItem("Collect scavengers from web...", ScuflIcons.webIcon);
		    menu.addSeparator();
		    menu.add(addWeb);
		    addWeb.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				String rootURL = (String)JOptionPane.showInputDialog(null,
										     "Address of the web page to crawl from?",
										     "Web root location",
										     JOptionPane.QUESTION_MESSAGE,
										     null,
										     null,
										     "http://cvs.mygrid.org.uk/scufl/");
				if (rootURL!=null) {
				    try {
					ScavengerTreePopupHandler.this.scavenger.addScavenger(new WebScavenger(rootURL, ScavengerTreePopupHandler.this.scavenger.treeModel));					
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
		    JMenuItem collect = new JMenuItem("Collect scavengers from model", Workbench.importIcon);
		    menu.add(collect);
		    collect.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				try {
				    ScavengerTreePopupHandler.this.scavenger.addScavengersFromModel();
				}
				catch (ScavengerCreationException sce) {
					JOptionPane.showMessageDialog(null,
								      "Unable to import scavengers!\n"+sce.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				}
			    }
			});
		    
		    menu.show(scavenger, e.getX(), e.getY());
		}
	    }
	}
    }

}
