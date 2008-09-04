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
/**
 * 
 */
package net.sf.taverna.t2.activities.dataflow.filemanager;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;

/**
 * A source description for a nested dataflow, opened from a
 * {@link DataflowActivity} within an a {@link Processor} which is in the parent
 * {@link Dataflow}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class NestedDataflowSource {

	private final DataflowActivity dataflowActivity;

	private final Dataflow parentDataflow;

	public NestedDataflowSource(Dataflow parentDataflow,
			DataflowActivity dataflowActivity) {
		this.parentDataflow = parentDataflow;
		this.dataflowActivity = dataflowActivity;
	}

	public DataflowActivity getDataflowActivity() {
		return dataflowActivity;
	}

	public Dataflow getParentDataflow() {
		return parentDataflow;
	}
}
