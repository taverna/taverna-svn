/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import org.embl.ebi.escience.scufl.*;

import org.embl.ebi.escience.scuflui.LinkingMenus;
import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import java.lang.Exception;
import java.lang.Object;
import java.lang.String;



/**
 * A static factory method to return an instance of JPopupMenu that is
 * appropriate to the supplied object. For instance, if you supply it
 * with a Processor implementation it will give options to view the processor
 * details, delete it etc.
 * @author Tom Oinn
 */
public class ScuflContextMenuFactory {

    /**
     * Creates a JPopupMenu appropriate to the object supplied. If it
     * doesn't understand the object it's been given it will throw a 
     * NoContextMenuFoundException back at you.
     * This method need a handle on the model it's working with so it
     * can return sensible things if the object is a string appropriate
     * to some node in the tree, i.e. 'Processors'
     */
    public static JPopupMenu getMenuForObject(Object theObject, ScuflModel model) 
	throws NoContextMenuFoundException {
	if (theObject == null) {
	    throw new NoContextMenuFoundException("Supplied user object was null, giving up.");
	}
	if (theObject instanceof Processor) {
	    return getProcessorMenu((Processor)theObject);
	}
	else if (theObject instanceof Port) {
	    // Is the port a workflow source?
	    Port thePort = (Port)theObject;
	    if (thePort instanceof OutputPort) {
		return LinkingMenus.linkFrom(thePort);
	    }
	    else if (thePort instanceof InputPort) {
		// If this is a workflow sink, give the option to remove it.
		if (thePort.getProcessor() == model.getWorkflowSinkProcessor()) {
		    JPopupMenu theMenu = new JPopupMenu();
		    JMenuItem title = new JMenuItem("Workflow sink : "+thePort.getName());
		    final Port sinkPort = thePort;
		    theMenu.add(title);
		    title.setEnabled(false);
		    theMenu.addSeparator();
		    JMenuItem delete = new JMenuItem("Remove from model", ScuflIcons.deleteIcon);
		    delete.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				sinkPort.getProcessor().removePort(sinkPort);
			    }
			});
		    theMenu.add(delete);
		    return theMenu;
		}
	    }
	    
	}
	else if (theObject instanceof DataConstraint) {
	    return getDataConstraintMenu((DataConstraint)theObject, model);
	}
	else if (theObject instanceof ConcurrencyConstraint) {
	    return getConcurrencyConstraintMenu((ConcurrencyConstraint)theObject, model);
	}
	else if (theObject instanceof String) {
	    String choice = (String)theObject;
	    if (choice.equals("Workflow inputs")) {
		// Show menu to create a new workflow source
		JPopupMenu theMenu = new JPopupMenu();
		JMenuItem title = new JMenuItem("Workflow inputs");
		theMenu.add(title);
		title.setEnabled(false);
		theMenu.addSeparator();
		JMenuItem createInput = new JMenuItem("Create new input",ScuflIcons.inputIcon);
		theMenu.add(createInput);
		final ScuflModel theModel = model;
		createInput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    String name = (String)JOptionPane.showInputDialog(null,
									  "Name for the new workflow input?",
									  "Name required",
									  JOptionPane.QUESTION_MESSAGE,
									  null,
									  null,
									  "");
			    if (name != null) {
				try {
				    theModel.getWorkflowSourceProcessor().addPort(new OutputPort(theModel.getWorkflowSourceProcessor(), name));
				}
				catch (PortCreationException pce) {
				    JOptionPane.showMessageDialog(null,
								  "Port creation exception : \n"+pce.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
				catch (DuplicatePortNameException dpne) {
				    JOptionPane.showMessageDialog(null,
							      "Duplicate name : \n"+dpne.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
			    }
			}
			
		    });
		return theMenu;
	    }
	    else if (choice.equals("Workflow outputs")) {
		// Show menu to create a new workflow sink
		JPopupMenu theMenu = new JPopupMenu();
		JMenuItem title = new JMenuItem("Workflow outputs");
		theMenu.add(title);
		title.setEnabled(false);
		theMenu.addSeparator();
		JMenuItem createOutput = new JMenuItem("Create new output",ScuflIcons.outputIcon);
		theMenu.add(createOutput);
		final ScuflModel theModel = model;
		createOutput.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
			    String name = (String)JOptionPane.showInputDialog(null,
									      "Name for the new workflow output?",
									      "Name required",
									      JOptionPane.QUESTION_MESSAGE,
									      null,
									      null,
									      "");
			    if (name != null) {
				try {
				    theModel.getWorkflowSinkProcessor().addPort(new InputPort(theModel.getWorkflowSinkProcessor(), name));
				}
				catch (PortCreationException pce) {
				    JOptionPane.showMessageDialog(null,
								  "Port creation exception : \n"+pce.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
				catch (DuplicatePortNameException dpne) {
				    JOptionPane.showMessageDialog(null,
								  "Duplicate name : \n"+dpne.getMessage(),
								  "Exception!",
								  JOptionPane.ERROR_MESSAGE);
				}
			    }
			}
		    });
		return theMenu;
	    }
	}
	
	throw new NoContextMenuFoundException("Didn't know how to create a context menu for a "+theObject.getClass().toString());
    }

    private static JPopupMenu getDataConstraintMenu(DataConstraint dc, ScuflModel model) {
	final DataConstraint theConstraint = dc;
	final ScuflModel theModel = model;
	JPopupMenu theMenu = new JPopupMenu();
	JMenuItem title = new JMenuItem("Link : "+theConstraint.getName());
	title.setEnabled(false);
	theMenu.add(title);
	theMenu.addSeparator();
	JMenuItem delete = new JMenuItem("Remove from model", ScuflIcons.deleteIcon);
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    theModel.destroyDataConstraint(theConstraint);
		}
	    });
	theMenu.add(delete);
	return theMenu;
    }

    private static JPopupMenu getConcurrencyConstraintMenu(ConcurrencyConstraint cc, ScuflModel model) {
	final ConcurrencyConstraint theConstraint = cc;
	final ScuflModel theModel = model;
	JPopupMenu theMenu = new JPopupMenu();
	JMenuItem title = new JMenuItem("Coordination : "+theConstraint.getName());
	title.setEnabled(false);
	theMenu.add(title);
	theMenu.addSeparator();
	JMenuItem delete = new JMenuItem("Remove from model", ScuflIcons.deleteIcon);
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    theModel.destroyConcurrencyConstraint(theConstraint);
		}
	    });
	theMenu.add(delete);
	return theMenu;
    }

    private static JPopupMenu getProcessorMenu(Processor processor) {
	final Processor theProcessor = processor;
	JPopupMenu theMenu = new JPopupMenu();
	JMenuItem title = new JMenuItem("Processor : "+theProcessor.getName());
	title.setEnabled(false);
	theMenu.add(title);
	theMenu.addSeparator();
	JMenuItem delete = new JMenuItem("Remove from model", ScuflIcons.deleteIcon);
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    theProcessor.getModel().destroyProcessor(theProcessor);
		}
	    });
	// Provide a submenu to create a coordination constraint
	theMenu.add(delete);
	JMenuItem block = new JMenu("Coordinate from");
	block.setIcon(ScuflIcons.constraintIcon);
	theMenu.add(block);
	// Iterate over the processors in the model to get the available
	// gate processors.
	Processor[] gp = processor.getModel().getProcessors();
	for (int i = 0; i < gp.length; i++) {
	    // Doesn't make sense to block on self, will deadlock.
	    if (gp[i]!=processor) {
		JMenuItem gpi = new JMenuItem(gp[i].getName());
		if (gp[i] instanceof SoaplabProcessor) {
		    gpi.setIcon(ScuflIcons.soaplabIcon);
		}
		else if (gp[i] instanceof WSDLBasedProcessor) {
		    gpi.setIcon(ScuflIcons.wsdlIcon);
		}
		else if (gp[i] instanceof TalismanProcessor) {
		    gpi.setIcon(ScuflIcons.talismanIcon);
		}
		block.add(gpi);
		final Processor controller = gp[i];
		final Processor target = processor;
		final ScuflModel model = processor.getModel();
		gpi.addActionListener(new ActionListener() {
			// Create a new concurrency constraint
			public void actionPerformed(ActionEvent ae) {
			    String ccName = target.getName()+"_BLOCKON_"+controller.getName();
			    try {
				ConcurrencyConstraint cc = new ConcurrencyConstraint(model,
										     ccName,
										     controller,
										     target,
										     ConcurrencyConstraint.SCHEDULED,
										     ConcurrencyConstraint.RUNNING,
										     ConcurrencyConstraint.COMPLETED);
				model.addConcurrencyConstraint(cc);
			    }
			    catch (Exception e) {
				JOptionPane.showMessageDialog(null,
							      "Something wasn't happy : \n"+e.getMessage(),
							      "Exception!",
							      JOptionPane.ERROR_MESSAGE);
			    }
			}
		    });
		
	    }
	}
	return theMenu;
    }
    
}

