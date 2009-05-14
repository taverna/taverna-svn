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
package net.sf.taverna.t2.monitor;

import javax.swing.tree.TreeModel;

/**
 * Aggregation of MonitorReceiver and TreeModel
 * <p>
 * Tracks the instantaneous state of a workflow invocation instance represented
 * as a tree of nodes where the tree structure mirrors the hierarchical
 * structure of the invocation and where nodes are associated with entities in
 * the workflow definition. Nodes may have named properties, some of which may
 * be mutable.
 * <p>
 * Nodes are identified by their owning process, which is an array of strings,
 * for instance ["dataflow2", "processor5", "fish"]. Node values are instances
 * of MonitorNode, with the root being a MonitorNode that has the Monitor as its
 * 'workflow object' field and an empty owning process array. Monitor extends
 * the TreeModel interface, use this interface to attach any listeners you need
 * to the monitor; TreeModel cannot be made a generic type without substantial
 * modification but all nodes <em>will</em> be instances of MonitorNode in a
 * compliant implementation.
 * 
 * @author Tom Oinn
 * @author Stian Soiland-Reyes
 */
public interface Monitor extends TreeModel, MonitorReceiver {

}
