/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.dataviewer;

import java.awt.BorderLayout;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.border.EmptyBorder;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.tree.TreePath;
import javax.swing.tree.TreeSelectionModel;

import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workbench.icons.WorkbenchIcons;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * A panel for displaying a DataThing item contained in a Baclava
 * document. Each data item has a name that corresponds to an input or an output
 * port that it came from and depth (0 for single result, 1 for list, 2 for list
 * of lists ...).
 * 
 * @author Alex Nenadic
 * 
 */
public class DataViewPanel extends JPanel {

	private static final long serialVersionUID = -5531195402446371947L;

	// DataThing being displayed 
	// (it either contains a single data item or a lists of (lists of ...) data items)
	DataThing dataThing;
	
	// T2Reference to the data contained in the DataThing
	private T2Reference dataReference;
	
	// Depth of the data item contained in DataThing (0 is single item, 
	// 1 if list, 2 if lists if lists etc.)
	int dataDepth;

	// Rendered result component
	private RenderedDataComponent renderedDataComponent;

	// DataThing is represented as a tree as it can contain either an
	// individual data item or a list of (lists of ... ) data items
	private JTree dataTree;

	private ReferenceService referenceService;

	private JButton saveAllButton;

	public DataViewPanel(DataThing dataThing, ReferenceService referenceService) {
		super(new BorderLayout());
		this.dataThing = dataThing;
		this.referenceService = referenceService;

		initComponents();
	}

	private void initComponents() {

		// Calculate the depth of DataThing
		Object dataObject = dataThing.getDataObject();
		dataDepth = calculateDataDepth(dataObject);
		
		// Register the data item with Reference Service
		// so we later on pass the reference to renderers 
		// (renderers do not expect actual data but a T2Reference to data)
		dataReference = referenceService.register(dataObject, dataDepth , true, null);
		
		// Create the tree from the DataThing - the nodes contain 
		// T2References to the data items contained in the DataThing	
		dataTree = new JTree(new DataTreeModel(dataReference, referenceService));
		
		// Fix for look and feel problems with multi-line labels.
		dataTree.getSelectionModel().setSelectionMode(
				TreeSelectionModel.SINGLE_TREE_SELECTION);
		dataTree.setExpandsSelectedPaths(true);
		dataTree.setRootVisible(false);
		dataTree.setCellRenderer(new DataTreeCellRenderer());
		
		// Expand the tree
		expandTree();

		// Split pane containing a tree with all DataThings from a port and a
		// component where currently selected individual data item (leaf) is rendered
		JSplitPane splitPanel = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT);

		// Component for rendering individual data items contained in tree nodes
		renderedDataComponent = new RenderedDataComponent(referenceService);

		dataTree.addTreeSelectionListener(new TreeSelectionListener() {

			public void valueChanged(TreeSelectionEvent e) {
				TreePath selectionPath = e.getNewLeadSelectionPath();
				if (selectionPath != null) {
					// Get the selected node
					final Object selectedNode = selectionPath
							.getLastPathComponent();
					renderedDataComponent
							.setNode((DataTreeNode) selectedNode);
				}
			}
		});

		JPanel saveAllButtonPanel = new JPanel(new GridBagLayout());
		saveAllButtonPanel.setBorder(new EmptyBorder(0,0,5,5));
		saveAllButton = new JButton("Save all values", WorkbenchIcons.saveAllIcon);
		saveAllButton.setFocusable(false);
		GridBagConstraints gbc = new GridBagConstraints();
		gbc.gridx = 0;
		gbc.gridy = 0;
		gbc.weightx = 1.0;
		gbc.fill = GridBagConstraints.NONE;
		gbc.anchor = GridBagConstraints.EAST;
		saveAllButtonPanel.add(saveAllButton, gbc);
		
		JPanel treePanel = new JPanel();
		treePanel.setLayout(new BorderLayout());
		treePanel.add(new JScrollPane(dataTree), BorderLayout.CENTER);

		splitPanel.setTopComponent(treePanel);
		splitPanel.setBottomComponent(renderedDataComponent);
		splitPanel.setDividerLocation(250);

		// Add all to main panel
		add(saveAllButtonPanel, BorderLayout.NORTH);
		add(splitPanel, BorderLayout.CENTER);

	}
	
	private int calculateDataDepth(Object dataObject) {

		if (dataObject instanceof Collection<?>){
			if (((Collection<?>)dataObject).isEmpty()){
				return 1;
			}
			else{
				// Calculate the depth of the first element in collection + 1
				return calculateDataDepth(((Collection<?>)dataObject).iterator().next()) + 1;
			}
		}
		else{
			return 0;
		}
	}

	public void expandTree() {

		if (dataTree != null){
			for (int row = 0; row < dataTree.getRowCount(); row ++) {
				dataTree.expandRow(row);
			 }
		}
	}
	
	public T2Reference getDataReference(){
		return dataReference;
	}

	
	public Object getDataObject(){
		DataTreeNode root = (DataTreeNode)dataTree.getModel().getRoot();
		return root.getAsObject();
	}
	
	public void setSaveAllButtonAction(final Action action) {
		saveAllButton.addActionListener(new ActionListener() {
			public void actionPerformed(ActionEvent e) {
				action.actionPerformed(e);
				saveAllButton.getParent().requestFocusInWindow();
			}
		});
	}
}
