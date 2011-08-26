/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scufl.provenance.process;

/**
 * Event corresponding to the start of the invocation
 * process with implicit iteration
 * @author Tom Oinn
 */
public class InvokingWithIteration extends Invoking {
    
    private String iterationNumber = "0";
    private String iterationTotal = "0";
    
    public InvokingWithIteration() {
	super();
    }
    
    public InvokingWithIteration(int iterationNumber,
				 int iterationTotal) {
	super();
	this.iterationNumber = ""+iterationNumber;
	this.iterationTotal = ""+iterationTotal;
    }

    public String getIterationNumber() {
	return this.iterationNumber;
    }
    
    public String getIterationTotal() {
	return this.iterationTotal;
    }
    
}
