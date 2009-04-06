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
import net.sf.taverna.t2.reference.ErrorDocument;
import net.sf.taverna.t2.reference.Identified;
import net.sf.taverna.t2.reference.IdentifiedList;
import net.sf.taverna.t2.reference.ReferenceServiceException;
import net.sf.taverna.t2.reference.StackTraceElementBean;
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
			System.out.println("Output reference made = " + reference);
			Object value;
			if (reference.containsErrors()) {
				System.out.println("Contains errors!");
				printAllErrors(context.getReferenceService().resolveIdentifier(reference, null, context));				
			}
			try {
				value = context.getReferenceService().renderIdentifier(
						reference, Object.class, context);
			} catch (ReferenceServiceException ex) {
				ex.printStackTrace();
				value = reference;
			}
			resultMap.put(portname, value);
			synchronized (this) {
				outputCount--;
			}
		}
	}

	@SuppressWarnings("unchecked")
	public void printAllErrors(Identified identified) {
		if (! identified.getId().containsErrors()) {
			return;
		}
		if (identified instanceof ErrorDocument) {
			ErrorDocument errorDoc = (ErrorDocument)identified;
			System.err.println("ERROR: " + identified.getId());
			System.err.println(" message: " + errorDoc.getMessage());
			if (errorDoc.getStackTraceStrings().isEmpty()) {
				System.err.println(" errorMessage: " + errorDoc.getExceptionMessage());
			} else {
				System.err.println(" stacktrace:");
				System.err.println(errorDoc.getExceptionMessage());
				for (StackTraceElementBean stackTrace : errorDoc.getStackTraceStrings()) {
					System.err.println("	at " + stackTrace.getClassName() + "." + stackTrace.getMethodName() + "(" + stackTrace.getFileName() + ":" + stackTrace.getLineNumber() + ")");
				}
			}
			if (!errorDoc.getErrorReferences().isEmpty()) {
				System.err.print("Caused by ");
				for (T2Reference errorRef : errorDoc.getErrorReferences()) {
					printAllErrors(context.getReferenceService().resolveIdentifier(errorRef, null, context));
				}
			}
			System.err.println("(end of workflow error " + identified.getId() + ")");
		} else if (identified instanceof IdentifiedList) {
			IdentifiedList list = (IdentifiedList) identified;
			for (Object object : list) {
				if (object instanceof Identified) {
					printAllErrors((Identified)object);
				}
			}			
		} else {
			// OK, no errors to report
		}
	}

	public boolean isFinished() {
		synchronized (this) {
			return outputCount == 0;
		}
	}

	public Object getResult(String outputName) {
		return resultMap.get(outputName);
	}

}
