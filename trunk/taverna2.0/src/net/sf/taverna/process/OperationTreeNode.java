/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.process;

import javax.swing.tree.DefaultMutableTreeNode;
import java.util.Date;

/**
 * Represents a single node in the OperationTree, contains an Operation
 * as its userObject
 * @author Tom Oinn
 */
public class OperationTreeNode extends DefaultMutableTreeNode {
    
    Date creationTime;

    /**
     * Construct with a new Operation and set the creation time
     * for cache integrity purposes
     */
    public OperationTreeNode(Operation theOperation) {
	super(theOperation);
	theOperation.containingNode = this;
	this.creationTime = new Date();
    }

    /**
     * Convenience method to return the Operation object
     */
    public Operation getOperation() {
	return (Operation)getUserObject();
    }

    /**
     * Is this node valid? A node is valid if the underlying operation
     * never expires or if the current system time in milliseconds is
     * less than the creation time plus the expiry time for the operation
     */
    public boolean isValid() {
	Operation theOperation = getOperation();
	if (theOperation.getExpiryTime() == -1) {
	    // Never expires therefore always valid
	    return true;
	}
	else if (theOperation.getExpiryTime() == 0) {
	    // Always expires therefore never valid
	    return false;
	}
	// Otherwise check the times
	Date currentDate = new Date();
	Date expiryDate = new Date(this.creationTime.getTime() + 
				   theOperation.getExpiryTime());
	return (currentDate.before(expiryDate));
    }

}
