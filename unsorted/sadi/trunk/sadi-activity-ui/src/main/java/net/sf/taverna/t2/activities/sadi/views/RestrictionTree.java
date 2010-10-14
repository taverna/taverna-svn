package net.sf.taverna.t2.activities.sadi.views;

import java.awt.Component;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.EventListener;

import javax.swing.AbstractCellEditor;
import javax.swing.JCheckBox;
import javax.swing.JTree;
import javax.swing.UIManager;
import javax.swing.event.EventListenerList;
import javax.swing.event.TreeModelEvent;
import javax.swing.event.TreeModelListener;
import javax.swing.tree.TreeCellEditor;
import javax.swing.tree.TreeCellRenderer;

import net.sf.taverna.t2.activities.sadi.RestrictionNode;

/**
 * @author Luke McCarthy
 */
public class RestrictionTree extends JTree
{
	private static final long serialVersionUID = 1L;

	private EventListenerList listenerList;
	private RestrictionTreeCellRenderer cellRenderer;
	
	public RestrictionTree(RestrictionNode root)
	{
		super(root);
		
		listenerList = new EventListenerList();
		cellRenderer = new RestrictionTreeCellRenderer();

		setRootVisible(true);
		setSelectionModel(null); // no selection, just check boxes...
		setCellRenderer(cellRenderer);
		setEditable(true);
		setCellEditor(cellRenderer);
		putClientProperty("JTree.lineStyle", "None");
		
		getModel().addTreeModelListener(new TreeModelListener() {
			public void treeNodesChanged(TreeModelEvent e) {
				RestrictionNode node = (RestrictionNode)e.getTreePath().getLastPathComponent();
				if (e.getChildIndices() != null && e.getChildIndices().length > 0) {
					node = node.getChildren().get(e.getChildIndices()[0]);
				}
				Object data = node.getUserObject();
				if (data instanceof Boolean) {
					boolean oldState = node.isSelected();
					boolean newState = (Boolean)data;
					if (oldState != newState) {
						node.setSelected(newState);
						fireNodeSelected(node, node.isSelected());
					}
				}
			}
			public void treeNodesInserted(TreeModelEvent e) {}
			public void treeNodesRemoved(TreeModelEvent e) {}
			public void treeStructureChanged(TreeModelEvent e) {}
		});
	}
	
	public void addNodeSelectionListener(RestrictionNodeSelectionListener l)
	{
		listenerList.add(RestrictionNodeSelectionListener.class, l);
	}
	
	public void removeNodeSelectionListener(RestrictionNodeSelectionListener l)
	{
		listenerList.remove(RestrictionNodeSelectionListener.class, l);
	}
	
	private void fireNodeSelected(RestrictionNode node, boolean selected)
	{
		for (RestrictionNodeSelectionListener l: listenerList.getListeners(RestrictionNodeSelectionListener.class))
			l.nodeSelected(node, selected);
	}
	
	public static class RestrictionTreeCellRenderer extends AbstractCellEditor implements TreeCellRenderer, TreeCellEditor
	{
		private static final long serialVersionUID = 1L;
		private JCheckBox check;
		
		public RestrictionTreeCellRenderer()
		{
			check = new JCheckBox();
			check.addItemListener(new ItemListener() {
				public void itemStateChanged(ItemEvent itemEvent) {
					stopCellEditing();
				}
			});
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.tree.TreeCellRenderer#getTreeCellRendererComponent(javax.swing.JTree, java.lang.Object, boolean, boolean, boolean, int, boolean)
		 */
		public Component getTreeCellRendererComponent(JTree tree, Object value,
				boolean selected, boolean expanded, boolean leaf, int row, boolean hasFocus)
		{
			String stringValue = tree.convertValueToText(value, selected, expanded, leaf, row, false);
			check.setText(stringValue);
			if (value instanceof RestrictionNode) {
				check.setSelected(((RestrictionNode)value).isSelected());
			}
			check.setEnabled(tree.isEnabled());
			if (selected) {
				check.setForeground(UIManager.getColor("Tree.selectionForeground"));
				check.setBackground(UIManager.getColor("Tree.selectionBackground"));
			} else {
				check.setForeground(UIManager.getColor("Tree.textForeground"));
				check.setBackground(UIManager.getColor("Tree.textBackground"));
			}
			return check;
		}
		
		public Component getTreeCellEditorComponent(JTree tree, Object value,
				boolean isSelected, boolean expanded, boolean leaf, int row)
		{
			return getTreeCellRendererComponent(tree, value, true, expanded, leaf, row, true);
		}
		
		/* (non-Javadoc)
		 * @see javax.swing.CellEditor#getCellEditorValue()
		 */
		public Object getCellEditorValue()
		{
			return check.isSelected() ? Boolean.TRUE : Boolean.FALSE;
		}
	}
	
	public interface RestrictionNodeSelectionListener extends EventListener
	{
		void nodeSelected(RestrictionNode node, boolean selected);
	}
}
