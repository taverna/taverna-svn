/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

public class BiomobyObjectProcessorFactory extends ProcessorFactory {

    private String mobyEndpoint;

    private String serviceName;

    private String authorityName;

    /**
     * 
     * @return the mobycentral registry endpoint used by this processor
     */
    public String getMobyEndpoint() {
        return this.mobyEndpoint;
    }
    /**
     * 
     * @return the name of the Moby datatype
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * 
     * @return the authority name. Not really used.
     */
    public String getAuthorityName() {
        return this.authorityName;
    }

    /**
     * Create a new factory configured with the specified
     * endpoint base and application name, which will 
     * be concatenated to produce the endpoint URL.
     */
    public BiomobyObjectProcessorFactory(String mobyEndpoint, String authorityName,
            String serviceName) {
        this.mobyEndpoint = mobyEndpoint;
        this.authorityName = authorityName;
        this.serviceName = serviceName;
        setName(serviceName);
    }

    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
        return "A MOBY Object processor based on Biomoby registry located at "
                + mobyEndpoint;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
        return org.biomoby.client.taverna.plugin.BiomobyObjectProcessor.class;
    }

}
