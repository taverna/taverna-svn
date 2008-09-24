package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.GridLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.prefs.Preferences;

import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.JToolBar;
import javax.swing.JTree;
import javax.swing.ListSelectionModel;
import javax.swing.event.DocumentEvent;
import javax.swing.event.DocumentListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.LSIDProvider;
import org.embl.ebi.escience.scufl.IterationStrategy;
import org.embl.ebi.escience.scufl.MinorScuflModelEvent;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scufl.ScuflModelEvent;
import org.embl.ebi.escience.scufl.ScuflModelEventListener;
import org.embl.ebi.escience.scufl.SemanticMarkup;
import org.embl.ebi.escience.scufl.WorkflowDescription;
import org.embl.ebi.escience.scufl.view.WorkflowSummaryAsHTML;
import org.embl.ebi.escience.scuflui.actions.OfflineToggleModel;
import org.embl.ebi.escience.scuflui.actions.ScuflModelActionRegistry;
import org.embl.ebi.escience.scuflui.actions.ScuflModelActionSPI;
import org.embl.ebi.escience.scuflui.shared.ExtensionFileFilter;
import org.embl.ebi.escience.scuflui.spi.WorkflowModelViewSPI;
import org.embl.ebi.escience.scuflui.treeview.ScuflModelTreeTable;

/**
 * An amalgam of the ScuflModelExplorerTreeTable and the
 * IterationStrategyControl panels linked such that selection of a Processor
 * node in the model explorer leads to context sensitive options appearing in a
 * properties tab.
 * 
 * @author Tom Oinn
 * @author Stuart Owen
 */
