/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.baclava.factory;

import javax.swing.tree.DefaultMutableTreeNode;
import org.embl.ebi.escience.baclava.DataThing;




/**
* A subclass of the DefaultMutableTreeNode with
* a reference back to the DataThing that the node
* was created from. This allows lookup of, for
* example, the appropriate icons for the various
* MIME types.
* @author Tom Oinn
*/
public class DataThingTreeNode extends DefaultMutableTreeNode {
    private DataThing theDataThing;
    public DataThingTreeNode(DataThing dataThing) {
	super();
	theDataThing = dataThing;
    }
    public DataThingTreeNode(DataThing dataThing, Object userObject) {
	super(userObject);
	theDataThing = dataThing;
    }
    public DataThing getDataThing() {
	return theDataThing;
    }
}
