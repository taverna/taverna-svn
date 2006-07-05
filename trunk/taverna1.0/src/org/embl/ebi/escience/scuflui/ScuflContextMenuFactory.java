/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Iterator;
import java.util.List;

import javax.swing.ButtonGroup;
import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;
import javax.swing.JRadioButtonMenuItem;
import javax.swing.tree.DefaultMutableTreeNode;

import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.ConcurrencyConstraint;
import org.embl.ebi.escience.scufl.DataConstraint;
import org.embl.ebi.escience.scufl.InputPort;
import org.embl.ebi.escience.scufl.MinorScuflModelEvent;
import org.embl.ebi.escience.scufl.OutputPort;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scuflui.actions.AddInputAction;
import org.embl.ebi.escience.scuflui.actions.AddOutputAction;
import org.embl.ebi.escience.scuflui.actions.EditMetadataAction;
import org.embl.ebi.escience.scuflui.actions.RemoveAction;
import org.embl.ebi.escience.scuflui.actions.RenameAction;
import org.embl.ebi.escience.scuflui.actions.SetDefaultValueAction;
import org.embl.ebi.escience.scuflui.processoractions.ProcessorActionRegistry;
import org.embl.ebi.escience.scuflui.processoractions.ProcessorActionSPI;
import org.embl.ebi.escience.scuflworkers.ProcessorEditor;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.embl.ebi.escience.scuflworkers.ScuflContextMenuAware;

