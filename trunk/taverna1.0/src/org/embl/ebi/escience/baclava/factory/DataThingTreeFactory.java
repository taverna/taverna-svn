/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import java.util.*;
import org.jdom.*;
import javax.swing.*;
import java.awt.*;
import java.awt.image.*;
import javax.swing.tree.*;
import org.embl.ebi.escience.baclava.*;
import org.embl.ebi.escience.scufl.*;

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
							     collectionType);
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