/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;
import javax.swing.*;
import javax.swing.event.*;
import javax.swing.table.*;
import java.awt.*;
import java.awt.event.*;
import javax.swing.tree.*;
import javax.swing.text.*;
import org.embl.ebi.escience.scufl.*;
import java.io.*;
import org.embl.ebi.escience.scufl.view.*;
import org.embl.ebi.escience.scufl.parser.*;
import java.net.*;
import java.util.prefs.*;
import java.util.*;
import org.embl.ebi.escience.scuflui.workbench.Workbench;

/**
 * An amalgam of the ScuflModelExplorerTreeTable and the
 * IterationStrategyControl panels linked such that selection
 * of a Processor node in the model explorer leads to context
 * sensitive options appearing in a properties tab.
 * @author Tom Oinn
 */
public class AdvancedModelExplorer extends JPanel 
    implements ScuflUIComponent {
    
    private ScuflModelTreeTable explorer;
    private JTabbedPane tabs;
    private JPanel propertiesPanel;
    private Object selectedObject = null;

    private JButton loadWorkflow, loadFromWeb, saveWorkflow, resetWorkflow, createNested;
    final JFileChooser fc = new JFileChooser();
    public AdvancedModelExplorer() {
	
	setLayout(new BorderLayout());

	// Create a tabbed layout and put the 
	// explorer component in the first tab
	tabs = new JTabbedPane();
	explorer = new ScuflModelTreeTable();
	JScrollPane explorerPane = new JScrollPane(explorer);
	explorerPane.setPreferredSize(new Dimension(0,0));
	explorerPane.getViewport().setBackground(java.awt.Color.WHITE);
	JPanel workflowPanel = new JPanel();
	workflowPanel.setLayout(new BorderLayout());
	workflowPanel.add(explorerPane, BorderLayout.CENTER);
	tabs.add("Workflow",workflowPanel);
	
	// Create the properties panel but disable it
	// and don't populate it for now
	propertiesPanel = new JPanel();
	tabs.add("Object properties", propertiesPanel);
	tabs.setEnabledAt(1, false);
	
	// Add the tabbed pane to the center area of the panel
	add(tabs, BorderLayout.CENTER);
	
	// Create the tool bar
	JToolBar toolbar = new JToolBar();
	toolbar.setFloatable(false);
	toolbar.setRollover(true);
	toolbar.setMaximumSize(new Dimension(2000,30));
	toolbar.setBorderPainted(true);

	// Add options to load the workflow, import from web, save and reset
	// These options were available from the workbench file menu previously
	// but I think they're more intuitive here as buttons.
	loadWorkflow = new JButton(Workbench.openIcon);
	loadWorkflow.setPreferredSize(new Dimension(25,25));
	loadFromWeb = new JButton(Workbench.openurlIcon);
	loadFromWeb.setPreferredSize(new Dimension(25,25));
	saveWorkflow = new JButton(Workbench.saveIcon);
	saveWorkflow.setPreferredSize(new Dimension(25,25));
	resetWorkflow = new JButton(Workbench.deleteIcon);
	resetWorkflow.setPreferredSize(new Dimension(25,25));
	createNested = new JButton(ScuflIcons.windowExplorer);
	createNested.setPreferredSize(new Dimension(25,25));
	
	toolbar.add(new JLabel(" Load "));
	toolbar.add(loadWorkflow);
	toolbar.addSeparator();
	toolbar.add(new JLabel("Load from web "));
	toolbar.add(loadFromWeb);
	toolbar.addSeparator();
	
	toolbar.add(new JLabel("Save "));
	toolbar.add(saveWorkflow);
	
	toolbar.addSeparator();
	toolbar.add(new JLabel("New subworkflow"));
	toolbar.add(createNested);

	toolbar.add(Box.createHorizontalGlue());
	
	toolbar.add(new JLabel("Reset "));
	toolbar.add(resetWorkflow);
	
	// Add the toolbar to the top of the panel
	workflowPanel.add(toolbar, BorderLayout.PAGE_START);
	
	// Add actionlistener to 'create new nested workflow' button
	createNested.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    ScuflModel targetModel = explorer.model;
		    if (targetModel != null) {
			try {
			    String name = targetModel.getValidProcessorName("NestedWorkflow");
			    Processor p = new org.embl.ebi.escience.scuflworkers.workflow.WorkflowProcessor(targetModel,
													    name);
			    targetModel.addProcessor(p);
			}
			catch (Exception ex) {
			    JOptionPane.showMessageDialog(AdvancedModelExplorer.this,
							  "Unable to create blank subworkflow : \n" +
							  ex.getMessage(),
							  "Error",
							  JOptionPane.ERROR_MESSAGE);
			}
		    }
		}
	    });

	// Add actionlistener to the load from file button
	loadWorkflow.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    // Load an XScufl definition here
		    Preferences prefs = Preferences.userNodeForPackage(Workbench.class);
		    String curDir = prefs.get("currentDir", System.getProperty("user.home"));
		    fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
		    fc.setCurrentDirectory(new File(curDir));
		    int returnVal = fc.showOpenDialog(AdvancedModelExplorer.this);
		    if (returnVal == JFileChooser.APPROVE_OPTION) {
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			final File file = fc.getSelectedFile();
			// mrp Refactored to do the heavy-lifting in a new thread
			new Thread(new Runnable() {
				public void run()
				{
				    try {
					// todo: does the update need running in the AWT thread?
					// perhaps this thread should be spawned in populate?
					XScuflParser.populate(file.toURL().openStream(),
							      explorer.model, null);
				    } catch (Exception ex) {
					JOptionPane.showMessageDialog(AdvancedModelExplorer.this,
								      "Problem opening workflow from file : \n" +
								      ex.getMessage(),
								      "Error",
								      JOptionPane.ERROR_MESSAGE);
				    }
				}
			    }).start();
			
		    }
		}
	    });
	
	// Add actionlistener to the load from web button
	loadFromWeb.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent ae) {
		    try {
			String name = (String)JOptionPane.showInputDialog(AdvancedModelExplorer.this,
									  "URL of an workflow definition to open?",
									  "URL Required",
									  JOptionPane.QUESTION_MESSAGE,
									  null,
									  null,
									  "http://");
			if (name != null) {
			    XScuflParser.populate((new URL(name)).openStream(), explorer.model, null);
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(AdvancedModelExplorer.this,
						      "Problem opening workflow from web : \n"+ex.getMessage(),
						      "Error!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });
	
	// Add actionlistener to the save button
	saveWorkflow.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    // Save to XScufl
		    try {
			Preferences prefs = Preferences.userNodeForPackage(AdvancedModelExplorer.class);
			String curDir = prefs.get("currentDir", System.getProperty("user.home"));
			fc.setFileFilter(new ExtensionFileFilter(new String[]{"xml"}));
			fc.setCurrentDirectory(new File(curDir));
			int returnVal = fc.showSaveDialog(AdvancedModelExplorer.this);
			if (returnVal == JFileChooser.APPROVE_OPTION) {
			    prefs.put("currentDir", fc.getCurrentDirectory().toString());
			    File file = fc.getSelectedFile();
			    XScuflView xsv = new XScuflView(explorer.model);
			    PrintWriter out = new PrintWriter(new FileWriter(file));
			    out.println(xsv.getXMLText());
			    explorer.model.removeListener(xsv);
			    out.flush();
			    out.close();
			}
		    }
		    catch (Exception ex) {
			JOptionPane.showMessageDialog(AdvancedModelExplorer.this,
						      "Problem saving workflow : \n"+ex.getMessage(),
						      "Error!",
						      JOptionPane.ERROR_MESSAGE);
		    }
		}
	    });

	// Add actionlistener to the reset button, throw up a dialog requesting
	// confirmation then nuke the workflow.
	resetWorkflow.addActionListener(new ActionListener() {
		public void actionPerformed(ActionEvent e) {
		    Object[] options = {"Confirm reset","Cancel"};
		    int n = JOptionPane.showOptionDialog(AdvancedModelExplorer.this,
							  "Are you sure you want to reset the model,\nany changes you have made will be lost?",
							  "Confirm workflow reset",
							  JOptionPane.YES_NO_OPTION,
							  JOptionPane.QUESTION_MESSAGE,
							  null,
							  options,
							  options[1]);
		    if (n == 0) {
			explorer.model.clear();
		    }
		    
		}
	    });
	
	
	// Bind a list selection listener to the explorer
	explorer.getSelectionModel().addListSelectionListener(new ListSelectionListener() {
		public void valueChanged(ListSelectionEvent e) {
		    if (e.getValueIsAdjusting()) {
			return;
		    }
		    ListSelectionModel lsm = (ListSelectionModel)e.getSource();
		    if (lsm.isSelectionEmpty()) {
			// Disable the properties tab again
			AdvancedModelExplorer.this.selectedObject = null;
			AdvancedModelExplorer.this.tabs.setEnabledAt(1, false);
		    }
		    else {
			int selectedRow = lsm.getMinSelectionIndex();
			JTree tree = AdvancedModelExplorer.this.explorer.getTree();
			DefaultMutableTreeNode node = 
			    (DefaultMutableTreeNode)tree.getPathForRow(selectedRow).getLastPathComponent();
			AdvancedModelExplorer.this.selectedObject = node.getUserObject();
			AdvancedModelExplorer.this.updateTab();
		    }
		}
	    });
	
	// Just in case, update the file chooser's ui model
	fc.updateUI();
    }
    
    /**
     * Called when a tree selection has been processed, inserting
     * the appropriate context object into the selectedObject value
     */
    private void updateTab() {
	if (selectedObject == null || selectedObject instanceof Processor == false) {
	    // Not a processor, don't show the info for now
	    tabs.setEnabledAt(1, false);
	}
	else {
	    // Clear the properties panel and regenerate it
	    propertiesPanel.removeAll();
	    propertiesPanel.setLayout(new BoxLayout(propertiesPanel, BoxLayout.PAGE_AXIS));
	    final Processor p = (Processor)selectedObject;
	    // Create a description section...
	    JPanel descriptionPanel = new JPanel() {
		    public Dimension getMaximumSize() {
			return new Dimension(99999,150);
		    }
		};
	    descriptionPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									"Processor Description for '"+p.getName()+"'"));
	    descriptionPanel.setLayout(new BorderLayout());
	    JTextArea description = new JTextArea(p.getDescription(),4,0);
	    JScrollPane descriptionPane = new JScrollPane(description);
	    descriptionPane.setPreferredSize(new Dimension(100,100));
	    description.getDocument().addDocumentListener(new DocumentListener() {
		    public void insertUpdate(DocumentEvent e) {
			try {
			    Document d = e.getDocument();
			    p.setDescription(d.getText(0, d.getLength()));
			}
			catch (BadLocationException ble) {
			    //
			}
		    }
		    public void removeUpdate(DocumentEvent e) {
			try {
			    Document d = e.getDocument();
			    p.setDescription(d.getText(0, d.getLength()));
			}
			catch (BadLocationException ble) {
			    //
			}
		    }
		    public void changedUpdate(DocumentEvent e) {
			try {
			    Document d = e.getDocument();
			    p.setDescription(d.getText(0, d.getLength()));
			}
			catch (BadLocationException ble) {
			    //
			}
		    }
		});
	    

	    //descriptionPane.setPreferredSize(new Dimension(0,0));
	    description.setEditable(true);
	    description.setLineWrap(true);
	    description.setWrapStyleWord(true);
	    descriptionPanel.add(descriptionPane);
	    propertiesPanel.add(descriptionPanel);
	    final JPanel iterationConfigPanel = new JPanel(){
		    public Dimension getMaximumSize() {
			return new Dimension(99999,100);
		    }
		};
	    iterationConfigPanel.setLayout(new BorderLayout());
	    iterationConfigPanel.setBorder(BorderFactory.createTitledBorder(BorderFactory.createEtchedBorder(),
									    "Configure Iterators"));
	    
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
			IterationStrategyEditorControl editor = new IterationStrategyEditorControl(p.getIterationStrategy());
			iterationConfigPanel.removeAll();
			JScrollPane pane = new JScrollPane(editor);
			pane.setPreferredSize(new Dimension(100,100));
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
			JEditorPane ed = new JEditorPane("text/html",noIteratorMessage);
			ed.setEditable(false);
			JScrollPane helpPane = new JScrollPane(ed);
			helpPane.setPreferredSize(new Dimension(100,100));
			iterationConfigPanel.add(helpPane);
			iterationConfigPanel.doLayout();
			doLayout();
			AdvancedModelExplorer.this.repaint();
		    }
		});
	    
	    JPanel buttonPanel = new JPanel() {
		    public Dimension getMaximumSize() {
			return new Dimension(99999,20);
		    }
		};
	    buttonPanel.setLayout(new GridLayout(0,2));
	    buttonPanel.add(createStrategy);
	    buttonPanel.add(resetStrategy);
	    propertiesPanel.add(buttonPanel);
	    propertiesPanel.add(new JScrollPane(iterationConfigPanel));
	    if (p.getIterationStrategy() == null) {
		resetStrategy.setEnabled(false);
		JEditorPane ed = new JEditorPane("text/html",noIteratorMessage);
		ed.setEditable(false);
		JScrollPane helpPane = new JScrollPane(ed);
		helpPane.setPreferredSize(new Dimension(100,100));
		iterationConfigPanel.add(helpPane);
	    }
	    else {
		createStrategy.setEnabled(false);
		IterationStrategyEditorControl editor = new IterationStrategyEditorControl(p.getIterationStrategy());
		JScrollPane pane = new JScrollPane(editor);
		pane.setPreferredSize(new Dimension(100,100));
		iterationConfigPanel.add(pane);
	    }
	    
	    tabs.setEnabledAt(1, true);
	}
    }

    public String getName() {
	return "Advanced model explorer";
    }

    public ImageIcon getIcon() {
	return ScuflIcons.windowExplorer;
    }

    public void attachToModel(ScuflModel theModel) {
	explorer.attachToModel(theModel);
    }

    public void detachFromModel() {
	explorer.detachFromModel();
    }

}
