/**
 * 
 */
package net.sf.taverna.t2.drizzle.view.subset;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JMenu;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPopupMenu;


import org.apache.log4j.Logger;
import org.embl.ebi.escience.scufl.AlternateProcessor;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflWorkflowProcessorFactory;
import org.embl.ebi.escience.scufl.parser.XScuflParser;
import org.embl.ebi.escience.scuflui.TavernaIcons;
import org.embl.ebi.escience.scuflui.shared.ShadedLabel;
import org.embl.ebi.escience.scuflui.shared.UIUtils;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;
import org.embl.ebi.escience.scuflworkers.ProcessorHelper;
import org.jdom.Document;
import org.jdom.Element;

/**
 * @author alanrw
 * 
 */
public class ActivitySelectionPopupMenu extends JPopupMenu {

	/**
	 * 
	 */
	private static final long serialVersionUID = -7354581610284964171L;
	static Logger logger = Logger
			.getLogger(ActivitySelectionPopupMenu.class);

	/**
	 * @param pf
	 * @param parent
	 */
	public ActivitySelectionPopupMenu(final ProcessorFactory pf,
			final ActivitySubsetPanel parent) {
		super();
		if (pf == null) {
			throw new NullPointerException("pf cannot be null"); //$NON-NLS-1$
		}
		if (parent == null) {
			throw new NullPointerException("parent cannot be null"); //$NON-NLS-1$
		}
		this.setToolTipText("Select an action to perform for the chosen activity"); //$NON-NLS-1$
		this.setName(pf.getName());
		this.add(new ShadedLabel(pf.getName(), ShadedLabel.TAVERNA_GREEN));
		this.addSeparator();
		JMenuItem invokeItem = new JMenuItem("Invoke", TavernaIcons.windowRun); //$NON-NLS-1$
		invokeItem.setToolTipText("Invoke the current activity so it can be run in isolation"); //$NON-NLS-1$
		
		invokeItem.addActionListener(new ActivityInvocationListener(pf));
		this.add(invokeItem);

		final ScuflModel currentWorkflow = parent.getCurrentWorkflow();
		if (parent.getCurrentWorkflow() != null) {
			JMenuItem addItem = new JMenuItem("Add to model", //$NON-NLS-1$
					TavernaIcons.importIcon);
			addItem.setToolTipText("Add the current activity with a default name to the current workflow"); //$NON-NLS-1$
			this.addSeparator();
			this.add(addItem);
			addItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String defaultName = pf.getName();
					String validName = currentWorkflow
							.getValidProcessorName(defaultName);
					try {
						pf.createProcessor(validName, currentWorkflow);
					} catch (ProcessorCreationException pce) {
						logger.error("Problem creating processor", pce); //$NON-NLS-1$
						JOptionPane.showMessageDialog(null,
								"Processor creation exception : \n" //$NON-NLS-1$
										+ pce.getMessage(), "Exception!", //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					} catch (DuplicateProcessorNameException dpne) {
						logger.error("Problem creating processor", dpne); //$NON-NLS-1$
						JOptionPane.showMessageDialog(null,
								"Duplicate name : \n" + dpne.getMessage(), //$NON-NLS-1$
								"Exception!", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
					}
				}
			});

			JMenuItem addWithNameItem = new JMenuItem("Add to model with name...", //$NON-NLS-1$
					TavernaIcons.importIcon);
			addWithNameItem.setToolTipText("Add the current activity to the current workflow with a given name"); //$NON-NLS-1$
			this.add(addWithNameItem);
			addWithNameItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					String name = (String) JOptionPane.showInputDialog(null,
							"Name for the new processor?", "Name required",  //$NON-NLS-1$//$NON-NLS-2$
							JOptionPane.QUESTION_MESSAGE, null, null, ""); //$NON-NLS-1$
					if (name != null) {
						try {
							pf.createProcessor(name, currentWorkflow);
						} catch (ProcessorCreationException pce) {
							JOptionPane.showMessageDialog(null,
									"Processor creation exception : \n" //$NON-NLS-1$
											+ pce.getMessage(), "Exception!", //$NON-NLS-1$
									JOptionPane.ERROR_MESSAGE);
						} catch (DuplicateProcessorNameException dpne) {
							JOptionPane.showMessageDialog(null,
									"Duplicate name : \n" + dpne.getMessage(), //$NON-NLS-1$
									"Exception!", JOptionPane.ERROR_MESSAGE); //$NON-NLS-1$
						}
					}
				}
			});

			// Prepare the 'add as alternate menu'
			Processor[] processors = currentWorkflow.getProcessors();
			if (processors.length > 0) {
				JMenu processorList = new JMenu("Add as alternate to..."); //$NON-NLS-1$
				processorList.setToolTipText("Add the current activity to the current workflow as an alternative for another activity"); //$NON-NLS-1$
				for (int i = 0; i < processors.length; i++) {
					String name = processors[i].getName();
					JMenuItem processorItem = new JMenuItem(name, ProcessorHelper
							.getPreferredIcon(processors[i]));
					processorItem.setToolTipText("Add the current activity to the current workflow as an alterantive for " + name); //$NON-NLS-1$
					processorList.add(processorItem);
					final Processor theProcessor = processors[i];
					processorItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							try {
								int numberOfAlternates = theProcessor
										.getAlternatesArray().length;
								Processor alternateProcessor = pf
										.createProcessor("alternate" //$NON-NLS-1$
												+ (numberOfAlternates + 1),
												null);
								AlternateProcessor alternate = new AlternateProcessor(
										alternateProcessor);
								theProcessor.addAlternate(alternate);
								if (theProcessor.getModel() != null) {
									boolean isOffline = theProcessor.getModel()
											.isOffline();
									if (isOffline) {
										alternateProcessor.setOffline();
									} else {
										alternateProcessor.setOnline();
									}
								}
								// Set the appropriate offline /
								// online status

							} catch (Exception ex) {
								ex.printStackTrace();
								JOptionPane
										.showMessageDialog(null,
												"Problem creating alternate : \n" //$NON-NLS-1$
														+ ex.getMessage(),
												"Exception!", //$NON-NLS-1$
												JOptionPane.ERROR_MESSAGE);
							}
						}

					});
				}
				this.add(processorList);
			}
		}
		// If this is a workflow factory then we might as well give
		// the user the option to import the complete workflow as
		// well as to wrap it in a processor
		if (pf instanceof ScuflWorkflowProcessorFactory) {
			JMenuItem importWorkflowItem = new JMenuItem("Import workflow...", //$NON-NLS-1$
					TavernaIcons.webIcon);
			importWorkflowItem.setToolTipText("Import the activities within the specified workflow"); //$NON-NLS-1$
			final String definitionURL = ((ScuflWorkflowProcessorFactory) pf)
					.getDefinitionURL();
			final Element definitionElement = ((ScuflWorkflowProcessorFactory) pf)
					.getDefinition();
			importWorkflowItem.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent ae) {
					try {
						String prefix = (String) JOptionPane.showInputDialog(
								UIUtils.getActionEventParentWindow(ae),
								"Optional name prefix?", "Prefix",  //$NON-NLS-1$//$NON-NLS-2$
								JOptionPane.QUESTION_MESSAGE, null, null, ""); //$NON-NLS-1$
						if (prefix != null) {
							if (prefix.equals("")) { //$NON-NLS-1$
								prefix = null;
							}
							if (definitionURL != null) {
								XScuflParser.populate((new URL(definitionURL))
										.openStream(), currentWorkflow, prefix);
							} else {
								// Is a literal definition
								XScuflParser.populate(new Document(
										(Element) definitionElement.clone()),
										currentWorkflow, prefix);
							}
						}
					} catch (Exception ex) {
						JOptionPane.showMessageDialog(null,
								"Problem opening XScufl from web : \n" //$NON-NLS-1$
										+ ex.getMessage(), "Exception!", //$NON-NLS-1$
								JOptionPane.ERROR_MESSAGE);
					}
				}
			});
			this.add(importWorkflowItem);
		}

	}

}