/**
 * A static factory method to return an instance of JPopupMenu that is
 * appropriate to the supplied object. For instance, if you supply it with a
 * Processor implementation it will give options to view the processor details,
 * delete it etc.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
public class ScuflContextMenuFactory {	

	/**
	 * Creates a JPopupMenu appropriate to the object supplied. If it doesn't
	 * understand the object it's been given it will throw a
	 * NoContextMenuFoundException back at you. This method need a handle on the
	 * model it's working with so it can return sensible things if the object is
	 * a string appropriate to some node in the tree, i.e. 'Processors'
	 */
	public static JPopupMenu getMenuForObject(DefaultMutableTreeNode theNode, Object theObject,
			ScuflModel methodSigModel) throws NoContextMenuFoundException {
		final ScuflModel model = methodSigModel;
		if (theObject == null) {
			throw new NoContextMenuFoundException("Supplied user object was null, giving up.");
		} else if (theObject instanceof Processor) {
			return getProcessorMenu((Processor) theObject);
		} else if (theObject instanceof AlternateProcessor) {
			return getAlternateProcessorMenu((AlternateProcessor) theObject);
		} else if (theObject instanceof Port) {
			JPopupMenu theMenu = new JPopupMenu();
			// Check whether the port parent node is an AlternateProcessor
			// in which case we display the mapping options
			final Port thePort = (Port) theObject;
			if (theNode != null) {
				DefaultMutableTreeNode parent = (DefaultMutableTreeNode) theNode.getParent();
				if (parent.getUserObject() instanceof AlternateProcessor) {
					final AlternateProcessor ap = (AlternateProcessor) parent.getUserObject();
					theMenu
							.add(new ShadedLabel("Map port '" + thePort.getName() + "' to...",
									ShadedLabel.TAVERNA_GREEN));
					theMenu.addSeparator();
					if (theObject instanceof OutputPort) {
						// Fetch the original port names from the output ports
						// on the
						// original processor
						OutputPort[] originalPorts = ap.getOriginalProcessor().getOutputPorts();
						for (int i = 0; i < originalPorts.length; i++) {
							JMenuItem item = new JMenuItem(originalPorts[i].getName(), TavernaIcons.outputPortIcon);
							final Port originalPort = originalPorts[i];
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									ap.getOutputMapping().put(originalPort.getName(), thePort.getName());
									ap.getOriginalProcessor().fireModelEvent(
											new MinorScuflModelEvent(ap.getProcessor(), "Port mapping changed"));
								}
							});
							theMenu.add(item);
						}
					} else {
						InputPort[] originalPorts = ap.getOriginalProcessor().getInputPorts();
						for (int i = 0; i < originalPorts.length; i++) {
							JMenuItem item = new JMenuItem(originalPorts[i].getName(), TavernaIcons.inputPortIcon);
							final Port originalPort = originalPorts[i];
							item.addActionListener(new ActionListener() {
								public void actionPerformed(ActionEvent ae) {
									ap.getInputMapping().put(originalPort.getName(), thePort.getName());
									ap.getOriginalProcessor().fireModelEvent(
											new MinorScuflModelEvent(ap.getProcessor(), "Port mapping changed"));
								}
							});
							theMenu.add(item);
						}
					}
					return theMenu;
				}
			}
			// Is the port a workflow source?
			// Port thePort = (Port)theObject;
			if (thePort instanceof OutputPort) {
				theMenu = createMenuForOutputPort(model, (OutputPort) thePort);
				return theMenu;
			} else if (thePort instanceof InputPort) {
				theMenu = createMenuForInputPort(model, (InputPort) thePort);
				return theMenu;
			}

		} else if (theObject instanceof DataConstraint) {
			return getDataConstraintMenu((DataConstraint) theObject, model);
		} else if (theObject instanceof ConcurrencyConstraint) {
			return getConcurrencyConstraintMenu((ConcurrencyConstraint) theObject, model);
		} else if (theObject instanceof String) {
			String choice = (String) theObject;
			if (choice.equals("Workflow inputs")) {
				// Show menu to create a new workflow source
				JPopupMenu theMenu = new JPopupMenu();
				theMenu.add(new ShadedLabel("Workflow inputs", ShadedLabel.TAVERNA_GREEN));
				theMenu.addSeparator();
				theMenu.add(new AddInputAction(model));
				return theMenu;
			} else if (choice.equals("Workflow outputs")) {
				// Show menu to create a new workflow sink
				JPopupMenu theMenu = new JPopupMenu();
				theMenu.add(new ShadedLabel("Workflow outputs", ShadedLabel.TAVERNA_GREEN));
				theMenu.addSeparator();
				theMenu.add(new AddOutputAction(model));
				return theMenu;
			}
		}

		throw new NoContextMenuFoundException("Didn't know how to create a context menu for a "
				+ theObject.getClass().toString());
	}

	private static JPopupMenu createMenuForOutputPort(final ScuflModel model, final OutputPort theOutputPort) {
		JPopupMenu theMenu = LinkingMenus.linkFrom(theOutputPort);
		if (theOutputPort instanceof ScuflContextMenuAware) {
			List<JMenuItem> items = ((ScuflContextMenuAware) theOutputPort)
					.contextMenuItems();
			if (items.size() > 0)
				theMenu.addSeparator();
			for (JMenuItem item : items) {
				theMenu.add(item);
			}
		}
		return theMenu;
	}

	private static JPopupMenu createMenuForInputPort(final ScuflModel model, final InputPort theInputPort) {
		// Workflow sink or processor inputs have the option to toggle
		// between NDSELECT and MERGE
		// operation when there's more than one input link to the port
		final JPopupMenu theMenu = new JPopupMenu();

		// If this is a workflow sink, give the option to remove it.
		if (theInputPort.getProcessor() == model.getWorkflowSinkProcessor()) {
			theMenu.add(new ShadedLabel("Workflow Output : " + theInputPort.getName(), ShadedLabel.TAVERNA_GREEN));
			final Port sinkPort = theInputPort;
			theMenu.add(new RenameAction(model, sinkPort));
			theMenu.add(new RemoveAction(model, sinkPort));
			theMenu.addSeparator();
			theMenu.add(new EditMetadataAction(model, sinkPort));
			theMenu.addSeparator();
		} else {
			theMenu.add(new ShadedLabel("Input port : " + theInputPort.getName(), ShadedLabel.TAVERNA_GREEN));
			if (theInputPort.isNameEditable()) {
				// ie. dynamic ports from Rserve and its like
				theMenu.add(new RenameAction(model, theInputPort));
				// FIXME: Check also isRemovable !
				theMenu.add(new RemoveAction(model, theInputPort));
			}
			if (theInputPort.getProcessor().getModel() != null) {
				theMenu.add(new SetDefaultValueAction(model, (InputPort) theInputPort));
			}
			if (theInputPort.getProcessor().getModel() != null && ((InputPort) theInputPort).hasDefaultValue()) {
				// theMenu.add(new ShadedLabel("Input port :
				// "+thePort.getName(), ShadedLabel.TAVERNA_GREEN));
				final InputPort ip = (InputPort) theInputPort;
				JMenuItem removeDefault = new JMenuItem("Remove default '" + ip.getDefaultValue() + "'",
						TavernaIcons.editIcon);
				removeDefault.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						ip.setDefaultValue(null);
					}
				});
				theMenu.add(removeDefault);
			}
		}

		// Add menu items to specify merge behaviour, whether to merge
		// the incoming links
		// or perform the pre 1.3.1 default of a non deterministic
		// selection
		theMenu.addSeparator();
		theMenu.add(new ShadedLabel("Incoming links...", ShadedLabel.TAVERNA_BLUE));
		theMenu.addSeparator();

		final ButtonGroup mergeGroup = new ButtonGroup();
		JRadioButtonMenuItem mergeItem = new JRadioButtonMenuItem("Merge all data");
		mergeItem.setSelected(theInputPort.getMergeMode() == InputPort.MERGE);
		mergeItem.setActionCommand("merge");
		JRadioButtonMenuItem selectItem = new JRadioButtonMenuItem("Select first link");
		selectItem.setSelected(theInputPort.getMergeMode() == InputPort.NDSELECT);
		selectItem.setActionCommand("select");
		mergeGroup.add(mergeItem);
		mergeGroup.add(selectItem);
		theMenu.add(mergeItem);
		theMenu.add(selectItem);
		ActionListener listener = new ActionListener() {
			public void actionPerformed(ActionEvent ae) {
				if (ae.getActionCommand().equals("merge")) {
					theInputPort.setMergeMode(InputPort.MERGE);
				} else {
					theInputPort.setMergeMode(InputPort.NDSELECT);
				}
			}
		};
		mergeItem.addActionListener(listener);
		selectItem.addActionListener(listener);

		if (theInputPort instanceof ScuflContextMenuAware) {
			List<JMenuItem> items = ((ScuflContextMenuAware) theInputPort)
					.contextMenuItems();
			if (items.size() > 0)
				theMenu.addSeparator();
			for (JMenuItem item : items) {
				theMenu.add(item);
			}
		}
		return theMenu;
	}

	private static JPopupMenu getDataConstraintMenu(DataConstraint dc, ScuflModel model) {
		JPopupMenu theMenu = new JPopupMenu();
		theMenu.add(new ShadedLabel("Link : " + dc.getName(), ShadedLabel.TAVERNA_GREEN));
		theMenu.addSeparator();
		theMenu.add(new RemoveAction(model, dc));
		return theMenu;
	}

	private static JPopupMenu getConcurrencyConstraintMenu(ConcurrencyConstraint cc, ScuflModel model) {
		JPopupMenu theMenu = new JPopupMenu();
		theMenu.add(new ShadedLabel("Coordination : " + cc.getName(), ShadedLabel.TAVERNA_GREEN));
		theMenu.addSeparator();
		theMenu.add(new RemoveAction(model, cc));
		return theMenu;
	}

	private static JPopupMenu getProcessorMenu(Processor processor) {
		final Processor theProcessor = processor;
		JPopupMenu theMenu = new JPopupMenu();
		theMenu.add(new ShadedLabel("Processor : " + theProcessor.getName(), ShadedLabel.TAVERNA_GREEN));
		theMenu.addSeparator();
		theMenu.add(new RenameAction(processor.getModel(), processor));
		theMenu.add(new RemoveAction(processor.getModel(), processor));
		theMenu.addSeparator();
		// Check whether we have an appropriate editor available....
		String tagName = ProcessorHelper.getTagNameForClassName(theProcessor.getClass().getName());
		ProcessorEditor pe = ProcessorHelper.getEditorForTagName(tagName);
		if (pe != null) {
			JMenuItem edit = new JMenuItem(pe.getEditorDescription(), TavernaIcons.editIcon);
			edit.addActionListener(pe.getListener(theProcessor));
			theMenu.add(edit);
		}

		// Use the ProcessorActionSPI to add new items from there (will
		// supercede the above code
		// for editors as well when I get around to porting them across)
		for (Iterator i = ProcessorActionRegistry.instance().getActions(processor).iterator(); i.hasNext();) {
			ProcessorActionSPI spi = (ProcessorActionSPI) i.next();
			JMenuItem item;
			if (spi.getIcon() != null) {
				item = new JMenuItem(spi.getDescription(), spi.getIcon());
			} else {
				item = new JMenuItem(spi.getDescription());
			}
			item.addActionListener(spi.getListener(processor));
			theMenu.add(item);
		}

		// Add breakpoint to the processor.
		final JMenuItem addBreakpoint = new JMenuItem("Add breakpoint", TavernaIcons.breakIcon);
		addBreakpoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				theProcessor.addBreakpoint();
			}
		});

		// Remove breakpoint from the processor.
		final JMenuItem rmvBreakpoint = new JMenuItem("Remove breakpoint", TavernaIcons.rbreakIcon);
		rmvBreakpoint.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				theProcessor.rmvBreakpoint();
			}
		});
		if (theProcessor.hasBreakpoint()) {
			theMenu.add(rmvBreakpoint);
		} else {
			theMenu.add(addBreakpoint);
		}

		JMenuItem block = new JMenu("Coordinate from");
		block.setIcon(TavernaIcons.constraintIcon);
		// Iterate over the processors in the model to get the available
		// gate processors.
		Processor[] gp = processor.getModel().getProcessors();
		if (gp.length > 1) {
			theMenu.add(block);
			((JMenu) block).add(new ShadedLabel("Processors", ShadedLabel.TAVERNA_ORANGE));
			((JMenu) block).addSeparator();
		}

		for (int i = 0; i < gp.length; i++) {
			// Doesn't make sense to block on self, will deadlock.
			if (gp[i] != processor) {
				JMenuItem gpi = new JMenuItem(gp[i].getName());
				gpi.setIcon(org.embl.ebi.escience.scuflworkers.ProcessorHelper.getPreferredIcon(gp[i]));
				block.add(gpi);
				final Processor controller = gp[i];
				final Processor target = processor;
				final ScuflModel model = processor.getModel();
				gpi.addActionListener(new ActionListener() {
					// Create a new concurrency constraint
					public void actionPerformed(ActionEvent ae) {
						String ccName = target.getName() + "_BLOCKON_" + controller.getName();
						try {
							// Constraints created by this menu are, for now,
							// always
							// of the form 'block scheduled to running until
							// completed',
							// as this is all the enactor can currently support.
							ConcurrencyConstraint cc = new ConcurrencyConstraint(model, ccName, controller, target,
									ConcurrencyConstraint.SCHEDULED, ConcurrencyConstraint.RUNNING,
									ConcurrencyConstraint.COMPLETED);
							model.addConcurrencyConstraint(cc);
						} catch (Exception e) {
							JOptionPane.showMessageDialog(null, "Something wasn't happy : \n" + e.getMessage(),
									"Exception!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});

			}
		}
		return theMenu;
	}

	private static JPopupMenu getAlternateProcessorMenu(AlternateProcessor ap) {
		JPopupMenu theMenu = new JPopupMenu();
		theMenu.add(new ShadedLabel("Alternate processor", ShadedLabel.TAVERNA_GREEN));
		theMenu.addSeparator();
		final Processor parentProcessor = ap.getOriginalProcessor();
		final AlternateProcessor alternate = ap;
		final Processor theProcessor = ap.getProcessor();
		// See whether we can configure this alternate
		String tagName = ProcessorHelper.getTagNameForClassName(theProcessor.getClass().getName());
		ProcessorEditor pe = ProcessorHelper.getEditorForTagName(tagName);
		if (pe != null) {
			JMenuItem edit = new JMenuItem(pe.getEditorDescription(), TavernaIcons.editIcon);
			edit.addActionListener(pe.getListener(theProcessor));
			theMenu.add(edit);
		}
		// Always show the delete option
		JMenuItem delete = new JMenuItem("Remove this alternate", TavernaIcons.deleteIcon);
		delete.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent a) {
				parentProcessor.removeAlternate(alternate);
			}
		});
		theMenu.add(delete);
		// If there is more than one alternate then we show the 'promote/demote'
		// options
		final int numberOfAlternates = parentProcessor.getAlternatesList().size();
		final int alternateIndex = parentProcessor.getAlternatesList().indexOf(alternate);
		if (numberOfAlternates > 1 && alternateIndex != -1) {
			if (alternateIndex > 0) {
				// Not the first item in the list so allow promotion
				JMenuItem promote = new JMenuItem("Promote alternate");
				promote.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent a) {
						// Swap this alternate with the one one lower in the
						// list
						List theList = parentProcessor.getAlternatesList();
						Object o = theList.get(alternateIndex);
						theList.remove(o);
						theList.add(alternateIndex - 1, o);
						parentProcessor.fireModelEvent(new ScuflModelEvent(parentProcessor, "Alternates reordered"));
					}
				});
				theMenu.add(promote);
			}
			if (alternateIndex < numberOfAlternates - 1) {
				// Not the last item in the list so allow demotion
				JMenuItem demote = new JMenuItem("Demote alternate");
				demote.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent a) {
						// Swap this alternate with the one one lower in the
						// list
						List theList = parentProcessor.getAlternatesList();
						Object o = theList.get(alternateIndex);
						theList.remove(o);
						theList.add(alternateIndex + 1, o);
						parentProcessor.fireModelEvent(new ScuflModelEvent(parentProcessor, "Alternates reordered"));
					}
				});
				theMenu.add(demote);
			}
		}

		return theMenu;
	}

}
