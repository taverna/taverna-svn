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

/**
 * An amalgam of the ScuflModelExplorerTreeTable and the
 * IterationStrategyControl panels linked such that selection
 * of a Processor node in the model explorer leads to context
 * sensitive options appearing in a properties tab.
 * @author Tom Oinn
 */
public class AdvancedModelExplorer extends JTabbedPane 
    implements ScuflUIComponent {
    
    private ScuflModelTreeTable explorer;
    private JTabbedPane tabs;
    private JPanel propertiesPanel;
    private Object selectedObject = null;

    public AdvancedModelExplorer() {
	
	// Create a tabbed layout and put the 
	// explorer component in the first tab
	tabs = this;
	explorer = new ScuflModelTreeTable();
	JScrollPane explorerPane = new JScrollPane(explorer);
	explorerPane.setPreferredSize(new Dimension(0,0));
	tabs.add("Workflow",explorerPane);
	
	// Create the properties panel but disable it
	// and don't populate it for now
	propertiesPanel = new JPanel();
	add("Object properties", propertiesPanel);
	setEnabledAt(1, false);
	
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
    }
    
    /**
     * Called when a tree selection has been processed, inserting
     * the appropriate context object into the selectedObject value
     */
    private void updateTab() {
	if (selectedObject instanceof Processor == false) {
	    // Not a processor, don't show the info for now
	    setEnabledAt(1, false);
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
	    JTextArea description = new JTextArea(p.getDescription(),5,0);
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
	    
	    final String noIteratorMessage = "<h2>No iterator strategy</h2>If you would like to override Taverna's default strategy you need to click the '<em><font color=\"green\">create strategy</font></em>' button and then use the editing controls to manipulate the tree of iterators.";
	    
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
	    
	    setEnabledAt(1, true);
	}
    }

    public String getName() {
	return "Advanced model explorer";
    }

    public void attachToModel(ScuflModel theModel) {
	explorer.attachToModel(theModel);
    }

    public void detachFromModel() {
	explorer.detachFromModel();
    }

}
