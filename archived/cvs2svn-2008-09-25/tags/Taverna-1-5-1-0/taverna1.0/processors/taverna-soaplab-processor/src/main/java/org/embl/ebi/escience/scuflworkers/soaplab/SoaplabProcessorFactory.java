/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Tom Oinn, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.soaplab;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

/**
 * Implementation of ProcessorFactory that creates SoaplabProcessor nodes
 * 
 * @author Tom Oinn
 */
public class SoaplabProcessorFactory extends ProcessorFactory {

	private String endpoint;

	private String applicationname;

	/**
	 * Create a new factory configured with the specified endpoint base and
	 * application name, which will be concatenated to produce the endpoint URL.
	 */
	public SoaplabProcessorFactory(String endpointbase, String applicationname) {
		init(endpointbase + applicationname);
	}

	/**
	 * Create a new factory with a single application endpoint parameter
	 */
	public SoaplabProcessorFactory(String completeEndpoint) {
		init(completeEndpoint);
	}

	private void init(String completeEndpoint) {
		String[] split = completeEndpoint.split("::");
		if (split.length == 2) {
			// Old form : http://foo.bar.com/root/category::analysisname
			this.applicationname = split[1];
			this.endpoint = split[0] + "." + split[1];
		} else {
			// New form : http://foo.bar.com/root/category.analysisname
			split = completeEndpoint.split("\\.");
			this.applicationname = split[split.length - 1];
			this.endpoint = completeEndpoint;
		}
		setName(this.applicationname);
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
		return "A processor based on Soaplab, with an access endpoint of "
				+ this.endpoint;
	}

	/**
	 * Return the Class object for processors that would be created by this
	 * factory
	 */
	public Class getProcessorClass() {
		return org.embl.ebi.escience.scuflworkers.soaplab.SoaplabProcessor.class;
	}

}
