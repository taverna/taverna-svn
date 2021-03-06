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
 * Each Processor inside a workflow will have one of these for each provenance
 * run. Its parent is a {@link ProcessProvenanceItem} and child is an
 * {@link ActivityProvenanceItem}. In theory there could be more than one
 * {@link ActivityProvenanceItem} per processor to cope with failover etc
 * 
 * @author Ian Dunlop
 * @author Stuart Owen
 * @author Paolo Missier
 * 
 */
public class ProcessorProvenanceItem extends AbstractProvenanceItem {

	private ActivityProvenanceItem activityProvenanceItem;
	private String identifier;
	private SharedVocabulary eventType = SharedVocabulary.PROCESSOR_EVENT_TYPE;

	public void setActivityProvenanceItem(
			ActivityProvenanceItem activityProvenanceItem) {
		this.activityProvenanceItem = activityProvenanceItem;
	}

	public ActivityProvenanceItem getActivityProvenanceItem() {
		return activityProvenanceItem;
	}

	public String getProcessorID() {
		return identifier;
	}

	public SharedVocabulary getEventType() {
		return eventType;
	}

}
