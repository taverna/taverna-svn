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

package net.sf.taverna.data.impl;

import java.util.*;
import javax.swing.tree.*;
import net.sf.taverna.data.*;
import org.w3c.dom.Document;


/**
 * Naive in memory implementation of the DataThing interface
 * @author Tom Oinn
 */
public class InMemoryDataThing extends AbstractDataThing implements MutableTreeNode {

    String lsid;
    DefaultTreeModel treeModel;
    Object dataValue;
    boolean isStructureRoot = false;
    private InMemoryDataThing parent = null;
    private List children = new ArrayList();
    int[] index = new int[0];
    
    /**
     * Implement MutableTreeNode
     */
    public void insert(MutableTreeNode child, int index) {
	if (child instanceof InMemoryDataThing) {
	    children.add(index, child);
	}
    }
    public void remove(int index) {
	children.remove(index);
    }
    public void remove(MutableTreeNode node) {
	children.remove(node);
    }
    public void removeFromParent() {
	this.parent = null;
    }
    public void setParent(MutableTreeNode newParent) {
	this.parent = (InMemoryDataThing)newParent;
    }
    public void setUserObject(Object object) {
	//
    }
    /**
     * Implement TreeNode
     */
    public Enumeration children() {
	final Iterator i = children.iterator();
	return new Enumeration() {
		public boolean hasMoreElements() {
		    return i.hasNext();
		}
		public Object nextElement() {
		    return i.next();
		}
	    };
    }
    public int getChildCount() {
	return this.children.size();
    }
    public TreeNode getChildAt(int index) {
	return (TreeNode)this.children.get(index);
    }
    public TreeNode getParent() {
	return this.parent;
    }
    
    public int[] getIndex() {
	return this.index;
    }

    public Object getValue() {
	return this.dataValue;
    }
    
    public int getDepth() {
	if (this.children.isEmpty()) {
	    return 0;
	}
	else {
	    return ((InMemoryDataThing)this.children.get(0)).getDepth()+1;
	}
    }

    public DataThing getRelated(int[] index) 
	throws DataThingIndexException {
	return getRelated((InMemoryDataThing)treeModel.getRoot(), index);
    }
    private DataThing getRelated(InMemoryDataThing searchFrom, int[] index) 
	throws DataThingIndexException {
	if (index.length == 0) {
	    return searchFrom;
	}
	else {
	    if (index[0] > searchFrom.getChildCount()) {
		throw new DataThingIndexException("Failed to find index "+index[0]);
	    }
	    int[] newIndex = new int[index.length - 1];
	    for (int i = 0; i < index.length - 1; i++) {
		newIndex[i] = index[i];
	    }
	    return getRelated((InMemoryDataThing)searchFrom.getChildAt(index[0]), newIndex);
	}
    }

    public DataSemanticMarkup getSemanticMarkup() {
	return null;
    }

    public InMemoryDataThing(String id, Object o) {
	this.lsid = id;
	this.treeModel = new DefaultTreeModel(this);
	this.dataValue = convert(o, this);
	this.isStructureRoot = true;
    }

    private InMemoryDataThing(InMemoryDataThing root, Object convertedObject, int[] index) {
	this.lsid = root.lsid;
	this.treeModel = root.treeModel;
	this.dataValue = convertedObject;
	this.isStructureRoot = false;
	this.index = index;
    }

    private Object convert(Object o, InMemoryDataThing parent) {
	if (o instanceof Object[] &&
	    o instanceof byte[] == false) {
	    List l = new DataThingList();
	    for (int i = 0; i < ((Object[])o).length; i++) {
		InMemoryDataThing objectThing = new InMemoryDataThing(this, null, appendInt(parent.index, i));
		Object o2 = convert(((Object[])o)[i], objectThing);
		treeModel.insertNodeInto(objectThing, parent, i);
		l.add(o2);
	    }
	    parent.dataValue  = l;
	    return l;
	}
	else if (o instanceof List) {
	    List l = new DataThingList();
	    int j = 0;
	    for (Iterator i = ((List)o).iterator(); i.hasNext();) {
		InMemoryDataThing objectThing = new InMemoryDataThing(this, null, appendInt(parent.index, j++));
		treeModel.insertNodeInto(objectThing, parent, j);
		l.add(convert(i.next(), objectThing));
	    }	 
	    parent.dataValue = l;
	    return l;
	}
	else {
	    parent.dataValue = o;
	    return o;
	}
    }
    private int[] appendInt(int[] source, int newInt) {
	int[] result = new int[source.length+1];
	for (int i = 0; i < source.length; i++) {
	    result[i] = source[i];
	}
	result[source.length] = newInt;
	return result;
    }


    public String getLSID() {
	return this.lsid + indexToString();
    }
    private String indexToString() {
	if (index.length == 0) {
	    return "";
	}
	StringBuffer sb = new StringBuffer();
	for (int i = 0; i < index.length; i++) {
	    sb.append("."+index[i]);
	}
	return sb.toString();
    }
    
    /** 
     * Trivial marker class to detect when we have
     * lists within the collection structure as opposed
     * to lists within the data items themselves
     */
    class DataThingList extends ArrayList {
	//
    }
    
}
