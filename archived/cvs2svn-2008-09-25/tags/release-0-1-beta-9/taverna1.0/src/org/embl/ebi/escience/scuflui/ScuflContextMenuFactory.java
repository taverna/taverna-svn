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
import javax.swing.tree.*;
import java.util.*;
import org.embl.ebi.escience.scufl.*;
import org.embl.ebi.escience.scuflui.workbench.GenericUIComponentFrame;
import org.embl.ebi.escience.scuflui.workbench.Workbench;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;

import org.embl.ebi.escience.scuflui.LinkingMenus;
import org.embl.ebi.escience.scuflui.NoContextMenuFoundException;
import org.embl.ebi.escience.scuflui.ScuflIcons;
import org.embl.ebi.escience.scuflui.ScuflSemanticMarkupEditor;
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
    public static JPopupMenu getMenuForObject(DefaultMutableTreeNode theNode, Object theObject, ScuflModel model) 
	throws NoContextMenuFoundException {
	if (theObject == null) {
	    throw new NoContextMenuFoundException("Supplied user object was null, giving up.");
	}
	else if (theObject instanceof Processor) {
	    return getProcessorMenu((Processor)theObject);
	}
	else if (theObject instanceof AlternateProcessor) {
	    return getAlternateProcessorMenu((AlternateProcessor)theObject);
	}
	else if (theObject instanceof Port) {
	    // Check whether the port parent node is an AlternateProcessor
	    // in which case we display the mapping options
	    DefaultMutableTreeNode parent = (DefaultMutableTreeNode)theNode.getParent();
	    final Port thePort = (Port)theObject;
	    if (parent.getUserObject() instanceof AlternateProcessor) {
		final AlternateProcessor ap = (AlternateProcessor)parent.getUserObject();
		JPopupMenu theMenu = new JPopupMenu();
		JMenuItem title = new JMenuItem("Map port '"+thePort.getName()+"' to...");
		theMenu.add(title);
		title.setEnabled(false);
		theMenu.addSeparator();
		if (theObject instanceof OutputPort) {
		    // Fetch the original port names from the output ports on the
		    // original processor
		    OutputPort[] originalPorts = ap.getOriginalProcessor().getOutputPorts();
		    for (int i = 0; i < originalPorts.length; i++) {
			JMenuItem item = new JMenuItem(originalPorts[i].getName(), ScuflIcons.outputPortIcon);
			final Port originalPort = originalPorts[i]; 
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
				    ap.getOutputMapping().put(originalPort.getName(), thePort.getName());
				    ap.getOriginalProcessor().fireModelEvent(new MinorScuflModelEvent(ap.getProcessor(), "Port mapping changed"));
				}
			    });
			theMenu.add(item);
		    }
		}
		else {
		    InputPort[] originalPorts = ap.getOriginalProcessor().getInputPorts();
		    for (int i = 0; i < originalPorts.length; i++) {
			JMenuItem item = new JMenuItem(originalPorts[i].getName(), ScuflIcons.inputPortIcon);
			final Port originalPort = originalPorts[i]; 
			item.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
				    ap.getInputMapping().put(originalPort.getName(), thePort.getName());
				    ap.getOriginalProcessor().fireModelEvent(new MinorScuflModelEvent(ap.getProcessor(), "Port mapping changed"));
				}
			    });
			theMenu.add(item);
		    }
		}
		return theMenu;
		
	    }
	    // Is the port a workflow source?
	    //Port thePort = (Port)theObject;
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
		    
		    JMenuItem edit = new JMenuItem("Edit metadata...", ScuflIcons.editIcon);
		    edit.addActionListener(new ActionListener() {
			    public void actionPerformed(ActionEvent ae) {
				// Show the configuration panel here.
				if (Workbench.workbench != null) {
				    GenericUIComponentFrame thing = new GenericUIComponentFrame(Workbench.workbench.model,
												new ScuflSemanticMarkupEditor(sinkPort.getMetadata()));
				    thing.setSize(400,600);
				    thing.setLocation(100,100);
				    Workbench.workbench.desktop.add(thing);
				    thing.moveToFront();
				}
			    }
			});
		    theMenu.add(edit);
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
	// Check whether we have an appropriate editor available....
	String tagName = ProcessorHelper.getTagNameForClassName(theProcessor.getClass().getName());
	ProcessorEditor pe = ProcessorHelper.getEditorForTagName(tagName);
	if (pe != null) {
	    JMenuItem edit = new JMenuItem(pe.getEditorDescription(), ScuflIcons.editIcon);
	    edit.addActionListener(pe.getListener(theProcessor));
	    theMenu.add(edit);
	}
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
		gpi.setIcon(org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredIcon(gp[i]));
		block.add(gpi);
		final Processor controller = gp[i];
		final Processor target = processor;
		final ScuflModel model = processor.getModel();
		gpi.addActionListener(new ActionListener() {
			// Create a new concurrency constraint
			public void actionPerformed(ActionEvent ae) {
			    String ccName = target.getName()+"_BLOCKON_"+controller.getName();
			    try {
				// Constraints created by this menu are, for now, always
				// of the form 'block scheduled to running until completed',
				// as this is all the enactor can currently support.
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
    
    private static JPopupMenu getAlternateProcessorMenu(AlternateProcessor ap) {
	JPopupMenu theMenu = new JPopupMenu();
	JMenuItem title = new JMenuItem("Alternate processor");
	title.setEnabled(false);
	theMenu.add(title);
	theMenu.addSeparator();
	final Processor parentProcessor = ap.getOriginalProcessor();
	final AlternateProcessor alternate = ap;
	final Processor theProcessor = ap.getProcessor();
	// See whether we can configure this alternate
	String tagName = ProcessorHelper.getTagNameForClassName(theProcessor.getClass().getName());
	ProcessorEditor pe = ProcessorHelper.getEditorForTagName(tagName);
	if (pe != null) {
	    JMenuItem edit = new JMenuItem(pe.getEditorDescription(), ScuflIcons.editIcon);
	    edit.addActionListener(pe.getListener(theProcessor));
	    theMenu.add(edit);
	}
	// Always show the delete option
	JMenuItem delete = new JMenuItem("Remove this alternate",ScuflIcons.deleteIcon);
	delete.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent a) {
		    parentProcessor.removeAlternate(alternate);
		}
	    });
	theMenu.add(delete);
	// If there is more than one alternate then we show the 'promote/demote' options
	final int numberOfAlternates = parentProcessor.getAlternatesList().size();
	final int alternateIndex = parentProcessor.getAlternatesList().indexOf(alternate);
	if (numberOfAlternates > 1 && alternateIndex != -1) {
	    if (alternateIndex > 0) { 
		// Not the first item in the list so allow promotion
		JMenuItem promote = new JMenuItem("Promote alternate");
		promote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
			    // Swap this alternate with the one one lower in the list
			    List theList = parentProcessor.getAlternatesList();
			    Object o = theList.get(alternateIndex);
			    theList.remove(o);
			    theList.add(alternateIndex-1, o);
			    parentProcessor.fireModelEvent(new ScuflModelEvent(parentProcessor, "Alternates reordered"));
			}
		    });
		theMenu.add(promote);
	    }
	    if (alternateIndex < numberOfAlternates-1) {
		// Not the last item in the list so allow demotion
		JMenuItem demote = new JMenuItem("Demote alternate");
		demote.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
			    // Swap this alternate with the one one lower in the list
			    List theList = parentProcessor.getAlternatesList();
			    Object o = theList.get(alternateIndex);
			    theList.remove(o);
			    theList.add(alternateIndex+1, o);
			    parentProcessor.fireModelEvent(new ScuflModelEvent(parentProcessor, "Alternates reordered"));
			}
		    });
		theMenu.add(demote);
	    }
	}

	return theMenu;
    }

}

