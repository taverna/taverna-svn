/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import javax.swing.tree.TreeCellRenderer;
import org.embl.ebi.escience.baclava.DataThing;

// Utility Imports
import java.util.Collection;
import java.util.Iterator;
import java.util.Set;




/**
 * Presents a tree view of a DataThing, walking
 * over the nested collections if any within the
 * DataThing and generating DataThingTreeNode
 * instances appropriately. In addition, there is
 * a standard renderer available to draw these nodes.
 * @author Tom Oinn
 */
public class DataThingTreeFactory {

    /**
     * Get a tree branch starting at the first
     * level of the collection or the first
     * data object contained within this DataThing
     */
    public static DataThingTreeNode getTree(DataThing theDataThing) {
	return doNode(theDataThing, theDataThing.getDataObject());
    }
    /**
     * Recursive collection walker to build the
     * data structure
     */
    private static DataThingTreeNode doNode(DataThing theDataThing,
					    Object theObject) {
	// Handle collections
	if (theObject instanceof Collection) {
	    // Only lists and sets supported for now
	    String collectionType = "List";
	    if (theObject instanceof Set) {
		collectionType = "Set";
	    }
	    DataThingTreeNode result = new DataThingTreeNode(theDataThing,
							     theObject);
	    for (Iterator i = ((Collection)theObject).iterator(); i.hasNext();) {
		result.add(doNode(theDataThing, i.next()));
	    }
	    return result;
	}
	else {
	    return new DataThingTreeNode(theDataThing, theObject);
	}
    }
    
    public static TreeCellRenderer getRenderer() {
	return new DataThingTreeNodeRenderer();
    }
  
}
