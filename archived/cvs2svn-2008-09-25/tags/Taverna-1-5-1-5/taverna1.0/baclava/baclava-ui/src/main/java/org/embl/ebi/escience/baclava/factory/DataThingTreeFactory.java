/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import java.util.Iterator;

import javax.swing.tree.TreeCellRenderer;

import org.embl.ebi.escience.baclava.DataThing;

/**
 * Presents a tree view of a DataThing, walking over the nested collections if
 * any within the DataThing and generating DataThingTreeNode instances
 * appropriately. In addition, there is a standard renderer available to draw
 * these nodes.
 * 
 * @author Tom Oinn
 */
public class DataThingTreeFactory {

	/**
	 * Get a tree branch starting at the first level of the collection or the
	 * first data object contained within this DataThing
	 */
	public static DataThingTreeNode getTree(DataThing theDataThing) {
		return doNode(theDataThing, theDataThing);
	}

	/**
	 * Recursive collection walker to build the data structure
	 */
	private static DataThingTreeNode doNode(DataThing theDataThing,
			DataThing nodeThing) {
		DataThingTreeNode node = new DataThingTreeNode(theDataThing, nodeThing);

		// Handle collections
		for (Iterator i = nodeThing.childIterator(); i.hasNext();) {
			node.add(doNode(theDataThing, (DataThing) i.next()));
		}

		return node;
	}

	/**
	 * Return the renderer for data thing tree nodes.
	 * 
	 * @return a TreeCellRenderer for our tree
	 */
	public static TreeCellRenderer getRenderer() {
		return new DataThingTreeNodeRenderer();
	}

}
