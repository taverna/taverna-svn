/*******************************************************************************
 * Copyright (C) 2010 The University of Manchester   
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
package net.sf.taverna.t2.activities.dataflow;

import java.net.URI;

import net.sf.taverna.t2.workflowmodel.Edits;
import net.sf.taverna.t2.workflowmodel.processor.activity.ActivityFactory;

/**
 * An {@link ActivityFactory} for creating <code>DataflowActivity</code>.
 * 
 * @author David Withers
 */
public class DataflowActivityFactory implements ActivityFactory {

	private Edits edits;
	
	@Override
	public DataflowActivity createActivity() {
		return new DataflowActivity();
	}

	@Override
	public URI getActivityURI() {
		return URI.create(DataflowActivity.URI);
	}

	@Override
	public Object createActivityConfiguration() {
		return edits.createDataflow();
	}

	/**
	 * Sets the edits.
	 * 
	 * @param edits the edits to set
	 */
	public void setEdits(Edits edits) {
		this.edits = edits;
	}

}
