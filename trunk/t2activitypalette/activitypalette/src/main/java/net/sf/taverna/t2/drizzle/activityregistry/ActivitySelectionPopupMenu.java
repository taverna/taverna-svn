/**
 * 
 */
package net.sf.taverna.t2.drizzle.activityregistry;

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

	private static Logger logger = Logger
			.getLogger(ActivitySelectionPopupMenu.class);

	public ActivitySelectionPopupMenu(final ProcessorFactory pf,
			final ActivityTabPanel parent) {
		super();
		this.setName(pf.getName());
		this.add(new ShadedLabel(pf.getName(), ShadedLabel.TAVERNA_GREEN));
		this.addSeparator();
		JMenuItem test = new JMenuItem("Invoke", TavernaIcons.windowRun);
		test.addActionListener(new ActivityInvocationListener(pf));
		this.add(test);

		final ScuflModel currentWorkflow = parent.getCurrentWorkflow();
		if (parent.getCurrentWorkflow() != null) {
			JMenuItem add = new JMenuItem("Add to model",
					TavernaIcons.importIcon);
			this.addSeparator();
			this.add(add);
			JMenuItem addWithName = new JMenuItem("Add to model with name...",
					TavernaIcons.importIcon);
			this.add(addWithName);
			// Prepare the 'add as alternate menu'
			Processor[] processors = currentWorkflow.getProcessors();
			if (processors.length > 0) {
				JMenu processorList = new JMenu("Add as alternate to...");
				for (int i = 0; i < processors.length; i++) {
					JMenuItem processorItem = new JMenuItem(processors[i]
							.getName(), ProcessorHelper
							.getPreferredIcon(processors[i]));
					processorList.add(processorItem);
					final Processor theProcessor = processors[i];
					processorItem.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							try {
								int numberOfAlternates = theProcessor
										.getAlternatesArray().length;
								Processor alternateProcessor = pf
										.createProcessor("alternate"
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
												"Problem creating alternate : \n"
														+ ex.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
						}

					});
				}
				this.add(processorList);

				// If this is a workflow factory then we might as well give
				// the user the option to import the complete workflow as
				// well as to wrap it in a processor
				if (pf instanceof ScuflWorkflowProcessorFactory) {
					JMenuItem imp = new JMenuItem("Import workflow...",
							TavernaIcons.webIcon);
					final String definitionURL = ((ScuflWorkflowProcessorFactory) pf)
							.getDefinitionURL();
					final Element definitionElement = (Element) ((ScuflWorkflowProcessorFactory) pf)
							.getDefinition();
					imp.addActionListener(new ActionListener() {
						public void actionPerformed(ActionEvent ae) {
							try {
								String prefix = (String) JOptionPane
										.showInputDialog(
												UIUtils
														.getActionEventParentWindow(ae),
												"Optional name prefix?",
												"Prefix",
												JOptionPane.QUESTION_MESSAGE,
												null, null, "");
								if (prefix != null) {
									if (prefix.equals("")) {
										prefix = null;
									}
									if (definitionURL != null) {
										XScuflParser.populate((new URL(
												definitionURL)).openStream(),
												currentWorkflow, prefix);
									} else {
										// Is a literal definition
										XScuflParser.populate(new Document(
												(Element) definitionElement
														.clone()),
												currentWorkflow, prefix);
									}
								}
							} catch (Exception ex) {
								JOptionPane
										.showMessageDialog(null,
												"Problem opening XScufl from web : \n"
														+ ex.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
						}
					});
					this.add(imp);
				}
				addWithName.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						String name = (String) JOptionPane.showInputDialog(
								null,
								"Name for the new processor?", "Name required",
								JOptionPane.QUESTION_MESSAGE, null, null, "");
						if (name != null) {
							try {
								pf.createProcessor(name, currentWorkflow);
							} catch (ProcessorCreationException pce) {
								JOptionPane
										.showMessageDialog(null,
												"Processor creation exception : \n"
														+ pce.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							} catch (DuplicateProcessorNameException dpne) {
								JOptionPane
										.showMessageDialog(null,
												"Duplicate name : \n"
														+ dpne.getMessage(),
												"Exception!",
												JOptionPane.ERROR_MESSAGE);
							}
						}
					}
				});
				add.addActionListener(new ActionListener() {
					public void actionPerformed(ActionEvent ae) {
						String defaultName = pf.getName();
						String validName = currentWorkflow
								.getValidProcessorName(defaultName);
						try {
							pf.createProcessor(validName, currentWorkflow);
						} catch (ProcessorCreationException pce) {
							logger.error("Problem creating processor", pce);
							JOptionPane.showMessageDialog(null,
									"Processor creation exception : \n"
											+ pce.getMessage(), "Exception!",
									JOptionPane.ERROR_MESSAGE);
						} catch (DuplicateProcessorNameException dpne) {
							logger.error("Problem creating processor", dpne);
							JOptionPane.showMessageDialog(null,
									"Duplicate name : \n" + dpne.getMessage(),
									"Exception!", JOptionPane.ERROR_MESSAGE);
						}
					}
				});
			}
		}

	}

}
