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
import javax.swing.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ScavengerHelper;
import org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessorFactory;

// Utility Imports
import java.util.Iterator;
import java.util.Enumeration;
import java.util.List;
import java.util.ArrayList;

// Network Imports
import java.net.URL;

import org.embl.ebi.escience.scuflui.workbench.ScavengerCreationException;
import org.embl.ebi.escience.scuflui.workbench.ScavengerTree;
import org.embl.ebi.escience.scuflui.workbench.WebScavenger;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.apache.log4j.Logger;

import java.lang.Class;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;
import org.jdom.*;



/**
 * A class to handle popup menus on nodes on the ScavengerTree tree
 * @author Tom Oinn
 */
public class ScavengerTreePopupHandler extends MouseAdapter {
  private static Logger LOG = Logger.getLogger(ProcessorFactory.class);

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
    TreePath path = scavenger.getPathForLocation(e.getX(), e.getY());
    if(path != null)
    {
	final DefaultMutableTreeNode node = (DefaultMutableTreeNode)(path.getLastPathComponent());
	final DefaultTreeModel tmodel = (DefaultTreeModel)scavenger.getModel();
	Object scuflObject = node.getUserObject();
	if (scuflObject != null) {
	    boolean addDescribeOption = true;
	    JPopupMenu menu = new JPopupMenu();
	    // Create the menu item to fetch descriptions
	    JMenuItem getDescriptions = new JMenuItem("Fetch descriptions", ScuflIcons.zoomIcon);
	    getDescriptions.addActionListener(new ActionListener() {
		    public void actionPerformed(ActionEvent ae) {
			// Create a new thread to iterate over all child
			// items and fetch descriptions where possible
			new Thread() {
			    public void run() {
				Enumeration en = node.depthFirstEnumeration();
				List pflist = new ArrayList();
				List nodelist = new ArrayList();
				while (en.hasMoreElements()) {
				    DefaultMutableTreeNode tn = (DefaultMutableTreeNode)en.nextElement();
				    if (tn.getUserObject() instanceof ProcessorFactory) {
					pflist.add(tn.getUserObject());
					nodelist.add(tn);
				    }
				}
				Iterator j = nodelist.iterator();
				for (Iterator i = pflist.iterator(); i.hasNext();) {
				    ProcessorFactory pf = (ProcessorFactory)i.next();
				    TreeNode tnode = (TreeNode)j.next();
				    if (pf.getDescription() == null) {
					//System.out.println("  fetching for "+pf.toString());
					try {
					    String description = pf.createProcessor("foo",null).getDescription();
					    //System.out.println(description);
					    pf.setDescription(description);
					    //System.out.println(pf.getDescription());
					}
					catch (Exception ex) {
					    pf.setDescription("<font color=\"red\">Cannot fetch description!</font>");
					    ex.printStackTrace();
					}
					finally {
					    tmodel.nodeChanged(tnode);
					}
				    }
				}
			    }
			}.start();
		    }
		});


	    if (scuflObject instanceof ProcessorFactory && scavenger.model != null) {
		final ProcessorFactory pf = (ProcessorFactory)scuflObject;
		// Show the popup for adding new processors to the model
		JMenuItem add = new JMenuItem("Add to model", ScuflIcons.importIcon);
		menu.add(add);
		JMenuItem addWithName = new JMenuItem("Add to model with name...", ScuflIcons.importIcon);
		menu.add(addWithName);
		// Prepare the 'add as alternate menu'
		Processor[] processors = scavenger.model.getProcessors();
		if (processors.length > 0) {
		    JMenu processorList = new JMenu("Add as alternate to...");
		    for (int i = 0; i < processors.length; i++) {
			JMenuItem processorItem = new JMenuItem(processors[i].getName(), ProcessorHelper.getPreferredIcon(processors[i]));
			processorList.add(processorItem);
			final Processor theProcessor = processors[i];
			processorItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
				    try {
					int numberOfAlternates = theProcessor.getAlternatesArray().length;
					Processor alternateProcessor = pf.createProcessor("alternate"+(numberOfAlternates+1), null);
					AlternateProcessor alternate = new AlternateProcessor(alternateProcessor);
					theProcessor.addAlternate(alternate);
					if (theProcessor.getModel() != null) {
					    boolean isOffline = theProcessor.getModel().isOffline();
					    if (isOffline) {
						alternateProcessor.setOffline();
					    }
					    else {
						alternateProcessor.setOnline();
					    }
					}
					// Set the appropriate offline / online status
					
				    }
				    catch (Exception ex) {
					ex.printStackTrace();
					JOptionPane.showMessageDialog(null,
								      "Problem creating alternate : \n"+ex.getMessage(),
								      "Exception!",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}

			    });
		    }
		    menu.add(processorList);
		}

		// If this is a workflow factory then we might as well give
		// the user the option to import the complete workflow as
		// well as to wrap it in a processor
		if (scuflObject instanceof WorkflowProcessorFactory) {
		    JMenuItem imp = new JMenuItem("Import workflow...", ScuflIcons.webIcon);
		    final String definitionURL = ((WorkflowProcessorFactory)scuflObject).getDefinitionURL();
		    final Element definitionElement = (Element)((WorkflowProcessorFactory)scuflObject).getDefinition();
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
					if (definitionURL != null) {
					    XScuflParser.populate((new URL(definitionURL)).openStream(),
								  ScavengerTreePopupHandler.this.scavenger.model,
								  prefix);
					}
					else {
					    // Is a literal definition
					    XScuflParser.populate(new Document((Element)definitionElement.clone()), 
								  ScavengerTreePopupHandler.this.scavenger.model,
								  prefix);
					}
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
		addWithName.addActionListener(new ActionListener() {
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
		add.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    String defaultName = pf.getName();
			    String validName = ScavengerTreePopupHandler.this.scavenger.model.getValidProcessorName(defaultName);
			    try {
				pf.createProcessor(validName, ScavengerTreePopupHandler.this.scavenger.model);
			    }
			    catch (ProcessorCreationException pce) {
            LOG.error("Problen creating processor", pce);
				 JOptionPane.showMessageDialog(null,
								  "Processor creation exception : \n"+pce.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
			    }
			    catch (DuplicateProcessorNameException dpne) {
            LOG.error("Problen creating processor", dpne);
				JOptionPane.showMessageDialog(null,
							      "Duplicate name : \n"+dpne.getMessage(),
							      "Exception!",
							      JOptionPane.ERROR_MESSAGE);
			    }
			}
		    });
		//menu.show(scavenger, e.getX(), e.getY());
	    }
	    else if (scuflObject instanceof String) {
		// Catch the click on the 'Available Processors' text to add
		// a new scavenger
		String choice = (String)scuflObject;
		if (choice.equals("Available Processors")) {
		    addDescribeOption = false;
		    //JPopupMenu menu = new JPopupMenu();
		    menu.setLabel("Create new scavenger");
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
		    JMenuItem collect = new JMenuItem("Collect scavengers from model", ScuflIcons.importIcon);
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
		    menu.addSeparator();
		    JMenuItem showAllNodes = new JMenuItem("Expand all");
		    menu.add(showAllNodes);
		    showAllNodes.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				ScavengerTreePopupHandler.this.scavenger.setExpansion(true);
			    }
			});

		    JMenuItem hideAllNodes = new JMenuItem("Collapse all");
		    menu.add(hideAllNodes);
		    hideAllNodes.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
					scavenger.setExpansion(false);
					scavenger.expandPath(new TreePath(scavenger.treeModel.getRoot()));
				    }
				});
		    
		    //menu.show(scavenger, e.getX(), e.getY());

		}
		else {
		    // Wasn't the 'available processors' link, so give the option to remove it
		    if (choice.equals("Internal Services")==false && choice.equals("Local Java widgets")==false) {
			//JPopupMenu menu = new JPopupMenu();
			JMenuItem remove = new JMenuItem("Remove from tree", ScuflIcons.deleteIcon);
			final String scavengerName = choice;
			remove.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
				    DefaultTreeModel treeModel = (DefaultTreeModel)ScavengerTreePopupHandler.this.scavenger.treeModel;
				    MutableTreeNode treeNode = node;
				    treeModel.removeNodeFromParent(treeNode);
				    // Also remove the name from the scavenger list
				    ScavengerTreePopupHandler.this.scavenger.scavengerList.remove(scavengerName);
				}
			    });
			menu.add(remove);
			//menu.show(scavenger, e.getX(), e.getY());
		    }
		}
	    }
	    if (addDescribeOption == true) {
		menu.addSeparator();
		menu.add(getDescriptions);
	    }
	    menu.show(scavenger, e.getX(), e.getY());
	}
    }
    }

}
