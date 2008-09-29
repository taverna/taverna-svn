/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

// import java.lang.Class;
// import java.lang.String;

/**
 * Implementation of ProcessorFactory that creates
 * BiomobyProcessor nodes. <p>
 *
 * @version $Id: BiomobyProcessorFactory.java,v 1.1.2.1 2006-07-05 16:40:12 davidwithers Exp $
 * @author Martin Senger
 */
public class BiomobyProcessorFactory extends ProcessorFactory {

    private String mobyEndpoint;

    private String serviceName;

    private String authorityName;

    /**
     * 
     * @return the mobycentral registry endpoint
     */
    public String getMobyEndpoint() {
        return this.mobyEndpoint;
    }

    /**
     * 
     * @return the name of the service
     */
    public String getServiceName() {
        return this.serviceName;
    }

    /**
     * 
     * @return the moby service instance authority's name
     */
    public String getAuthorityName() {
        return this.authorityName;
    }

    /**
     * Create a new factory configured with the specified
     * endpoint base and application name, which will 
     * be concatenated to produce the endpoint URL.
     */
    public BiomobyProcessorFactory(String mobyEndpoint, String authorityName,
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
        return "A processor based on Biomoby registry located at "
                + mobyEndpoint;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
        return org.biomoby.client.taverna.plugin.BiomobyProcessor.class;
    }

}