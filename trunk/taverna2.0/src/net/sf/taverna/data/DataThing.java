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

package net.sf.taverna.data;

import javax.swing.tree.TreeNode;

/**
 * Represents a single data item within a Taverna process flow. Allows
 * for implementations to back out the actual data storage i.e. to disk
 * or, more sophisticated, to something like SRB.<p>
 * All data items in Taverna exist conceptually within a collection structure,
 * specifically a tree where non leaf nodes are collections and leaf nodes
 * are concrete data values. Such trees must have a constant depth across
 * all leaf nodes, so no non leaf node will ever have both leaf and non leaf
 * children. This property is represented within this interface by the getIndex
 * method which returns an array of int values. The array is interpreted as
 * being the path of indices from the root to the node that this represents.
 * An empty array in this context means that the object is the root of the
 * data collection.<p>
 * Similarly the getDepth method returns the number of levels below this node
 * the tree extends. For a leaf node this will be zero. A single data item
 * not contained within a collection will therefore have a getDepth of zero
 * and getIndex containing a zero length int[]<p>
 * This interface extends javax.swing.tree.TreeNode, most of the methods in
 * this are trivial to implement as wrappers around the methods explicitly
 * defined here. By forcing implementations to implement TreeNode as well
 * the various UI aspects such as DataThing display in the result browser
 * should be massively simplified. *
 * @author Tom Oinn
 */
public interface DataThing extends TreeNode {

    /**
     * Returns an array of int values interpreted as
     * being the path of indices from the root to the node that this represents.
     * An empty array in this context means that the object is the root of the
     * data collection.
     */
    public int[] getIndex();
    
    /**
     * Returns the number of levels below this node
     * the tree extends. For a leaf node this will be zero. A single data item
     * not contained within a collection will therefore have a getDepth of zero
     * and getIndex containing a zero length int[]
     */
    public int getDepth();
    
    /**
     * Returns the value of this DataThing as a java object. Depending on the
     * implementation this object may or may not provide a sensible equals() 
     * method, so it's not guaranteed that the following code would produce
     * a sensible result :
     * <pre>
     * DataThing dt = ....
     * Object o1 = dt.getValue();
     * Object o2 = dt.getValue();
     * System.out.println("Values match : "+(o1.equals(o2)));
     * </pre>
     * Common sense would suggest that this would always return true, but as
     * with many things involving computers common sense may well not be a valid
     * assumption. Be aware of the implementation details!<p>
     * In instances where this node is a collection (i.e. has a getDepth value
     * greater than zero) this should return an object implementing the TavernaList
     * interface - collection structures in Taverna are inherently ordered. In other
     * cases it will return an arbitrary object. This allows tasks to return lists
     * of List object, in the previous architecture this wasn't possible as we used
     * a generic Java List rather than our own implementation.
     */
    public Object getValue();
    
    /**
     * Get the LSID of this data item. The LSID specification mandates various
     * properties of objects refered to by LSIDs, probably worth taking a look
     * at that specification at 
     * <href="http://lsid.sourceforge.net/">http://lsid.sourceforge.net</href> 
     * for more details.
     */
    public String getLSID();

    /**
     * Returns another DataThing locator from the same collection structure
     * as this one. This must satisfy <code>dt.getRelated(dt.getIndex()) == dt</code>
     * for all DataThing implementations. To get the root of any collection
     * you can use the call 'getRelated(new int[0])', this is the only call
     * that must never fail.
     * @exception DataThingIndexException if the specified index doesn't
     * exist within the collection containing the target object.
     */
    public DataThing getRelated(int[] index) throws DataThingIndexException;

    /**
     * Returns a DataSemanticMarkup implementation containing metadata about
     * this DataThing.
     */
    public DataSemanticMarkup getSemanticMarkup();
    
}
