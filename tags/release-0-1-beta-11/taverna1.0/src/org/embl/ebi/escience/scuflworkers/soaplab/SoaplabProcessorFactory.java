/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor;
import java.lang.Class;
import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * SoaplabProcessor nodes
 * @author Tom Oinn
 */
public class SoaplabProcessorFactory extends ProcessorFactory {

    private String endpoint;
    private String applicationname;

    /**
     * Create a new factory configured with the specified
     * endpoint base and application name, which will 
     * be concatenated to produce the endpoint URL.
     */
    public SoaplabProcessorFactory(String endpointbase, String applicationname) {
	this.endpoint = endpointbase+applicationname;
	String[] split = applicationname.split(":");
	this.applicationname = split[split.length - 1];
    }
    
    /**
     * Create a new factory with a single application endpoint parameter
     */
    public SoaplabProcessorFactory(String completeEndpoint) {
	this.endpoint = completeEndpoint;
	String[] split = applicationname.split(":");
	this.applicationname = split[split.length - 1];
    }

    /**
     * Return the application name as the toString result
     */
    public String toString() {
	return this.applicationname;
    }
    
    /**
     * Return the endpoint
     */
    public String getEndpoint() {
	return this.endpoint;
    }
    
    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
	return "A processor based on Soaplab, with an access endpoint of "+this.endpoint;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor.class;
    }
    
}
