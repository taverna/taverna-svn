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
package net.sf.taverna.t2.provenance.item;

import net.sf.taverna.t2.provenance.vocabulary.SharedVocabulary;

/**
 * Contains details of port names and the input data they receive. Parent is an
 * {@link IterationProvenanceItem}
 * 
 * @author Paolo Missier
 * @author Stuart Owen
 * @author Ian Dunlop
 * 
 */
public class InputDataProvenanceItem extends DataProvenanceItem {

	private String processId;
	private String identifier;
	private String parentId;
	private SharedVocabulary eventType = SharedVocabulary.INPUTDATA_EVENT_TYPE;

	/**
	 * Used when generating the xml version by the {@link DataProvenanceItem}.
	 * Identifies this {@link DataProvenanceItem} as containing input
	 */
	protected boolean isInput() {
		return true;
	}

	public InputDataProvenanceItem() {
	}

	public SharedVocabulary getEventType() {
		return eventType;
	}


	public String getIdentifier() {
		return identifier;
	}

	public void setProcessId(String processId) {
		this.processId = processId;
	}

	public String getParentId() {
		// TODO Auto-generated method stub
		return parentId;
	}

	public void setIdentifier(String identifier) {
		this.identifier = identifier;
		// TODO Auto-generated method stub

	}

	public void setParentId(String parentId) {
		this.parentId = parentId;
		// TODO Auto-generated method stub

	}

	public String getProcessId() {
		// TODO Auto-generated method stub
		return processId;
	}

}
