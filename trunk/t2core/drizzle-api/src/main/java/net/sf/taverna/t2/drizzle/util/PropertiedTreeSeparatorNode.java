package net.sf.taverna.t2.drizzle.util;

import javax.swing.table.TableModel;

/**
 * @author alanrw
 *
 * A PropertiedTreeSeparatorNode indicates an expected change in the display of a PropertiedTreeModel.
 * 
 * @param <O> The class of object that is encapsulated in nodes.
 */
public interface PropertiedTreeSeparatorNode<O> extends PropertiedTreeNode<O> {
	TableModel getTableModel();
	// No additional methods
}
