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

import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.t2.facade.ResultListener;
import net.sf.taverna.t2.invocation.InvocationContext;
import net.sf.taverna.t2.invocation.WorkflowDataToken;
import net.sf.taverna.t2.reference.T2Reference;
import net.sf.taverna.t2.workflowmodel.Dataflow;

public class CaptureResultsListener implements ResultListener {

	private int outputCount;
	private Map<String, Object> resultMap = new HashMap<String, Object>();
	private final InvocationContext context;

	public CaptureResultsListener(Dataflow dataflow, InvocationContext context) {

		this.context = context;
		outputCount = dataflow.getOutputPorts().size();
	}

	public void resultTokenProduced(WorkflowDataToken dataToken, String portname) {
		if (dataToken.getIndex().length == 0) {
			T2Reference reference = dataToken.getData();
			System.out.println("Output reference = " + reference);
			resultMap.put(portname, context.getReferenceService()
					.renderIdentifier(reference, Object.class, context));
			outputCount--;
		}
	}

	public boolean isFinished() {
		return outputCount == 0;
	}

	public Object getResult(String outputName) {
		return resultMap.get(outputName);
	}

}
