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

import java.util.Collection;

import net.sf.taverna.t2.activities.dataflow.DataflowActivity;
import net.sf.taverna.t2.workbench.file.FileManager;
import net.sf.taverna.t2.workflowmodel.Dataflow;
import net.sf.taverna.t2.workflowmodel.Processor;
import net.sf.taverna.t2.workflowmodel.processor.activity.NestedDataflowSource;
import net.sf.taverna.t2.workflowmodel.utils.Tools;

/**
 * A source description for a nested dataflow, opened from a
 * {@link DataflowActivity} within an a {@link Processor} which is in the parent
 * {@link Dataflow}.
 * 
 * @author Stian Soiland-Reyes
 * 
 */
public class NestedDataflowActivitySource implements NestedDataflowSource<DataflowActivity> {

	public final DataflowActivity dataflowActivity;

	public final Dataflow parentDataflow;

	public NestedDataflowActivitySource(Dataflow parentDataflow,
			DataflowActivity dataflowActivity) {
		this.parentDataflow = parentDataflow;
		this.dataflowActivity = dataflowActivity;
	}

	public DataflowActivity getNestedDataflow() {
		return dataflowActivity;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime
				* result
				+ ((dataflowActivity == null) ? 0 : dataflowActivity.hashCode());
		result = prime * result
				+ ((parentDataflow == null) ? 0 : parentDataflow.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		final NestedDataflowActivitySource other = (NestedDataflowActivitySource) obj;
		if (dataflowActivity == null) {
			if (other.dataflowActivity != null)
				return false;
		} else if (!dataflowActivity.equals(other.dataflowActivity))
			return false;
		if (parentDataflow == null) {
			if (other.parentDataflow != null)
				return false;
		} else if (!parentDataflow.equals(other.parentDataflow))
			return false;
		return true;
	}

	public Dataflow getParentDataflow() {
		return parentDataflow;
	}
	
	@Override
	public String toString() {
		StringBuilder sb = new StringBuilder();
		//sb.append("Nested workflow");
		
		Collection<Processor> processors = Tools.getProcessorsWithActivity(getParentDataflow(),
				getNestedDataflow());
		if (! processors.isEmpty()) {
			Processor processor = processors.iterator().next();
			//sb.append(' ');
			sb.append(processor.getLocalName());
			sb.append(" in ");
			// TODO: Is this safe? This might make a loop if a nested workflow has a parent
			// in a nested workflow..
			sb.append(FileManager.getInstance().getDataflowName(getParentDataflow()));
		} else {
			sb.append("Nested workflow");
		}
		return sb.toString();
	}
	
}