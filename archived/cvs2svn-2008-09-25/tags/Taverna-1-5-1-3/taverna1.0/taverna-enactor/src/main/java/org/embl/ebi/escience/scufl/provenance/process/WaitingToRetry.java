/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

/**
 * Event corresponding to the processor entering a sleep state while it waits to
 * retry.
 * 
 * @author Tom Oinn
 */
public class WaitingToRetry extends ProcessEvent {

	private String timeDelay, retryNumber, maxRetries;

	public String getTimeDelay() {
		return timeDelay;
	}

	public String getRetryNumber() {
		return retryNumber;
	}

	public String getMaxRetries() {
		return maxRetries;
	}

	public WaitingToRetry(int timeDelay, int retryNumber, int maxRetries) {
		super();
		this.timeDelay = "" + timeDelay;
		this.retryNumber = "" + retryNumber;
		this.maxRetries = "" + maxRetries;
	}

}
