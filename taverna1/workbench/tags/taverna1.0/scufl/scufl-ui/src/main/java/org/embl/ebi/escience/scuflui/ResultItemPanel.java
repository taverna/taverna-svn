/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflui;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Toolkit;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.dnd.DnDConstants;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Enumeration;
import java.util.List;
import java.util.prefs.Preferences;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JEditorPane;
import javax.swing.JFileChooser;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.JTree;
import javax.swing.event.TreeSelectionEvent;
import javax.swing.event.TreeSelectionListener;
import javax.swing.text.JTextComponent;
import javax.swing.tree.DefaultMutableTreeNode;
import javax.swing.tree.DefaultTreeModel;
import javax.swing.tree.TreeNode;
import javax.swing.tree.TreePath;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.baclava.factory.DataThingTreeFactory;
import org.embl.ebi.escience.baclava.factory.DataThingTreeNode;
import org.embl.ebi.escience.baclava.factory.DataThingTreeTransferHandler;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;
import org.embl.ebi.escience.scufl.enactor.event.UserChangedDataEvent;
import org.embl.ebi.escience.scufl.enactor.implementation.WorkflowEventDispatcher;
import org.embl.ebi.escience.scuflui.renderers.RendererException;
import org.embl.ebi.escience.scuflui.renderers.RendererRegistry;
import org.embl.ebi.escience.scuflui.shared.BytesSelection;
import org.embl.ebi.escience.scuflui.spi.RendererSPI;

/**
 * A JPanel to represent a single result DataThing to the user at the end of the
 * workflow
 * 
 * @author Tom Oinn
 * @author Matthew Pocock
 * @author Stian Soiland
 */
public class ResultItemPanel extends JPanel {

	private static Logger logger = Logger.getLogger(ResultItemPanel.class);

	private static WorkflowEventDispatcher DISPATCHER =
		WorkflowEventDispatcher.DISPATCHER;

	final RendererRegistry renderers;

	WorkflowInstance workflowInstance;

	String processorName = null;

	String portName = null;

	public void setSelectedPort(String processor, String port) {
		processorName = processor;
		portName = port;
	}

	public ResultItemPanel(DataThing theDataThing) {
		this(theDataThing, RendererRegistry.instance());
	}

	public ResultItemPanel(DataThing theDataThing,
		WorkflowInstance workflowInstance) {
		this(theDataThing, RendererRegistry.instance());
		this.workflowInstance = workflowInstance;
	}

	public ResultItemPanel(final DataThing theDataThing,
		RendererRegistry renderers) {
		super(new BorderLayout());

		this.renderers = renderers;

		// Construct the scrollable view of the structure
		// of the DataThing by using a copy.
		final DataThing newDataThing = (DataThing) theDataThing.clone();
		final TreeNode treeNode = DataThingTreeFactory.getTree(newDataThing);
		final JTree structureTree = new JTree(treeNode);
		// Fix for look and feel problems with multiline labels.
		structureTree.setRowHeight(0);
		structureTree.setCellRenderer(DataThingTreeFactory.getRenderer());
		// structureTree.setModel(tm);
		new DataThingTreeTransferHandler(structureTree,
			DnDConstants.ACTION_COPY);
		String viewerHelp =
			"<h2>Result browser</h2>Click on items in the tree to the "
				+ "left of this panel to select them and show their values "
				+ "in this area. Right clicking on an item within the tree "
				+ "will allow you to select different rendering options that "
				+ "might be available, for example displaying an XML file as "
				+ "text or as a navigable tree.";
		JEditorPane help = new JEditorPane("text/html", viewerHelp);
		help.setPreferredSize(new Dimension(200, 100));
		help.setEditable(false);
		JScrollPane helpPanel = new JScrollPane(help);
		helpPanel.getViewport().setBackground(Color.WHITE);
		final JSplitPane splitPane =
			new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, new JScrollPane(
				structureTree), helpPanel);
		splitPane.setDividerLocation(-1);
		Object data = newDataThing.getDataObject();
		if (!(data instanceof Collection && ((Collection) data).isEmpty())) {
			// non-collection or non-empty collection
			// Add an action listener to display the data
			structureTree.addTreeSelectionListener(new StructureTreeListener(
				treeNode, newDataThing, splitPane, structureTree));
		}

