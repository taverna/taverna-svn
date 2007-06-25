package net.sf.taverna.t2.utility;

import javax.swing.tree.TreePath;

public interface TypedTreeModel<NodeType> {

	void addTreeModelListener(TypedTreeModelListener<NodeType> l);

	NodeType getChild(NodeType parent, int index);

	int getChildCount(NodeType parent);

	int getIndexOfChild(NodeType parent, NodeType child);

	NodeType getRoot();

	boolean isLeaf(NodeType node);

	void removeTreeModelListener(TypedTreeModelListener<NodeType> l);

	void valueForPathChanged(TreePath path, Object newValue);

}
