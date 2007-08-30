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
 * Filename           $RCSfile: NestedWorkflowCompletionEvent.java,v $
 * Revision           $Revision: 1.5 $
 * Release status     $State: Exp $
 * Last modified on   $Date: 2007-05-30 14:08:31 $
 *               by   $Author: sowen70 $
 * Created on 22-Mar-2006
 *****************************************************************/
package org.embl.ebi.escience.scufl.enactor.event;

import java.util.Iterator;
import java.util.Map;

import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.enactor.WorkflowInstance;

/**
 * An event fired when a WorkflowTask has finished its execution.
 * 
 * IMPORTANT: If a listener is using this event to inspect the contained nested workflow instance, its imperitive this gets used within the same thread, since it is soon destroyed once the event is consumed.
 * 
 * @author Stuart Owen
 *
 */
public class NestedWorkflowCompletionEvent extends WorkflowInstanceEvent {
	private WorkflowInstance nestedWorkflowInstance = null;
	
	private boolean isIterating;

	private Map inputMap, outputMap;

	private Processor processor;	

	public boolean isIterating() {
		return isIterating;
	}

    public Map getInputMap() {
		return this.inputMap;
	}

	public Map getOutputMap() {
		return this.outputMap;
	}	

	public Processor getProcessor() {
	    return processor;
	}
	
	/**
	 * Print a summary of the event details
	 */
	public String toString() {
		StringBuffer sb = new StringBuffer();
		sb.append("NestedWorkflow '" + processor.getName() + "' complete ");
		if (isIterating) {
			sb.append("(iterating)");
		} else {
			sb.append("(simple)");
		}
		sb.append("\n");
		String prefix = "in  ";
		for (Iterator i = inputMap.keySet().iterator(); i.hasNext();) {
			String inputKey = (String) i.next();
			DataThing inputThing = (DataThing) inputMap.get(inputKey);
			String mainLSID = inputThing.getLSID(inputThing.getDataObject());
			sb.append(prefix + "'" + inputKey + "'->" + mainLSID + "\n");
			prefix = "    ";
		}
		prefix = "out ";
		for (Iterator i = outputMap.keySet().iterator(); i.hasNext();) {
			String outputKey = (String) i.next();
			DataThing outputThing = (DataThing) outputMap.get(outputKey);
			String mainLSID = outputThing.getLSID(outputThing.getDataObject());
			sb.append(prefix + "" + mainLSID + "->'" + outputKey + "'\n");
			prefix = "    ";
		}
		return sb.toString();
	}

	public NestedWorkflowCompletionEvent(boolean isIterating, Map inputs,
			Map outputs, Processor proc, WorkflowInstance workflow,
			WorkflowInstance nestedWorkflow) {
		super(workflow);
		this.isIterating = isIterating;
		this.inputMap = inputs;
		this.outputMap = outputs;
		this.processor = proc;
		nestedWorkflowInstance = nestedWorkflow;
	}

	public WorkflowInstance getNestedWorkflowInstance() {
		return nestedWorkflowInstance;
	}

}
