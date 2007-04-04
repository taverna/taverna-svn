/*
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI & Edward Kawas, The BioMoby Project
 */
package org.biomoby.client.taverna.plugin;

import org.biomoby.client.CentralImpl;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

// import java.lang.Class;
// import java.lang.String;

/**
 * Implementation of ProcessorFactory that creates
 * BiomobyProcessor nodes. <p>
 *
 * @version $Id: BiomobyProcessorFactory.java,v 1.3 2007-04-04 14:29:55 edwardkawas Exp $
 * @author Martin Senger
 */
public class BiomobyProcessorFactory extends ProcessorFactory {

    private String registry_url;
    
    private String registry_uri;

    private String serviceName;

    private String authorityName;
    
    // DEFAULT to Alive status
    private boolean isAlive = true;

    /**
     * 
     * @return the mobycentral registry endpoint
     */
    public String getMobyEndpoint() {
        return this.registry_url;
    }
    
    public String getMobyURI(){
    	return this.registry_uri;
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
     * 
     * @param mobyEndpoint the registry endpoint.
     * @param authorityName the service providers' authority
     * @param serviceName the name of the service
     * 
     */
    public BiomobyProcessorFactory(String mobyEndpoint, String authorityName,
            String serviceName) {
        this.registry_url = mobyEndpoint;
        this.registry_uri = CentralImpl.DEFAULT_NAMESPACE;
        this.authorityName = authorityName;
        this.serviceName = serviceName;
        setName(serviceName);
    }

    /**
     * Create a new factory configured with the specified
     * endpoint base and application name, which will 
     * be concatenated to produce the endpoint URL.
     * 
     * @param registry_url the registry endpoint.
     * @param registry_uri the registry namespace.
     * @param authorityName the service providers' authority
     * @param serviceName the name of the service
     * 
     */
    public BiomobyProcessorFactory(String registry_url, String registry_uri, String authorityName,
            String serviceName) {
        this.registry_url = registry_url;
        this.registry_uri = registry_uri;
        this.authorityName = authorityName;
        this.serviceName = serviceName;
        setName(serviceName);
    }

    /**
     * Return a textual description of the factory
     */
    public String getProcessorDescription() {
        return "A processor based on Biomoby registry located at "
                + registry_url;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
        return org.biomoby.client.taverna.plugin.BiomobyProcessor.class;
    }

	/**
	 * @return the isAlive
	 */
	public boolean isAlive() {
		return isAlive;
	}

	/**
	 * @param isAlive the isAlive to set
	 */
	public void setAlive(boolean isAlive) {
		this.isAlive = isAlive;
	}

}