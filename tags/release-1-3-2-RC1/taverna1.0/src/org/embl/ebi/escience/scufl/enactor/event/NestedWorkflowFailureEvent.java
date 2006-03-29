/*
 * Copyright (C) 2003 The University of Manchester 
 *
 * Modifications to the initial code base are copyright of their
 * respective authors, or their employers as appropriate.  Authorship
 * of the modifications may be determined from the ChangeLog placed at
 * the end of this file.
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU Lesser General Public License
 * as published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 * 
 * This program is distributed in the hope that it will be useful, but
 * WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 * Lesser General Public License for more details.
 * 
 * You should have received a copy of the GNU Lesser General Public
 * License along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA 02111-1307
 * USA.
 *
 ****************************************************************
 * Source code information
 * -----------------------
 * Filename           $RCSfile: NestedWorkflowFailureEvent.java,v $
 * Revision           $Revision: 1.1 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2006-03-22 15:19:32 $
 *               by   $Author: sowen70 $
 * Created on 22-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl.enactor.event;

import java.util.Map;

import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

public class NestedWorkflowFailureEvent extends ProcessFailureEvent {

	private WorkflowInstance nestedWorkflow = null;

	public NestedWorkflowFailureEvent(WorkflowInstance workflow, Processor processor, Exception cause, Map inputs,
			WorkflowInstance nestedWorkflow) {
		super(workflow, processor, cause, inputs);
		this.nestedWorkflow = nestedWorkflow;
	}

	public WorkflowInstance getNestedWorkflow() {
		return this.nestedWorkflow;
	}

}
