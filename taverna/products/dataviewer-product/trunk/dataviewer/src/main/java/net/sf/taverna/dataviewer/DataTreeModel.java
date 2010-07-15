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

import javax.swing.tree.DefaultTreeModel;

import net.sf.taverna.dataviewer.DataTreeNode.DataTreeNodeState;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.reference.T2ReferenceType;

import org.apache.log4j.Logger;

/**
 * Model for the tree containing T2References to data.
 * 
 * @author Alex Nenadic
 *
 */
@SuppressWarnings("serial")
public class DataTreeModel extends DefaultTreeModel {

	private static Logger logger = Logger.getLogger(DataTreeModel.class);

	private ReferenceService referenceService;

	public DataTreeModel(T2Reference dataReference, ReferenceService referenceService) {
		
		super(new DataTreeNode(DataTreeNodeState.ROOT));
		
		this.referenceService = referenceService;
		
		createTree(dataReference, (DataTreeNode)this.getRoot());
	}

	// Construct the tree
	public void createTree(T2Reference t2Ref, DataTreeNode parentNode){
		
		// If reference contains a list of data references
		if (t2Ref.getReferenceType() == T2ReferenceType.IdentifiedList) {
			try {
				IdentifiedList<T2Reference> list = referenceService.getListService().getList(t2Ref);
				if (list == null) {
					logger.warn("Could not resolve " + t2Ref);
					return;
				}
				DataTreeNode listNode = new DataTreeNode(t2Ref, DataTreeNodeState.DATA_LIST, referenceService); // list node
				insertNodeInto(listNode, parentNode, parentNode.getChildCount());
				for (T2Reference ref : list) {
					createTree(ref, listNode);
				}
			} catch (NullPointerException e) {
				logger .error("Error resolving data entity list "
						+ t2Ref, e);
			}
		} else { // reference to single data item
			// insert data node
			DataTreeNode dataNode = new DataTreeNode(t2Ref, DataTreeNodeState.DATA_ITEM, referenceService); // data node
			insertNodeInto(dataNode, parentNode, parentNode.getChildCount());
		}	
	}
	
}