@SuppressWarnings("serial")
public class AdvancedModelExplorer extends JPanel implements
		WorkflowModelViewSPI {

	private static Logger logger = Logger
			.getLogger(AdvancedModelExplorer.class);
	
	private ScuflModelTreeTable explorer;

	private JTabbedPane tabs;

	private JPanel propertiesPanel;

	private Object selectedObject = null;

	private ScuflModel model;

	protected JCheckBox workOffline;

	final JFileChooser fc = new JFileChooser();

	/**
	 * Called when a tree selection has been processed, inserting the
	 * appropriate context object into the selectedObject value
	 */
	private void updateTab() {
		if (selectedObject instanceof ScuflModel) {
			updateTabForWorkflow();
		} else if (selectedObject instanceof Processor) {
			updateTabForProcessor((Processor) selectedObject);
		} else if (selectedObject instanceof String) {
			if (((String) selectedObject).equals("Processors")) {
				updateTabForSummary();
			} else {
				tabs.setEnabledAt(1, false);
				tabs.setIconAt(1, null);
				tabs.setTitleAt(1, "");
			}
		} else if (selectedObject instanceof Port) {
			Port p = (Port) selectedObject;
			if (p.isSource() || p.isSink()) {
				SemanticMarkup m = p.getMetadata();
				propertiesPanel.removeAll();
				propertiesPanel.setLayout(new BoxLayout(propertiesPanel,
						BoxLayout.PAGE_AXIS));
				propertiesPanel.add(new ScuflSemanticMarkupEditor(m),
						BorderLayout.CENTER);
				tabs.setEnabledAt(1, true);
				tabs.setTitleAt(1, "Metadata for '" + p.getName() + "'");
				tabs.setIconAt(1, (p.isSource()) ? TavernaIcons.inputIcon
						: TavernaIcons.outputIcon);
			} else {
				tabs.setEnabledAt(1, false);
				tabs.setIconAt(1, null);
				tabs.setTitleAt(1, "");
			}
		} else {
			tabs.setEnabledAt(1, false);
			tabs.setIconAt(1, null);
			tabs.setTitleAt(1, "");
		}
	}

	private void updateTabForSummary() {
		propertiesPanel.removeAll();
		propertiesPanel.setLayout(new BoxLayout(propertiesPanel,
				BoxLayout.PAGE_AXIS));
		final String htmlSummary = WorkflowSummaryAsHTML.getSummary(model);
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		toolbar.setBorderPainted(true);
		JButton saveHTML = new JButton(TavernaIcons.saveIcon);
		saveHTML.setPreferredSize(new Dimension(25, 25));
		toolbar.add(new JLabel(" Save HTML description "));
		toolbar.add(saveHTML);
		propertiesPanel.add(toolbar, BorderLayout.PAGE_START);
		saveHTML.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				try {
					Preferences prefs = Preferences
							.userNodeForPackage(AdvancedModelExplorer.class);
					String curDir = prefs.get("currentDir", System
							.getProperty("user.home"));
					fc.resetChoosableFileFilters();
					fc.setFileFilter(new ExtensionFileFilter(new String[] {
							"html", "htm" }));
					fc.setCurrentDirectory(new File(curDir));
					int returnVal = fc
							.showSaveDialog(AdvancedModelExplorer.this);
					if (returnVal == JFileChooser.APPROVE_OPTION) {
						prefs.put("currentDir", fc.getCurrentDirectory()
								.toString());
						File file = fc.getSelectedFile();
						PrintWriter out = new PrintWriter(new FileWriter(file));
						out.println(htmlSummary);
						out.flush();
						out.close();
					}
				} catch (Exception ex) {
					JOptionPane.showMessageDialog(AdvancedModelExplorer.this,
							"Problem saving workflow : \n" + ex.getMessage(),
							"Error!", JOptionPane.ERROR_MESSAGE);
				}
			}
		});
		JEditorPane ed = new JEditorPane("text/html", htmlSummary);
		ed.setEditable(false);
		JScrollPane edPane = new JScrollPane(ed);
		propertiesPanel.add(edPane);
		edPane.setPreferredSize(new Dimension(100, 100));
		tabs.setEnabledAt(1, true);
		tabs.setIconAt(1, TavernaIcons.openurlIcon);
		tabs.setTitleAt(1, "Remote resource usage");
	}

	private void updateTabForWorkflow() {
		propertiesPanel.removeAll();
		propertiesPanel.setLayout(new BorderLayout());
		propertiesPanel.add(new WorkflowMetadata(), BorderLayout.CENTER);
		tabs.setEnabledAt(1, true);
		tabs.setTitleAt(1, "Workflow metadata");
		tabs.setIconAt(1, TavernaIcons.windowExplorer);

	}

	private void updateTabForProcessor(Processor processor) {
		// Clear the properties panel and regenerate it
		propertiesPanel.removeAll();
		propertiesPanel.setLayout(new BoxLayout(propertiesPanel,
				BoxLayout.PAGE_AXIS));
		final Processor p = processor;
		// Create a description section...
		JPanel descriptionPanel = new JPanel() {
			public Dimension getMaximumSize() {
				return new Dimension(99999, 150);
			}
		};
		descriptionPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(),
				"Processor Description for '" + p.getName() + "'"));
		descriptionPanel.setLayout(new BorderLayout());
		JTextArea description = new JTextArea(p.getDescription(), 4, 0);
		JScrollPane descriptionPane = new JScrollPane(description);
		descriptionPane.setPreferredSize(new Dimension(100, 100));
		description.getDocument().addDocumentListener(new DocumentListener() {
			public void insertUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					p.setDescription(d.getText(0, d.getLength()));
				} catch (BadLocationException ble) {
					//
				}
			}

			public void removeUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					p.setDescription(d.getText(0, d.getLength()));
				} catch (BadLocationException ble) {
					//
				}
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					p.setDescription(d.getText(0, d.getLength()));
				} catch (BadLocationException ble) {
					//
				}
			}
		});

		// descriptionPane.setPreferredSize(new Dimension(0,0));
		description.setEditable(true);
		description.setLineWrap(true);
		description.setWrapStyleWord(true);
		description.setFont(Font.getFont("Dialog"));
		descriptionPanel.add(descriptionPane);
		propertiesPanel.add(descriptionPanel);
		final JPanel iterationConfigPanel = new JPanel() {
			public Dimension getMaximumSize() {
				return new Dimension(99999, 100);
			}
		};
		iterationConfigPanel.setLayout(new BorderLayout());
		iterationConfigPanel.setBorder(BorderFactory.createTitledBorder(
				BorderFactory.createEtchedBorder(), "Configure Iterators"));

		final JButton createStrategy = new JButton("Create iteration strategy");
		final JButton resetStrategy = new JButton("Reset iteration strategy");

		final String noIteratorMessage = "<h2>No iterator strategy</h2>If you would like to override Taverna's default strategy you need to click the '<em><font color=\"green\">create strategy</font></em>' button and then use the editing controls to manipulate the tree of iterators. Note that the iteration strategy created will only include the inputs bound at the time you press the button, if you subsequently add new input links you will have to reset and recreate the strategy; all bound inputs <font color=\"red\">must</font> be included.";

		createStrategy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Derive the default iteration strategy from the
				// processor
				p.setIterationStrategy(new IterationStrategy(p));
				createStrategy.setEnabled(false);
				resetStrategy.setEnabled(true);
				IterationStrategyEditorControl editor = new IterationStrategyEditorControl(
						p.getIterationStrategy());
				iterationConfigPanel.removeAll();
				JScrollPane pane = new JScrollPane(editor);
				pane.setPreferredSize(new Dimension(100, 100));
				iterationConfigPanel.add(pane);
				doLayout();
				AdvancedModelExplorer.this.repaint();

			}
		});

		resetStrategy.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				// Reset the iteration strategy to null
				p.setIterationStrategy(null);
				resetStrategy.setEnabled(false);
				createStrategy.setEnabled(true);
				iterationConfigPanel.removeAll();
				JEditorPane ed = new JEditorPane("text/html", noIteratorMessage);
				ed.setEditable(false);
				JScrollPane helpPane = new JScrollPane(ed);
				helpPane.setPreferredSize(new Dimension(100, 100));
				iterationConfigPanel.add(helpPane);
				iterationConfigPanel.doLayout();
				doLayout();
				AdvancedModelExplorer.this.repaint();
			}
		});

		JPanel buttonPanel = new JPanel() {
			public Dimension getMaximumSize() {
				return new Dimension(99999, 20);
			}
		};
		buttonPanel.setLayout(new GridLayout(0, 2));
		buttonPanel.add(createStrategy);
		buttonPanel.add(resetStrategy);
		propertiesPanel.add(buttonPanel);
		propertiesPanel.add(new JScrollPane(iterationConfigPanel));
		if (p.getIterationStrategy() == null) {
			resetStrategy.setEnabled(false);
			JEditorPane ed = new JEditorPane("text/html", noIteratorMessage);
			ed.setEditable(false);
			JScrollPane helpPane = new JScrollPane(ed);
			helpPane.setPreferredSize(new Dimension(100, 100));
			iterationConfigPanel.add(helpPane);
		} else {
			createStrategy.setEnabled(false);
			IterationStrategyEditorControl editor = new IterationStrategyEditorControl(
					p.getIterationStrategy());
			JScrollPane pane = new JScrollPane(editor);
			pane.setPreferredSize(new Dimension(100, 100));
			iterationConfigPanel.add(pane);
		}

		tabs.setEnabledAt(1, true);
		tabs.setTitleAt(1, "Metadata for '" + p.getName() + "'");
		tabs.setIconAt(1, org.embl.ebi.escience.scuflworkers.ProcessorHelper
				.getPreferredIcon(p));
	}

	public String getName() {
		return "Advanced model explorer";
	}

	public ImageIcon getIcon() {
		return TavernaIcons.windowExplorer;
	}

	private ScuflModelEventListener listener = null;

	public void attachToModel(ScuflModel theModel) {
		if (this.model!=null) {
			logger.warn("Did not detachFromModel() before attachToModel()");
			detachFromModel();
		}
		this.model = theModel;

		setLayout(new BorderLayout());
		removeAll();
		// Create a tabbed layout and put the
		// explorer component in the first tab
		tabs = new JTabbedPane();
		tabs.setPreferredSize(new Dimension(450, 100));
		explorer = new ScuflModelTreeTable();
		JScrollPane explorerPane = new JScrollPane(explorer);
		explorerPane.setPreferredSize(new Dimension(0, 0));
		explorerPane.getViewport().setBackground(java.awt.Color.WHITE);
		JPanel workflowPanel = new JPanel();
		workflowPanel.setLayout(new BorderLayout());
		workflowPanel.add(explorerPane, BorderLayout.CENTER);
		tabs.add("Workflow", workflowPanel);

		// Create the properties panel but disable it
		// and don't populate it for now
		propertiesPanel = new JPanel();
		tabs.add("Object properties", propertiesPanel);
		tabs.setEnabledAt(1, false);

		// Add the tabbed pane to the center area of the panel
		add(tabs, BorderLayout.CENTER);
		add(new JLabel(getName()), BorderLayout.NORTH);

		// Create the tool bar
		JToolBar toolbar = new JToolBar();
		toolbar.setFloatable(false);
		toolbar.setRollover(true);
		toolbar.setMaximumSize(new Dimension(2000, 30));
		toolbar.setBorderPainted(true);

		for (ScuflModelActionSPI action : ScuflModelActionRegistry.instance()
				.getScuflModelActions(model)) {
			toolbar.add(new JButton(action));
		}
		toolbar.addSeparator();

		workOffline = new JCheckBox("Offline");
		workOffline.setModel(new OfflineToggleModel(model));
		toolbar.add(workOffline);

		// toolbar.add(Box.createHorizontalGlue());
		// toolbar.add(new JButton(new ResetAction(model,this)));

		// Add the toolbar to the top of the panel
		workflowPanel.add(toolbar, BorderLayout.PAGE_START);

		// Bind a list selection listener to the explorer
		explorer.getSelectionModel().addListSelectionListener(
				new ListSelectionListener() {
					public void valueChanged(ListSelectionEvent e) {
						if (e.getValueIsAdjusting()) {
							return;
						}
						ListSelectionModel lsm = (ListSelectionModel) e
								.getSource();
						if (lsm.isSelectionEmpty()) {
							// Disable the properties tab again
							AdvancedModelExplorer.this.selectedObject = null;
							AdvancedModelExplorer.this.tabs.setEnabledAt(1,
									false);
						} else {
							int selectedRow = lsm.getMinSelectionIndex();
							JTree tree = AdvancedModelExplorer.this.explorer
									.getTree();
							DefaultMutableTreeNode node = (DefaultMutableTreeNode) tree
									.getPathForRow(selectedRow)
									.getLastPathComponent();
							AdvancedModelExplorer.this.selectedObject = node
									.getUserObject();
							AdvancedModelExplorer.this.updateTab();
						}
					}
				});

		// Just in case, update the file chooser's ui model
		fc.updateUI();

		workOffline.setSelected(theModel.isOffline());
		explorer.attachToModel(theModel);
		listener = new ScuflModelEventListener() {
			public void receiveModelEvent(ScuflModelEvent event) {
				if (event != null
						&& event.getSource() == AdvancedModelExplorer.this.model) {
					boolean currentOfflineStatus = AdvancedModelExplorer.this.model
							.isOffline();
					workOffline.setSelected(currentOfflineStatus);
				}
			}
		};
		theModel.addListener(listener);
		revalidate();
	}

	public void detachFromModel() {
		if (this.model != null) {
			explorer.detachFromModel();
			this.model.removeListener(listener);
			this.model = null;
		}
	}

	public void onDisplay() {
		// TODO Auto-generated method stub

	}

	public void onDispose() {
		detachFromModel();
	}

	public class WorkflowMetadata extends JPanel {

		private WorkflowDescription wd = model.getDescription();

		private boolean updated = false;

		private DocumentFocusListener documentFocusListener = new DocumentFocusListener();

		public WorkflowMetadata() {
			super();
			setLayout(new BoxLayout(this, BoxLayout.PAGE_AXIS));
			add(makeAuthor());
			add(makeTitle());
			add(makeLsid());
			add(makeDescription());
		}

		private JPanel makeAuthor() {
			JTextField author = new JTextField(wd.getAuthor());
			JPanel authorPanel = titledPanel(author, "Author");
			addDocumentListener(author, new MetadataDocListener() {
				public void setValue(String text) {
					wd.setAuthor(text);
				}
			});
			return authorPanel;
		}

		private JPanel makeTitle() {
			JTextField title = new JTextField(wd.getTitle());
			JPanel titlePanel = titledPanel(title, "Descriptive title");

			addDocumentListener(title, new MetadataDocListener() {
				public void setValue(String text) {
					wd.setTitle(text);
				}
			});
			return titlePanel;
		}

		private JPanel makeLsid() {
			final JTextField lsid = new JTextField(wd.getLSID());
			lsid.setEditable(false);
			JPanel lsidPanel = titledPanel(lsid, "LSID");
			JButton assignNewLSID = new JButton("New", TavernaIcons.openurlIcon);
			assignNewLSID.setPreferredSize(new Dimension(80, 25));
			assignNewLSID.addActionListener(new ActionListener() {
				public void actionPerformed(ActionEvent e) {
					WorkflowDescription wd = AdvancedModelExplorer.this.model
							.getDescription();
					String newLSID = DataThing.SYSTEM_DEFAULT_LSID_PROVIDER
							.getID(LSIDProvider.WFDEFINITION);
					wd.setLSID(newLSID);
					lsid.setText(newLSID);
				}
			});
			if (DataThing.SYSTEM_DEFAULT_LSID_PROVIDER == null) {
				assignNewLSID.setEnabled(false);
			}
			lsidPanel.add(assignNewLSID, BorderLayout.EAST);
			return lsidPanel;
		}

		private JPanel makeDescription() {
			JTextArea description = new JTextArea(wd.getText());
			JScrollPane descriptionPane = new JScrollPane(description);			
			JPanel descriptionPanel = new JPanel() {
				public Dimension getMaximumSize() {
					return new Dimension(99999, 3000);
				}
			};
			descriptionPanel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), "Workflow description"));
			descriptionPanel.setLayout(new BorderLayout());			
			
			descriptionPane.setPreferredSize(new Dimension(100, 100));
			description.setEditable(true);
			description.setLineWrap(true);
			description.setWrapStyleWord(true);
			description.setFont(Font.getFont("Dialog"));
			descriptionPanel.add(descriptionPane);
			
			addDocumentListener(description, new MetadataDocListener() {
				public void setValue(String text) {
					wd.setText(text);
				}
			});
			
			return descriptionPanel;
		}

		private JPanel titledPanel(final JComponent field, String title) {
			JPanel panel = new JPanel() {
				public Dimension getMaximumSize() {
					if (field instanceof JTextField) {
						// Avoid enormous JTextField height
						return new Dimension(99999, 50);
					}
					else if (field instanceof JTextArea) {
						return new Dimension(99999, 3000);
					} else {
						return super.getMaximumSize();
					}
				}
			};
			panel.setBorder(BorderFactory.createTitledBorder(
					BorderFactory.createEtchedBorder(), title));
			panel.setLayout(new BorderLayout());
			panel.add(field, BorderLayout.CENTER);
			return panel;
		}

		private void addDocumentListener(JTextComponent description,
				MetadataDocListener listener) {
			description.getDocument().addDocumentListener(listener);
			description.addFocusListener(documentFocusListener);
		}

		public synchronized void checkUpdated() {
			if (updated) {
				model.fireModelEvent(new MinorScuflModelEvent(this,
						"Metadata updated"));
				updated = false;
			}
		}

		public synchronized void setUpdated() {
			updated = true;
		}

		private class DocumentFocusListener implements FocusListener {
			public void focusGained(FocusEvent e) {
			}

			public void focusLost(FocusEvent e) {
				checkUpdated();
			}
		}

		private abstract class MetadataDocListener implements DocumentListener {

			/**
			 * Implement this to do wd.setAuthor(text); etc.
			 * 
			 */
			public abstract void setValue(String text);

			public void insertUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					setValue(d.getText(0, d.getLength()));
					setUpdated();
				} catch (BadLocationException ble) {
					//
				}
			}

			public void removeUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					setValue(d.getText(0, d.getLength()));
					setUpdated();
				} catch (BadLocationException ble) {
					//
				}
			}

			public void changedUpdate(DocumentEvent e) {
				try {
					Document d = e.getDocument();
					setValue(d.getText(0, d.getLength()));
					setUpdated();
				} catch (BadLocationException ble) {
					//
				}
			}
		}
	}
}
