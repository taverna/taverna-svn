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
package net.sf.taverna.t2.workflowmodel.impl;

import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.EditException;
import net.sf.taverna.t2.workflowmodel.NamingException;
import net.sf.taverna.t2.workflowmodel.Merge;

/**
 * Removes a merge from a dataflow.
 * 
 * @author David Withers
 */
public class RemoveMergeEdit extends AbstractDataflowEdit{
	
	private Merge merge;
	
	public RemoveMergeEdit(Dataflow dataflow, Merge merge) {
		super(dataflow);
		this.merge = merge;
	}

	@Override
	protected void doEditAction(DataflowImpl dataflow) throws EditException {
		dataflow.removeMerge(merge);
	}

	@Override
	protected void undoEditAction(DataflowImpl dataflow) {
		if (merge instanceof MergeImpl) {
			try {
				dataflow.addMerge((MergeImpl) merge);
			} catch (NamingException e) {
				//a merge with this name has already been removed
			}
		}
	}
	
}
