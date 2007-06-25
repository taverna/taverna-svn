package net.sf.taverna.t2.utility;

public interface TypedTreeModelListener<S> {

	void treeNodesChanged(TypedTreeModelEvent<S> e);
	
	void treeNodesInserted(TypedTreeModelEvent<S> e);
	
	void treeNodesRemoved(TypedTreeModelEvent<S> e);
	
	void treeStructureChanged(TypedTreeModelEvent<S> e);
	
}
