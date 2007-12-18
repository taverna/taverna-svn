/*
 * Copyright 2005 Tom Oinn, EMBL-EBI
 *
 *  This file is part of Taverna.  Further information, and the
 *  latest version, can be found at http://taverna.sf.net
 * 
 *  Taverna is in turn part of the myGrid project, more details
 *  can be found at http://www.mygrid.org.uk
 *
 *  Taverna is free software; you can redistribute it and/or modify
 *  it under the terms of the GNU Lesser General Public License as
 *  published by the Free Software Foundation; either version 2.1
 *  of the License, or (at your option) any later version.
 *
 *  Taverna is distributed in the hope that it will be useful, but
 *  WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *  GNU Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with Taverna; if not, write to the Free Software
 *  Foundation, Inc., 59 Temple Place, Suite 330, Boston, MA  02111-1307  USA
 */

package net.sf.taverna.interaction.workflow.processor;

// Interaction interfaces
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import net.sf.taverna.interaction.workflow.InteractionEvent;
import net.sf.taverna.interaction.workflow.InteractionReceipt;
import net.sf.taverna.interaction.workflow.InteractionRequest;
import net.sf.taverna.interaction.workflow.InteractionService;
import net.sf.taverna.interaction.workflow.InteractionStateListener;
import net.sf.taverna.interaction.workflow.SubmissionException;
import net.sf.taverna.interaction.workflow.TerminalInteractionStatus;
import net.sf.taverna.interaction.workflow.impl.DefaultInteractionRequest;

import org.apache.log4j.Logger;
import org.embl.ebi.escience.baclava.DataThing;
import org.embl.ebi.escience.scufl.IProcessorTask;
import org.embl.ebi.escience.scufl.Port;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scuflworkers.ProcessorTaskWorker;

import uk.ac.soton.itinnovation.taverna.enactor.entities.TaskExecutionException;

/**
 * Task to send an interaction request to the interaction service, wait for a
 * response then fetch any result data and proceed.
 * 
 * @author Tom Oinn
 */
public class InteractionServiceTask implements ProcessorTaskWorker {

	private InteractionServiceProcessor processor;

	private static Logger logger = Logger
			.getLogger(InteractionServiceTask.class);

	/**
	 * Create a new task to represent the specified processor
	 */
	public InteractionServiceTask(Processor p) {
		this.processor = (InteractionServiceProcessor) p;
	}

	/**
	 * Fetch and remove the email address from the input map, use the remaining
	 * inputs and properties of the processor to construct an
	 * InteractionRequest, submit this to the InteractionService defined by the
	 * processor then wait for completion or failure events from the
	 * InteractionReceipt
	 */
	public Map execute(Map inputMap, IProcessorTask parentTask)
			throws TaskExecutionException {
		try {
			// Reject any attempts to invoke without a target email address
			if (inputMap.containsKey("emailAddress") == false) {
				throw new TaskExecutionException(
						"Must specify an email address for the "
								+ "Interaction Service processor.");
			}
			DataThing addressThing = (DataThing) inputMap.get("emailAddress");
			String email = (String) addressThing.getDataObject();

			// Reject attempts to invoke if any of the inputs are unbound
			// in this workflow, avoids sending invalid interaction requests
			// that are guaranteed to fail
			Port[] inputs = processor.getInputPorts();
			for (int i = 0; i < inputs.length; i++) {
				String inputPortName = inputs[i].getName();
				if (inputMap.containsKey(inputPortName) == false) {
					throw new TaskExecutionException("Required input '"
							+ inputPortName + "' " + "is not bound, failing.");
				}
			}

			// Clone the input map and remove the email address from it
			Map otherInputs = new HashMap(inputMap);
			otherInputs.remove("emailAddress");

			// Create a Date to represent the expiry time
			long currentTime = new Date().getTime();
			Date expiryTime = new Date(currentTime
					+ processor.getRequestLifetime());

			// Use the InteractionService to submit a new InteractionRequest
			// based
			// on the properties gathered so far
			InteractionRequest request = DefaultInteractionRequest
					.createRequest(processor.getInteractionPattern(), email,
							otherInputs, expiryTime);

			// Submit the request to the interaction service proxy
			final InteractionReceipt receipt;
			try {
				InteractionService service = processor.getInteractionService();
				receipt = service.submitRequest(request);
			} catch (SubmissionException se) {
				throw new TaskExecutionException(
						"Submission to interaction service failed", se);
			}

			// Create a new callback for the InteractionReceipt then go to
			// sleep,
			// the callback will wake this thread up if anything interesting
			// happens.
			final Thread currentThread = Thread.currentThread();
			receipt.addInteractionStateListener(new InteractionStateListener() {
				public void stateChanged(InteractionEvent ie) {
					if (receipt.getInteractionStatus() instanceof TerminalInteractionStatus) {
						currentThread.interrupt();
					}
				}
			});

			// Test for completion of the callback here to avoid a potential
			// race condition where the callback interrupts this thread before
			// its started trying to sleep
			if (receipt.getInteractionStatus() instanceof TerminalInteractionStatus == false) {
				try {
					while (true) {
						Thread.sleep(1000);
					}
				} catch (InterruptedException ie) {
					// Fine, we've been woken up by the callback thread
				}
			}

			TerminalInteractionStatus status = (TerminalInteractionStatus) receipt
					.getInteractionStatus();
			if (status.getStatusCode() != TerminalInteractionStatus.COMPLETED) {
				throw new TaskExecutionException(
						"Interaction receipt returned but failed.");
			} else {
				return (Map) status.getResultData();
			}
		} catch (Exception ex) {
			// Rethrow any TaskExecutionExceptions, otherwise create
			// new ones with the underlying exception as their cause
			if (ex instanceof TaskExecutionException) {
				throw (TaskExecutionException) ex;
			}
			ex.printStackTrace();
			TaskExecutionException te = new TaskExecutionException(
					"Error occured during invocation " + ex.getMessage());
			te.initCause(ex);
			throw te;
		}
	}

}
