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
package net.sf.taverna.t2.testing;

import net.sf.taverna.t2.annotation.AbstractAnnotatedThing;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.ReferenceService;
import net.sf.taverna.t2.workflowmodel.Datalink;
import net.sf.taverna.t2.workflowmodel.EventHandlingInputPort;
import net.sf.taverna.t2.workflowmodel.Port;

public class DummyEventHandler extends AbstractAnnotatedThing<Port> implements
		EventHandlingInputPort {

	protected int eventCount = 0;
	public ReferenceService referenceService;
	private Object result;

	public DummyEventHandler(ReferenceService referenceService) {
		super();
		this.referenceService = referenceService;
	}

	public void receiveEvent(WorkflowDataToken token) {
		eventCount++;
		result = referenceService.renderIdentifier(token.getData(),
				Object.class, null);
		System.out.println(token);
	}

	public Object getResult() {
		return result;
	}

	public int getEventCount() {
		return this.eventCount;
	}

	public void reset() {
		this.eventCount = 0;
		this.result = null;
	}

	public int getDepth() {
		return 0;
	}

	public String getName() {
		return "Test port";
	}

	public Datalink getIncomingLink() {
		// TODO Auto-generated method stub
		return null;
	}

}
