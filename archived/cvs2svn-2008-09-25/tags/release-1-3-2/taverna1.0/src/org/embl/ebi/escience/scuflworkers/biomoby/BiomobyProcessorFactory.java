/**
 * This file is a component of the Taverna project,
 * and is licensed under the GNU LGPL.
 * Copyright Martin Senger, EMBL-EBI
 */
package org.embl.ebi.escience.scuflworkers.biomoby;
import org.embl.ebi.escience.scufl.DuplicateProcessorNameException;
import org.embl.ebi.escience.scufl.Processor;
import org.embl.ebi.escience.scufl.ProcessorCreationException;
import org.embl.ebi.escience.scufl.ScuflModel;
import org.embl.ebi.escience.scuflworkers.ProcessorFactory;

import org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessor;
// import java.lang.Class;
// import java.lang.String;



/**
 * Implementation of ProcessorFactory that creates
 * BiomobyProcessor nodes. <p>
 *
 * @version $Id: BiomobyProcessorFactory.java,v 1.4 2004-11-12 16:34:28 mereden Exp $
 * @author Martin Senger
 */
public class BiomobyProcessorFactory extends ProcessorFactory {

    private String mobyEndpoint;
    private String serviceName;
    private String authorityName;

    public String getMobyEndpoint() {
	return this.mobyEndpoint;
    }

    public String getServiceName() {
	return this.serviceName;
    }

    public String getAuthorityName() {
	return this.authorityName;
    }
    
    
    /**
     * Create a new factory configured with the specified
     * endpoint base and application name, which will 
     * be concatenated to produce the endpoint URL.
     */
    public BiomobyProcessorFactory (String mobyEndpoint,
				    String authorityName,
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
	return "A processor based on Biomoby registry located at " + mobyEndpoint;
    }

    /**
     * Return the Class object for processors that would
     * be created by this factory
     */
    public Class getProcessorClass() {
	return org.embl.ebi.escience.scuflworkers.biomoby.BiomobyProcessor.class;
    }
    
}