		// Add a mouse listener to allow the user to save results to disc
		// and chose renderers
		structureTree.addMouseListener(new StructureMouseListener(treeNode,
			splitPane, structureTree));
		add(splitPane, BorderLayout.CENTER);
	}

	// VERY TEMP
	// If expand is true, expands all nodes in the tree.
	// Otherwise, collapses all nodes in the tree.
	public void expandAll(JTree tree, boolean expand) {
		TreeNode root = (TreeNode) tree.getModel().getRoot();
		// Traverse tree from root
		expandAll(tree, new TreePath(root), expand);
	}

	private void expandAll(JTree tree, TreePath parent, boolean expand) {
		// Traverse children
		TreeNode node = (TreeNode) parent.getLastPathComponent();

		for (Enumeration e = node.children(); e.hasMoreElements();) {
			TreeNode n = (TreeNode) e.nextElement();
			TreePath path = parent.pathByAddingChild(n);
			expandAll(tree, path, expand);
		}

		// Expansion or collapse must be done bottom-up
		if (expand) {
			tree.expandPath(parent);
		} else {
			tree.collapsePath(parent);
		}
	}

	private class StructureMouseListener extends MouseAdapter {

		private class SelectRenderer extends AbstractAction {
			private final DataThing thing;

			private final RendererSPI renderer;

			private SelectRenderer(RendererSPI renderer, DataThing thing) {
				super("View as " + renderer.getName(), renderer.getIcon(
					ResultItemPanel.this.renderers, thing));
				this.renderer = renderer;
				this.thing = thing;
			}

			public void actionPerformed(ActionEvent e) {
				final JComponent component;
				try {
					component = renderer.getComponent(renderers, thing);
				} catch (RendererException re) {
					JOptionPane.showMessageDialog(ResultItemPanel.this,
						"Could not render data, try another viewer.\n"
							+ re.getMessage(), "Data rendering error",
						JOptionPane.ERROR_MESSAGE);
					logger.error("Unable to load renderer", re);
					return;
				}
				if (ui == null) {
					return;
				}

				// Add the update button and the text area.
				JPanel panel = new JPanel(new BorderLayout());

				JPanel leftPanel = new JPanel(new BorderLayout());
				panel.add(leftPanel, BorderLayout.CENTER);

				JScrollPane scrollPane = new JScrollPane(component);
				scrollPane.getViewport().setBackground(Color.WHITE);
				leftPanel.add(scrollPane);

				if (processorName != null
					&& workflowInstance.isDataNonVolatile(processorName)) {
					JPanel rightPanel = new JPanel();
					JButton cmdEdit = new JButton("Change");
					cmdEdit.addActionListener(new ChangeAction(component,
						thing, treeNode, tree));
					rightPanel.add(cmdEdit);
					panel.add(rightPanel, BorderLayout.EAST);
				}

				panel.setPreferredSize(new Dimension(200, 80));
				pane.setRightComponent(panel);
			}
		}

		private final TreeNode treeNode;

		private final JSplitPane pane;

		private final JTree tree;

		private StructureMouseListener(TreeNode treeNode, JSplitPane pane,
			JTree tree) {
			this.treeNode = treeNode;
			this.pane = pane;
			this.tree = tree;
		}

		public void mousePressed(MouseEvent e) {
			if (e.isPopupTrigger()) {
				doEvent(e);
			}
		}

		public void mouseReleased(MouseEvent e) {
			if (e.isPopupTrigger()) {
				doEvent(e);
			}
		}

		void doEvent(MouseEvent e) {
			final DataThingTreeNode node =
				(DataThingTreeNode) (tree.getPathForLocation(e.getX(), e.getY()).getLastPathComponent());
			final Object theDataObject = node.getUserObject();
			JPopupMenu theMenu = new JPopupMenu();

			if (node.isLeaf()) {
				// Can only save/copy on leaf nodes
				Action saveAction = new SaveAction(theDataObject);
				theMenu.add(new JMenuItem(saveAction));

				Action copyAction = new CopyAction(theDataObject);
				theMenu.add(copyAction);
				theMenu.addSeparator();
			}
			for (JMenuItem viewerMenu : viewers(node)) {
				theMenu.add(viewerMenu);
			}
			theMenu.show(tree, e.getX(), e.getY());
		}

		private List<JMenuItem> viewers(final DataThingTreeNode node) {
			// all possible viewers
			List<JMenuItem> items = new ArrayList<JMenuItem>();
			final DataThing nodeThing = node.getNodeThing();
			Object data = nodeThing.getDataObject();
			if (data instanceof Collection && ((Collection) data).isEmpty()) {
				// No renderers
				return items;
			}
			for (RendererSPI renderer : renderers.getRenderers(nodeThing)) {
				items.add(new JMenuItem(new SelectRenderer(renderer, nodeThing)));
			}
			return items;
		}
	}

	private class StructureTreeListener implements TreeSelectionListener {
		private class EditActionListener implements ActionListener {
			private final JComponent component;

			private EditActionListener(JComponent component) {
				this.component = component;
			}

			public void actionPerformed(ActionEvent ae) {
				DataThingTreeNode node =
					(DataThingTreeNode) tree.getLastSelectedPathComponent();
				if (node != null /*
									 * && node.isLeaf()
									 */)
					if (component instanceof JTextComponent) {
						DataThing dataThing = node.getNodeThing();
						String oldData = (String) dataThing.getDataObject();
						String newData = ((JTextComponent) component).getText();
						if (!oldData.equals(newData)) {
							String oldLSID =
								StructureTreeListener.this.thing.getLSID(thing.getDataObject());
							DataThing thing =
								StructureTreeListener.this.thing.drillAndSet(
									dataThing, newData);
							if (thing != null
								&& workflowInstance.changeOutputPortTaskData(
									processorName, portName, thing)) {
								DISPATCHER.fireEvent(new UserChangedDataEvent(
									workflowInstance, oldLSID, thing));

								tree.setEditable(true);
								((DefaultMutableTreeNode) tn).removeAllChildren();
								DefaultMutableTreeNode root =
									(DefaultMutableTreeNode) tree.getModel().getRoot();
								root.removeAllChildren();
								DataThingTreeNode rootDataThing =
									DataThingTreeFactory.getTree(thing);
								DefaultTreeModel newTree =
									new DefaultTreeModel(rootDataThing);
								tree.setModel(newTree);
								tree.setEditable(false);
								tree.repaint();
							}
						}
					} else {
						// TODO for other renderers
						// newDataThing.setDataObject(((JTable)component).getValueAt(0,0));
					}
			}
		}

		private final TreeNode tn;

		private final DataThing thing;

		private final JSplitPane pane;

		private final JTree tree;

		private StructureTreeListener(TreeNode tn, DataThing thing,
			JSplitPane pane, JTree tree) {
			this.tn = tn;
			this.thing = thing;
			this.pane = pane;
			this.tree = tree;
		}

		public void valueChanged(TreeSelectionEvent e) {
			DataThingTreeNode node =
				(DataThingTreeNode) tree.getLastSelectedPathComponent();
			if (node != null /* && node.isLeaf() */) {
				// Only interested in leaf nodes as they contain the
				// data
				DataThing dataThing = node.getNodeThing();
				RendererSPI renderer =
					ResultItemPanel.this.renderers.getRenderer(dataThing);

				if (renderer == null) {
					return;
				}
				try {
					final JComponent component =
						renderer.getComponent(ResultItemPanel.this.renderers,
							dataThing);
					if (component == null) {
						return;
					}
					JScrollPane foo = new JScrollPane(component);
					foo.getViewport().setBackground(Color.WHITE);
					foo.setPreferredSize(new Dimension(100, 100));
					// Add the update button and the text area.
					JPanel panel = new JPanel(new BorderLayout());
					JPanel rightPanel = new JPanel(new FlowLayout());
					JPanel leftPanel = new JPanel(new BorderLayout());
					JButton cmdEdit = new JButton("Change");
					cmdEdit.addActionListener(new EditActionListener(component));
					leftPanel.add(foo);
					rightPanel.add(cmdEdit);
					panel.add(leftPanel, BorderLayout.CENTER);
					if (processorName != null
						&& workflowInstance.isDataNonVolatile(processorName))
						panel.add(rightPanel, BorderLayout.EAST);
					panel.setPreferredSize(new Dimension(200, 80));
					pane.setRightComponent(panel);
					// Reset the widths of the split Pane to
					// show the entire tree
					pane.setDividerLocation(-1);
				} catch (RendererException re) {
					JOptionPane.showMessageDialog(ResultItemPanel.this,
						"Could not render data correctly, try selecting another viewer.\n"
							+ re.getMessage(), "Data rendering error",
						JOptionPane.ERROR_MESSAGE);
					logger.error("Problem loading renderer", re);
				}
			}
		}
	}

	private class CopyAction extends AbstractAction {
		private final Object object;

		private CopyAction(Object object) {
			super("Copy", TavernaIcons.copyIcon);
			this.object = object;
		}

		public void actionPerformed(ActionEvent ae) {
			try {
				Clipboard clipboard =
					Toolkit.getDefaultToolkit().getSystemClipboard();

				if (object instanceof byte[]) {
					// FIXME: Does not seem to work properly
					BytesSelection bytesSelection =
						new BytesSelection((byte[]) object);
					clipboard.setContents(bytesSelection, bytesSelection);
				} else {
					// String
					StringSelection stringSelection =
						new StringSelection(object.toString());
					clipboard.setContents(stringSelection, stringSelection);
				}
			} catch (Exception ex) {
				logger.warn("Could not copy " + object, ex);
				JOptionPane.showMessageDialog(ResultItemPanel.this,
					"Problem copying data : \n" + ex.getMessage(),
					"Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}
	}

	private class SaveAction extends AbstractAction {

		private final Object object;

		private SaveAction(Object object) {
			super("Save to file", TavernaIcons.saveIcon);
			this.object = object;
		}

		public void actionPerformed(ActionEvent ae) {
			JFileChooser fc = new JFileChooser();
			Preferences prefs =
				Preferences.userNodeForPackage(ResultItemPanel.class);
			String curDir =
				prefs.get("currentDir", System.getProperty("user.home"));
			fc.setCurrentDirectory(new File(curDir));
			// Popup a save dialog and allow the user to store
			// the data to disc
			int returnVal = fc.showSaveDialog(ResultItemPanel.this);
			if (returnVal != JFileChooser.APPROVE_OPTION) {
				return;
			}
			prefs.put("currentDir", fc.getCurrentDirectory().toString());
			File file = fc.getSelectedFile();
			try {
				FileOutputStream fos = new FileOutputStream(file);
				if (object instanceof byte[]) {
					fos.write((byte[]) object);
					fos.flush();
					fos.close();
				} else {
					// String
					Writer out =
						new BufferedWriter(new OutputStreamWriter(fos));
					out.write(object.toString());
					out.flush();
					out.close();
				}
			} catch (IOException ioe) {
				JOptionPane.showMessageDialog(ResultItemPanel.this,
					"Problem saving data : \n" + ioe.getMessage(),
					"Exception!", JOptionPane.ERROR_MESSAGE);
			}
		}

	}

	private class ChangeAction implements ActionListener {

		JComponent component;

		DataThing thing;

		TreeNode treeNode;

		JTree tree;

		ChangeAction(JComponent component, DataThing thing, TreeNode tn,
			JTree tree) {
			super();
			this.component = component;
			this.thing = thing;
			this.treeNode = tn;
			this.tree = tree;
		}

		public void actionPerformed(ActionEvent ae) {
			DataThingTreeNode node =
				(DataThingTreeNode) tree.getLastSelectedPathComponent();
			if (node == null /* || ! node.isLeaf() */) {
				return;
			}
			if (!(component instanceof JTextComponent)) {
				// TODO for other renderers
				// newDataThing.setDataObject(((JTable)component).getValueAt(0,0));
				return;
			}

			DataThing dataThing = node.getNodeThing();
			String oldData = (String) dataThing.getDataObject();
			String newData = ((JTextComponent) component).getText();
			if (oldData.equals(newData)) {
				return;
			}
			String oldLSID = this.thing.getLSID(thing.getDataObject());
			DataThing thing = this.thing.drillAndSet(dataThing, newData);
			if (thing != null
				&& workflowInstance.changeOutputPortTaskData(processorName,
					portName, thing)) {
				DISPATCHER.fireEvent(new UserChangedDataEvent(workflowInstance,
					oldLSID, thing));
				// System.out.println("DATATHING CHANGED EVENT!!!!");

				// Update the JTree
				tree.setEditable(true);
				((DefaultMutableTreeNode) treeNode).removeAllChildren();
				DefaultMutableTreeNode root =
					(DefaultMutableTreeNode) tree.getModel().getRoot();
				root.removeAllChildren();
				DataThingTreeNode rootDataThing =
					DataThingTreeFactory.getTree(thing);
				DefaultTreeModel newTree = new DefaultTreeModel(rootDataThing);
				tree.setModel(newTree);
				tree.setEditable(false);
				tree.repaint();
				// structureTree.update(structureTree.getGraphics());
			}
		}
	}

}
