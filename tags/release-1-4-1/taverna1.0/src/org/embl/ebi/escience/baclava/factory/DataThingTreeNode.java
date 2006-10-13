/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.baclava.DataThing;

/**
 * A subclass of the DefaultMutableTreeNode with a reference back to the
 * DataThing that the node was created from. This allows lookup of, for example,
 * the appropriate icons for the various MIME types.
 * <p>
 * The DataThing that is displayed in the tree can be retrieved using
 * getDataThing(). The DataThing that is represented by this node can be
 * retrieved using getNodeThing() and the object this node represents can be got
 * by getUserObject().
 * 
 * @author Tom Oinn
 */
public class DataThingTreeNode extends DefaultMutableTreeNode {
	private final DataThing dataThing;

	private final DataThing nodeThing;

	public DataThingTreeNode(DataThing dataThing) {
		this(dataThing, dataThing);
	}

	public DataThingTreeNode(DataThing dataThing, DataThing nodeThing) {
		super(nodeThing.getDataObject());
		this.dataThing = dataThing;
		this.nodeThing = nodeThing;
	}

	public DataThing getDataThing() {
		return dataThing;
	}

	public DataThing getNodeThing() {
		return nodeThing;
	}

	public String toString() {
		return "DataThing Node";
	}
}
