/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

/**
 * Event corresponding to the start of the invocation
 * process in the absence of implicit iteration
 * @author Tom Oinn
 */
public class Invoking extends ProcessEvent {
    
    private String retryCount = "0";
    private String timeout = "0";
    
    public String getRetryCount() {
	return retryCount;
    }

    public String getTimeout() {
	return timeout;
    }
    
}
