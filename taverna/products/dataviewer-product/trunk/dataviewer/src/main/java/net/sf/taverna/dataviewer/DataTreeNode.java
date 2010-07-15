/*******************************************************************************
 * Copyright (C) 2007 The University of Manchester   
 * 
 *  Modifications to the initial code base are copyright of their
 *  respective authors, or their employers as appropriate.
 * 
 *  This program is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public License
 *  as published by the Free Software Foundation; either version 2.1 of
 *  the License, or (at your option) any later version.
 *    
 *  This program is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *    
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this program; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 ******************************************************************************/
package net.sf.taverna.dataviewer;

import java.util.ArrayList;
import java.util.List;

import javax.swing.tree.DefaultMutableTreeNode;

import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

import org.apache.log4j.Logger;

/**
 * Node in the data tree that contains a T2Reference to the actual data item.
 * Inspired by {@link net.sf.taverna.t2.workbench.views.results.workflow.WorkflowResultTreeNode}.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class DataTreeNode extends DefaultMutableTreeNode {

	@SuppressWarnings("unused")
	private static Logger logger = Logger.getLogger(DataTreeNode.class);

	private T2Reference dataReference;

	private ReferenceService referenceService;

	public enum DataTreeNodeState {
		ROOT, DATA_LIST, DATA_ITEM
	};

	private DataTreeNodeState state;

	public DataTreeNode(T2Reference reference, DataTreeNodeState state, ReferenceService referenceService) {
		this.dataReference = reference;
		this.state = state;
		this.referenceService = referenceService;
	}

	public DataTreeNode(DataTreeNodeState state) {
		this.dataReference = null;
		this.state = state;
	}

	public DataTreeNodeState getState() {
		return state;
	}

	public void setState(DataTreeNodeState state) {
		this.state = state;
	}

	public T2Reference getDataReference() {
		return dataReference;
	}
	
	public void setReference(T2Reference dataReference) {
		this.dataReference = dataReference;
	}

	public String toString() {
		if (state.equals(DataTreeNodeState.ROOT)) {
			return "Data values:";
		}
		if (state.equals(DataTreeNodeState.DATA_LIST)) {
			if (getChildCount() == 0) {
				return "Empty list";
			}
			return "List...";
		}
		return dataReference.toString();
	}

	public boolean isState(DataTreeNodeState state) {
		return this.state.equals(state);
	}

	public int getValueCount() {
		int result = 0;
		if (isState(DataTreeNodeState.DATA_ITEM)) {
			result = 1;
		} else if (isState(DataTreeNodeState.DATA_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				DataTreeNode child = (DataTreeNode) this.getChildAt(i);
				result += child.getValueCount();
			}
		}
		return result;
	}

	public int getSublistCount() {
		int result = 0;
		if (isState(DataTreeNodeState.DATA_LIST)) {
			int childCount = this.getChildCount();
			for (int i = 0; i < childCount; i++) {
				DataTreeNode child = (DataTreeNode) this.getChildAt(i);
				if (child.isState(DataTreeNodeState.DATA_LIST)) {
					result++;
				}
			}
		}
		return result;
	}

	public Object getAsObject() {
		if (dataReference != null) {
			Identified identified = referenceService.resolveIdentifier(
					dataReference, null, null);
			if (identified instanceof ErrorDocument) {
				ErrorDocument errorDocument = (ErrorDocument) identified;
				return errorDocument.getMessage();
			}
		}
		if (isState(DataTreeNodeState.ROOT)) {
			if (getChildCount() == 0) {
				return null;
			} else {
				return ((DataTreeNode) getChildAt(0)).getAsObject();
			}
		}
		if (isState(DataTreeNodeState.DATA_LIST)) {
			List<Object> result = new ArrayList<Object>();
			for (int i = 0; i < getChildCount(); i++) {
				DataTreeNode child = (DataTreeNode) getChildAt(i);
				result.add(child.getAsObject());
			}
			return result;
		}
		if (dataReference == null) {
			return null;
		}
		try {
			Object result = referenceService.renderIdentifier(dataReference,
					Object.class, null);
			return result;
		} catch (Exception e) {
			// Not good to catch exception but
			return null;
		}
	}
}
