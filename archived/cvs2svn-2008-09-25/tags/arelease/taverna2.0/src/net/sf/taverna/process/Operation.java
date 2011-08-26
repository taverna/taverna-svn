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

/**
 * Represents a single Operation, either concrete or abstract
 * @author Tom Oinn
 */
public abstract class Operation {
    
    /**
     * Will be set by the OperationTreeNode on construction
     */
    OperationTreeNode containingNode = null;

    /**
     * Will be set by the OperationTreeNode on construction
     */
    OperationGroup operationGroup = null;

    /**
     * Return the number of milliseconds from creation for which this
     * Operation should be regarded as valid. If this value is equals to
     * -1 then the operation is regarded as never expiring, conversely
     * if equal to 0 it expires instantly and will never be cached. Note
     * that this is only applicable to abstract operations - a concrete
     * operation has no children in the OperationTree and therefore expiry
     * is a meaningless concept in these cases.<p>
     * By default the expiry time is set to -1 meaning that the operation
     * will be resolved at most once then cached.
     */
    public long getExpiryTime() {
	return -1;
    }
    
    /**
     * Return an array of Operations representing those found on the path
     * back to the root (if attached) of the OperationTree that this Operation
     * is located within. If there is no OperationTree defined for this node,
     * that is to say the Operation has never been added as an OperationTreeNode
     * then this will return a single element array containing itself.
     */
    public Operation[] getPath() {
	if (containingNode == null) {
	    return new Operation[]{this};
	}
	else {
	    OperationTreeNode[] treeNodes = (OperationTreeNode[])containingNode.getPath();
	    Operation[] result = new Operation[treeNodes.length];
	    for (int i = 0; i < result.length; i++) {
		result[i] = treeNodes[i].getOperation();
	    }
	    return result;
	}
    }

    /**
     * Return the OperationGroup which contains this Operation, or null if the
     * Operation has not been attached to an OperationGroup at this point
     */
    public OperationGroup getOperationGroup() {
	return this.operationGroup;
    }

    /**
     * Return true if this Operation object is one that can be invoked directly,
     * false if further refinement is required. By default this returns true.
     */
    public boolean isConcrete() {
	return true;
    }
    
    /**
     * Resolve the operation by one level, returning an array of Operation objects
     * to be inserted into the OperationTree as child nodes of this one. By default
     * throws a ResolutionException.
     * @throws ResolutionException if there is a problem resolving the Operation.
     */
    public Operation[] resolve() throws ResolutionException {
	throw new ResolutionException("Cannot resolve a concrete Operation any further.");
    }

    /**
     * Operations may be backed by some set of underlying resources, this method
     * allows the Operation implementation to return a set of these resources. By
     * default returns a new Object[0].<p>
     * Example resource description classes can be found in the resource subpackage
     */
    public Object[] getResources() {
	return new Object[0];
    }

}
