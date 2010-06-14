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
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.reference.T2Reference;

/**
 * When the {@link WorkflowInstanceFacade} for a processor receives a data token
 * one of these is created. This is especially important for data which flows
 * straight through a facade without going into the dispatch stack (a rare event
 * but it can happen)
 * 
 * @author Ian Dunlop
 * 
 */
public class WorkflowDataProvenanceItem extends AbstractProvenanceItem {

	private ReferenceService referenceService;
	/** The port name that the data is for */
	private String portName;
	/** A reference to the data token received in the facade */
	private T2Reference data;
	private SharedVocabulary eventType = SharedVocabulary.WORKFLOW_DATA_EVENT_TYPE;
	private boolean isFinal;
	private int[] index;
	private boolean isInputPort;

	public boolean isInputPort() {
		return isInputPort;
	}

	public void setInputPort(boolean isInputPort) {
		this.isInputPort = isInputPort;
	}

	public WorkflowDataProvenanceItem() {
	}

	public SharedVocabulary getEventType() {
		return eventType;
	}

	public void setPortName(String portName) {
		this.portName = portName;
	}

	public String getPortName() {
		return portName;
	}

	public void setData(T2Reference data) {
		this.data = data;
	}

	public T2Reference getData() {
		return data;
	}

	public void setReferenceService(ReferenceService referenceService) {
		this.referenceService = referenceService;
	}

	public ReferenceService getReferenceService() {
		return referenceService;
	}

	public void setIndex(int[] index) {
		this.index = index;
	
	}
	
	public int[] getIndex() {
		return index;
	}

	public void setFinal(boolean isFinal) {
		this.isFinal = isFinal;
	}
	
	public boolean isFinal() {
		return isFinal;
	}	

}
